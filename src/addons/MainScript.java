package addons;

import haven.HavenPanel;
import haven.Config;
import haven.Coord;
import haven.Gob;

public class MainScript{
	public static HavenUtil m_util;
	public static boolean stop = false;
	public static boolean cleanupRunning = false;
	
	public MainScript(HavenPanel havenPanel){
		m_util = new HavenUtil(havenPanel);
	}
	
	public static void flaskScript(){
		if(!Config.runFlaskRunning){
			RunFlaskScript rfs = new RunFlaskScript(m_util);
			
			if(rfs != null){
				Config.runFlaskRunning = true;
				rfs.start();
			}
			
		}
	}
	
	public static void cleanupItems(int areaSize, Gob object){
		if(!cleanupRunning && object != null){
			Coord pickupCoord = m_util.m_hPanel.ui.mainview.mousepos;
			Coord c1 = pickupCoord.add(-11*areaSize,-11*areaSize);
			Coord c2 = pickupCoord.sub(-11*areaSize,-11*areaSize);
			
			CleanupScript cs = new CleanupScript(m_util, c1, c2, object, new Coord(0,0) );
			
			if(cs != null){
				stop = false;
				cleanupRunning = true;
				cs.start();
			}
		}
	}
	
	public static void stop(int button){
		if(button == 1){
			stop = true;
		}
	}
}