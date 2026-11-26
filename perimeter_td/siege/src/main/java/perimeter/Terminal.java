package perimeter;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.*;

public class Terminal extends AbstractContainerMenu {
    static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,"perimeter_siege");
    static final RegistryObject<MenuType<Terminal>> TYPE=MENUS.register("terminal",()->new MenuType<>(Terminal::new,FeatureFlags.DEFAULT_FLAGS));
    static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,"perimeter_siege");
    static final RegistryObject<Item> MED=ITEMS.register("injector",()->new Item(new Item.Properties()));
    static final String[] TABS={"Штаб","Снабжение","Оборона","Продажа","Перековка"};
    static final int[] RADIO={3,10,8,31,13,14,15,16,17,18};
    static final String[] HQ={"Начать оборону","Взять вылазку","Получить железо","Получить брёвна","Броня I · 1 ОТ","Броня II · 2 ОТ","Здоровье I · 1 ОТ","Здоровье II · 2 ОТ","Разведка I · 1 ОТ","Разведка II · 2 ОТ"};
    record Sale(Item item,int count,int price,String name){}
    static final List<Sale> SALES=List.of(new Sale(Items.COBBLESTONE,32,1,"Булыжник"),new Sale(Items.OAK_LOG,12,2,"Дубовые брёвна"),new Sale(Items.IRON_INGOT,6,4,"Железо"),new Sale(Items.COPPER_INGOT,12,2,"Медь"),new Sale(Items.REDSTONE,12,3,"Редстоун"),new Sale(Items.ROTTEN_FLESH,16,2,"Гнилая плоть"),new Sale(Items.IRON_NUGGET,9,1,"Железные самородки"));
    final SimpleContainer rows=new SimpleContainer(45);
    final SimpleContainerData state=new SimpleContainerData(9);
    ItemStack original=ItemStack.EMPTY;
    final Player owner;
    int chosen=-1;
    Terminal(int id,Inventory inventory){
        super(TYPE.get(),id);owner=inventory.player;
        for(int i=0;i<45;i++)addSlot(new Slot(rows,i,-10000,-10000));
        addDataSlots(state);
    }
    static void open(ServerPlayer p,int tab){if(ForgeCounter.core(p)==null)return;
        p.openMenu(new SimpleMenuProvider((id,inv,user)->{var m=new Terminal(id,inv);m.state.set(0,tab);m.refresh(p);return m;},Component.literal("Терминал ядра")));
    }
    static ItemStack picture(ForgeCounter.Offer g){
        String a=g.action();
        if(a.equals("med")||a.equals("crate"))return Arsenal.supply(a.equals("crate"));
        if(!a.isEmpty()&&(g.icon()==Items.FLINT||a.equals("glock_17")||a.equals("ak47"))){
            boolean ammo=g.icon()==Items.FLINT;
            Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz",ammo?"ammo":"modern_kinetic_gun"));
            if(item!=null&&item!=Items.AIR){var stack=new ItemStack(item);stack.getOrCreateTag().putString(ammo?"AmmoId":"GunId","tacz:"+a);return stack;}
        }
        return new ItemStack(g.icon());
    }
    static boolean defense(ForgeCounter.Offer g){return List.of("gun","walls","mine","sawmill","repair","fort").contains(g.action());}
    static List<ForgeCounter.Offer> offers(int tab){return ForgeCounter.GOODS.stream().filter(g->defense(g)==(tab==2)).toList();}
    static boolean sell(ServerPlayer p,Sale s){
        Inventory inv=p.getInventory();int have=0;
        for(var stack:inv.items)if(stack.is(s.item())&&!stack.hasTag())have+=stack.getCount();
        if(have<s.count())return false;
        int left=s.count();for(var stack:inv.items)if(left>0&&stack.is(s.item())&&!stack.hasTag()){int n=Math.min(left,stack.getCount());stack.shrink(n);left-=n;}
        inv.setChanged();ForgeCounter.pay(p,-s.price());return true;
    }
    int tab(){return state.get(0);}
    int cash(){return (state.get(1)&65535)|((state.get(2)&65535)<<16);}
    static ItemStack named(ItemStack s,String label){s.setHoverName(Component.literal(label));return s;}
    void refresh(ServerPlayer p){
        int money=ForgeCounter.value(p,"cash");state.set(1,money&65535);state.set(2,money>>>16);
        state.set(3,ForgeCounter.value(p,"hp"));state.set(4,ForgeCounter.value(p,"max"));state.set(5,ForgeCounter.value(p,"wave"));state.set(6,ForgeCounter.value(p,"active"));state.set(7,ForgeCounter.value(p,"rest"));
        if(chosen>=0&&!ItemStack.matches(original,p.getInventory().getItem(chosen)))chosen=-1;
        state.set(8,chosen+1);rows.clearContent();
        if(tab()==0){Item[] icons={Items.BELL,Items.COMPASS,Items.IRON_INGOT,Items.OAK_LOG,Items.SHIELD,Items.SHIELD,Items.GOLDEN_APPLE,Items.GOLDEN_APPLE,Items.SPYGLASS,Items.SPYGLASS};for(int i=0;i<HQ.length;i++)rows.setItem(i,named(new ItemStack(icons[i]),HQ[i]));}
        if(tab()==1||tab()==2){var gs=offers(tab());for(int i=0;i<gs.size();i++){var g=gs.get(i);rows.setItem(i,named(picture(g),g.name()+" ×"+g.count()+" · "+g.price()+" кр."));}}
        if(tab()==3)for(int i=0;i<SALES.size();i++){var g=SALES.get(i);rows.setItem(i,named(new ItemStack(g.item()),g.name()+" ×"+g.count()+" · +"+g.price()+" кр."));}
        if(tab()==4)for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(Arsenal.eligible(s))rows.setItem(i,s.copy());}
    }
    @Override public void broadcastChanges(){if(owner instanceof ServerPlayer p&&p.tickCount%10==0)refresh(p);super.broadcastChanges();}
    @Override public void clicked(int slot,int button,ClickType type,Player p){}
    @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
    @Override public boolean stillValid(Player p){return p.isAlive()&&(!(p instanceof ServerPlayer sp)||ForgeCounter.core(sp)!=null);}
    @Override public boolean clickMenuButton(Player who,int button){
        if(!(who instanceof ServerPlayer p)||who!=owner||!stillValid(p))return false;
        if(button>=0&&button<TABS.length)state.set(0,button);
        else if(button==90&&tab()==4&&chosen>=0){
            var s=p.getInventory().getItem(chosen);int fee=ForgeCounter.fee(s);
            if(Arsenal.eligible(s)&&ItemStack.matches(original,s)&&ForgeCounter.value(p,"cash")>=fee){
                Arsenal.roll(s,p.getRandom());ForgeCounter.pay(p,fee);original=s.copy();p.getInventory().setChanged();
            }
        }else{
            int i=button-16;if(i<0||i>=rows.getContainerSize()||rows.getItem(i).isEmpty())return false;
            if(tab()==4){ItemStack s=p.getInventory().getItem(i);if(Arsenal.eligible(s)&&ItemStack.matches(s,rows.getItem(i))){chosen=i;original=s.copy();}}
            else if(tab()==3)sell(p,SALES.get(i));
            else if(tab()==1||tab()==2)new ForgeCounter(containerId,p,true).buy(offers(tab()).get(i));
            else if(tab()==0){var obj=p.getScoreboard().getObjective("radio");if(obj!=null)p.getScoreboard().getOrCreatePlayerScore(p.getScoreboardName(),obj).setScore(RADIO[i]);}
        }
        refresh(p);broadcastChanges();return true;
    }
}