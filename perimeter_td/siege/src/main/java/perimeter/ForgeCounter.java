package perimeter;

import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="perimeter_siege")
public class ForgeCounter extends ChestMenu {
    private final ServerPlayer owner;
    private final SimpleContainer icons;
    private final boolean shop;
    int selected=-1;
    private ItemStack original=ItemStack.EMPTY;
    static final int[] FEES={2,3,4,6,8,10};
    record Offer(Item icon,String name,int count,int price,String action){}
    static final List<Offer> GOODS=List.of(
        new Offer(Items.COOKED_BEEF,"Паёк",8,3,""),new Offer(Items.BREAD,"Хлеб",4,1,""),
        new Offer(Items.OAK_LOG,"Брёвна",12,4,""),new Offer(Items.COBBLESTONE,"Булыжник",32,2,""),
        new Offer(Items.IRON_INGOT,"Железо",6,8,""),new Offer(Items.COPPER_INGOT,"Медь",12,5,""),
        new Offer(Items.REDSTONE,"Редстоун",12,6,""),new Offer(Items.TORCH,"Факелы",16,2,""),
        new Offer(Items.ARROW,"Стрелы",24,3,""),new Offer(Items.SHIELD,"Щит",1,6,""),
        new Offer(Items.RABBIT_FOOT,"Автоинъектор",1,5,"med"),new Offer(Items.GOLDEN_APPLE,"Золотое яблоко",1,9,""),
        new Offer(Items.IRON_CHESTPLATE,"Железная кираса",1,12,""),new Offer(Items.CROSSBOW,"Арбалет",1,9,""),
        new Offer(Items.IRON_HORSE_ARMOR,"Glock 17",1,15,"glock_17"),
        new Offer(Items.GOLDEN_HORSE_ARMOR,"АКМ",1,26,"ak47"),
        new Offer(Items.FLINT,"9 мм",30,4,"9mm"),new Offer(Items.FLINT,"7,62 × 39",30,6,"762x39"),
        new Offer(Items.NAUTILUS_SHELL,"Ящик арсенала",1,18,"crate"),
        new Offer(Items.DISPENSER,"Пост базы: автопушка I / II",1,24,"gun"),
        new Offer(Items.BRICKS,"Укрепление базы",1,18,"walls"),
        new Offer(Items.BLAST_FURNACE,"Шахта",1,15,"mine"),new Offer(Items.STONECUTTER,"Лесопилка",1,12,"sawmill"),
        new Offer(Items.ANVIL,"Ремонт узла",1,6,"repair"));
    ForgeCounter(int id,ServerPlayer p,boolean shop){this(id,p,shop,new SimpleContainer(54));}
    private ForgeCounter(int id,ServerPlayer p,boolean shop,SimpleContainer c){
        super(MenuType.GENERIC_9x6,id,p.getInventory(),c,6);owner=p;icons=c;this.shop=shop;refresh();
    }
    static Entity core(ServerPlayer p){
        List<Entity> near=p.serverLevel().getEntities(p,new AABB(p.blockPosition()).inflate(9),
            e->e.getTags().contains("rd.core")&&e.distanceToSqr(p)<=64);
        return near.isEmpty()?null:near.get(0);
    }
    static int value(ServerPlayer p,String key){
        Scoreboard b=p.getScoreboard();Objective obj=b.getObjective("rd");
        return obj==null?0:b.getOrCreatePlayerScore("#"+key,obj).getScore();
    }
    static void pay(ServerPlayer p,int amount){
        Scoreboard b=p.getScoreboard();
        b.getOrCreatePlayerScore("#cash",b.getObjective("rd")).add(-amount);
    }
    static int fee(ItemStack s){return FEES[Arsenal.affix(s)==null?0:Arsenal.rank(Arsenal.affix(s))];}
    static ItemStack icon(Item item,String text){ItemStack s=new ItemStack(item);s.setHoverName(Component.literal(text));return s;}
    void refresh(){
        icons.clearContent();
        if(shop){
            for(int i=0;i<GOODS.size();i++){
                Offer g=GOODS.get(i);
                icons.setItem(i,icon(g.icon(),g.name()+" ×"+g.count()+" • "+g.price()+" кр."));
            }
        }else{
            if(selected>=0&&!ItemStack.matches(original,owner.getInventory().getItem(selected)))selected=-1;
            for(int i=0;i<owner.getInventory().getContainerSize();i++){
                ItemStack s=owner.getInventory().getItem(i);
                if(!Arsenal.eligible(s))continue;
                ItemStack copy=s.copy();
                if(i==selected)copy.setHoverName(Component.literal("✔ "+s.getHoverName().getString()));
                icons.setItem(i,copy);
            }
            if(selected>=0){
                int cost=fee(original);boolean rich=value(owner,"cash")>=cost;
                icons.setItem(49,icon(rich?Items.LIME_DYE:Items.GRAY_DYE,(rich?"Перековать":"Недостаточно кредитов")+" • "+cost+" • "+Arsenal.info(original)));
                icons.setItem(48,icons.getItem(selected).copy());
            }else icons.setItem(49,icon(Items.GRAY_DYE,"Выбери оружие или броню в верхней части окна"));
        }
        icons.setItem(53,icon(Items.GOLD_NUGGET,"Казна: "+value(owner,"cash")+" кр."));
    }
    @Override public boolean stillValid(Player p){return p==owner&&p.isAlive()&&core(owner)!=null;}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    @Override public void broadcastChanges(){if(owner!=null&&owner.tickCount%10==0)refresh();super.broadcastChanges();}
    @Override public void clicked(int slot,int button,ClickType type,Player p){
        if(p==owner&&stillValid(p)&&type==ClickType.PICKUP&&button==0){
            if(shop&&slot>=0&&slot<GOODS.size())buy(GOODS.get(slot));
            else if(!shop&&slot>=0&&slot<owner.getInventory().getContainerSize()){
                ItemStack real=owner.getInventory().getItem(slot);
                if(Arsenal.eligible(real)&&ItemStack.matches(real,unmarked(slot,real))){
                    selected=slot;original=real.copy();
                }
            }else if(!shop&&slot==49&&selected>=0){
                ItemStack s=owner.getInventory().getItem(selected);
                if(ItemStack.matches(s,original)&&Arsenal.eligible(s)&&value(owner,"cash")>=fee(s)){
                    int cost=fee(s);Arsenal.roll(s,owner.getRandom());pay(owner,cost);
                    owner.getInventory().setChanged();original=s.copy();
                }
            }
        }
        refresh();sendAllDataToRemote();
    }
    private ItemStack unmarked(int i,ItemStack real){return i==selected?original:icons.getItem(i);}
    void buy(Offer g){
        if(value(owner,"cash")<g.price())return;
        String a=g.action();ItemStack out;
        switch(a){
            case "gun","walls","mine","sawmill","repair" -> {
                boolean ok=switch(a){case "gun"->value(owner,"gun")<2;case "walls"->value(owner,"max")<60;case "mine"->value(owner,"mine")<4;case "sawmill"->value(owner,"saw")<4;default->value(owner,"hp")<value(owner,"max");};
                Entity base=core(owner);
                if(ok&&base!=null)owner.server.getCommands().performPrefixedCommand(owner.createCommandSourceStack().withPermission(2).withPosition(base.position()).withSuppressedOutput(),"function relay:"+a);
                return;
            }
            case "med","crate" -> out=Arsenal.supply(a.equals("crate"));
            case "glock_17","ak47","9mm","762x39" -> {
                boolean gun=a.equals("glock_17")||a.equals("ak47");
                Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz",gun?"modern_kinetic_gun":"ammo"));
                if(item==null||item==Items.AIR)return;
                out=new ItemStack(item,g.count());out.getOrCreateTag().putString(gun?"GunId":"AmmoId","tacz:"+a);
                if(gun){out.getOrCreateTag().putInt("GunCurrentAmmoCount",a.equals("ak47")?30:17);Arsenal.roll(out,owner.getRandom());Arsenal.affix(out).putInt("tier",0);}
            }
            default -> out=new ItemStack(g.icon(),g.count());
        }
        pay(owner,g.price());Arsenal.grant(owner,out);
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event){
        for(String cmd:List.of("forgegear","supplyshop"))event.getDispatcher().register(Commands.literal(cmd).executes(ctx->{
            ServerPlayer p=ctx.getSource().getPlayerOrException();boolean shop=cmd.equals("supplyshop");
            if(core(p)==null){p.displayClientMessage(Component.literal("Подойди к оператору ближе чем на 8 блоков."),false);return 0;}
            p.openMenu(new SimpleMenuProvider((id,inv,who)->new ForgeCounter(id,p,shop),Component.literal(shop?"Склад снабжения":"Мастерская • выбери предмет")));
            return 1;
        }));
    }
}