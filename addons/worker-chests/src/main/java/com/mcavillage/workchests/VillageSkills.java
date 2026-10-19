package com.mcavillage.workchests;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Standalone RPG progression with player-persistent NBT. This never modifies
 * MCA's own relationship scores or replaces vanilla player experience.
 */
@Mod.EventBusSubscriber(modid=WorkerChests.MODID)
public final class VillageSkills {
    private static final String ROOT="mcaworkchestsRpg";
    private static final String[] IDS={"charisma","stealth","endurance","craft","engineering"};
    private static final String[] RU={"Харизма","Скрытность","Выносливость","Ремесло","Инженерия"};
    private static final String[] PERKS={"Базовый талант","Профессионал","Мастер"};
    private static final int[] REQUIRE={2,5,9};
    private static final int MAX_LEVEL=20;
    private VillageSkills(){}

    private static int index(String id) {
        for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;
        return -1;
    }
    private static CompoundTag data(Player player) {
        CompoundTag persistent=player.getPersistentData();
        if(!persistent.contains(ROOT))persistent.put(ROOT,new CompoundTag());
        return persistent.getCompound(ROOT);
    }
    private static int level(CompoundTag root,String id){return root.getInt(id+"_level");}
    private static int rank(CompoundTag root,String id){return root.getInt(id+"_rank");}
    private static int threshold(int level){return 35+level*20;}
    private static void say(ServerPlayer p,String s){
        p.sendSystemMessage(Component.literal(s));
    }
    private static void addXp(ServerPlayer p,String id,int amount,long cooldownTicks) {
        CompoundTag root=data(p);
        long now=p.serverLevel().getGameTime();
        String key=id+"_last";
        if(cooldownTicks>0 && root.contains(key) && now-root.getLong(key)<cooldownTicks)return;
        root.putLong(key,now);
        int lv=level(root,id);
        if(lv>=MAX_LEVEL)return;
        int xp=root.getInt(id+"_xp")+amount;
        boolean upgraded=false;
        while(lv<MAX_LEVEL && xp>=threshold(lv)){
            xp-=threshold(lv);lv++;upgraded=true;
        }
        root.putInt(id+"_level",lv);
        root.putInt(id+"_xp",lv>=MAX_LEVEL?0:xp);
        if(upgraded) {
            int i=index(id);
            say(p,"§6✦ Навык «"+RU[i]+"» повышен до "+lv+
                    "! Открой §e/skills§6 и выбери талант.");
        }
    }
    private static void show(ServerPlayer p){
        CompoundTag root=data(p);
        say(p,"§6═══ ДРЕВО НАВЫКОВ ПОСЕЛЕНИЯ ═══");
        for(int i=0;i<IDS.length;i++){
            String id=IDS[i];
            int lv=level(root,id),rank=rank(root,id);
            say(p,"§e"+RU[i]+" §f"+lv+"/20 §7| опыт "+root.getInt(id+"_xp")+
                  "/"+threshold(lv)+" §7| таланты "+rank+"/3");
            if(rank<3) {
                say(p,"§8  Следующий: "+PERKS[rank]+" (ур. "+REQUIRE[rank]+") — "+
                       "/skills unlock "+id);
            }
        }
        say(p,"§7Уровни: общайся с жителями, крадись, исследуй, создавай и строй.");
    }
    private static int unlock(ServerPlayer p,String id){
        int i=index(id);
        if(i<0){say(p,"§cНеизвестный навык. Открой /skills.");return 0;}
        CompoundTag root=data(p);
        int rank=rank(root,id),lv=level(root,id);
        if(rank>=3){say(p,"§aВсе таланты «"+RU[i]+"» уже открыты.");return 0;}
        if(lv<REQUIRE[rank]){
            say(p,"§cДля таланта «"+PERKS[rank]+"» нужен уровень "+REQUIRE[rank]+".");return 0;
        }
        root.putInt(id+"_rank",rank+1);
        say(p,"§a✦ Открыт талант «"+PERKS[rank]+"»: "+RU[i]+" ("+(rank+1)+"/3)");
        return 1;
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("skills")
          .executes(c->{show(c.getSource().getPlayerOrException());return 1;})
          .then(Commands.literal("unlock")
            .then(Commands.argument("skill",StringArgumentType.word())
              .suggests((c,b)->{
                  for(String id:IDS)b.suggest(id);
                  return b.buildFuture();
              })
              .executes(c->unlock(c.getSource().getPlayerOrException(),
                       StringArgumentType.getString(c,"skill"))))));
    }

    @SubscribeEvent
    public static void villagers(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        Entity target=e.getTarget();
        ResourceLocation type=ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if(type==null || !(type.getNamespace().equals("mca") ||
                          type.toString().equals("minecraft:villager")))return;
        addXp(p,"charisma",7,80);
        int r=rank(data(p),"charisma");
        if(r>0 && p.getHealth()<p.getMaxHealth() && p.tickCount%20==0)
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,40+20*r,0));
    }
    @SubscribeEvent
    public static void breakBlock(BlockEvent.BreakEvent e){
        if(!(e.getPlayer() instanceof ServerPlayer p))return;
        addXp(p,"engineering",3,30);
        if(rank(data(p),"engineering")>=1 && p.getRandom().nextFloat()<0.04F)
            p.giveExperiencePoints(1);
    }
    @SubscribeEvent
    public static void craft(PlayerEvent.ItemCraftedEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        addXp(p,"craft",4,25);
        if(rank(data(p),"craft")>=1 && p.getRandom().nextFloat()<0.04F)
            p.giveExperiencePoints(1);
    }
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END || !(e.player instanceof ServerPlayer p))return;
        if(p.tickCount%100!=0)return;
        if(p.isCrouching() && p.getDeltaMovement().horizontalDistanceSqr()>0.0004)
            addXp(p,"stealth",3,80);
        if(p.isSprinting() && p.getDeltaMovement().horizontalDistanceSqr()>0.01)
            addXp(p,"endurance",3,80);
        if(rank(data(p),"endurance")>=2 && p.isSprinting())
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,110,0,true,false));
    }
    @SubscribeEvent
    public static void hurt(LivingHurtEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        CompoundTag root=data(p);
        if(p.isCrouching() && rank(root,"stealth")>0){
            float reduction=Math.min(0.24F,0.08F*rank(root,"stealth"));
            e.setAmount(e.getAmount()*(1.0F-reduction));
        }
        if(rank(root,"endurance")>=1 && p.getHealth()<=p.getMaxHealth()*0.4F)
            e.setAmount(e.getAmount()*0.92F);
    }
    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone e){
        if(e.isWasDeath()){
            CompoundTag previous=e.getOriginal().getPersistentData();
            if(previous.contains(ROOT)){
                e.getEntity().getPersistentData().put(ROOT,previous.getCompound(ROOT).copy());
            }
        }
    }
}
