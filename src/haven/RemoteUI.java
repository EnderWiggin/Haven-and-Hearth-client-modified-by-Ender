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

public class RemoteUI implements UI.Receiver {
    Session sess;
    UI ui;
	
    public RemoteUI(Session sess) {
	this.sess = sess;
	Widget.initbardas();
    }
	
    public void rcvmsg(int id, String name, Object... args) {
	Message msg = new Message(Message.RMSG_WDGMSG);
	msg.adduint16(id);
	msg.addstring(name);
	msg.addlist(args);
	sess.queuemsg(msg);
    }
	
    public void run(UI ui) throws InterruptedException {
	this.ui = ui;
	ui.setreceiver(this);
	while(sess.alive()) {
	    Message msg;
	    while((msg = sess.getuimsg()) != null) {
		if(msg.type == Message.RMSG_NEWWDG) {
		    int id = msg.uint16();
		    String type = msg.string();
		    Coord c = msg.coord();
		    int parent = msg.uint16();
		    Object[] args = msg.list();
		    if(type.equals("cnt")){
		    	args[0] = MainFrame.getInnerSize();
		    }else if(type.equals("img") && args.length >= 1){
		    	if(((String)args[0]).equals("gfx/ccscr"))
		    		c = MainFrame.getCenterPoint().add(-400, -300);
		    	if(((String)args[0]).equals("gfx/logo2"))
		    		c = MainFrame.getCenterPoint().add(-415, -300);
				if(((String)args[0]).indexOf("gfx/hud/prog/") >= 0) // new
					addons.HavenUtil.HourglassID = id; // new
		    }else if(type.equals("charlist") && args.length >= 1){
		    	c = MainFrame.getCenterPoint().add(-380, -50);
		    }else if(type.equals("ibtn") && args.length >= 2){
		    	if(((String)args[0]).equals("gfx/hud/buttons/ncu") && ((String)args[1]).equals("gfx/hud/buttons/ncd")){
		    		c = MainFrame.getCenterPoint().add(86, 214);
		    	}
		    }else if(type.equals("wnd") && c.x == 400 && c.y == 200){
		    	c = MainFrame.getCenterPoint().add(0,-100);
		    }
		    ui.newwidget(id, type, c, parent, args);
		} else if(msg.type == Message.RMSG_WDGMSG) {
		    int id = msg.uint16();
		    String name = msg.string();
		    ui.uimsg(id, name, msg.list());
		} else if(msg.type == Message.RMSG_DSTWDG) {
		    int id = msg.uint16();
		    ui.destroy(id);
			if(id == addons.HavenUtil.HourglassID) addons.HavenUtil.HourglassID = -1; // new
		}
	    }
	    synchronized(sess) {
		sess.wait();
	    }
	}
    }
}
