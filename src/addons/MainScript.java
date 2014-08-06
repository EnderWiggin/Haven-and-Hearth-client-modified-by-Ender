package addons;

import haven.*;

public class MainScript{
	public static boolean stop = false;
	public static boolean cleanupRunning = false;
	public static boolean landscapeRunning = false;
	public static boolean feastRunning = false;
	public static Coord m_c1;
	public static Coord m_c2;
	public static int m_Type;
	
	public static void flaskScript(){
		if(!Config.runFlaskRunning){
			RunFlaskScript rfs = new RunFlaskScript(UI.instance.m_util);
			
			if(rfs != null){
				Config.runFlaskRunning = true;
				rfs.start();
			}
			
		}
	}
	
	public static void multiTool(int modify, Gob object){
		if(object == null) return;
		int type = ObjectType(object);
		
		if(type == 0){
			int range = 1;
			if(modify == 4) range = 1000;
			
			cleanupItems(range, object);
		}else if(type == 1){
			if(modify == 1)
				object.animalTag = false;
			else if(modify == 4)
				object.animalTag = true;
		}
	}
	
	private static int ObjectType(Gob object){
		String name = object.resname();
		
		if(name.contains("/items/"))
			return 0;
		if(name.equals("gfx/kritter/hen/cdv") || name.equals("gfx/kritter/hen/cock-dead") || name.equals("gfx/kritter/hare/cdv"))
			return 0;
		if(name.equals("gfx/kritter/sheep/s") || name.equals("gfx/kritter/pig/s") || name.equals("gfx/kritter/cow/s"))
			return 1;
		
		return -1;
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
	
	public static void autoLand(){
		if(!landscapeRunning){
			
			AutoLandscape al = new AutoLandscape(UI.instance.m_util, m_c1, m_c2, m_Type);
			
			if(al != null){
				stop = false;
				landscapeRunning = true;
				al.start();
			}
		}
	}
	
	public static void autoFeast(){
		if(!feastRunning){
			
			AutoFeast af = new AutoFeast(UI.instance.m_util, m_Type);
			
			if(af != null){
				stop = false;
				feastRunning = true;
				af.start();
			}
		}
	}
	
	public static void stop(int button){
		if(button == 1 || button == 3){
			stop = true;
		}
	}
}