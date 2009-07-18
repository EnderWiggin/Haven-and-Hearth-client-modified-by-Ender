package haven;

import java.awt.*;

public class Speaking extends GAttrib {
    Coord off;
    Text text;
    static IBox sb = null;
    Tex svans;
    static final int sx = 3;
	
    public Speaking(Gob gob, Coord off, String text) {
	super(gob);
	if(sb == null)
	    sb = new IBox("gfx/hud/emote", "tl", "tr", "bl", "br", "el", "er", "et", "eb");
	svans = Resource.loadtex("gfx/hud/emote/svans");
	this.off = off;
	this.text = Text.render(text, Color.BLACK);
    }
	
    public void update(String text) {
	this.text = Text.render(text, Color.BLACK);
    }
	
    public void draw(GOut g, Coord c) {
	Coord sz = text.sz();
	if(sz.x < 10)
	    sz.x = 10;
	Coord tl = c.add(new Coord(sx, sb.bsz().y + sz.y + svans.sz().y - 1).inv());
	Coord ftl = tl.add(sb.tloff());
	g.chcolor(Color.WHITE);
	g.frect(ftl, sz);
	sb.draw(g, tl, sz.add(sb.bsz()));
	g.chcolor(Color.BLACK);
	g.image(text.tex(), ftl);
	g.chcolor(Color.WHITE);
	g.image(svans, c.add(0, -svans.sz().y));
    }
}
