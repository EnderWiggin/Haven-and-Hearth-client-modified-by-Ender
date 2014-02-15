package haven;

import java.applet.Applet;
import java.applet.AudioClip;
import java.util.HashSet;
import java.net.URL;

public class Sound{
	private AudioClip m_clip;
	public static HashSet<Integer> soundSet = new HashSet<Integer>();
	
	public static final Sound white = new Sound("custom_wav/tapSound.wav");
	public static final Sound red = new Sound("custom_wav/boarFound.wav");
	public static final Sound troll = new Sound("custom_wav/trollFound.wav");
	public static final Sound bell = new Sound("custom_wav/doorbell.wav");
	public static final Sound flotsam = new Sound("custom_wav/playerFound.wav");
	public static final Sound bear = new Sound("custom_wav/bearFound.wav");
	public static final Sound pearl = new Sound("custom_wav/oreFound.wav");
	public static final Sound aggro = new Sound("custom_wav/clickSound.wav");
	public static final Sound death = new Sound("custom_wav/sirenSound.wav");
	public static final Sound error = new Sound("custom_wav/error.wav");
	
	public static void soundGobList(Gob g){
		if(!soundCheck(g.id)) return;
		
		String resname = g.resname();
		
		if(resname.endsWith("borka/s") && g.isHuman() ){
			KinInfo kin = g.getattr(KinInfo.class);
			if(kin == null){
				safePlay("white");
			}else if(kin.group == 2){
				safePlay("red");
			}
		}else if(resname.endsWith("troll/s") ){
			safePlay("troll");
		}else if(resname.endsWith("bear/s") ){
			safePlay("bear");
		}else if(resname.endsWith("chimingbluebell") ){
			safePlay("bell");
		}else if(resname.endsWith("flotsam") ){
			safePlay("flotsam");
		}
	}
	
	public static boolean soundCheck(int id){
		if(!soundSet.contains(id) ){
			soundSet.add(id);
			return true;
		}
		return false;
	}
	
	public static void safePlay(String str){
		if(!Config.confSounds.get(str)) return;
		
		playSound(str);
	}
	
	public static void playSound(String str){
		if(str == "white") Sound.white.play();
		if(str == "red") Sound.red.play();
		if(str == "troll") Sound.troll.play();
		if(str == "bell") Sound.bell.play();
		if(str == "flotsam") Sound.flotsam.play();
		if(str == "bear") Sound.bear.play();
		if(str == "pearl") Sound.pearl.play();
		if(str == "aggro") Sound.aggro.play();
		if(str == "death") Sound.death.play();
		if(str == "error") Sound.error.play();
	}
	
	public Sound(String fileName){
		try{
			URL url = new URL("file:"+fileName);
			m_clip = Applet.newAudioClip(url);
			//System.out.println(fileName);
		}catch(Exception e){
			System.out.println("Clip loading error.");
		}
	}
	
	public void play(){
		try{
			new Thread(){
				public void run(){
					m_clip.play();
				}
			}.start();
		}catch(Exception ec){
			System.out.println("Play error.");
		}
	}
}