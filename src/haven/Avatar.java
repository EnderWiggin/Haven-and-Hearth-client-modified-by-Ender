package haven;

import static haven.Resource.imgc;
import java.awt.Graphics;
import java.util.*;

public class Avatar extends GAttrib {
	List<Resource> layers;
	TexIM image = null;
	public static final Coord sz = new Coord(212, 249);
	
	public Avatar(Gob gob) {
		super(gob);
	}
	
	void setlayers(List<Resource> layers) {
		Collections.sort(layers);
		if(!layers.equals(this.layers)) {
			this.layers = layers;
			if(image != null)
				image.dispose();
			image = null;
			
		}
	}
	
	public Tex tex() {
		TexIM image = this.image;
		if(image == null) {
                        long begin = System.currentTimeMillis();
			image = new TexIM(sz);
			Graphics g = image.graphics();
			for(Resource l : layers) {
				if(l.loading)
					g.drawImage(l.layer(imgc).img, 0, 0, null);
			}
			image.update();
			this.image = image;
                        System.out.println(System.currentTimeMillis() - begin);
		}
		return(image);
	}
}
