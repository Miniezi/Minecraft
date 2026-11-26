package perimeter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="perimeter_siege")
public class ArsenalCheck {
    @SubscribeEvent public static void run(ServerStartedEvent event){
        if(!Boolean.getBoolean("perimeter.testArsenal"))return;
        RandomSource random=RandomSource.create(1234);
        ItemStack gun=new ItemStack(Items.CROSSBOW);
        CompoundTag original=gun.getOrCreateTag();
        original.putString("GunId","tacz:ak47");
        original.putInt("GunCurrentAmmoCount",13);
        CompoundTag attachments=new CompoundTag();
        attachments.putString("scope","test:optic");
        original.put("Attachments",attachments);
        CompoundTag expected=original.copy();
        Arsenal.roll(gun,random);
        int tier=Arsenal.rank(Arsenal.affix(gun));
        for(int i=0;i<100;i++){
            int before=Arsenal.prefix(Arsenal.affix(gun));
            Arsenal.roll(gun,random);
            if(before==Arsenal.prefix(Arsenal.affix(gun))||tier!=Arsenal.rank(Arsenal.affix(gun)))throw new AssertionError("Invalid reforge");
        }
        CompoundTag result=gun.getTag().copy();
        result.remove(Arsenal.KEY);
        if(!result.equals(expected))throw new AssertionError("Gun NBT changed");
        boolean[] found=new boolean[6];
        for(int i=0;i<10000;i++)found[Arsenal.tier(random)]=true;
        for(boolean b:found)if(!b)throw new AssertionError("Missing rarity");
        var level=event.getServer().overworld();
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        var spawn=level.getSharedSpawnPos();level.getChunkAt(spawn);player.setPos(spawn.getX()+0.5,spawn.getY(),spawn.getZ()+0.5);
        var marker=net.minecraft.world.entity.EntityType.MARKER.create(level);
        marker.moveTo(player.position());marker.addTag("rd.core");level.addFreshEntity(marker);
        var board=player.getScoreboard();
        var objective=board.getObjective("rd");
        if(objective==null)objective=board.addObjective("rd",net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY,net.minecraft.network.chat.Component.literal("rd"),net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER);
        var cash=board.getOrCreatePlayerScore("#cash",objective);
        gun.getOrCreateTagElement(Arsenal.KEY).putInt("tier",1);
        player.getInventory().setItem(12,gun);cash.setScore(6);
        var menu=new ForgeCounter(1,player,false);
        menu.clicked(12,0,net.minecraft.world.inventory.ClickType.PICKUP,player);
        for(int i=0;i<2;i++){
            int previous=Arsenal.prefix(Arsenal.affix(gun));
            menu.clicked(49,0,net.minecraft.world.inventory.ClickType.PICKUP,player);
            if(menu.selected!=12||previous==Arsenal.prefix(Arsenal.affix(gun)))throw new AssertionError("Lost selection or failed to reroll");
        }
        var empty=gun.copy();menu.clicked(49,0,net.minecraft.world.inventory.ClickType.PICKUP,player);
        if(cash.getScore()!=0||!ItemStack.matches(gun,empty))throw new AssertionError("Empty treasury reroll");
        for(String kind:new String[]{"guard","scout"}){
            if(!SiegeBoss.weapon(kind,random).getTag().getString(SiegeBoss.RELIC).equals(kind))throw new AssertionError("Boss pool");
        }
        if(!(SiegeBoss.scale("guard",5)>1 && SiegeBoss.scale("guard",22)<1 && SiegeBoss.scale("scout",5)<1 && SiegeBoss.scale("scout",22)>1))throw new AssertionError("Boss tradeoffs");
        for(int wave=1;wave<=60;wave++){
            var plan=Director.plan(wave);
            for(String id:plan)if(Director.type(id)==null)throw new AssertionError("Registry: "+id);
            if(plan.stream().filter(Director::calamity).count()!=(wave>=10&&wave%5==0?1:0))throw new AssertionError("Boss cadence");
            if(wave<5&&plan.stream().anyMatch(id->!java.util.Arrays.asList(Director.BASIC).contains(id)))throw new AssertionError("Stage leakage");
        }
        for(String fn:new String[]{"wave","battle","win","base_tick","rest","input"}){
            if(event.getServer().getFunctions().get(new net.minecraft.resources.ResourceLocation("relay",fn)).isEmpty())throw new AssertionError("Unloaded wave function: "+fn);
        }
        event.getServer().getCommands().performPrefixedCommand(marker.createCommandSourceStack().withPermission(2),"function relay:wave");
        if(board.getOrCreatePlayerScore("#rostered",objective).getScore()!=1)throw new AssertionError("Wave not armed");
        Director.step(level,marker);
        var tickets=level.getEntitiesOfClass(net.minecraft.world.entity.Marker.class,marker.getBoundingBox().inflate(3),e->e.getTags().contains(Director.TICKET));
        if(tickets.size()!=2)throw new AssertionError("Wave one");
        var ticket=tickets.get(0);
        var enemy=(net.minecraft.world.entity.Mob)level.getEntity(ticket.getPersistentData().getUUID("mob"));
        if(Director.finish(enemy,level.damageSources().generic())||!enemy.isAlive())throw new AssertionError("Uncredited death");
        enemy.setHealth(1);enemy.invulnerableTime=0;
        enemy.hurt(level.damageSources().generic(),10);
        if(enemy.isRemoved()||ticket.isRemoved()||enemy.getHealth()<1)throw new AssertionError("Environment completed ticket");
        enemy.invulnerableTime=0;enemy.hurt(level.damageSources().playerAttack(player),50);
        if(!enemy.isRemoved()||!ticket.isRemoved())throw new AssertionError("Player finish");
        for(String id:Director.CAL){
            var ticket2=net.minecraft.world.entity.EntityType.MARKER.create(level);
            ticket2.moveTo(marker.position());ticket2.addTag(Director.TICKET);ticket2.getPersistentData().putString("form",id);level.addFreshEntity(ticket2);
            var boss=Director.spawn(level,ticket2);
            if(!boss.getClass().getName().contains(".Calamities."))throw new AssertionError("Wrong boss: "+id);
            if(!Director.finish(boss,level.damageSources().playerAttack(player)))throw new AssertionError("No trophy for "+id);
        }
        var term=new Terminal(3,player.getInventory());
        player.getInventory().setItem(0,new ItemStack(Items.COPPER_INGOT,12));cash.setScore(0);
        term.clickMenuButton(player,3);term.clickMenuButton(player,19);term.clickMenuButton(player,19);
        if(cash.getScore()!=2||!player.getInventory().getItem(0).isEmpty())throw new AssertionError("Sale not atomic");
        var v=net.minecraft.world.entity.EntityType.VILLAGER.create(level);v.moveTo(marker.position());v.addTag("rd.lure");level.addFreshEntity(v);
        BaseOps.set(level,"max",120);BaseOps.set(level,"hp",120);BaseOps.set(level,"active",1);
        BaseOps.update(level,marker);
        var zombie=net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
        v.hurt(level.damageSources().mobAttack(zombie),40);
        if(BaseOps.get(level,"hp")!=119||v.getHealth()!=119)throw new AssertionError("Core damage");
        cash.setScore(30);
        BaseOps.cmd(level,marker.createCommandSourceStack(),"function relay:repair");
        if(cash.getScore()!=30||BaseOps.get(level,"hp")!=119)throw new AssertionError("Combat repair");
        BaseOps.set(level,"active",0);
        BaseOps.cmd(level,marker.createCommandSourceStack(),"function relay:repair");
        if(BaseOps.get(level,"hp")!=120||cash.getScore()!=24)throw new AssertionError("Intermission repair");
        BaseOps.set(level,"hp",1);BaseOps.set(level,"active",1);v.hurt(level.damageSources().mobAttack(zombie),5);
        BaseOps.update(level,marker);
        oldCore=marker;
        if(!Arsenal.supply(false).is(Terminal.MED.get()))throw new AssertionError("Medical item");
        
    }
    static net.minecraft.world.entity.Entity oldCore;
    static int elapsed;
    @SubscribeEvent public static void verifyMove(net.minecraftforge.event.TickEvent.ServerTickEvent e){
        if(oldCore==null||e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
        var l=e.getServer().overworld();var next=BaseOps.anchor(l);
        if(oldCore.isRemoved()&&next!=null&&next.distanceToSqr(oldCore)>350*350&&BaseOps.get(l,"hp")==120&&BaseOps.get(l,"wave")==0){
            oldCore=null;System.out.println("PERIMETER_ARSENAL_PASS");e.getServer().halt(false);return;
        }
        if(++elapsed>400)throw new AssertionError("Relocation: oldRemoved="+oldCore.isRemoved()+" replacement="+next+" hp="+BaseOps.get(l,"hp")+" wave="+BaseOps.get(l,"wave"));
    }
}
