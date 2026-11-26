package perimeter;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.level.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.commands.Commands;

@Mod("perimeter_siege")
public class Siege {
    public Siege(){MinecraftForge.EVENT_BUS.register(this);var bus=net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();Terminal.MENUS.register(bus);Terminal.ITEMS.register(bus);}
    static boolean wave(Entity e){return e!=null && e.getTags().contains("rd.mob");}
    static boolean attacker(Entity e){return wave(e) || e instanceof Mob && "spore".equals(ForgeRegistries.ENTITY_TYPES.getKey(e.getType()).getNamespace());}
    static class Ledger extends SavedData {
        final Map<BlockPos,BlockState> repair=new HashMap<>();
        final Map<BlockPos,Integer> hits=new HashMap<>();
        static Ledger get(ServerLevel l){return l.getDataStorage().computeIfAbsent(n->read(l,n),Ledger::new,"perimeter_damage");}
        static Ledger read(ServerLevel l,CompoundTag tag){
            Ledger d=new Ledger();
            for(Tag t:tag.getList("blocks",10)){
                CompoundTag b=(CompoundTag)t;
                d.repair.put(BlockPos.of(b.getLong("pos")),NbtUtils.readBlockState(l.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),b.getCompound("state")));
            }
            return d;
        }
        public CompoundTag save(CompoundTag tag){
            ListTag list=new ListTag();
            repair.forEach((p,s)->{CompoundTag b=new CompoundTag();b.putLong("pos",p.asLong());b.put("state",NbtUtils.writeBlockState(s));list.add(b);});
            tag.put("blocks",list);return tag;
        }
    }
    static int crackId(BlockPos p){return (int)(p.asLong()^(p.asLong()>>>32))|0x80000000;}
    static void forget(ServerLevel l,BlockPos p){
        Ledger d=Ledger.get(l);
        if(d.hits.remove(p)!=null)l.destroyBlockProgress(crackId(p),p,-1);
        if(d.repair.remove(p)!=null)d.setDirty();
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public void broken(BlockEvent.BreakEvent e){
        if(e.getLevel() instanceof ServerLevel l)forget(l,e.getPos());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public void placed(BlockEvent.EntityPlaceEvent e){
        if(e.getLevel() instanceof ServerLevel l){
            if(e instanceof BlockEvent.EntityMultiPlaceEvent multi)multi.getReplacedBlockSnapshots().forEach(s->forget(l,s.getPos()));
            else forget(l,e.getPos());
        }
    }
    static boolean solid(ServerLevel l,BlockPos p){
        BlockState s=l.getBlockState(p);
        return !s.isAir() && !s.getCollisionShape(l,p).isEmpty();
    }
    static void strike(ServerLevel l,Entity mob,BlockPos p){
        BlockState s=l.getBlockState(p);
        float hard=s.getDestroySpeed(l,p);
        if(hard<0 || !solid(l,p) || s.hasBlockEntity() || !s.getFluidState().isEmpty() || s.getPistonPushReaction()==PushReaction.BLOCK)return;
        Ledger d=Ledger.get(l);
        int hp=Math.max(6,Math.min(600,(int)Math.ceil(8+hard*12)));
        int damage=d.hits.merge(p.immutable(),2,Integer::sum);
        l.destroyBlockProgress(crackId(p),p,Math.min(9,damage*10/hp));
        if(damage>=hp){
            d.hits.remove(p);l.destroyBlockProgress(crackId(p),p,-1);
            if(l.destroyBlock(p,false) && wave(mob)){d.repair.putIfAbsent(p.immutable(),s);d.setDirty();}
        }
    }
    @SubscribeEvent public void nativeBreak(LivingDestroyBlockEvent e){
        if(attacker(e.getEntity()))e.setCanceled(true);
    }
    @SubscribeEvent public void grief(EntityMobGriefingEvent e){
        if(attacker(e.getEntity()))e.setResult(Event.Result.DENY);
    }
    @SubscribeEvent public void blast(ExplosionEvent.Detonate e){
        if(attacker(e.getExplosion().getIndirectSourceEntity()))e.getAffectedBlocks().clear();
    }
    @SubscribeEvent public void tick(TickEvent.LevelTickEvent e){
        if(e.phase!=TickEvent.Phase.END || !(e.level instanceof ServerLevel l) || l.getGameTime()%20!=0)return;
        for(Entity core:l.getAllEntities()){
            if(!core.getTags().contains("rd.core"))continue;
            for(Mob mob:l.getEntitiesOfClass(Mob.class,new AABB(core.blockPosition()).inflate(38),Siege::attacker)){
                Entity goal=mob.getTarget();
                if(goal==null && wave(mob)){
                    var operators=l.getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class,new AABB(core.blockPosition()).inflate(3),BaseOps::operator);
                    if(!operators.isEmpty()){mob.setTarget(operators.get(0));goal=operators.get(0);}else goal=core;
                }
                if(goal==null)continue;
                if(wave(mob) && !mob.getTags().contains("rd.boss") && mob.getNavigation().isDone())mob.getNavigation().moveTo(goal.getX(),goal.getY(),goal.getZ(),1);
                double dx=goal.getX()-mob.getX(), dz=goal.getZ()-mob.getZ();
                double dist=Math.hypot(dx,dz);
                if(dist<1)continue;
                BlockPos front=BlockPos.containing(mob.getX()+dx/dist*.95,mob.getY()+.1,mob.getZ()+dz/dist*.95);
                if(solid(l,front))strike(l,mob,front);
                else if(solid(l,front.above()))strike(l,mob,front.above());
            }
            break;
        }
    }
    @SubscribeEvent public void register(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("perimeter_restore").requires(s->s.hasPermission(2)).executes(c->{
            ServerLevel l=c.getSource().getLevel();Ledger d=Ledger.get(l);
            d.hits.keySet().forEach(p->l.destroyBlockProgress(crackId(p),p,-1));d.hits.clear();
            d.repair.forEach((p,s)->{if(l.getBlockState(p).isAir())l.setBlock(p,s,3);});
            d.repair.clear();d.setDirty();return 1;
        }));
    }
}
