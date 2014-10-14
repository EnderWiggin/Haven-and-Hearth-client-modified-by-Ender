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
import java.awt.font.TextAttribute;
import java.util.LinkedList;
import java.util.List;

import java.net.URI;
import java.awt.Desktop;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.KeyEvent;

public class Textlog extends Widget {
    public int maxLines = 150;
    static Tex texpap = Resource.loadtex("gfx/hud/texpap");
    static Tex schain = Resource.loadtex("gfx/hud/schain");
    static Tex sflarp = Resource.loadtex("gfx/hud/sflarp");
    static RichText.Foundry fnd = new RichText.Foundry(TextAttribute.FAMILY, "Sans Serif", TextAttribute.SIZE, 12, TextAttribute.FOREGROUND, Color.BLACK);
    List<Text> lines;
    int maxy, cury;
    int margin = 3;
    boolean sdrag = false;
    public boolean drawbg = true;
    public Color defcolor = Color.BLACK;
	
	public SelBox selection = new SelBox();
	
	static final String urlRegex = "\\b(https?://|ftp://|www.)"
            + "[-A-Za-z0-9+&@#/%?=~_|!:,.;]"
            + "*[-A-Za-z0-9+&@#/%=~_|]";
	
    static {
	Widget.addtype("log", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Textlog(c, (Coord)args[0], parent));
		}
	    });
    }
	
    public void draw(GOut g) {
	Coord dc = new Coord();
	if (drawbg)
	    for (dc.y = 0; dc.y < sz.y; dc.y += texpap.sz().y) {
		for (dc.x = 0; dc.x < sz.x; dc.x += texpap.sz().x) {
		    g.image(texpap, dc);
		}
	    }
	g.chcolor();
	
	int y = -cury;
	synchronized(lines) {
		boolean selRow = false;
		boolean endline = false;
	    for(Text line : lines) {
			int dy1 = sz.y + y;
			int dy2 = dy1 + line.sz().y;
			
			if(hasfocus && selection.sel && !endline){
				Coord dotStart = null, dotEnd = null;
				Color col = g.getcolor();
				g.chcolor(51, 153, 255, 128);
				
				int ymem = -1;
				int dsy1 = 0;
				int wid = 0;
				int xdraw = 0;
				int ydraw = 0;
				int ysel = 0;
				
				for(RichText.Part part : ((RichText)line).parts){
					xdraw = part.x;
					wid = part.width();
					
					if(selection.partStart != null && part == selection.partStart){
						selRow = true;
						xdraw += selection.advStart;
						wid -= selection.advStart;
					}
					
					if(!selRow) continue;
					
					if(selection.partEnd != null && part == selection.partEnd){
						selRow = false;
						endline = true;
						wid -= part.width() - selection.advEnd;
					}
					
					dsy1 = dy1 + part.y;
					ydraw = part.height();
					
					if(dsy1 < 0){
						ydraw += dsy1;
						dsy1 = 0;
					}else if(dsy1 + ydraw > sz.y){
						ydraw = sz.y - dsy1;
					}
					if(ydraw < 0) ydraw = 0;
					
					if(selRow || endline) g.frect(Coord.z.add(margin + xdraw, dsy1), Coord.z.add(wid, ydraw) );
					
					if(endline) break;
				}
				
				g.chcolor(col);
			}
			
			if((dy2 > 0) && (dy1 < sz.y))
				g.image(line.tex(), new Coord(margin, dy1));
			y += line.sz().y;
	    }
	}
	
	if(maxy > sz.y) {
	    int fx = sz.x - sflarp.sz().x;
	    int cx = fx + (sflarp.sz().x / 2) - (schain.sz().x / 2);
	    for(y = 0; y < sz.y; y += schain.sz().y - 1)
		g.image(schain, new Coord(cx, y));
	    double a = (double)(cury - sz.y) / (double)(maxy - sz.y);
	    int fy = (int)((sz.y - sflarp.sz().y) * a);
	    g.image(sflarp, new Coord(fx, fy));
	}
    }
	
    public Textlog(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
	lines = new LinkedList<Text>();
	maxy = cury = 0;
    }
    
    public void append(String line, Color col) {
	Text rl;
	if(col == null)
	    col = defcolor;
	
	line = RichText.Parser.quote(line);
	if(Config.use_smileys){
	    line = Config.mksmiley(line);
	}
	if(Config.chatBoxInteraction){
		line = findURL(line);
	}
	rl = fnd.render(line, sz.x - (margin * 2) - sflarp.sz().x, TextAttribute.FOREGROUND, col, TextAttribute.SIZE, 12);
	synchronized(lines) {
	    lines.add(rl);
	    if((maxLines > 0)&&(lines.size() > maxLines)){
		Text tl = lines.remove(0);
		int dy = tl.sz().y;
		maxy -= dy;
		cury -= dy;
	    }
	}
	if(cury == maxy)
	    cury += rl.sz().y;
	maxy += rl.sz().y;
    }
        
    public void append(String line) {
	append(line, null);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "apnd") {
	    append((String)args[0]);
	}
    }
	
	public boolean keydown(KeyEvent ev) {
		if(ev.isControlDown() && ev.getKeyCode() == KeyEvent.VK_C){
			copySelected();
			return true;
		}else if(ev.isControlDown() && ev.getKeyCode() == KeyEvent.VK_A && lines.size() > 0){
			selection.sel = true;
			Text first = lines.get(0);
			Text last = lines.get(lines.size() - 1);
			selection.selStart(first, ((RichText)first).charNum(new Coord(0,0) ) );
			selection.selEnd(last, ((RichText)last).charNum( last.sz() ) );
			return true;
		}
		return false;
    }
	
	void copySelected(){
		if(!selection.sel) return;
		String string = "";
		synchronized(lines){
			int y = -cury;
			boolean selRow = false;
			boolean f = true;
			for(Text line : lines) {
				if(selection.selStart() == line && selection.selEnd() == line){
					string += ((RichText)line).getString(selection.selCharStart(), selection.selCharEnd());
					break;
				}else if(selection.selStart() == line){
					selRow = true;
					
					string += ((RichText)line).getString(selection.selCharStart(), -1);
				}else if(selection.selEnd() == line){
					selRow = false;
					
					string += ((RichText)line).getString(0, selection.selCharEnd());
					break;
				}else if(selRow){
					string += ((RichText)line).getString(0, -1);
				}
				
				if(selRow) string += "\n";
			}
		}
		
		Utils.setClipboard(string);
	}
        
    public boolean mousewheel(Coord c, int amount) {
	cury += amount * 20;
	if(cury < sz.y)
	    cury = sz.y;
	if(cury > maxy)
	    cury = maxy;
	return(true);
    }
        
    public boolean mousedown(Coord c, int button) {
		parent.setfocus(this);
		if(button != 1)
			return(false);
		int fx = sz.x - sflarp.sz().x;
		int cx = fx + (sflarp.sz().x / 2) - (schain.sz().x / 2);
		if((maxy > sz.y) && (c.x >= fx)) {
			sdrag = true;
			ui.grabmouse(this);
			mousemove(c);
			return(true);
		}
		
		selection.sel = false;
		if(!Config.chatBoxInteraction) return false;
		int y = -cury;
		synchronized(lines) {
			for(Text line : lines) {
				int dy1 = sz.y + y;
				Coord cc = new Coord(margin, dy1);
				if(c.isect(cc, line.sz() )){
					selection.selStart(line, ((RichText)line).charNum(c.add(cc.inv()) ) );
					selection.selCharEnd = selection.selCharStart;
					
					ui.grabmouse(this);
					
					selection.draging = true;
					
					if(!selection.sel && selection.time > System.currentTimeMillis() ){
						selection.time = 0;
						selection.sel = true;
						selection.selStart(line, ((RichText)line).charNum(new Coord(0,0) ) );
						selection.selEnd(line, ((RichText)line).charNum( line.sz() ) );
					}
					
					selection.time = System.currentTimeMillis() + 200;
					
					return(true);
				}
				y += line.sz().y;
			}
		}
		return(true);
    }
	
	public void mousemove(Coord c) {
		if(sdrag) {
			double a = (double)(c.y - (sflarp.sz().y / 2)) / (double)(sz.y - sflarp.sz().y);
			if(a < 0)
			a = 0;
			if(a > 1)
			a = 1;
			cury = (int)(a * (maxy - sz.y)) + sz.y;
		}else if(selection.draging){
			int y = -cury;
			synchronized(lines) {
				for(Text line : lines) {
					int dy1 = sz.y + y;
					Coord cc = new Coord(margin, dy1);
					if(c.isect(cc, new Coord(sz.x - margin, line.sz().y) )){
						if(!selection.sel && (selection.selCharStart != selection.selCharEnd || selection.selStart != selection.selEnd)){
							selection.sel = true;
						}
						
						selection.selEnd(line, ((RichText)line).charNum(c.add(cc.inv()) ) );
						
						if(selection.sel) selection.invertSel();
					}
					y += line.sz().y;
				}
			}
		}
    }
    
    public void setprog(double a){
	if(a < 0)
	    a = 0;
	if(a > 1)
	    a = 1;
	cury = (int)(a * (maxy - sz.y)) + sz.y;
    }
    
    public boolean mouseup(Coord c, int button) {
		if((button == 1) && sdrag) {
			sdrag = false;
			ui.grabmouse(null);
			return(true);
		}else if(button == 1) {
			int y = -cury;
			synchronized(lines) {
				for(Text line : lines) {
					int dy1 = sz.y + y;
					Coord cc = new Coord(margin, dy1);
					if(selection.draging){
						selection.draging = false;
						ui.grabmouse(null);
					}
					if(!selection.sel){
						if(c.isect(cc, line.sz() ) ){
							if(openURL( ((RichText)line).actionURL(c.add(cc.inv()) ) )){
								return true;
							}
						}
					}
					y += line.sz().y;
				}
			}
		}
		return(false);
    }
	
	boolean openURL(String s){
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(s));
				return true;
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		
		return false;
	}
	
	String findURL(String line){
		String ln = "";
        String[] parts = line.split("\\s+");
		
        for(String s : parts){
			if(isURL(s) ){
				ln = ln + " $url[" + s + "]";
			}else{
				ln = ln + " " + s;
			}
		}
		
		return ln;
	}
	
	public static boolean isURL(String url){
		return url.matches(urlRegex); 
	}
	
	public class SelBox{
		boolean sel = false;
		boolean draging = false;
		boolean invert = false;
		
		int selCharStart = -1;
		int selCharEnd = -1;
		
		Text selStart;
		Text selEnd;
		
		long time = 0;
		
		RichText.Part partStart;
		RichText.Part partEnd;
		
		int advStart;
		int advEnd;
		
		public SelBox(){}
		
		Text selStart(){
			if(invert){
				return selEnd;
			}
			return selStart;
		}
		
		Text selEnd(){
			if(invert){
				return selStart;
			}
			return selEnd;
		}
		
		int selCharStart(){
			if(invert){
				return selCharEnd;
			}
			return selCharStart;
		}
		
		int selCharEnd(){
			if(invert){
				return selCharStart;
			}
			return selCharEnd;
		}
		
		void selStart(Text line, int ch){
			selStart = line;
			selCharStart = ch;
			
			fixSelect();
		}
		
		void selEnd(Text line, int ch){
			selEnd = line;
			selCharEnd = ch;
			
			fixSelect();
		}
		
		void fixSelect(){
			if(selStart() != null){
				advStart = ((RichText)selStart() ).getAdv(selection.selCharStart() );
				partStart = ((RichText)selStart() ).getPart(selection.selCharStart() );
			}
			
			if(selEnd() != null){
				advEnd = ((RichText)selEnd() ).getAdv(selection.selCharEnd() );
				partEnd = ((RichText)selEnd() ).getPart(selection.selCharEnd() );
			}
		}
		
		void invertSel(){
			invert = invertTest();
		}
		
		boolean invertTest(){
			if(selStart == selEnd){
				if(selCharEnd < selCharStart) return true;
			}else if(lineTest() ){
				return true;
			}
			
			return false;
		}
		
		boolean lineTest(){
			synchronized(lines) {
				for(Text line : lines) {
					if(line == selEnd){
						return true;
					}else if(selStart == line){
						return false;
					}
				}
			}
			
			return false;
		}
	}
}
