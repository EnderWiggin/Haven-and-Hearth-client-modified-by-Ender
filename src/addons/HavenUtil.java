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
	public static HavenUtil instance;
	public static final int ACTIONBAR_NUMPAD = 2;
	public static final int ACTIONBAR_F = 1;
	public static final int ACTIONBAR_DIGIT = 0;
	
	public static int HourglassID = -1;
	
	UI ui;
	
	public HavenUtil(UI u){
		ui = u;
		instance = this;
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
	
	public void clickWorld(int button, Coord c){
		ui.mainview.wdgmsg("click", new Coord(0, 0), c, button, 0);
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
	
	public void dropItemInInv(Coord c, Inventory inv){
		if(inv == null){
			return;
		}
		
		inv.drop(new Coord(0,0), c);
	}
	
	public void dropItemOnGround(Item item){
		if(item == null) return;
		item.wdgmsg("drop", new Object[]{Coord.z});
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
	
	public void itemAction(Item item){
		if(item == null){
			return;
		}
		item.wdgmsg("iact", Coord.z);
	}
	
	public void transferItem(Item item){
		if(item == null){
			return;
		}
		item.wdgmsg("transfer", Coord.z);
	}
	
	public void transferItemTo(Inventory inv, int mod){
		inv.wdgmsg("xfer", 1, mod);
	}
	
	public void transferItemFrom(Inventory inv, int mod){
		inv.wdgmsg("xfer", -1, mod);
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
	
	public boolean findFlaskToolbar(int bar, int slot){
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
	
	boolean findObject(Gob object){
		synchronized(ui.mainview.glob.oc){
			for(Gob g : ui.mainview.glob.oc){
				if(object.id == g.id) return true;
			}
		}
		
		return false;
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
	
	public ArrayList<Item> getItemsFromInv(Inventory inv){
		ArrayList<Item> list = new ArrayList<Item>();
		
		if(inv == null){
			return list;
		}
		
		for(Widget wdg = inv.child; wdg != null; wdg = wdg.next){
			if(wdg instanceof Item){
				Item i = (Item)wdg;
				list.add(i);
			}
		}
		return list;
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
	
	boolean checkBuff(String name){
		if(ui.mainview == null){
			return false;
		}
		if(ui.mainview.glob == null){
			return false;
		}
		
		synchronized (ui.mainview.glob.buffs) {
			for(Buff b : ui.mainview.glob.buffs.values()) {
				Indir<Resource> ir = b.res;
				if(getResName(b.res).contains(name)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	String getResName(Indir<Resource> indir){
		if(indir.get() != null) {
			return indir.get().name;
		}else{
			int count = 0;
			while(indir.get() == null && count < 1000){wait(50); count++;}
			
			if(indir.get() != null) return indir.get().name;
		}
		return "";
	}
	
	void toggleTracking(){
		sendAcction("tracking");
		/*int k = -2;
		
		if(checkBuff("tracking")){
			ui.mainview.glob.buffs.remove(k);
		}else{
			Buff buff = new Buff(k, Resource.load("paginae/act/tracking").indir());
			buff.major = true;
			ui.mainview.glob.buffs.put(k, buff);
		}*/
	}
	
	public void turnTrackingOn(boolean turnon){
		if(turnon)
			if(!checkBuff("tracking"))
				toggleTracking();
		if(!turnon)
			if(checkBuff("tracking"))
				toggleTracking();
	}
	
	void toggleCriminal(){
		sendAcction("crime");
		/*int k = -1;
		
		if(checkBuff("crime")){
			ui.mainview.glob.buffs.remove(k);
		}else{
			Buff buff = new Buff(k, Resource.load("paginae/act/crime").indir());
			buff.major = true;
			ui.mainview.glob.buffs.put(k, buff);
		}*/
	}
	
	void turnCriminalOn(boolean turnon){
		if(turnon)
			if(!checkBuff("crime"))
				toggleCriminal();
		if(!turnon)
			if(checkBuff("crime"))
				toggleCriminal();
	}
	
	void toggleBotIcon(){
		int k = -4;
		
		if(checkBuff("eye")){
			ui.mainview.glob.buffs.remove(k);
		}else{
			Buff buff = new Buff(k, Resource.load("paginae/act/eye").indir());
			buff.major = true;
			ui.mainview.glob.buffs.put(k, buff);
		}
	}
	
	void turnBotIconOn(boolean turnon){
		if(turnon)
			if(!checkBuff("eye"))
				toggleBotIcon();
		if(!turnon)
			if(checkBuff("eye"))
				toggleBotIcon();
	}
	
	public void sendAcction(String str){
		String[] action = {str};
		ui.mnu.wdgmsg("act", (Object[])action);
	}
	
	public void sendAcction(String str1, String str2){
		String[] action = {str1, str2};
		ui.mnu.wdgmsg("act", (Object[])action);
	}
	
	public boolean flowerMenuReady(){
		return ui.flowerMenu != null;
	}
	
	public ArrayList<Coord> getTilesInRegion(Coord pos1, Coord pos2){
		ArrayList<Coord> list = new ArrayList<Coord>();
		
		Coord p1 = pos1.div(11);
		Coord p2 = pos2.div(11);
		
		int smallestX = p1.x;
		int largestX = p2.x;
		if(p2.x < p1.x){
			smallestX = p2.x;
			largestX = p1.x;
		}
		int smallestY = p1.y;
		int largestY = p2.y;
		if(p2.y < p1.y){
			smallestY = p2.y;
			largestY = p1.y;
		}
		
		for( int y = largestY; y >= smallestY ; y-- ){
			for( int x = largestX; x >= smallestX ; x-- ){
				Coord tc = new Coord(x , y);
				list.add(tc);
			}
		}
		
		return list;
	}
	
	public int getTileID(Coord c){
		return ui.mainview.map.gettilen(c.div(11) );
	}
	
	public void clickButton(String name){
		
		Widget root = ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			if(!((Window)w).cap.text.contains(name))
				continue;
			
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof Button){
					try{
						wdg.wdgmsg("activate");
					}catch(Exception e){}
				}
			}
		}
	}
	
	public int getHunger(){
		int hunger = 2000;
		Widget root = ui.root;
		
		for(Widget w = root.child; w != null; w = w.next){
			if ((w instanceof IMeter)){
				if(((IMeter)w).bg.name.contains("hngr")){
					for(Meter m : ((IMeter)w).meters){
						
						if(m.a < 100){
							if(m.c.getRed() == 96 && m.c.getGreen() == 0 && m.c.getBlue() == 0){
								hunger =(int)( m.a * 5);
							}else if(m.c.getRed() == 255 && m.c.getGreen() == 64 && m.c.getBlue() == 0){
								hunger =(int)( 500 + m.a * 3.33);
							}else if(m.c.getRed() == 255 && m.c.getGreen() == 192 && m.c.getBlue() == 0){
								hunger =(int)( 800 + m.a * 1);
							}else if(m.c.getRed() == 0 && m.c.getGreen() == 255 && m.c.getBlue() == 0){
								hunger =(int)( 900 + m.a * 1);
							}else if(m.c.getRed() == 255 && m.c.getGreen() == 0 && m.c.getBlue() == 0){
								hunger =(int)( 1000 + m.a * 1);
							}else{
								hunger = 2000;
							}
						}
					}
				}
			}
		}
		return hunger;
	}
}