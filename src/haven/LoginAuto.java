package haven;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginAuto extends Thread {
	static final Pattern ptrn = Pattern.compile("(\\d+):(.+):(.+)");
	private final Object LOCK = new Object();
	private List<Character> info = new ArrayList<Character>();
	
	String fire = "Your hearth fire";
	String logged = "Where you logged out";
	String type;
	String username;
	String charname;
	Session sess;
	Widget root = null;
	
	public LoginAuto(int loginType, String uname, String cname, Session s){
		if(loginType == 0){
			type = fire;
		}else if(loginType == 1){
			type = logged;
		}
		username = uname;
		charname = cname;
		sess = s;
	}
	
	public void run(){
		waitForUI();
		
		if(charWindow())
			if(charClick())
				waitForLogin();
		
		System.out.println("Logged in.");
	}
	
	void waitForUI(){
		int count = 0;
		while(root == null){
			try{Thread.sleep(200);}catch(Exception e){}
			if(sess != null){
				if(sess.ui != null){
					if(sess.ui.root != null){
						root = sess.ui.root;
						break;
					}
				}
			}
			
			count++;
			if(count > 40) break;
		}
	}
	
	boolean waitForLogin(){
		int count = 0;
		while(true){
			try{Thread.sleep(200);}catch(Exception e){}
			
			if(findMapView() ){
				return true;
			}
			
			count++;
			if(count > 100) break;
		}
		
		return false;
	}
	
	boolean findMapView(){
		for(Widget w = root.child; w != null; w = w.next){
			if(w instanceof MapView){
				if(w != null){
					//System.out.println("mapview found");
					return true;
				}
			}
		}
		
		return false;
	}
	
	boolean charClick(){
		int count = 0;
		int breakCount = 0;
		sess.charname = charname;
		while(true){
			try{Thread.sleep(200);}catch(Exception e){}
			Window wd = getClickWindow();
			if(wd != null){
				for(Widget wdg = wd.child; wdg != null; wdg = wdg.next) {
					if(wdg instanceof Button){
						//System.out.println( ((Button)wdg).text.text );
						if(((Button)wdg).text.text.contains(type) ){
							((Button)wdg).wdgmsg("activate");
							return true;
						}
					}
				}
				
				count++;
				if(count > 50) return false;
			}else if(findMapView() ){
				return false;
			}
			
			breakCount++;
			if(breakCount > 50) break;
		}
		
		return false;
	}
	
	Window getClickWindow(){
		for(Widget w = root.child; w != null; w = w.next){
			if (!(w instanceof Window))
				continue;
			if(((Window)w).cap == null)
				continue;
			if(((Window)w).cap.text == null)
				continue;
			if(!((Window)w).cap.text.equals(charname))
				continue;
			
			return (Window)w;
		}
		
		return null;
	}
	
	boolean charWindow(){
		int count = 0;
		int breakCount = 0;
		while(true){
			try{Thread.sleep(200);}catch(Exception e){}
			Charlist cl = findLogin();
			if(cl != null){
				synchronized(cl.chars) {
					for(Charlist.Char c : cl.chars) {
						if(charname.equals(c.name)){
							cl.wdgmsg("play", c.name);
							return true;
						}
					}
				}
				
				count++;
				if(count > 50) return false;
			}
			
			breakCount++;
			if(breakCount > 50) break;
		}
		
		return false;
	}
	
	Charlist findLogin(){
		for(Widget w = root.child; w != null; w = w.next){
			for(Widget wdg = w.child; wdg != null; wdg = wdg.next) {
				if(wdg instanceof Charlist)
					return (Charlist)wdg;
			}
		}
		
		return null;
	}
}