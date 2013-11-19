package addons;

//import javafx.scene.media.Media;
//import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;

import haven.HavenPanel;
import haven.Coord;
import haven.Gob;
import haven.Glob;
import haven.Sprite;
import haven.RemoteUI;
import haven.Resource;
import haven.ResDrawable;
import haven.Window;
import haven.Widget;
import haven.Inventory;
import haven.Img;
import haven.Button;
import haven.ISBox;
import haven.Item;
import haven.IMeter;
import haven.IMeter.Meter;
import haven.Moving;
import haven.UI;
import haven.Label;
import haven.IButton;
import haven.Buff;
import haven.Makewindow;
import haven.LoginScreen;
import haven.Charlist;
import haven.MapView;
import haven.VMeter;
import haven.HackThread;
import haven.Progress;
import haven.Config;
import haven.GOut;
import haven.Utils;
import haven.KinInfo;
import haven.CharWnd;
import haven.CharWnd.Study;
import haven.Fightview;
import haven.MCache;

public class HavenUtil{
	
	public static final int ACTIONBAR_NUMPAD = 0;
	public static final int ACTIONBAR_F = 1;
	public static final int ACTIONBAR_DIGIT = 2;
	
	public static int HourglassID = -1;
	
	HavenPanel m_hPanel;
	
	public HavenUtil(HavenPanel hp){
		m_hPanel = hp;
	}
	
	public void wait(int time){
		try{
			Thread.sleep(time);
		}
		catch(Exception e){}
	}
	
	public void sendSlenMessage(String str){
		m_hPanel.ui.slen.error(str);
	}
	
	public Gob getPlayerGob(){
		return m_hPanel.ui.mainview.glob.oc.getgob(m_hPanel.ui.mainview.playergob);
	}
	
	public Coord getPlayerCoord(){
		try{
			return getPlayerGob().getc();
		}catch(Exception e){}
		
		return getPlayerGob().getc();
	}	
	
	public void clickWorldObject(int button, Gob object){
		if(object == null) return;
		
		m_hPanel.ui.mainview.wdgmsg("click", new Coord(200,150), object.getc(), button, 0, object.id, object.getc());
	}
	
