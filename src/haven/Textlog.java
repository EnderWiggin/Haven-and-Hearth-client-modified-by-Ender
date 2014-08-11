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
	    for(Text line : lines) {
		int dy1 = sz.y + y;
		int dy2 = dy1 + line.sz().y;
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
	if(Config.urlLinking){
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
        
    public boolean mousewheel(Coord c, int amount) {
	cury += amount * 20;
	if(cury < sz.y)
	    cury = sz.y;
	if(cury > maxy)
	    cury = maxy;
	return(true);
    }
        
    public boolean mousedown(Coord c, int button) {
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
	int y = -cury;
	synchronized(lines) {
		for(Text line : lines) {
			int dy1 = sz.y + y;
			Coord cc = new Coord(margin, dy1);
			if(c.isect(cc, line.sz() )){
				openURL( ((RichText)line).actionURL(c.add(cc.inv()) ) );
			}
			y += line.sz().y;
		}
	}
	return(Config.urlLinking);
    }
        
    public void mousemove(Coord c) {
	if(sdrag) {
	    double a = (double)(c.y - (sflarp.sz().y / 2)) / (double)(sz.y - sflarp.sz().y);
	    if(a < 0)
		a = 0;
	    if(a > 1)
		a = 1;
	    cury = (int)(a * (maxy - sz.y)) + sz.y;
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
	}
	return(false);
    }
	
	void openURL(String s){
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(s));
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
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
}
