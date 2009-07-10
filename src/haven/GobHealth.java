package haven;

import java.awt.Color;

public class GobHealth extends GAttrib {
    int hp;
    HpFx fx = new HpFx();
    
    public GobHealth(Gob g, int hp) {
	super(g);
	this.hp = hp;
    }
    
    private class HpFx implements Sprite.Part.Effect {
	public GOut apply(GOut in) {
	    return(new GOut(in) {
		    {chcolor();}
		    
		    public void chcolor(Color col) {
			super.chcolor(Utils.blendcol(col, new Color(255, 0, 0, 128 - ((hp * 128) / 4))));
		    }
		});
	}
    }

    public Sprite.Part.Effect getfx() {
	if(hp == 4)
	    return(null);
	return(fx);
    }
}
