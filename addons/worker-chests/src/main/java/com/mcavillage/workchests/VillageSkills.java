package com.mcavillage.workchests;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
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
    private static final String[] IDS={"charisma","stealth","endurance","craft","engineering","exploration","agriculture"};
    private static final String[] RU={"Харизма","Скрытность","Выносливость","Ремесло","Инженерия","Исследование","Земледелие"};
    private static final String[] PERKS={"Базовый талант","Профессионал","Мастер"};
    private static final int[] REQUIRE={2,5,9};
    private static final String[] HINTS={"Общение с жителями и восстановление","Тихое движение и защита","Бег, скорость и живучесть","Создание предметов и опыт","Добыча блоков и технический опыт","Открытие новых биомов и экспедиции","Урожай и деревенское хозяйство"};
    private static final String[][] PERK_HINTS={
        {"Общение восстанавливает здоровье","Продлённое восстановление","Максимальное восстановление"},
        {"-8% урона при скрытности","-16% урона при скрытности","-24% урона при скрытности"},
        {"-8% урона при низком здоровье","Скорость I при беге","Скорость II при беге"},
        {"4% шанс +1 опыта при крафте","8% шанс +1 опыта при крафте","12% шанс +1 опыта при крафте"},
        {"4% шанс +1 опыта при добыче","8% шанс +1 опыта при добыче","12% шанс +1 опыта при добыче"},
        {"Удача I в экспедиции","Ночное зрение под землёй","Удача II в экспедиции"},
        {"4% шанс +1 опыта за урожай","8% шанс +1 опыта за урожай","12% шанс +1 опыта за урожай"}
    };
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
            int earned=root.getInt(id+"_xp"), needed=threshold(lv);
            int filled=lv>=MAX_LEVEL?12:Math.min(12,(12*earned)/needed);
            String bar="§a"+"▰".repeat(filled)+"§8"+"▱".repeat(12-filled);
            say(p,"§e"+RU[i]+" §f"+lv+"/20 §7| таланты "+rank+"/3");
            say(p,"  "+bar+" §7"+earned+"/"+needed+" §8— "+HINTS[i]);
            if(rank<3) {
                final String skillName=RU[i];
                final String perkInfo=PERK_HINTS[i][rank];
                p.sendSystemMessage(Component.literal("  §a[Открыть талант] §7"+PERKS[rank]+" (ур. "+REQUIRE[rank]+") §8— "+perkInfo)
                    .withStyle(style->style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/skills unlock "+id))
                       .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("Нажми, чтобы открыть "+skillName)))));
            }
        }
        say(p,"§7Опыт навыков отдельный от обычного XP. Нажимай зелёные таланты.");
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
        if(e.getState().getBlock() instanceof CropBlock crop && crop.isMaxAge(e.getState())) {
            addXp(p,"agriculture",8,20);
            int farmRank=rank(data(p),"agriculture");
            if(farmRank>0 && p.getRandom().nextFloat()<0.04F*farmRank)p.giveExperiencePoints(1);
            return;
        }
        addXp(p,"engineering",3,30);
        int engineering=rank(data(p),"engineering");
        if(engineering>0 && p.getRandom().nextFloat()<0.04F*engineering)
            p.giveExperiencePoints(1);
    }
    @SubscribeEvent
    public static void craft(PlayerEvent.ItemCraftedEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        addXp(p,"craft",4,25);
        int craftRank=rank(data(p),"craft");
        if(craftRank>0 && p.getRandom().nextFloat()<0.04F*craftRank)
            p.giveExperiencePoints(1);
    }
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END || !(e.player instanceof ServerPlayer p))return;
        if(p.tickCount%100!=0)return;
        // Only genuinely new biomes grant exploration XP (maximum 128 unique entries).
        CompoundTag progression=data(p);
        String biome=p.serverLevel().getBiome(p.blockPosition()).unwrapKey()
                .map(key->key.location().toString()).orElse("unknown");
        ListTag discovered=progression.getList("exploration_biomes",Tag.TAG_STRING);
        boolean seen=false;
        for(int i=0;i<discovered.size();i++)if(discovered.getString(i).equals(biome)){seen=true;break;}
        if(!seen && discovered.size()<128 && !biome.equals("unknown")){
            discovered.add(StringTag.valueOf(biome));
            progression.put("exploration_biomes",discovered);
            addXp(p,"exploration",24,0);
            say(p,"§b✦ Исследован новый биом: §f"+biome+" §7("+discovered.size()+"/128)");
        }
        int exploring=rank(progression,"exploration");
        if(exploring>=1)p.addEffect(new MobEffectInstance(MobEffects.LUCK,110,exploring>=3?1:0,true,false));
        if(exploring>=2 && p.getBlockY()<45)
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,230,0,true,false));
        if(p.isCrouching() && p.getDeltaMovement().horizontalDistanceSqr()>0.0004)
            addXp(p,"stealth",3,80);
        if(p.isSprinting() && p.getDeltaMovement().horizontalDistanceSqr()>0.01)
            addXp(p,"endurance",3,80);
        int endurance=rank(data(p),"endurance");
        if(endurance>=2 && p.isSprinting())
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,110,endurance>=3?1:0,true,false));
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
