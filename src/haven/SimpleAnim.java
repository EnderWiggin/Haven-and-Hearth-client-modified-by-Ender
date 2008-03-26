package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class SimpleAnim extends SimpleDrawable {
	Anim anim;
	int cf;
	int de;
	
	public SimpleAnim(Gob gob, String res) {
		super(gob, res);
		this.res = res;
		anim = Resource.loadanim(res);
		cf = 0;
		de = 0;
	}

	public boolean checkhit(Coord c) {
		int cl = anim.frames.get(cf).getRGB(c.x, c.y);
		return(Utils.rgbm.getAlpha(cl) >= 128);
	}

	public void draw(GOut g, Coord sc) {
		g.image(anim.tex(cf), sc.add(anim.cc.inv()));
	}

	public void draw2(BufferedImage t, Coord sc) {
		if(anim.isgay)
			Utils.drawgay(t, anim.frames.get(cf), sc.add(anim.cc.inv()));
		else
			t.getGraphics().drawImage(anim.frames.get(cf), sc.x - anim.cc.x, sc.y - anim.cc.y, null);
	}
	
	public Coord getoffset() {
		return(anim.cc);
	}

	public Coord getsize() {
		return(anim.sz);
	}
	
	public void ctick(int dt) {
		de += dt;
		while(de > anim.dur.get(cf)) {
			de -= anim.dur.get(cf);
			if(++cf >= anim.frames.size())
				cf = 0;
		}
	}
	
	public Sprite shadow() {
		return(null);
	}
}
