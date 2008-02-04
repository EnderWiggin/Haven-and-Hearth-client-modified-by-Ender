package haven;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Speaking extends GAttrib {
	Coord off;
	String text;
	static IBox sb = null;
	BufferedImage svans;
	static final int sx = 3;
	
	public Speaking(Gob gob, Coord off, String text) {
		super(gob);
		if(sb == null) {
			sb = new IBox(Resource.loadimg("gfx/hud/emote/tl.gif"),
					Resource.loadimg("gfx/hud/emote/tr.gif"),
					Resource.loadimg("gfx/hud/emote/bl.gif"),
					Resource.loadimg("gfx/hud/emote/br.gif"),
					Resource.loadimg("gfx/hud/emote/el.gif"),
					Resource.loadimg("gfx/hud/emote/er.gif"),
					Resource.loadimg("gfx/hud/emote/et.gif"),
					Resource.loadimg("gfx/hud/emote/eb.gif"));
		}
		svans = Resource.loadimg("gfx/hud/emote/svans.gif");
		this.off = off;
		this.text = text;
	}
	
	public void draw(Graphics g, Coord c) {
		FontMetrics m = g.getFontMetrics();
		java.awt.geom.Rectangle2D ts = m.getStringBounds(text, g);
		Coord sz = new Coord((int)ts.getWidth(), (int)ts.getHeight());
		if(sz.x < 10)
			sz.x = 10;
		Coord tl = c.add(new Coord(sx, sb.bsz().y + sz.y + svans.getHeight() - 1).inv());
		Coord ftl = tl.add(sb.tloff());
		g.setColor(Color.WHITE);
		g.fillRect(ftl.x, ftl.y, sz.x, sz.y);
		sb.draw(g, tl, sz.add(sb.bsz()));
		g.setColor(Color.BLACK);
		Utils.drawtext(g, text, ftl);
		g.drawImage(svans, c.x, c.y - svans.getHeight(), null);
	}
}
