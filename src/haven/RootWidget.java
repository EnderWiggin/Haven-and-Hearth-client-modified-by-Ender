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

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.media.opengl.GLException;

import com.sun.opengl.util.Screenshot;

public class RootWidget extends ConsoleHost {
    public static Resource defcurs = Resource.load("gfx/hud/curs/arw");
    Logout logout = null;
    Profile gprof;
    boolean afk = false;
    public static boolean screenshot = false;
    public static boolean names_ready = true;
    private long last = 0;
    private int ticks = 0;
    private int fps = 0;
	public ChatHW ircchat = null;
	
    public RootWidget(UI ui, Coord sz) {
	super(ui, new Coord(0, 0), sz);
	setfocusctl(true);
	cursor = defcurs;
    }
	
    public boolean globtype(char key, KeyEvent ev) {
	if(!super.globtype(key, ev)) {
	    int code = ev.getKeyCode();
	    boolean ctrl = ev.isControlDown();
	    boolean alt = ev.isAltDown();
		boolean shift = ev.isShiftDown();
	    if(Config.profile && (key == '`')) {
		new Profwnd(ui.slen, ui.mainview.prof, "MV prof");
	    } else if(Config.profile && (key == '~')) {
		new Profwnd(ui.slen, gprof, "Glob prof");
	    } else if(Config.profile && (key == '!')) {
		new Profwnd(ui.slen, ui.mainview.mask.prof, "ILM prof");
	    } else if((code == KeyEvent.VK_N)&&ctrl) {
		Config.nightvision = !Config.nightvision;
		Config.saveOptions();
	    } else if((code == KeyEvent.VK_X)&&ctrl) {
		Config.xray = !Config.xray;
	    } else if((code == KeyEvent.VK_C)&&alt) {
		Config.muteChat = !Config.muteChat;
		String str = "Chat mute is turned "+((Config.muteChat)?"ON":"OFF");
		ui.cons.out.println(str);
		ui.slen.error(str);
	    } else if((code == KeyEvent.VK_F)&&ctrl) {
		Config.fps = !Config.fps;
	    } else if((code == KeyEvent.VK_K)&&ctrl) {
		Config.TEST = !Config.TEST;
	    } else if((code == KeyEvent.VK_D)&&ctrl) {
		Config.dbtext = !Config.dbtext;
	    } else if((code == KeyEvent.VK_P)&&ctrl) {
		//Config.profile = !Config.profile;
	    } else if((code == KeyEvent.VK_H)&&ctrl) {
		Config.hide = !Config.hide;
		Config.saveOptions();
	    } else if((code == KeyEvent.VK_Q)&&alt) {
		ui.spd.setspeed(0, true);
	    } else if((code == KeyEvent.VK_W)&&alt) {
		ui.spd.setspeed(1, true);
	    } else if((code == KeyEvent.VK_E)&&alt) {
		ui.spd.setspeed(2, true);
	    } else if((code == KeyEvent.VK_R)&&alt) {
		ui.spd.setspeed(3, true);
	    } else if((code == KeyEvent.VK_G)&&ctrl) {
		Config.grid = !Config.grid;
	    } else if((code == KeyEvent.VK_G)&&alt) {
		IRChatHW.open();
	    } else if(((int)key == 2)&ctrl) {//CTRL-B have code of 02
		BuddyWnd.instance.visible = !BuddyWnd.instance.visible;
	    } else if(((int)key == 20)&ctrl) {//CTRL-T have code of 20
		CharWnd.instance.toggle();
	    } else if(code == KeyEvent.VK_HOME) {
		ui.mainview.resetcam();
	    } else if(code == KeyEvent.VK_END) {
		screenshot = true;
		} else if(code == KeyEvent.VK_UP) { // new
			for(Widget w = ui.root.child; w != null; w = w.next){
				if(w instanceof Fightview){
					((Fightview)w).currentUp();
				}
			}
	    } else if(code == KeyEvent.VK_DOWN) { // new
			for(Widget w = ui.root.child; w != null; w = w.next){
				if(w instanceof Fightview){
					((Fightview)w).currentDown();
				}
			}
	    } else if((code == KeyEvent.VK_A)&&ctrl) { // new
			Config.pathDrinker = !Config.pathDrinker;
			String str = "Auto drinker: "+((Config.pathDrinker)?"ON":"OFF");
			ui.cons.out.println(str);
			ui.slen.error(str);
			addons.MainScript.flaskScript();
	    } else if((code == KeyEvent.VK_B)&&ctrl) { // new
			Config.minerSafety = !Config.minerSafety;
			String str = "Mining safety: "+((Config.minerSafety)?"ON":"OFF");
			ui.cons.out.println(str);
			ui.slen.error(str);
	    } else if((code == KeyEvent.VK_S)&&alt) { // new
			addons.MainScript.cleanupItems(1000, ui.mainview.gobAtMouse);
	    } else if((code == KeyEvent.VK_S)&&shift) { // new
			addons.MainScript.cleanupItems(1, ui.mainview.gobAtMouse);
	    }else if(key == ':') {
		entercmd();
	    } else if(key != 0) {
		wdgmsg("gk", (int)key);
	    }
	}
	return(true);
    }

    public void draw(GOut g) {
	if(screenshot&&Config.sshot_noui){visible = false;}
	super.draw(g);
	drawcmd(g, new Coord(20, 580));
	if(screenshot && (!Config.sshot_nonames || names_ready)){
	    visible = true;
	    screenshot = false;
	    try {
		Coord s = MainFrame.getInnerSize();
		String stamp = Utils.sessdate(System.currentTimeMillis());
		String ext = Config.sshot_compress?".jpg":".png";
		File f = new File("screenshots/SS_"+stamp+ext);
		f.mkdirs();
		Screenshot.writeToFile(f, s.x, s.y);
	    } catch (GLException e){e.printStackTrace();}
	    catch (IOException e){e.printStackTrace();}
	}
	
//	if(!afk && (System.currentTimeMillis() - ui.lastevent > 300000)) {
//	    afk = true;
//	    Widget slen = findchild(SlenHud.class);
//	    if(slen != null)
//		slen.wdgmsg("afk");
//	} else if(afk && (System.currentTimeMillis() - ui.lastevent < 300000)) {
//	    afk = false;
//	}
	if(Config.fps){
	    long now = System.currentTimeMillis();
	    ticks++;
	    if((now - last)>1000){
		fps = (int) (ticks*(now - last)/1000);
		last = now;
		ticks = 0;
	    }
	    g.text("FPS: "+fps, Coord.z);
	}
    }
    
    public void error(String msg) {
    }
}
