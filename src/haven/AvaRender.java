package haven;

import static haven.Resource.imgc;
import java.awt.Graphics;
import java.util.*;

public class AvaRender {
    List<Indir<Resource>> layers;
    List<Resource.Image> images;
    boolean loading;
    TexIM image = null;
    public static final Coord sz = new Coord(212, 249);
    
    public AvaRender(List<Indir<Resource>> layers) {
        Collections.sort(layers);
        this.layers = layers;
        loading = true;
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
            for(Resource.Image i : images)
                g.drawImage(i.img, i.o.x, i.o.y, null);
            image.update();
            this.image = image;
        }
        return(image);
    }
}
