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

import javax.media.opengl.GL;

public class ILM extends Tex {
	BufferedImage bufw;
	WritableRaster buf;
	public static ComponentColorModel acm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] {8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
	Color amb;
	
	public ILM(Coord sz) {
		super(sz);
		clear();
		redraw();
		amb = new Color(0, 0, 0, 0);
	}
	
	protected Color setenv(GL gl) {
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		return(amb);
	}
	
	protected void fill(GL gl) {
		ByteBuffer data = ByteBuffer.allocate(pixels.length);
		data.put(pixels);
		data.rewind();
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_ALPHA, tdim.x, tdim.y, 0, GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, data);
	}
	
	public void redraw() {
		Graphics2D g = bufw.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, dim.x, dim.y);
		for(int i = 0; i < 10; i++) {
			int c = (int)(Math.random() * 128);
			g.setColor(new Color(c, c, c));
			g.fillRect((int)(Math.random() * 800), (int)(Math.random() * 600), (int)(Math.random() * 200) + 50, (int)(Math.random() * 200) + 50);
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
