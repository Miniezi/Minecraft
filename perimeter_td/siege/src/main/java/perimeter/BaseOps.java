package perimeter;
import java.util.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import static net.minecraft.commands.Commands.*;

@Mod.EventBusSubscriber(modid="perimeter_siege")
public class BaseOps {
    static int get(ServerLevel l,String key){var b=l.getScoreboard();var o=b.getObjective("rd");return o==null?0:b.getOrCreatePlayerScore("#"+key,o).getScore();}
    static void set(ServerLevel l,String k,int v){var b=l.getScoreboard();b.getOrCreatePlayerScore("#"+k,b.getObjective("rd")).setScore(v);}
    static Entity anchor(ServerLevel l){for(Entity e:l.getAllEntities())if(!e.isRemoved()&&e.getTags().contains("rd.core"))return e;return null;}
    static void cmd(ServerLevel l,CommandSourceStack src,String command){l.getServer().getCommands().performPrefixedCommand(src.withPermission(2).withSuppressedOutput(),command);}
    static boolean operator(Entity e){return e instanceof Villager&&e.getTags().contains("rd.lure");}
    static void clearWave(ServerLevel l){
        List<Entity> list=new ArrayList<>();for(Entity e:l.getAllEntities())if(e.getTags().contains("rd.mob")||e.getTags().contains("rd.ticket"))list.add(e);
        list.forEach(Entity::discard);set(l,"active",0);set(l,"rostered",0);
    }
    static void update(ServerLevel l,Entity core){
        if(core.getPersistentData().getBoolean("collapsed")){relocate(l,core);return;}
        var zone=new AABB(core.blockPosition()).inflate(4);
        var villagers=l.getEntitiesOfClass(Villager.class,zone,BaseOps::operator);
        if(villagers.isEmpty())return;
        Villager v=villagers.get(0);
        if(get(l,"max")<100){int bonus=Math.max(0,get(l,"max")-30);set(l,"max",120+bonus);set(l,"hp",Math.min(120+bonus,90+get(l,"hp")));}
        int hp=get(l,"hp"),max=get(l,"max");
        if(hp<=0){core.getPersistentData().putBoolean("collapsed",true);return;}
        v.setInvulnerable(false);v.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);v.setHealth(hp);
        v.setCustomName(Component.literal("§b◆ Ядро  §c♥ "+hp+"§7 / "+max));v.setCustomNameVisible(true);
        var signs=l.getEntitiesOfClass(ArmorStand.class,zone,e->e.getTags().contains("rd.clock"));
        ArmorStand sign=signs.isEmpty()?null:signs.get(0);
        if(sign==null){
            sign=EntityType.ARMOR_STAND.create(l);var n=sign.saveWithoutId(new net.minecraft.nbt.CompoundTag());n.putBoolean("Marker",true);n.putBoolean("Invisible",true);n.putBoolean("NoGravity",true);sign.load(n);
            sign.setPos(core.getX()+1.8,core.getY()+1.5,core.getZ());sign.addTag("rd.clock");sign.setInvulnerable(true);sign.setCustomNameVisible(true);l.addFreshEntity(sign);
        }
        int w=get(l,"wave");String status=get(l,"active")==1?"§c⚔ Волна "+w:w==0?"§aПКМ по ядру • начать оборону":"§e⌛ Волна "+(w+1)+" через "+get(l,"rest")+" с";
        sign.setCustomName(Component.literal(status));
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void hit(LivingAttackEvent e){
        if(!operator(e.getEntity())||!(e.getEntity().level() instanceof ServerLevel l))return;
        e.setCanceled(true);
        if(!(e.getSource().getEntity() instanceof Mob)||get(l,"active")!=1||get(l,"hp")<=0)return;
        int hp=get(l,"hp")-1;set(l,"hp",hp);e.getEntity().setHealth(hp);
        if(hp==0){Entity core=anchor(l);if(core!=null)core.getPersistentData().putBoolean("collapsed",true);e.getEntity().die(e.getSource());}
    }
    @SubscribeEvent public static void death(LivingDeathEvent e){
        if(operator(e.getEntity())&&e.getEntity().level() instanceof ServerLevel l){var c=anchor(l);if(c!=null)c.getPersistentData().putBoolean("collapsed",true);}
    }
    static void relocate(ServerLevel l,Entity old){
        var pos=old.blockPosition();
        BlockPos next=old.getPersistentData().contains("evacPos")?BlockPos.of(old.getPersistentData().getLong("evacPos")):null;
        for(int i=0;next==null&&i<32;i++){
            double angle=l.random.nextDouble()*Math.PI*2;int radius=400+l.random.nextInt(300);
            BlockPos p=pos.offset((int)(radius*Math.cos(angle)),0,(int)(radius*Math.sin(angle)));
            if(!l.getWorldBorder().isWithinBounds(p))continue;
            var ground=l.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p);
            if(!l.getFluidState(ground.below()).isEmpty()||ground.getY()+5>=l.getMaxBuildHeight())continue;
            next=ground;break;
        }
        if(next==null)return;
        old.getPersistentData().putLong("evacPos",next.asLong());l.setChunkForced(next.getX()>>4,next.getZ()>>4,true);
        if(!l.isPositionEntityTicking(next))return;
        var src=old.createCommandSourceStack();clearWave(l);
        var ledger=Siege.Ledger.get(l);ledger.hits.keySet().forEach(p->l.destroyBlockProgress(Siege.crackId(p),p,-1));ledger.hits.clear();ledger.repair.clear();ledger.setDirty();
        for(Entity e:l.getEntities(old,new AABB(pos).inflate(130),e->operator(e)||e.getTags().contains("rd.clock")||e.getTags().contains("rd.site")||e.getTags().contains("rd.guard")))e.discard();
        for(String area:List.of("~-7 ~-1 ~-7 ~7 ~3 ~7","~-11 ~-1 ~1 ~-9 ~2 ~3","~9 ~-1 ~1 ~11 ~2 ~3"))cmd(l,src,"fill "+area+" air");
        cmd(l,src,"forceload remove ~-32 ~-32 ~32 ~32");
        set(l,"site",0);set(l,"rest",0);set(l,"built",0);
        int gen=get(l,"baseGen")+1;set(l,"baseGen",gen);
        var newPos=Vec3.atBottomCenterOf(next);old.discard();
        cmd(l,l.getServer().createCommandSourceStack().withLevel(l).withPosition(newPos),"function relay:deploy");
        for(ServerPlayer p:l.getServer().getPlayerList().getPlayers())if(p.getTags().contains("rd.joined"))move(p,l,next,gen);
        l.getServer().getPlayerList().broadcastSystemMessage(Component.literal("Ядро погибло. Отряд эвакуирован на новую базу."),false);
    }
    static void move(ServerPlayer p,ServerLevel l,BlockPos pos,int gen){
        p.closeContainer();p.teleportTo(l,pos.getX()+.5,pos.getY(),pos.getZ()+2.5,0,0);
        p.setRespawnPosition(l.dimension(),pos.offset(0,0,2),0,true,false);
        var data=Arsenal.record(p);data.putInt("baseGen",gen);Arsenal.save(p,data);
        var obj=l.getScoreboard().getObjective("rd_raid");if(obj!=null)l.getScoreboard().getOrCreatePlayerScore(p.getScoreboardName(),obj).setScore(0);
    }
    @SubscribeEvent public static void sync(TickEvent.LevelTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.level instanceof ServerLevel l)||l!=l.getServer().overworld()||l.getGameTime()%10!=0)return;
        Entity c=anchor(l);if(c==null)return;update(l,c);if(c.isRemoved())return;
        int gen=get(l,"baseGen");for(ServerPlayer p:l.getServer().getPlayerList().getPlayers())if(p.getTags().contains("rd.joined")&&Arsenal.record(p).getInt("baseGen")<gen)move(p,l,c.blockPosition(),gen);
    }
    static void interact(PlayerInteractEvent.EntityInteract e){
        if(!operator(e.getTarget()))return;e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getEntity() instanceof ServerPlayer p && e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND)Terminal.open(p,0);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void click(PlayerInteractEvent.EntityInteract e){interact(e);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void precise(PlayerInteractEvent.EntityInteractSpecific e){
        if(!operator(e.getTarget()))return;e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getEntity() instanceof ServerPlayer p&&e.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND)Terminal.open(p,0);
    }
    static int start(CommandSourceStack source,int chosen){
        ServerLevel l=source.getServer().overworld();Entity c=anchor(l);if(c==null)return 0;
        if(chosen==0&&get(l,"active")==1){source.sendFailure(Component.literal("Волна уже идёт"));return 0;}
        if(chosen>0){clearWave(l);set(l,"wave",chosen-1);}
        set(l,"rest",0);cmd(l,c.createCommandSourceStack(),"function relay:wave");return 1;
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){
        var root=literal("perimeter").requires(s->s.hasPermission(2));
        root.then(literal("start").executes(c->start(c.getSource(),0)));
        root.then(literal("wave").then(argument("number",IntegerArgumentType.integer(1,10000)).executes(c->start(c.getSource(),IntegerArgumentType.getInteger(c,"number")))));
        root.then(literal("money").then(argument("amount",IntegerArgumentType.integer(1,1000000)).executes(c->{var l=c.getSource().getServer().overworld();if(anchor(l)==null)return 0;set(l,"cash",(int)Math.min(1000000000L,(long)get(l,"cash")+IntegerArgumentType.getInteger(c,"amount")));return 1;})));
        var loot=literal("loot");for(String name:List.of("normal","sieger","howitzer","hindenburg"))loot.then(literal(name).then(argument("players",EntityArgument.players()).executes(c->{var group=EntityArgument.getPlayers(c,"players");for(var p:group)Arsenal.grant(p,name.equals("normal")?Arsenal.supply(true):SiegeBoss.box(name));return group.size();})));
        root.then(loot);e.getDispatcher().register(root);
    }
}