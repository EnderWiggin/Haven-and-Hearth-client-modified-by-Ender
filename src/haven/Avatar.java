package haven;

import java.util.List;

public class Avatar extends GAttrib {
    AvaRender rend = null;
        
    public Avatar(Gob gob) {
	super(gob);
    }
	
    void setlayers(List<Indir<Resource>> layers) {
	rend = new AvaRender(layers);
    }
}
