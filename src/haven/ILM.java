package haven;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Collection;
import javax.media.opengl.GL;

public class ILM extends TexRT {
	public final static BufferedImage ljusboll;
	Collection<Lumin> ll;
	TexI lbtex;
	Color amb;
	
	static {
		int sz = 200, min = 50;
		BufferedImage lb = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
		Graphics g = lb.createGraphics();
		for(int y = 0; y < sz; y++) {
			for(int x = 0; x < sz; x++) {
				double dx = sz / 2 - x;
				double dy = sz / 2 - y;
				double d = Math.sqrt(dx * dx + dy * dy);
				int gs;
				if(d > sz / 2)
					gs = 255;
				else if(d < min)
					gs = 0;
				else
					gs = (int)(((d - min) / ((sz / 2) - min)) * 255);
				gs /= 2;
				Color c = new Color(gs, gs, gs, 128 - gs);
				g.setColor(c);
				g.fillRect(x, y, 1, 1);
			}
		}
		ljusboll = lb;
	}
	
	public ILM(Coord sz) {
		super(sz);
		amb = new Color(0, 0, 0, 0);
	}
	
	protected Color setenv(GL gl) {
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		return(amb);
	}
	
	public void init(GL gl) {
		if(lbtex == null)
			lbtex = new TexI(ljusboll);
		gl.glClearColor(255, 255, 255, 255);
	}
	
	public void subrend(GOut g) {
		GL gl = g.gl;
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		if(ll != null) {
			for(Lumin lum : ll) {
				Coord sc = lum.gob.sc.add(lum.off).add(-lum.sz, -lum.sz);
				g.image(lbtex, sc, new Coord(lum.sz * 2, lum.sz * 2));
			}
		}
	}
	
	public void update(Collection<Lumin> objs) {
		ll = objs;
		update();
		ll = null;
	}
}
