package addons;

import java.util.ArrayList;
import java.awt.Rectangle;

import haven.Gob;
import haven.Coord;
import haven.Inventory;


public class CleanupScript extends Thread{
	Coord m_p1;
	Coord m_p2;
	String m_str;
	int m_itemSize = 1;
	int m_bagSpace = 0;
	int m_cyckles = 0;
	boolean m_bagPickup = false;
	Inventory m_bag;
	Coord m_ignoreC;
	Gob m_gob;
	
	HavenUtil m_util;
	
	public CleanupScript(HavenUtil util, Coord p1, Coord p2, Gob gob, Coord ign){
		m_p1 = p1;
		m_p2 = p2;
		m_util = util;
		m_gob = gob;
		m_str = gob.resname();
		m_ignoreC = ign;
	}
	
	void coordSorting(){
		int smallestX = m_p1.x;
		int largestX = m_p2.x;
		if(m_p2.x < m_p1.x){
			smallestX = m_p2.x;
			largestX = m_p1.x;
		}
		int smallestY = m_p1.y;
		int largestY = m_p2.y;
		if(m_p2.y < m_p1.y){
			smallestY = m_p2.y;
			largestY = m_p1.y;
		}
		
		m_p1 = new Coord(smallestX, smallestY);
		m_p2 = new Coord(largestX, largestY);
	}
	
	void waitForPickup(Gob g){
		ArrayList<Gob> checklist = getList();
		int count = 0;
		while( checkID(g, checklist) && !MainScript.stop){
			m_util.wait(50);
			checklist = getList();
			
			if(!m_util.checkPlayerWalking() ) count++;
			else count = 0;
			
			if(count > 100){
				count = 0;
				m_util.clickWorldObject(3, g);
			}
		}
	}
	
	void cleanupBuffer(ArrayList<Gob> tempListBuffer){
		while(!MainScript.stop){
			boolean found = false;
			ArrayList<Gob> checklist = getList();
			for(Gob b : tempListBuffer){
				if( checkID(b, checklist) ){
					m_util.clickWorldObject(3, b);
					m_util.wait(130);
					found = true;
				}
			}
			
			if(!found) break;
		}
	}
	
	boolean checkID(Gob i, ArrayList<Gob> list){
		for(Gob j : list){
			if(i.id == j.id) return true;
		}
		
		return false;
	}
	
	ArrayList<Gob> getList(){
		ArrayList<Gob> objects = m_util.getObjectsInRegion(m_p1, m_p2);
		ArrayList<Gob> list = new ArrayList<Gob>();
		
		for(Gob g : objects){
			if(!g.getc().equals(m_ignoreC) )
				if(g.resname().equals(m_str))
					list.add(g);
		}
		
		return list;
		
	}
	
	int checkSize(String s){
		int itemSize = 1;
		
		if(s.equals("gfx/terobjs/items/wood")){
			itemSize = 2;
		}else if(s.equals("gfx/terobjs/items/flaxseed") && m_bagPickup ){
			m_cyckles = -1;
			itemSize = 0;
		}else if(s.equals("gfx/terobjs/items/rope")){
			itemSize = 2;
		}else if(s.equals("gfx/terobjs/items/board")){
			itemSize = (m_bag.isz.x * m_bag.isz.y) / m_bag.isz.x;
		}else{
			itemSize = 1;
		}
		
		return itemSize;
	}
	
	void runCleanup(Gob g){
		ArrayList<Gob> list = getList();
		m_bagPickup = m_util.getItemFromBag("gfx/invobjs/bag-seed") != null;
		m_bagSpace = m_util.getPlayerBagSpace();
		
		ArrayList<Gob> tempListBuffer = new ArrayList<Gob>();
		Coord skipCoord = Coord.z;
		Coord pickupCoord = Coord.z;
		
		while(list.size() > 0 && m_cyckles < m_bagSpace && !MainScript.stop){
			m_util.clickWorldObject(3, g);
			
			ArrayList<Gob> tempList = new ArrayList<Gob>(list);
			tempList.remove(g);
			Gob nextItem = m_util.getClosestObjectInArray(tempList);
			
			m_cyckles += checkSize(g.resname() );
			
			if(nextItem == null || !nextItem.getc().equals(m_util.getPlayerCoord() ) || m_cyckles >= m_bagSpace){
				waitForPickup(g);
				
				cleanupBuffer(tempListBuffer);
				
				tempListBuffer.clear();
			}else{
				tempListBuffer.add(g);
				m_util.wait(130);
			}
			
			list.remove(g);
			g = m_util.getClosestObjectInArray(list);
		}
	}
	
	boolean cleanupItem(String s){
		if(s.contains("/items/"))
			return true;
		if(s.equals("gfx/kritter/hen/cdv") || s.equals("gfx/kritter/hen/cock-dead"))
			return true;
		
		return false;
	}
	
	public void run(){
		if(cleanupItem(m_str) ){
			m_util.sendSlenMessage("Picking up "+ m_str);
			m_util.openInventory();
			m_bag = m_util.getInventory("Inventory");
			coordSorting();
			
			runCleanup(m_gob);
		}
		
		MainScript.cleanupRunning = false;
	}
}