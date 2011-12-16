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

public class RichTextBox extends Widget {
    public Color bg = Color.BLACK;
    private final RichText.Foundry fnd;
    private RichText text;
    private Scrollbar sb;
    public boolean registerclicks = false;
    
    public RichTextBox(Coord c, Coord sz, Widget parent, String text, RichText.Foundry fnd) {
	super(c, sz, parent);
	this.fnd = fnd;
	this.text = fnd.render(text, sz.x - 20);
	this.sb = new Scrollbar(new Coord(sz.x, 0), sz.y, this, 0, this.text.sz().y + 20 - sz.y);
    }
    
    public RichTextBox(Coord c, Coord sz, Widget parent, String text, Object... attrs) {
	this(c, sz, parent, text, new RichText.Foundry(attrs));
    }
    
    public void draw(GOut g) {
	if(bg != null) {
	    g.chcolor(bg);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	}
	g.image(text.tex(), new Coord(10, 10 - sb.val));
	super.draw(g);
    }
    
    public void settext(String text) {
	this.text = fnd.render(text, sz.x - 20);
	sb.max = this.text.sz().y + 20 - sz.y;
	sb.val = 0;
    }
    
    public void setsz(Coord s) {
	sz = s;
	sb.c = new Coord(sz.x - 10, 0);
	sb.sz = new Coord(10, sz.y);
	settext(text.text);
    }
    
    public boolean mousewheel(Coord c, int amount) {
	sb.ch(amount * 20);
	return(true);
    }
    
    public boolean mousedown(Coord c, int button) {
	if(registerclicks) {
	    String action = text.actionat(c.sub(10, 10 - sb.val));
	    if(action!=null) {
		wdgmsg("click", action, button);
	    }
	}
	return super.mousedown(c, button);
    }
}
