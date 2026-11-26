package perimeter;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid="perimeter_siege",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public class TerminalScreen extends Screen implements MenuAccess<Terminal>{
    private final Terminal menu;private int x,y,w,h,scroll,prev=-1;
    private static final int BG=0xff142129,PANEL=0xff21323b,ACCENT=0xff51e3bf,TEXT=0xffedf4f6,MUTED=0xffb3c7cc;
    TerminalScreen(Terminal m,Inventory inv,Component title){super(title);menu=m;}
    @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->MenuScreens.register(Terminal.TYPE.get(),TerminalScreen::new));}
    public Terminal getMenu(){return menu;}
    @Override protected void init(){w=Math.min(388,width-10);h=Math.min(240,height-8);x=(width-w)/2;y=(height-h)/2;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){if(minecraft.player!=null)minecraft.player.closeContainer();super.onClose();}
    List<Integer> entries(){var ids=new ArrayList<Integer>();for(int i=0;i<45;i++)if(!menu.rows.getItem(i).isEmpty())ids.add(i);return ids;}
    int visible(){return Math.max(1,(h-91)/22);}
    boolean inside(double a,double b,int l,int t,int r,int bot){return a>=l&&a<r&&b>=t&&b<bot;}
    void send(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override public boolean mouseScrolled(double a,double b,double dy){scroll=Math.max(0,Math.min(Math.max(0,entries().size()-visible()),scroll-(int)Math.signum(dy)));return true;}
    @Override public boolean mouseClicked(double a,double b,int button){
        if(button!=0)return false;
        if(inside(a,b,x+w-22,y,x+w,y+18)){onClose();return true;}
        if(inside(a,b,x,y+35,x+w,y+55)){send(Math.min(4,(int)((a-x)*5/w)));scroll=0;return true;}
        if(menu.tab()==4&&inside(a,b,x+5,y+h-26,x+w-5,y+h-4)){send(90);return true;}
        var ids=entries();int row=(int)((b-y-58)/22);
        if(inside(a,b,x+5,y+58,x+w-5,y+58+visible()*22)&&row+scroll<ids.size()){send(16+ids.get(row+scroll));return true;}
        return super.mouseClicked(a,b,button);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);if(prev!=menu.tab()){prev=menu.tab();scroll=0;}
        int tab=menu.tab();var ids=entries();scroll=Math.min(scroll,Math.max(0,ids.size()-visible()));
        g.fill(x,y,x+w,y+h,BG);g.fill(x,y,x+w,y+2,ACCENT);
        g.drawString(font,"Узел обороны",x+8,y+6,TEXT);g.drawString(font,"✕",x+w-15,y+6,MUTED);
        String phase=menu.state.get(6)==1?"Волна "+menu.state.get(5):menu.state.get(5)==0?"Подготовка":"Старт через "+menu.state.get(7)+" с";
        g.drawString(font,"♥ "+menu.state.get(3)+"/"+menu.state.get(4)+"   "+phase+"   Казна: "+menu.cash(),x+8,y+21,ACCENT);
        for(int i=0;i<5;i++){int a=x+w*i/5,b=x+w*(i+1)/5;g.fill(a,y+35,b-1,y+54,i==tab?0xff345a57:PANEL);g.drawCenteredString(font,Terminal.TABS[i],(a+b)/2,y+41,i==tab?ACCENT:TEXT);}
        ItemStack hover=ItemStack.EMPTY;
        for(int r=0;r<visible()&&r+scroll<ids.size();r++){
            int id=ids.get(r+scroll),top=y+58+22*r;ItemStack stack=menu.rows.getItem(id);
            boolean over=inside(mx,my,x+5,top,x+w-5,top+21);
            boolean selected=tab==4&&menu.state.get(8)==id+1;
            g.fill(x+5,top,x+w-5,top+20,selected?0xff315247:over?0xff344953:PANEL);
            int color=Arsenal.tint(stack);g.fill(x+5,top,x+8,top+20,color);g.renderItem(stack,x+11,top+2);
            String label=stack.getHoverName().getString();if(tab==4)label+="  ·  "+Arsenal.info(stack);
            g.drawString(font,font.plainSubstrByWidth(label,w-48),x+32,top+6,tab==4?color:TEXT);
            if(over)hover=stack;
        }
        if(tab==4){int index=menu.state.get(8)-1;ItemStack s=index<0?ItemStack.EMPTY:menu.rows.getItem(index);int fee=ForgeCounter.fee(s);
            g.fill(x+5,y+h-26,x+w-5,y+h-5,s.isEmpty()||menu.cash()<fee?PANEL:0xff27685a);
            g.drawCenteredString(font,s.isEmpty()?"Выбери предмет":menu.cash()<fee?"Не хватает кредитов":"Перековать ещё раз · "+fee+" кр.",x+w/2,y+h-19,TEXT);
        }else if(!ids.isEmpty())g.drawString(font,(scroll+1)+"–"+Math.min(ids.size(),scroll+visible())+" / "+ids.size()+"   •   прокрутка колесом",x+8,y+h-18,MUTED);
        if(!hover.isEmpty()&&tab==4)g.renderTooltip(font,hover,mx,my);
    }
}