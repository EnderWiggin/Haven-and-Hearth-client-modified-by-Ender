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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ender.GoogleTranslator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ChatHW extends HWindow {
    TextEntry in;
    Textlog out;
    static final Collection<Integer> todarken = new ArrayList<Integer>();
    static final Pattern hlpatt = Pattern.compile("@\\$\\[(.+)\\]");
	chatLog logger;
	
    static {
	Widget.addtype("slenchat", new WidgetFactory() {
	    public Widget create(Coord c, Widget parent, Object[] args) {
		String t = (String)args[0];
		boolean cl = false;
		if(args.length > 1)
		cl = (Integer)args[1] != 0;
		return(new ChatHW(parent, t, cl));
	    }
	});
	todarken.add(Color.GREEN.getRGB());
	todarken.add(Color.CYAN.getRGB());
	todarken.add(Color.YELLOW.getRGB());
    }

    public ChatHW(Widget parent, String title, boolean closable) {
	super((Widget)UI.instance.chat, title, closable);
	in = new TextEntry(new Coord(0, sz.y - 20), new Coord(sz.x, 20), this, "");
	in.canactivate = true;
	in.bgcolor = new Color(64, 64, 64, 192);
	out = new Textlog(Coord.z, new Coord(sz.x, sz.y - 20), this);
	out.drawbg = false;

	if(cbtn != null) {
	    cbtn.raise();
	    if (title.equals("Area Chat"))
		cbtn.hide();
	}
	setsz(sz);
	logger = new chatLog();
    }

    public void setsz(Coord s) {
	super.setsz(s);
	in.c = new Coord(0, sz.y - 20);
	in.sz = new Coord(sz.x, 20);
	out.sz = new Coord(sz.x, sz.y - 20);
    }

    @Override
    public void setfocus(Widget w) {
	if(w == this){
	    w = in;
	}
	if(w == null)
	    return;
	super.setfocus(w);
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "log") {

	    if(Config.muteChat){return;}

	    Color col = null;
	    if(args.length > 1)
		col = (Color)args[1];
	    if(args.length > 2)
		makeurgent((Integer)args[2]);
	    String str = (String)args[0];
	    int id = 0;
		String[] s2 = null;
	    try{
			Matcher m = hlpatt.matcher(str);
			String s = "";
			if(m.find()){
				s = m.group(1);
			}
			s2 = s.split(",");
			id = Integer.parseInt(s2[0]);
	    } catch(Exception e){}
	    Gob gob;
	    if(id != 0) {
		catchBroadcast(s2);
		if ((gob = ui.sess.glob.oc.getgob(id)) != null){
		    gob.highlight = new Gob.HlFx(System.currentTimeMillis());
		}
	    } else {
		if((col != null)&&(todarken.contains(col.getRGB())))
		    col = col.darker();
		str = GoogleTranslator.translate(str);
		if(Config.timestamp)
		    str = Utils.timestamp() + str;
		out.append(str, col);
		if(Config.chatLogger)
			logger.save(str);
	    }
	} else if(msg == "focusme") {
	    shp.setawnd(this, true);
	    setfocus(in);
	} else {
	    super.uimsg(msg, args);
	}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == in) {
	    if(msg == "activate") {
		wdgmsg("msg", args[0]);
		in.settext("");
		return;
	    }
	}
	super.wdgmsg(sender, msg, args);
    }

    public boolean mousewheel(Coord c, int amount)
    {
	return(out.mousewheel(c, amount));
    }
	
	void catchBroadcast(String[] s2){
		if(s2 == null) return;
		
		if(Config.showDirection && s2.length == 6){
			Gob gob;
			if((gob = ui.sess.glob.oc.getgob(Integer.parseInt(s2[4]) )) != null){
				int olid = Integer.parseInt(s2[5]);
				boolean found = false;
				
				for(int i = 0; i<TrackingWnd.instances.size(); i++){
					TrackingWnd wnd = TrackingWnd.instances.get(i);
					if(wnd.broadcastID == olid){
						found = true;
						break;
					}
				}
				
				if(!found){
					new TrackingWnd(gob, olid,
					Integer.parseInt(s2[0]),
					Integer.parseInt(s2[1]),
					Integer.parseInt(s2[2]),
					Integer.parseInt(s2[3]),
					null);
				}
			}
		}
	}
	
	private class chatLog{
		BufferedWriter buffWriter;
		
		public void createFile(){
			try{
				String timeString = Utils.sessdate(System.currentTimeMillis());
				File file = new File("./logs/"+timeString+"_"+title+".save");
				File folder = file.getParentFile();
				
				if(!folder.exists()){
					folder.mkdirs();
				}
				file.createNewFile();
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				buffWriter = new BufferedWriter(fw);
				
				buffWriter.write(timeString);
				buffWriter.newLine();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		void save(String str){
			if(buffWriter == null) createFile();
			
			if(!Config.timestamp)
				str = Utils.timestamp() + str;
			
			try {
				buffWriter.write(str);
				buffWriter.newLine();
				buffWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
