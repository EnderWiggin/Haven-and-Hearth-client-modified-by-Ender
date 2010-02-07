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

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;

public class Text {
    public static final Foundry std;
    public BufferedImage img;
    public final String text;
    private FontMetrics m;
    private Tex tex;
    public static final Color black = Color.BLACK;
    public static final Color white = Color.WHITE;
	
    static {
	std = new Foundry(new Font("SansSerif", Font.PLAIN, 10));
    }
	
    public static int[] findspaces(String text) {
	java.util.List<Integer> l = new ArrayList<Integer>();
	for(int i = 0; i < text.length(); i++) {
	    char c = text.charAt(i);
	    if(Character.isWhitespace(c))
		l.add(i);
	}
	int[] ret = new int[l.size()];
	for(int i = 0; i < ret.length; i++)
	    ret[i] = l.get(i);
	return(ret);
    }
        
    public static class Foundry {
	private FontMetrics m;
	Font font;
	Color defcol;
	public boolean aa = false;
		
	public Foundry(Font f, Color defcol) {
	    font = f;
	    this.defcol = defcol;
	    BufferedImage junk = TexI.mkbuf(new Coord(10, 10));
	    Graphics tmpl = junk.getGraphics();
	    tmpl.setFont(f);
	    m = tmpl.getFontMetrics();
	}
		
	public Foundry(Font f) {
	    this(f, Color.WHITE);
	}
	
	public Foundry(String font, int psz) {
	    this(new Font(font, Font.PLAIN, psz));
	}
		
	private Coord strsize(String text) {
	    int h = m.getAscent() + m.getDescent();
	    return(new Coord(m.stringWidth(text), h));
	}
                
	public Text renderwrap(String text, Color c, int width) {
	    Text t = new Text(text);
	    int y = 0;
	    int[] sl = findspaces(text);
	    int s = 0, e = 0, i = 0;
	    java.util.List<String> lines = new LinkedList<String>();
	    while(s < text.length()) {
		do {
		    int te;
		    if(i < sl.length)
			te = sl[i];
		    else
			te = text.length();
		    Coord b = strsize(text.substring(s, te));
		    if(b.x > width) {
			break;
		    } else {
			e = te;
			i++;
		    }
		    if((te < text.length()) && (text.charAt(te) == '\n')) {
			e = te;
			break;
		    }
		} while(i <= sl.length);
		if(e < s)
		    return(Text.render("Text rendering failure", Color.RED));
		String line = text.substring(s, e);
		lines.add(line);
		Coord b = strsize(line);
		y += b.y;
		s = e + 1;
	    }
	    t.img = TexI.mkbuf(new Coord(width, y));
	    Graphics g = t.img.createGraphics();
	    if(aa)
		Utils.AA(g);
	    g.setFont(font);
	    g.setColor(c);
	    t.m = g.getFontMetrics();
	    y = 0;
	    for(String line : lines) {
		g.drawString(line, 0, y + t.m.getAscent());
		Coord b = strsize(line);
		y += b.y;
	    }
	    g.dispose();
	    return(t);
	}
                
	public Text renderwrap(String text, int width) {
	    return(renderwrap(text, defcol, width));
	}
                
	public Text render(String text, Color c) {
	    Text t = new Text(text);
	    Coord sz = strsize(text);
	    if(sz.x < 1)
		sz = sz.add(1, 0);
	    t.img = TexI.mkbuf(sz);
	    Graphics g = t.img.createGraphics();
	    if(aa)
		Utils.AA(g);
	    g.setFont(font);
	    g.setColor(c);
	    t.m = g.getFontMetrics();
	    g.drawString(text, 0, t.m.getAscent());
	    g.dispose();
	    return(t);
	}
		
	public Text render(String text) {
	    return(render(text, defcol));
	}
                
	public Text renderf(String fmt, Object... args) {
	    return(render(String.format(fmt, args)));
	}
    }
	
    private Text(String text) {
	this.text = text;
    }
	
    public Coord sz() {
	return(Utils.imgsz(img));
    }
	
    public Coord base() {
	return(new Coord(0, m.getAscent()));
    }
    
    public int advance(int pos) {
	return(m.stringWidth(text.substring(0, pos)));
    }
    
    public int charat(int x) {
	int l = 0, r = text.length() + 1;
	while(true) {
	    int p = (l + r) / 2;
	    int a = advance(p);
	    if(a < x) {
		if(l == p)
		    return(l);
		l = p;
	    } else if(a > x) {
		r = p;
	    } else {
		return(p);
	    }
	}
    }
	
    public static Text render(String text, Color c) {
	return(std.render(text, c));
    }
	
    public static Text renderf(Color c, String text, Object... args) {
	return(std.render(String.format(text, args), c));
    }
	
    public static Text render(String text) {
	return(render(text, Color.WHITE));
    }
	
    public Tex tex() {
	if(tex == null)
	    tex = new TexI(img);
	return(tex);
    }
    
    public static void main(String[] args) throws Exception {
	String cmd = args[0].intern();
	if(cmd == "render") {
	    PosixArgs opt = PosixArgs.getopt(args, 1, "aw:f:s:");
	    boolean aa = false;
	    String font = "SansSerif";
	    int width = 100, size = 10;
	    for(char c : opt.parsed()) {
		if(c == 'a') {
		    aa = true;
		} else if(c == 'f') {
		    font = opt.arg;
		} else if(c == 'w') {
		    width = Integer.parseInt(opt.arg);
		} else if(c == 's') {
		    size = Integer.parseInt(opt.arg);
		}
	    }
	    Foundry f = new Foundry(font, size);
	    f.aa = aa;
	    Text t = f.renderwrap(opt.rest[0], width);
	    java.io.OutputStream out = new java.io.FileOutputStream(opt.rest[1]);
	    javax.imageio.ImageIO.write(t.img, "PNG", out);
	    out.close();
	}
    }
}
