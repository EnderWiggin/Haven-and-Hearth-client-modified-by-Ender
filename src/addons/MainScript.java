package addons;

import haven.*;

public class MainScript{
	public static boolean stop = false;
	public static boolean cleanupRunning = false;
	
	public static void flaskScript(){
		if(!Config.runFlaskRunning){
			RunFlaskScript rfs = new RunFlaskScript(UI.instance.m_util);
			
			if(rfs != null){
				Config.runFlaskRunning = true;
				rfs.start();
			}
			
		}
	}
	
	public static void cleanupItems(int areaSize, Gob object){
		if(!cleanupRunning && object != null){
			Coord pickupCoord = UI.instance.mainview.mousepos;
			Coord c1 = pickupCoord.add(-11*areaSize,-11*areaSize);
			Coord c2 = pickupCoord.sub(-11*areaSize,-11*areaSize);
			
			CleanupScript cs = new CleanupScript(UI.instance.m_util, c1, c2, object, new Coord(0,0) );
			
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