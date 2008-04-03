package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Reader;
import java.util.*;

public class Avatar extends GAttrib {
	List<Layer> layers;
	static List<String> prios;
	TexIM image = null;
	public static final Coord sz = new Coord(212, 249);
	
	static {
		prios = new ArrayList<String>();
		Reader r = Resource.gettext("gfx/hud/equip/prio");
		Scanner s = new Scanner(r);
		try {
			while(true)
				prios.add(s.nextLine());
		} catch(NoSuchElementException e) {}
	}
	
	private class Layer {
		String res;
		BufferedImage img = null;
		int prio;
		
		Layer(String res) {
			this.res = res;
			String bn = res.substring(res.lastIndexOf('/') + 1);
			bn = bn.substring(0, bn.lastIndexOf('.'));
			this.prio = prios.indexOf(bn);
		}
		
		BufferedImage img() {
			if(img == null)
				img = Resource.loadimg(res);
			return(img);
		}
		
		public String toString() {
			return(res + ":" + prio);
		}
	}
	
	public Avatar(Gob gob) {
		super(gob);
	}
	
	void setlayers(List<String> layers) {
		this.layers = new ArrayList<Layer>();
		for(String res : layers)
			this.layers.add(new Layer(res));
		sort();
		if(image != null)
			image.dispose();
		image = null;
	}

	private void sort() {
		Collections.sort(layers, new Comparator<Layer>() {
			public int compare(Layer a, Layer b) {
				return(a.prio - b.prio);
			}
		});
	}
	
	public Tex tex() {
		TexIM image = this.image;
		if(image == null) {
			image = new TexIM(sz);
			Graphics g = image.graphics();
			for(Layer l : layers)
				g.drawImage(l.img(), 0, 0, null);
			image.update();
			this.image = image;
		}
		return(image);
	}
}
