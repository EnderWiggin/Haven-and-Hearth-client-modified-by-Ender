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

public class ILM extends TexI {
	BufferedImage bufw;
	WritableRaster buf;
	public final static BufferedImage ljusboll;
	public static ComponentColorModel acm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] {8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
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
		clear();
		Collection<Lumin> el = java.util.Collections.emptyList();
		redraw(el);
		amb = new Color(0, 0, 0, 0);
	}
	
	protected Color setenv(GL gl) {
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		return(amb);
	}
	
	protected void fill(GOut g) {
		GL gl = g.gl;
		ByteBuffer data = ByteBuffer.wrap(pixels);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_ALPHA, tdim.x, tdim.y, 0, GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, data);
	}
	
	public void redraw(Collection<Lumin> objs) {
		Graphics2D g = bufw.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, dim.x, dim.y);
		for(Lumin lum : objs) {
			Coord sc = lum.gob.sc.add(lum.off).add(-lum.sz, -lum.sz);
			g.drawImage(ljusboll, sc.x, sc.y, lum.sz * 2, lum.sz * 2, null);
		}
		g.dispose();
		pixels = ((DataBufferByte)buf.getDataBuffer()).getData();
		dispose();
	}
	
	public void clear() {
		buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, tdim.x, tdim.y, 1, null);
		bufw = new BufferedImage(acm, buf, false, null);
	}
}
