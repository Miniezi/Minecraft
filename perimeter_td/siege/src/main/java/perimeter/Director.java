package perimeter;

import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;

@Mod.EventBusSubscriber(modid="perimeter_siege")
public class Director {
    static final String TICKET="rd.ticket";
    static final String[] CAL={"sieger","howitzer","hindenburg"};
    static final String[] BASIC={"inf_human","inf_villager","inf_husk"};
    static final String[] EVOLVED={"slasher","spitter","brute"};
    static final String[] HYPER={"ogre","wendigo"};
    static List<String> plan(int wave){
        List<String> result=new ArrayList<>();
        boolean boss=wave>=10 && wave%5==0;
        int n=boss?3:Math.min(12,2+(wave-1)/2);
        for(int i=0;i<n;i++)result.add(BASIC[(i+wave-1)%BASIC.length]);
        if(wave>=5)for(int i=0;i<(boss?1:Math.min(4,1+(wave-5)/4));i++)result.add(EVOLVED[(i+wave-5)%EVOLVED.length]);
        if(wave>=11&&!boss)for(int i=0;i<Math.min(2,1+(wave-11)/8);i++)result.add(HYPER[(wave+i-11)%HYPER.length]);
        if(boss)result.add(CAL[((wave-10)/5)%CAL.length]);
        return result;
    }
    static boolean calamity(String id){return Arrays.asList(CAL).contains(id);}
    static boolean campaign(Entity e){return e.getPersistentData().hasUUID("rdTicket");}
    static EntityType<?> type(String id){return ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("spore",id));}
    static int muster(ServerLevel l,Entity core,int wave){
        var roster=plan(wave);
        for(String id:roster)if(type(id)==null)throw new IllegalStateException("Missing Spore mob "+id);
        for(int i=0;i<roster.size();i++){
            Marker m=EntityType.MARKER.create(l);m.setPos(core.position());m.addTag(TICKET);
            CompoundTag d=m.getPersistentData();d.putString("form",roster.get(i));d.putInt("slot",i);d.putInt("delay",calamity(roster.get(i))?8:i/3*6);
            l.addFreshEntity(m);
        }
        return 1;
    }
    static Mob spawn(ServerLevel l,Entity ticket){
        var d=ticket.getPersistentData();String id=d.getString("form");
        if(!(type(id).create(l) instanceof Mob mob))throw new IllegalStateException(id);
        int i=d.getInt("slot");int dx=new int[]{1,0,-1,0}[i%4],dz=new int[]{0,1,0,-1}[i%4];
        int range=calamity(id)?30:19;BlockPos at=ticket.blockPosition().offset(dx*range+i%3,0,dz*range);
        at=l.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at);
        mob.moveTo(at.getX()+.5,at.getY()+(id.equals("hindenburg")?10:0),at.getZ()+.5,0,0);
        mob.finalizeSpawn(l,l.getCurrentDifficultyAt(at),MobSpawnType.EVENT,null,null);
        mob.setPersistenceRequired();mob.addTag("rd.mob");if(calamity(id))mob.addTag("rd.boss");
        mob.getPersistentData().putUUID("rdTicket",ticket.getUUID());
        d.putUUID("mob",mob.getUUID());
        if(!l.addFreshEntity(mob))throw new IllegalStateException("Spawn denied "+id);
        if(calamity(id))l.getServer().getPlayerList().broadcastSystemMessage(Component.literal("Бедствие: "+mob.getName().getString()),false);
        return mob;
    }
    static int step(ServerLevel l,Entity core){
        for(Marker ticket:l.getEntitiesOfClass(Marker.class,new AABB(core.blockPosition()).inflate(3),e->e.getTags().contains(TICKET))){
            var d=ticket.getPersistentData();
            if(d.getInt("delay")>0){d.putInt("delay",d.getInt("delay")-1);continue;}
            Entity mob=d.hasUUID("mob")?l.getEntity(d.getUUID("mob")):null;
            if(mob==null){spawn(l,ticket);continue;}
            if(mob.distanceToSqr(core)>52*52)mob.teleportTo(core.getX()+22,core.getY()+2,core.getZ());
        }
        return 1;
    }
    @SubscribeEvent public static void lock(LivingEvent.LivingTickEvent event){
        if(!(event.getEntity() instanceof Mob mob)||!(mob.level() instanceof ServerLevel l)||!campaign(mob))return;
        if(mob instanceof Infected inf){inf.setEvoPoints(0);inf.setEvolution(0);}
        var starvation=ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("spore","starvation"));if(starvation!=null&&mob.hasEffect(starvation))mob.removeEffect(starvation);
        if(mob.tickCount%20==0){
            Entity ticket=l.getEntity(mob.getPersistentData().getUUID("rdTicket"));
            if(ticket==null||!ticket.getPersistentData().hasUUID("mob")||!mob.getUUID().equals(ticket.getPersistentData().getUUID("mob")))mob.discard();
        }
    }
    @SubscribeEvent public static void ecology(EntityJoinLevelEvent e){
        if(!(e.getLevel() instanceof ServerLevel l))return;
        Entity mob=e.getEntity();ResourceLocation id=ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if(id==null||!id.getNamespace().equals("spore")||campaign(mob))return;
        if(mob.getClass().getPackageName().endsWith(".Sentities.Calamities")){e.setCanceled(true);return;}
        // Native attacks and multipart hitboxes can still join. Suppress independent infected and remains around the base.
        String path=id.getPath();
        if((mob instanceof Infected||path.contains("corpse")||path.contains("remains")||path.contains("spore_cloud"))&&
           !l.getEntitiesOfClass(Marker.class,mob.getBoundingBox().inflate(64),m->m.getTags().contains("rd.core")).isEmpty())e.setCanceled(true);
    }
    static boolean finish(Mob mob,DamageSource src){
        if(!(mob.level() instanceof ServerLevel l)||!(src.getEntity() instanceof ServerPlayer p))return false;
        Entity ticket=l.getEntity(mob.getPersistentData().getUUID("rdTicket"));
        if(ticket==null||!ticket.getPersistentData().hasUUID("mob")||!mob.getUUID().equals(ticket.getPersistentData().getUUID("mob")))return false;
        String id=ticket.getPersistentData().getString("form");
        ticket.discard();mob.discard(); // bypass Spore's native death chain, including corpse blocks and death clouds
        if(calamity(id)||p.getRandom().nextInt(3)==0){
            ItemStack s=calamity(id)?SiegeBoss.box(id):new ItemStack(p.getRandom().nextBoolean()?Items.IRON_NUGGET:Items.COPPER_INGOT);
            l.addFreshEntity(new ItemEntity(l,mob.getX(),mob.getY(),mob.getZ(),s));
        }
        return true;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void lethal(LivingDamageEvent e){
        if(!(e.getEntity() instanceof Mob mob)||!campaign(mob)||mob.level().isClientSide||e.getAmount()<mob.getHealth())return;
        if(finish(mob,e.getSource()))e.setAmount(0);
        else e.setAmount(Math.max(0,mob.getHealth()-1));
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){
        for(String cmd:List.of("perimeter_muster","perimeter_wave_tick"))e.getDispatcher().register(Commands.literal(cmd).requires(s->s.hasPermission(2)).executes(c->{
            Entity core=c.getSource().getEntity();if(core==null||!core.getTags().contains("rd.core"))return 0;
            var l=c.getSource().getLevel();var b=l.getScoreboard();
            return cmd.equals("perimeter_muster")?muster(l,core,b.getOrCreatePlayerScore("#wave",b.getObjective("rd")).getScore()):step(l,core);
        }));
    }
}