package addons;

import java.util.ArrayList;
import java.awt.Rectangle;

import haven.Gob;
import haven.Coord;
import haven.Inventory;


public class AutoLandscape extends Thread{
	static int[] m_stone = {3, 4, 5, 6, 7, 9, 13, 14, 15, 19, 24, 25, 26 };
	static int[] m_grass = {3, 4, 5, 6, 7, 8, 9, 19 };
	static int[] m_dirt = {9, 13, 14, 15};
	
	Coord m_p1;
	Coord m_p2;
	int m_type;
	HavenUtil m_util;
	
	public AutoLandscape(HavenUtil util, Coord p1, Coord p2, int type){
		m_p1 = p1;
		m_p2 = p2;
		m_util = util;
		m_type = type;
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
		
		m_p1 = new Coord(smallestX, smallestY).div(11).mul(11);
		m_p2 = new Coord(largestX, largestY).div(11).mul(11).add(10,10);
	}
	
	ArrayList<landObjects> getObjects(){
		ArrayList<landObjects> objects = new ArrayList<landObjects>();
		
		if(m_type == 1){
			cropLandscape(objects);
		}else{
			tileLandscape(objects);
		}
		
		return objects;
	}
	
	void tileLandscape(ArrayList<landObjects> objects){
		for(Coord c : m_util.getTilesInRegion(m_p1, m_p2) ){
			landObjects o = new tileObject(m_type, c);
			objects.add(o);
		}
	}
	
	void cropLandscape(ArrayList<landObjects> objects){
		for(Gob g : m_util.getObjectsInRegion(m_p1, m_p2) ){
			landObjects o = new cropObject(m_type, g);
			objects.add(o);
		}
	}
	
	void processObjects(ArrayList<landObjects> objects){
		while(!MainScript.stop && objects.size() > 0){
			landObjects obj = closest(objects);
			if(obj == null) return;
			obj.process();
			objects.remove(obj);
		}
	}
	
	landObjects closest(ArrayList<landObjects> objects){
		landObjects obj = null;
		Coord c = m_util.getPlayerCoord();
		double dist = 0;
		
		for(landObjects o : objects){
			if(obj == null){
				obj = o;
				dist = o.dist(c);
			}else if(o.dist(c) < dist){
				obj = o;
				dist = o.dist(c);
			}
		}
		
		return obj;
	}
	
	void autoLandscape(){
		ArrayList<landObjects> objects = getObjects();
		processObjects(objects);
	}
	
	public void run(){
		coordSorting();
		autoLandscape();
		MainScript.landscapeRunning = false;
	}
	
	public class landObjects{
		int type;
		Gob crop;
		Coord tile;
		
		public landObjects(){}
		
		void acctionType(){
			switch(type){
				case 1:
				m_util.sendAcction("harvest");
				break;
				case 2:
				m_util.sendAcction("stoneroad", "stone");
				break;
				case 3:
				m_util.sendAcction("grass");
				break;
				case 4:
				m_util.sendAcction("dirt");
				break;
			}
		}
		
		void process(){}
		
		double dist(Coord c){return 0;}
	}
	
	public class cropObject extends landObjects{
		public cropObject(int t, Gob crp){
			type = t;
			crop = crp;
		}
		
		void process(){
			if(!filterObject()) return;
			acctionType();
			m_util.clickWorldObject(1, crop);
			m_util.clickWorld(3, Coord.z);
			
			while(!MainScript.stop && m_util.findObject(crop) ) m_util.wait(100);
		}
		
		boolean filterObject(){
			int stage = cropStage(crop.resname() );
			if(stage == -1 || crop.GetBlob(0) < stage ) return false;
			return true;
		}
		
		int cropStage(String s){
			if(s.equals("gfx/terobjs/plants/carrot") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/beetroot") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/wheat") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/hemp") ){
				return 4;
			}else if(s.equals("gfx/terobjs/plants/flax") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/peas") ){
				return 4;
			}else if(s.equals("gfx/terobjs/plants/hops") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/pumpkin") ){
				return 6;
			}else if(s.equals("gfx/terobjs/plants/onion") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/poppy") ){
				return 4;
			}else if(s.equals("gfx/terobjs/plants/tea") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/wine") ){
				return 3;
			}else if(s.equals("gfx/terobjs/plants/tobacco") ){
				return 4;
			}else if(s.equals("gfx/terobjs/plants/pepper") ){
				return 3;
			}
			
			return -1;
		}
		
		double dist(Coord c){
			return crop.getc().dist(c);
		}
	}
	
	public class tileObject extends landObjects{
		public tileObject(int t, Coord c){
			type = t;
			tile = c.mul(11).add(5,5);
		}
		
		void process(){
			if(!filterObject()) return;
			acctionType();
			m_util.clickWorld(1, tile);
			m_util.clickWorld(3, tile);
			
			pauseType();
		}
		
		boolean filterObject(){
			int[] list = getArray();
			if(list == null) return false;
			int id = m_util.getTileID(tile);
			
			for(int i : list){
				if(id == i) return true;
			}
			
			return false;
		}
		
		int[] getArray(){
			switch(type){
				case 2:
				return m_stone;
				case 3:
				return m_grass;
				case 4:
				return m_dirt;
			}
			
			return null;
		}
		
		void pauseType(){
			switch(type){
				case 3:
				grassPause();
				break;
				case 2:
				case 4:
				stoneDirtPause();
				break;
			}
		}
		
		void grassPause(){
			while(!MainScript.stop){
				if(m_util.getPlayerCoord().equals(tile) && !m_util.checkPlayerWalking() ) break;
				m_util.wait(100);
			}
		}
		
		void stoneDirtPause(){
			while(!MainScript.stop){
				if(m_util.hasHourglass() ) break;
				m_util.wait(100);
			}
			while(!MainScript.stop){
				if(!m_util.hasHourglass() ) break;
				m_util.wait(100);
			}
		}
		
		double dist(Coord c){
			return tile.dist(c);
		}
	}
}