	public Inventory getInventory(String name){
		
		Widget root = m_hPanel.ui.root;
		Widget inv = null;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			if(!((Window)w).cap.text.equals(name))
				continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof Inventory){
					inv = wdg;
				}
			}
		}
		if(inv != null)
			return (Inventory)inv;
		
		return null;
		
	}
	
	public boolean hasHourglass(){
		if(HourglassID == -1){
			wait(10);
			if(HourglassID == -1){
				wait(10);
				if(HourglassID == -1){
					return false;
				}
			}
		}
		
		return true;
	}
	
	public int getStamina(){
		int stamina = 0;
		Widget root = m_hPanel.ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if ((w instanceof IMeter)){
				if(((IMeter)w).bg.name.contains("nrj")){
					for(Meter m : ((IMeter)w).meters){
						stamina = m.a;
					}
				}
			}
		}
		
		return stamina;
	}
	
	public boolean checkPlayerWalking(){
		Gob g = getPlayerGob();
			if (g.checkWalking()){
				return true;
			}
			
		return false;
	}
	
	public Item getItemFromBag(String name){
		Item i = null;
		Inventory inv = getInventory("Inventory");
		if(inv == null){
			return i;
		}
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				if(((Item)wdg).GetResName().contains(name)){
					i = (Item)wdg;
				}
			}
		}
		return i;
	}
	
	public boolean mouseHoldingAnItem(){
		/*if(m_hPanel.ui.mousegrab == null){
			return false;
		}
		return true;*/
		
		if(getMouseItem() == null)
			return false;
		
		return true;
	}
	
	public Item getMouseItem(){
		Widget root = m_hPanel.ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if(w instanceof Item) return (Item)w;
		}
		/*if(m_hPanel.ui.mousegrab instanceof Item){
			return (Item)m_hPanel.ui.mousegrab;
		}*/
		return null;
	}
	
	public void dropItemInBag(Coord c){
		Inventory bag = getInventory("Inventory");
		if(bag == null){
			return;
		}
		
		bag.drop(new Coord(0,0), c);
		
	}
	
	public void pickUpItem(Item i){
		if(i == null){
			return;
		}
		i.wdgmsg("take", new Object[]{m_hPanel.mousepos});
	}
	
	public void itemInteract(Item item){
		if(item == null){
			return;
		}
		item.wdgmsg("itemact", 0);
	}
	
	Item findFlask(){
		Item flask = getItemFromBag("waterskin");
		if(flask == null){
			flask = getItemFromBag("waterflask");
			if(flask == null){
				return null;
			}
		}
		
		return flask;
	}
	
	public void useActionBar(int bar, int button){
		if(bar == 0){
			if(m_hPanel.ui.mnu.numpadbar.layout[button] == null){
				return;
			}
			m_hPanel.ui.mnu.numpadbar.layout[button].use();
		}
		if(bar == 1){
			if(m_hPanel.ui.mnu.functionbar.layout[button] == null){
				return;
			}
			m_hPanel.ui.mnu.functionbar.layout[button].use();
		}
		if(bar == 2){
			if(m_hPanel.ui.mnu.digitbar.layout[button] == null){
				return;
			}
			m_hPanel.ui.mnu.digitbar.layout[button].use();
		}
	}
	
	float waterFlaskInfo(Item i){
		if(i == null) return 100;
		String str = new String(i.tooltip);
		if(str.contains("Empty"))
			return 0;
		if(str.contains("Waterflask"))
			return Float.parseFloat(str.substring(12,15));
		if(str.contains("Waterskin"))
			return Float.parseFloat(str.substring(11,14));
		return 0;
	}
	
	boolean findFlaskToolbar(){
		String quickname = "empty";
		
		if(m_hPanel.ui.mnu.functionbar.layout[1] != null)
			if(m_hPanel.ui.mnu.functionbar.layout[1].getres() != null)
				quickname = m_hPanel.ui.mnu.functionbar.layout[1].getres().name;
		
		if(!quickname.contains("waterskin") && !quickname.contains("waterflask") ){
			//setBeltSlot(2, 1, flask);
			return false;
		}
		//wait(100);
		
		return true;
	}
	
	public void setBeltSlot(int slot, int bar, Item i){
		if(i==null && slot > 0 && slot < 13)
			return;
		int jump = 0;
		Coord c = new Coord(i.c);
		
		if(mouseHoldingAnItem() )
			dropItemInBag(c);
		else
			pickUpItem(i);
		
		//while(!mouseHoldingAnItem() && !InfoWindow.stop) wait(100);
		if(slot > 5)
			jump += 10;
		
		Coord slotCoord = new Coord(25, 22 + 30 * slot + jump);
		
		if(bar == 0){
			m_hPanel.ui.mnu.digitbar.drop(slotCoord, new Coord(10,10));
		}
		if(bar == 1){
			m_hPanel.ui.mnu.functionbar.drop(slotCoord, new Coord(10,10));
		}
		if(bar == 2){
			m_hPanel.ui.mnu.numpadbar.drop(slotCoord, new Coord(10,10));
		}
		
		Inventory bag = getInventory("Inventory");
		//bag.drop(new Coord(0,0), c);
		dropItemInBag(c);
		//while(mouseHoldingAnItem() && !InfoWindow.stop) wait(100);
	}
	
	public Gob getClosestObjectInArray(ArrayList<Gob> list){
		double min = 1000;
		Gob closest = null;
		for(Gob g : list){
			double dist = g.getc().dist(getPlayerCoord());
			if(closest == null){
				min = dist;
				closest = g;
			}else if(min > dist){
				min = dist;
				closest = g;
			}
		}
		return closest;
	}
	
	ArrayList<Gob> getObjectsInRegion(Coord p1, Coord p2){
		ArrayList<Gob> list = new ArrayList<Gob>();
		
		int smallestX = p1.x;
		int largestX = p2.x;
		
		int smallestY = p1.y;
		int largestY = p2.y;
		
		Rectangle rect = new Rectangle(smallestX, smallestY, largestX - smallestX, largestY - smallestY);
		
		synchronized(m_hPanel.ui.mainview.glob.oc){
			for(Gob g : m_hPanel.ui.mainview.glob.oc){
				if(rect.contains(g.getc().x, g.getc().y))
					list.add(g);
			}
		}
		
		return list;
	}
	
	public ArrayList<Item> getItemsFromBag(){
		Inventory inv = getInventory("Inventory");
		ArrayList<Item> list = new ArrayList<Item>();
		
		if(inv == null){
			return null;
		}
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				Item i = (Item)wdg;
				list.add(i);
			}
		}
		return list;
	}
	
	void togleInventory(){
		m_hPanel.ui.root.wdgmsg("gk", 9);
	}
	
	public boolean isInventoryOpen(){
		return getInventory("Inventory") != null;
	}
	
	void openInventory(){
		if(!isInventoryOpen()){
			togleInventory();
			while(!isInventoryOpen() && !MainScript.stop){
				wait(200);
			}
		}
	}
	
	public int getPlayerBagSpace(){
		Inventory bag = getInventory("Inventory");
		
		if(bag == null)
			return -1;
		
		int items = itemCount(bag);
		int bagSize = bag.isz.x * bag.isz.y;
		
		return bagSize - items;
	}
	
	public int itemCount(Inventory inv){
		int count = 0;
		
		for(Widget item = inv.child; item != null; item = item.next){
			if(item instanceof Item){
				int size = (item.sz.x / 30) * (item.sz.y / 30);
				count += size;
			}
		}
		
		return count;
	}
}