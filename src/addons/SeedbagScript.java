package addons;

import java.util.ArrayList;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;

import haven.Inventory;
import haven.Item;
import haven.Widget;
import haven.Window;
import haven.IButton;
import haven.Coord;

public class SeedbagScript extends Thread{
	boolean m_transfer;
	HavenUtil m_util;
	
	public SeedbagScript(HavenUtil util, boolean transfer){
		m_util = util;
		m_transfer = transfer;
	}
	
	ArrayList<Inventory> openSeedbags(){
		int seedbagCount = 0;
		ArrayList<Inventory> list = null;
		
		for(Item i : m_util.getItemsFromBag() ){
			if(i.GetResName().contains("gfx/invobjs/bag-seed") ){
				m_util.itemAction(i);
				seedbagCount++;
			}
		}
		
		while(!MainScript.stop){
			m_util.wait(100);
			list = getInventorys("Seedbag");
			if(list.size() == seedbagCount) break;
		}
		
		int count = 0;
		while(!MainScript.stop && count < 10){
			m_util.wait(100);
			count++;
		}
		
		return list;
	}
	
	ArrayList<Inventory> getInventorys(String name){
		Widget root = m_util.ui.root;
		ArrayList<Inventory> list = new ArrayList<Inventory>();
		
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
					list.add((Inventory)wdg);
				}
			}
		}
		
		return list;
	}
	
	void closeSeedbags(){
		if(m_transfer) return;
		String name = "Seedbag";
		Widget root = m_util.ui.root;
		ArrayList<Inventory> list = new ArrayList<Inventory>();
		
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
				if(wdg instanceof IButton){
					wdg.wdgmsg("activate");
					break;
				}
			}
		}
	}
	
	void transfer(ArrayList<Inventory> list){
		for(Inventory inv : list){
			for(Item i : m_util.getItemsFromInv(inv) ){
				m_util.transferItem(i);
			}
		}
	}
	
	private static final Comparator<itemSort> assending = new Comparator<itemSort>() {
		@Override
		public int compare(itemSort item1, itemSort item2) {
			return item1.q - item2.q;
		}
    };
	
	void sort(ArrayList<Inventory> list){
		ArrayList<itemSort> collection = new ArrayList<itemSort>();
		ArrayList<Integer> seedInvCount = new ArrayList<Integer>();
		
		for(int invGroup = 0; invGroup < list.size(); invGroup++){ // get all items and there seedbag location
			Inventory inv = list.get(invGroup);
			int sortCount = 0;
			for(Item itm : m_util.getItemsFromInv(inv) ){
				collection.add(new itemSort(itm, invGroup, inv) );
				sortCount++;
			}
			seedInvCount.add(sortCount);
		}
		
		Collections.sort(collection, assending); // sort all seeds by q into a collected array
		
		int to = 0;
		int from = 0;
		int group = 0;
		for(int sz : seedInvCount){ // find what seedbag they need to land in next for auto highest planting
			to += sz;
			for(int j = from; j < to; j++){
				itemSort is = collection.get(j);
				is.setTo(group);
			}
			from += sz;
			group++;
		}
		
		itemSort coll = findMoving(collection); // find first moving item
		if(coll != null){
			Inventory lastInv = coll.inv;
			Coord lastC = coll.c;
			m_util.pickUpItem(coll.itm);
			while(collection.size() > 1 && !MainScript.stop){ // pickup seed and place on top of other seeds to arrange optimal arrangement
				itemSort nextColl = null;
				collection.remove(coll);
				
				for(itemSort is : collection){
					if(coll != is && is.current == coll.to){
						nextColl = is;
						break;
					}
				}
				
				if(nextColl == null){ // drop and pickup new seed if moving is not found
					m_util.dropItemInInv(lastC, lastInv);
					
					coll = findMoving(collection);
					if(coll == null) break;
					
					m_util.pickUpItem(coll.itm);
					lastInv = coll.inv;
					lastC = coll.c;
					
					continue;
				}
				
				Inventory inv = nextColl.inv;
				Coord c = nextColl.c;
				m_util.dropItemInInv(c, inv);
				
				coll = nextColl;
			}
			m_util.dropItemInInv(lastC, lastInv);
		}
		
		int count = 0;
		while(!MainScript.stop && count < 10){ // wait 1 sec to update seeds
			m_util.wait(100);
			count++;
		}
		
		if(MainScript.stop) return;
		
		collection.clear();
		for(int invGroup = 0; invGroup < list.size(); invGroup++){ // repeat same procedure to sort seeds inside the seedbags
			Inventory inv = list.get(invGroup);
			for(Item itm : m_util.getItemsFromInv(inv) ){
				collection.add(new itemSort(itm, invGroup, inv) );
			}
		}
		
		Collections.sort(collection, assending);
		
		for(int i = 0; i < collection.size(); i++){
			itemSort cl = collection.get(i);
			m_util.pickUpItem(cl.itm);
			if(i != collection.size() - 1) m_util.dropItemInInv(cl.c, cl.inv);
		}
	}
	
	itemSort findMoving(ArrayList<itemSort> collection){
		for(itemSort is : collection){
			if(is.to != is.current){
				return is;
			}
		}
		
		return null;
	}
	
	Coord spotCoord(int spot){
		int x = 1 + (spot%3) * 31;
		int y = 1 + (spot/3) * 31;
		return new Coord(x, y);
	}
	
	public void run(){
		closeSeedbags();
		ArrayList<Inventory> list = openSeedbags();
		
		if(!MainScript.stop){
			if(m_transfer){
				transfer(list);
			}else{
				sort(list);
			}
		}
		
		closeSeedbags();
		MainScript.seedbagRunning = false;
	}
	
	public class itemSort{
		Item itm;
		Inventory inv;
		int current;
		int to;
		int q;
		Coord c;
		
		public itemSort(Item i, int cur, Inventory iv){
			itm = i;
			current = cur;
			inv = iv;
			q = i.q;
			c = i.c;
		}
		
		void setTo(int t){
			to = t;
		}
	}
}