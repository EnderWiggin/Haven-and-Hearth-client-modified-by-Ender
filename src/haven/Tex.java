package haven;

import java.awt.Color;
import java.nio.*;
import java.util.*;
import javax.media.opengl.*;

public abstract class Tex {
	protected Coord dim;
    
	public Tex(Coord sz) {
		dim = sz;
	}
	
	public Coord sz() {
		return(dim);
	}

	public static int nextp2(int in) {
		int ret;
	
		for(ret = 1; ret < in; ret <<= 1);
		return(ret);
	}

	public abstract void render(GOut g, Coord c, Coord ul, Coord br, Coord sz);

	public void render(GOut g, Coord c) {
		render(g, c, Coord.z, dim, dim);
	}
    
	public void crender(GOut g, Coord c, Coord ul, Coord sz, Coord tsz) {
		Coord t = new Coord(c);
		Coord uld = new Coord(0, 0);
		Coord brd = new Coord(dim);
		Coord szd = new Coord(tsz);
		if(c.x < ul.x) {
			t.x = ul.x;
			uld.x = ul.x - c.x;
			szd.x -= uld.x;
		}
		if(c.y < ul.y) {
			t.y = ul.y;
			uld.y = ul.y - c.y;
			szd.y -= uld.y;
		}
		if(c.x + tsz.x > ul.x + sz.x) {
			int pd = c.x + tsz.x - ul.x - sz.x;
			szd.x -= pd;
			brd.x -= (pd * dim.x) / tsz.x;
		}
		if(c.y + tsz.y > ul.y + sz.y) {
			int pd = c.y + dim.y - ul.y - sz.y;
			szd.y -= pd;
			brd.y -= (pd * dim.y) / tsz.y;
		}
		render(g, t, uld, brd, szd);
	}
	
	public void crender(GOut g, Coord c, Coord ul, Coord sz) {
		crender(g, c, ul, sz, dim);
	}
		
	public void dispose() {}
}
