package haven;

import static haven.Resource.imgc;
import java.awt.Graphics;
import java.util.*;

public class Avatar extends GAttrib {
	List<Indir<Resource>> layers;
	List<Resource.Image> images;
	boolean loading;
	TexIM image = null;
	public static final Coord sz = new Coord(212, 249);
	private static Comparator<Indir<Resource>> rescomp = new Comparator<Indir<Resource>>() {
		public int compare(Indir<Resource> a, Indir<Resource> b) {
			if((a.get() == null) && (b.get() == null))
				return(0);
			if((a.get() != null) && (b.get() == null))
				return(-1);
			if((a.get() == null) && (b.get() != null))
				return(1);
			return(a.get().compareTo(b.get()));
		}
	};
	
	public Avatar(Gob gob) {
		super(gob);
	}
	
	void setlayers(List<Indir<Resource>> layers) {
		Collections.sort(layers, rescomp);
		if(!layers.equals(this.layers)) {
			this.layers = layers;
			loading = true;
		}
	}
	
	public Tex tex() {
		TexIM image = this.image;
		if(loading) {
			List<Resource.Image> images = new ArrayList<Resource.Image>();
			loading = false;
			for(Indir<Resource> r : layers) {
				if(r.get() == null)
					loading = true;
				else
					images.addAll(r.get().layers(imgc));
			}
			Collections.sort(images);
			if(!images.equals(this.images)) {
				this.images = images;
				if(image != null)
					image.dispose();
				image = null;
			}
		}
		if(image == null) {
			image = new TexIM(sz);
			Graphics g = image.graphics();
			loading = false;
			for(Resource.Image i : images)
				g.drawImage(i.img, i.o.x, i.o.y, null);
			image.update();
			this.image = image;
		}
		return(image);
	}
}
