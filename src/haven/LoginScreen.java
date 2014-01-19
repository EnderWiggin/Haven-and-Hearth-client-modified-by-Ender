/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class LoginScreen extends Widget {
	static final Pattern ptrn = Pattern.compile("(\\d+):(.+):(.+)");
    Login cur;
    Text error;
    IButton btn;
    static Text.Foundry textf, textfs;
    Tex bg = Resource.loadtex("gfx/loginscr");
    Tex logo = Resource.loadtex("gfx/logo");
    Text progress = null;
	
	TextEntry user, pass;
	Button fire, last;
	private static final Object LOCK = new Object();
	CharacterList LL;
	CharacterList CL;
	public static List<Character> info = new ArrayList<Character>();
	
	protected static BufferedImage[] charUp = new BufferedImage[] {
	Resource.loadimg("gfx/hud/new/upup"),
	Resource.loadimg("gfx/hud/new/updown")};
	protected static BufferedImage[] charDown = new BufferedImage[] {
	Resource.loadimg("gfx/hud/new/downup"),
	Resource.loadimg("gfx/hud/new/downdown")};
	protected static BufferedImage[] charCross = new BufferedImage[] {
	Resource.loadimg("gfx/hud/new/crossup"),
	Resource.loadimg("gfx/hud/new/crossdown")};
	
    static {
	textf = new Text.Foundry(new java.awt.Font("Sans", java.awt.Font.PLAIN, 16));
	textfs = new Text.Foundry(new java.awt.Font("Sans", java.awt.Font.PLAIN, 14));
    }
	
    public LoginScreen(Widget parent) {
		super(Coord.z, new Coord(800, 600), parent);
		setfocustab(true);
		parent.setfocus(this);
		new Img(Coord.z, bg, this);
		new Img(new Coord(420, 215).add(logo.sz().div(2).inv()), logo, this);
		loadLoginInfo();
		clearTrash();
		
		LL = new CharacterList(new Coord(20, 20), new Coord(180, 400), this, getLogins(), "login") {
		public void changed(Character c) {
			if(c != null){
				CL.updateList(getCharacters(c.loginName));
				
				CL.repop();
				
				if(user != null) user.settext(c.loginName);
				if(pass != null){
					pass.settext("");
					pass.text = c.password;
				}
			}else if(c == null){
				CL.clearSelection();
				CL.updateList(new ArrayList<Character>());
			}
		}
		};
		
		CL = new CharacterList(new Coord(600, 20), new Coord(180, 300), this, null , "character") {
		public void changed(Character c) {
			
		}
		};
		
		new Button(new Coord(50, 440), 120, this, "Save Login") { public void click() {
		Character c = new Character();
		
		if(user.text != "" && pass.text != ""){
			if(!checkIfNameExists(user.text) ){
				c.group = 0;
				c.loginName = user.text;
				c.password = pass.text;
				info.add(c);
				saveToFile();
				
				LL.updateList(getLogins() );
				LL.repop();
			}
		}
		} };
		
		fire = new Button(new Coord(615, 340), 150, this, "Your hearth fire");
		
		last = new Button(new Coord(615, 362), 150, this, "Where you logged out");
		
		//show changelog on first run after update;
		boolean same = Config.currentVersion.equals(MainFrame.VERSION); 
		if(!same){
			Config.currentVersion = MainFrame.VERSION;
			Config.saveOptions();
			showChangelog();
		}
    }

    private void showChangelog() {
	Window wnd = new Window(new Coord(100,50), new Coord(50,50), ui.root, "Changelog");
	wnd.justclose = true;
	Textlog txt= new Textlog(Coord.z, new Coord(350,500), wnd);
	int maxlines = txt.maxLines = 200; 
	wnd.pack();
	try {
	    FileInputStream fstream;
	    fstream = new FileInputStream("changelog.txt");
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
	    String strLine;
	    int count = 0;
	    while ((count<maxlines)&&(strLine = br.readLine()) != null)   {
		txt.append(strLine);
		count++;
	    }
	    br.close();
	    in.close();
	    fstream.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
	txt.setprog(0);
    }

    private static abstract class Login extends Widget {
	private Login(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	}
		
	abstract Object[] data();
	abstract boolean enter();
    }

    private class Pwbox extends Login {
	//TextEntry user, pass;
	CheckBox savepass;
		
	private Pwbox(String username, boolean save) {
	    super(new Coord(345, 310), new Coord(150, 150), LoginScreen.this);
	    setfocustab(true);
	    new Label(new Coord(0, 0), this, "User name", textf);
		user = new TextEntry(new Coord(0, 20), new Coord(150, 20), this, username) { public void focus(){
			LL.clearSelection();
			CL.clearSelection();
		} };
		new Label(new Coord(0, 60), this, "Password", textf);
		pass = new TextEntry(new Coord(0, 80), new Coord(150, 20), this, "") { public void focus(){
			LL.clearSelection();
			CL.clearSelection();
		} };
	    pass.pw = true;
	    savepass = new CheckBox(new Coord(0, 110), this, "Remember me");
	    savepass.a = save;
	    if(user.text.equals(""))
		setfocus(user);
	    else
		setfocus(pass);
	}
		
	public void wdgmsg(Widget sender, String name, Object... args) {
	}
		
	Object[] data() {
	    return(new Object[] {user.text, pass.text, savepass.a});
	}
		
	boolean enter() {
	    if(user.text.equals("")) {
		setfocus(user);
		return(false);
	    } else if(pass.text.equals("")) {
		setfocus(pass);
		return(false);
	    } else {
		return(true);
	    }
	}
    }
	
    private class Tokenbox extends Login {
	Text label;
	Button btn;
		
	private Tokenbox(String username) {
	    super(new Coord(295, 310), new Coord(250, 100), LoginScreen.this);
	    label = textfs.render("Identity is saved for " + username, java.awt.Color.WHITE);
	    btn = new Button(new Coord(75, 30), 100, this, "Forget me");
	}
		
	Object[] data() {
	    return(new Object[0]);
	}
		
	boolean enter() {
	    return(true);
	}
		
	public void wdgmsg(Widget sender, String name, Object... args) {
	    if(sender == btn) {
		LoginScreen.this.wdgmsg("forget");
		return;
	    }
	    super.wdgmsg(sender, name, args);
	}
		
	public void draw(GOut g) {
	    g.image(label.tex(), new Coord((sz.x / 2) - (label.sz().x / 2), 0));
	    super.draw(g);
	}
    }

    private void mklogin() {
	synchronized(ui) {
	    btn = new IButton(new Coord(373, 460), this, Resource.loadimg("gfx/hud/buttons/loginu"), Resource.loadimg("gfx/hud/buttons/logind"));
	    progress(null);
	}
    }
	
    private void error(String error) {
	synchronized(ui) {
	    if(this.error != null)
		this.error = null;
	    if(error != null)
		this.error = textf.render(error, java.awt.Color.RED);
	}
    }
    
    private void progress(String p) {
	synchronized(ui) {
	    if(progress != null)
		progress = null;
	    if(p != null)
		progress = textf.render(p, java.awt.Color.WHITE);
	}
    }
    
    private void clear() {
	if(cur != null) {
	    ui.destroy(cur);
	    cur = null;
	    ui.destroy(btn);
	    btn = null;
	}
	progress(null);
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == btn) {
			if(cur.enter())
			super.wdgmsg("login", cur.data());
			return;
		}
		
		if(sender == fire){
			if(user != null && pass != null && CL.sel != null){
				LL.clearSelection();
				super.wdgmsg("loginauto", new Object[] {user.text, pass.text, false, 0, CL.sel.characterName});
			}
			return;
		}
		
		if(sender == last){
			if(user != null && pass != null && CL.sel != null){
				LL.clearSelection();
				super.wdgmsg("loginauto", new Object[] {user.text, pass.text, false, 1, CL.sel.characterName});
			}
			return;
		}
		
		super.wdgmsg(sender, msg, args);
    }
	
    public void uimsg(String msg, Object... args) {
	synchronized(ui) {
	    if(msg == "passwd") {
		clear();
		cur = new Pwbox((String)args[0], (Boolean)args[1]);
		mklogin();
	    } else if(msg == "token") {
		clear();
		cur = new Tokenbox((String)args[0]);
		mklogin();
	    } else if(msg == "error") {
		error((String)args[0]);
	    } else if(msg == "prg") {
		error(null);
		clear();
		progress((String)args[0]);
	    }
	}
    }
	
    public void draw(GOut g) {
        c = MainFrame.getCenterPoint().sub(400, 300);
	super.draw(g);
	if(error != null)
	    g.image(error.tex(), new Coord(420 - (error.sz().x / 2), 500));
	if(progress != null)
	    g.image(progress.tex(), new Coord(420 - (progress.sz().x / 2), 350));
    }
	
    public boolean type(char k, java.awt.event.KeyEvent ev) {
	if(k == 10) {
	    if((cur != null) && cur.enter())
		wdgmsg("login", cur.data());
	    return(true);
	}
	return(super.type(k, ev));
    }
	
	/////////////
	
	boolean checkIfNameExists(String name){
		for(Character c : info){
			if(c.loginName.equals(name) ){
				return true;
			}
		}
		
		return false;
	}
	
	void clearTrash(){
		boolean trashed = false;
		synchronized(LOCK) {
			int i = 0;
			while(i < info.size() ){
				Character c = info.get(i);
				boolean hasLogin = false;
				if(c.group == 0) hasLogin = true;
				
				int j = info.size() - 1;
				while(j >= 0){
					Character d = info.get(j);
					if(j > i && c.group == d.group && c.loginName.equals(d.loginName) && c.characterName.equals(d.characterName)){
						info.remove(d);
						trashed = true;
					}else if(c.group == 1 && d.group == 0 && c.loginName.equals(d.loginName) ){
						hasLogin = true;
					}
					
					j--;
				}
				
				if(!hasLogin){
					info.remove(c);
					trashed = true;
				}
				
				i++;
			}
		}
		
		if(trashed) saveToFile();
	}
	
	void editInfo(List<Character> list, Character sel, boolean del){
		boolean loginDelete = false;
		if(del && sel.group == 0)
			loginDelete = true;
		
		synchronized(LOCK) {
			int i = info.size() - 1;
			while(i >= 0){
				Character c = info.get(i);
				if(c.group == 0 && sel.group == 0){
					info.remove(c);
				}else if(loginDelete && c.loginName.equals(sel.loginName)){
					info.remove(c);
				}else if(c.group == 1 && sel.group == 1 && c.loginName.equals(sel.loginName)){
					info.remove(c);
				}
				
				i--;
			}
			
			info.addAll(list);
		}
		
		saveToFile();
	}
	
	private static void loadLoginInfo(){
		synchronized(LOCK) {
			info.clear();
			try{
				File file = null;
				String fileLocation = System.getProperty("user.home") + "/.haven/loginInfo.conf";
				file = new File(fileLocation);
				
				if(!file.exists()){
					//System.out.println("File doesn't exist");
					return;
				}
				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
				String strLine = br.readLine();
				
				if(!strLine.equals("Login Info") ) return;
				
				while((strLine = br.readLine()) != null){
					try{
						Character c = new Character();
						Matcher m = ptrn.matcher(strLine);
						
						while(m.find() ){
							c.group = Integer.parseInt(m.group(1) );
							c.loginName = m.group(2);
							
							if(c.group == 0)
								c.password = m.group(3);
							if(c.group == 1)
								c.characterName = m.group(3);
						}
						
						info.add(c);
					} catch(Exception e){}
				}
				
				br.close();
				
				//System.out.println("Done");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	private static void saveToFile(){
		synchronized(LOCK) {
			try {
				File file = null;
				String fileLocation = System.getProperty("user.home") + "/.haven/loginInfo.conf";
				file = new File(fileLocation);
				
				if (!file.exists()) {
					file.createNewFile();
				}
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write("Login Info");
				for(int i = 0; i < 2; i++){
					for(Character c : info){
						if(c.group == i){
							bw.newLine();
							String str = "";
							
							if(i == 0) str = "0:" + c.loginName + ":" + c.password;
							if(i == 1) str = "1:" + c.loginName + ":" + c.characterName;
							
							bw.write(str);
						}
					}
				}
				
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class Character {
		int group = -1;
		String loginName = "";
		String characterName = "";
		String password = "";
		
		Text name;
	}
	
	List<Character> getLogins(){
		List<Character> logins = new ArrayList<Character>();
		
		if(info == null) return null;
		
		synchronized(LOCK) {
			for(Character c : info){
				if(c.group != 0) continue;
				
				c.name = Text.render(c.loginName);
				if(c.group == 0) logins.add(c);
			}
		}
		
		return logins;
	}
	
	List<Character> getCharacters(String login){
		List<Character> characters = new ArrayList<Character>();
		
		if(info == null) return null;
		
		synchronized(LOCK) {
			for(Character c : info){
				if(c.group != 1) continue;
				
				if(c.loginName.equals(login) ){
					c.name = Text.render(c.characterName);
					characters.add(c);
				}
			}
		}
		
		return characters;
	}
	
	private class CharacterList extends Widget {
		private List<Character> list = new ArrayList<Character>();
		Scrollbar sb = null;
		int h;
		Character sel;
		String cap;
		
		public CharacterList(Coord c, Coord sz, Widget parent, List<Character> l, String cp) {
			super(c, sz, parent);
			h = sz.y / 20;
			//h = sz.y;
			sel = null;
			sb = new Scrollbar(new Coord(sz.x, 0), sz.y, this, 0, 4);
			if(l != null) list = l;
			cap = cp;
			//System.out.println("test");
			
			new IButton(new Coord(0, sz.div(2).y - 20 ), this, charUp[0], charUp[1]) { public void click() {
				if(list.size() > 1 && sel != null && list.indexOf(sel) != 0 ){
					int index = list.indexOf(sel);
					list.remove(index);
					list.add(index - 1, sel);
					
					editInfo(list, sel, false);
				}
			} };
			new IButton(new Coord(0, sz.div(2).y ), this, charDown[0], charDown[1]) { public void click() {
				if(list.size() > 1 && sel != null && list.indexOf(sel) != list.size() - 1 ){
					int index = list.indexOf(sel);
					list.remove(index);
					list.add(index + 1, sel);
					
					editInfo(list, sel, false);
				}
			} };
			new IButton(new Coord(0, sz.div(2).y + 20 ), this, charCross[0], charCross[1]) { public void click() {
				if(sel != null){
					if(sel.group == 0 && CL != null){
						CL.updateList(new ArrayList<Character>() );
						CL.repop();
					}
					list.remove(sel);
					
					editInfo(list, sel, true);
					LL.clearSelection();
					CL.clearSelection();
				}
			} };
			
			repop();
		}

		public void draw(GOut g) {
			g.chcolor(32, 19, 50, 128);
			g.frect(Coord.z, sz);
			g.chcolor();
			synchronized(LOCK) {
			if(list.size() == 0) {
				g.atext("No Characters Loaded.", sz.div(2), 0.5, 0.5);
			} else {
				for(int i = 0; i < h; i++) {
				if(i + sb.val >= list.size())
					continue;
				Character c = list.get(i + sb.val);
				if(c == sel) {
					g.chcolor(96, 96, 96, 255);
					g.frect(new Coord(0, i * 20), new Coord(sz.x, 20));
					g.chcolor();
				}
				g.aimage(c.name.tex(), new Coord(25, i * 20 + 10), 0, 0.5);
				g.chcolor();
				}
			}
			}
			super.draw(g);
		}

		public void repop() {
			sb.val = 0;
			synchronized(LOCK) {
			sb.max = list.size() - h;
			}
		}

		public boolean mousewheel(Coord c, int amount) {
			sb.ch(amount);
			return(true);
		}

		public void select(Character c) {
			this.sel = c;
			changed(this.sel);
		}

		public boolean mousedown(Coord c, int button) {
			if(super.mousedown(c, button))
			return(true);
			synchronized(LOCK) {
			if(button == 1) {
				int sel = (c.y / 20) + sb.val;
				if(sel >= list.size())
				sel = -1;
				if(sel < 0)
				select(null);
				else
				select(list.get(sel));
				return(true);
			}
			}
			return(false);
		}
		
		public void updateList(List<Character> l){
			synchronized(LOCK) {
				list = l;
			}
		}
		
		public void clearSelection(){
			sel = null;
		}

		public void changed(Character c) {
		}
	}
	
	public static void addChars(List<Charlist.Char> chars, String login){
		loadLoginInfo();
		synchronized(LOCK){
			for(Charlist.Char cha : chars){
				boolean found = false;
				for(Character c : info){
					if(c.loginName.equals(login) && c.characterName.equals(cha.name) ){
						found = true;
						break;
					}
				}
				
				if(!found){
					Character c = new Character();
					c.group = 1;
					c.loginName = login;
					c.characterName = cha.name;
					
					info.add(c);
				}
			}
		}
		saveToFile();
	}
}