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

import haven.*;
import haven.IMeter.Meter;
import haven.CharWnd.Study;

public class HavenUtil{
	
	public static final int ACTIONBAR_NUMPAD = 2;
	public static final int ACTIONBAR_F = 1;
	public static final int ACTIONBAR_DIGIT = 0;
	
	public static int HourglassID = -1;
	
	UI ui;
	
	public HavenUtil(UI u){
		ui = u;
	}
	
	public void wait(int time){
		try{
			Thread.sleep(time);
		}
		catch(Exception e){}
	}
	
	public void sendSlenMessage(String str){
		ui.slen.error(str);
	}
	
	public Gob getPlayerGob(){
		return ui.mainview.glob.oc.getgob(ui.mainview.playergob);
	}
	
	public Coord getPlayerCoord(){
		try{
			return getPlayerGob().getc();
		}catch(Exception e){}
		
		return getPlayerGob().getc();
	}	
	
	public void clickWorldObject(int button, Gob object){
		if(object == null) return;
		
		ui.mainview.wdgmsg("click", new Coord(200,150), object.getc(), button, 0, object.id, object.getc());
	}
	
	public Inventory getInventory(String name){
		Widget root = ui.root;
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
		Widget root = ui.root;
		
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
		/*if(ui.mousegrab == null){
			return false;
		}
		return true;*/
		
		if(getMouseItem() == null)
			return false;
		
		return true;
	}
	
	public Item getMouseItem(){
		Widget root = ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if(w instanceof Item) return (Item)w;
		}
		/*if(ui.mousegrab instanceof Item){
			return (Item)ui.mousegrab;
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
		i.wdgmsg("take", new Object[]{ui.mainview.mousepos});
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
	
	public void useActionBar(int bar, int slot){
		if(bar == 0){
			if(ui.mnu.digitbar.layout[slot] == null){
				return;
			}
			ui.mnu.digitbar.layout[slot].use();
		}
		if(bar == 1){
			if(ui.mnu.functionbar.layout[slot] == null){
				return;
			}
			ui.mnu.functionbar.layout[slot].use();
		}
		if(bar == 2){
			if(ui.mnu.numpadbar.layout[slot] == null){
				return;
			}
			ui.mnu.numpadbar.layout[slot].use();
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
	
	boolean findFlaskToolbar(int bar, int slot){
		String quickname = "empty";
		ToolbarWnd barPad = null;
		
		if(bar == 0){
			if(slot < 0 || slot > 9) return false;
			barPad = ui.mnu.digitbar;
		}else if(bar == 1){
			if(slot < 0 || slot > 11) return false;
			barPad = ui.mnu.functionbar;
		}else if(bar == 2){
			if(slot < 0 || slot > 9) return false;
			barPad = ui.mnu.numpadbar;
		}
		
		if(barPad == null) return false;
		
		if(barPad.layout[slot] != null)
			if(barPad.layout[slot].getres() != null)
				quickname = barPad.layout[slot].getres().name;
		
		if(!quickname.contains("waterskin") && !quickname.contains("waterflask") ){
			return false;
		}
		
		return true;
	}
	
	public void setBeltSlot(int bar, int slot, Item i){
		String quickname = "empty";
		ToolbarWnd barPad = null;
		
		if(bar == 0){
			if(slot < 0 || slot > 9) return;
			barPad = ui.mnu.digitbar;
		}else if(bar == 1){
			if(slot < 0 || slot > 11) return;
			barPad = ui.mnu.functionbar;
		}else if(bar == 2){
			if(slot < 0 || slot > 9) return;
			barPad = ui.mnu.numpadbar;
		}
		
		if(barPad == null) return;
		
		Coord c = i.c;
		
		if(mouseHoldingAnItem() )
			dropItemInBag(c);
		else
			pickUpItem(i);
		
		int belt = barPad.belt;
		int s = barPad.getbeltslot();
		String val = "@"+s;
		barPad.layout[slot] = new ToolbarWnd.Slot(val, belt, slot);
		ui.slen.wdgmsg("setbelt", s, 0);
		ToolbarWnd.setbeltslot(belt, slot, val);
		
		dropItemInBag(c);
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
		
		synchronized(ui.mainview.glob.oc){
			for(Gob g : ui.mainview.glob.oc){
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
		ui.root.wdgmsg("gk", 9);
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
	
	public static String flaskText(int val){
		String str = "";
		if(val >= 48 && val <= 57){
			str = String.valueOf(val - 48);
		}else if(val >= 112 && val <= 123){
			str = "F"+String.valueOf(val - 111);
		}else if(val >= 96 && val <= 105){
			str = "N"+String.valueOf(val - 96);
		}
		
		return str;
	}
	
	public Coord flaskToCoord(int val){
		Coord c = new Coord();
		if(val >= 48 && val <= 57){
			c.x = 0;
			c.y = val - 49;
			if(val == 48) c.y = 9;
			return c;
		}else if(val >= 112 && val <= 123){
			c.x = 1;
			c.y = val - 112;
			return c;
		}else if(val >= 96 && val <= 105){
			c.x = 2;
			c.y = val - 96;
			return c;
		}
		
		return null;
	}
	
	public void moveAllWindowsToView(){
		Widget root = ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			((Window)w).moveWindowToView();
		}
	}
}