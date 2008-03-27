package haven;

import java.awt.image.BufferedImage;
import java.util.*;

public class Layered extends Drawable {
	List<SimpleDrawable> layers;
	Sprite shadow = null;
	int cf = 0, de = 0;
	
	public Layered(Gob gob) {
		super(gob);
		layers = new ArrayList<SimpleDrawable>();
	}

	public void setlayers(List<SimpleDrawable> layers) {
		shadow = null;
		for(SimpleDrawable l : layers) {
			Sprite sdw = l.shadow();
			if(sdw != null)
				shadow = sdw;
		}
		this.layers = layers;
		de = 0;
		cf = 0;
		sort();
	}
	
	public boolean checkhit(Coord c) {
		for(SimpleDrawable d : layers) {
			if(d.checkhit(c))
				return(true);
		}
		return(false);
	}

	public void draw(GOut g, Coord sc) {
		Coord sz = getsize();
		BufferedImage buf = Tex.mkbuf(sz);
		Coord cc = getoffset();
		for(SimpleDrawable d : layers)
			d.draw2(buf, cc);
		Coord dc = sc.add(getoffset().inv());
		g.image(buf, dc);
	}

	public Coord getoffset() {
		return(layers.get(0).getoffset());
	}

	public Coord getsize() {
		return(layers.get(0).getsize());
	}

	private void sort() {
		Collections.sort(layers, new Comparator<SimpleDrawable>() {
			public int compare(SimpleDrawable aa, SimpleDrawable bb) {
				if(aa instanceof SimpleAnim) {
					SimpleAnim a = (SimpleAnim)aa;
					SimpleAnim b = (SimpleAnim)bb;
					return(a.anim.prio.get(cf) - b.anim.prio.get(cf));
				} else {
					SimpleSprite a = (SimpleSprite)aa;
					SimpleSprite b = (SimpleSprite)bb;
					return(a.spr.prio - b.spr.prio);
				}
			}
		});
	}
	
	public void ctick(int dt) {
		de += dt;
		if(layers.get(0) instanceof SimpleAnim) {
			Anim anim = ((SimpleAnim)layers.get(0)).anim;
			while(de > anim.dur.get(cf)) {
				de -= anim.dur.get(cf);
				if(++cf >= anim.frames.size())
					cf = 0;
			}
			for(SimpleDrawable d : layers) {
				((SimpleAnim)d).cf = cf;
			}
			sort();
		}
	}
	
	public Sprite shadow() {
		return(shadow);
	}
}
