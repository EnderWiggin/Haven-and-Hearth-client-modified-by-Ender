package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.color.ColorSpace;
import java.nio.ByteBuffer;
import javax.media.opengl.*;

public class TexI extends Tex {
	public static ComponentColorModel glcm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	protected byte[] pixels;
	public BufferedImage back;

	public TexI(BufferedImage img) {
		super(Utils.imgsz(img));
		back = img;
		pixels = convert(img, tdim);
	}

	public TexI(Coord sz) {
		super(sz);
		pixels = new byte[tdim.x * tdim.y * 4];
	}

	protected void fill(GOut g) {
		GL gl = g.gl;
		ByteBuffer data = ByteBuffer.wrap(pixels);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tdim.x, tdim.y, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data);
	}
	
	protected void update(byte[] n) {
		if(n.length != pixels.length)
			throw(new RuntimeException("Illegal new texbuf size (" + n.length + " != " + pixels.length + ")"));
		pixels = n;
		dispose();
	}
	
	public int getRGB(Coord c) {
		return(back.getRGB(c.x, c.y));
	}
	
	public static BufferedImage mkbuf(Coord sz) {
		WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, sz.x, sz.y, 4, null);
		BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
		return(tgt);
	}
	
	public static byte[] convert(BufferedImage img, Coord tsz) {
		WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, tsz.x, tsz.y, 4, null);
		BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
		Graphics g = tgt.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return(((DataBufferByte)buf.getDataBuffer()).getData());
	}
}
