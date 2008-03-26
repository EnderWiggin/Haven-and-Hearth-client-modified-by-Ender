package haven;

import java.nio.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.color.ColorSpace;
import javax.media.opengl.*;
import java.util.*;

public class Tex {
	private int id = -1;
	private byte[] pixels;
	protected Coord dim, tdim;
	public static ComponentColorModel glcm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
	protected static Collection<Integer> disposed = new LinkedList<Integer>();
    
	public Tex(BufferedImage img) {
		dim = Utils.imgsz(img);
		tdim = new Coord(nextp2(dim.x), nextp2(dim.y));
		pixels = convert(img, tdim);
		/*
		  for(int i = 0; i < pixels.length; i++) {
		  System.out.print(String.format("%02x", pixels[i]));
		  if(i % 32 == 31)
		  System.out.println();
		  else if(i % 4 == 3)
		  System.out.print(" ");
		  }
		*/
	}
    
	protected Tex(Coord sz) {
		dim = sz;
		tdim = new Coord(Tex.nextp2(sz.x), Tex.nextp2(sz.y));
		pixels = new byte[tdim.x * tdim.y * 4];
	}
	
	protected void update(byte[] n) {
		if(n.length != pixels.length)
			throw(new RuntimeException("Illegal new texbuf size"));
		pixels = n;
		dispose();
	}
	
	public Coord sz() {
		return(dim);
	}
	
	public static BufferedImage mkbuf(Coord sz) {
		WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, sz.x, sz.y, 4, null);
		BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
		return(tgt);
	}
	
	public static int nextp2(int in) {
		int ret;
	
		for(ret = 1; in != 0; in >>= 1)
			ret <<= 1;
		return(ret);
	}

	public static byte[] convert(BufferedImage img, Coord tsz) {
		WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, tsz.x, tsz.y, 4, null);
		BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
		Graphics g = tgt.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return(((DataBufferByte)buf.getDataBuffer()).getData());
	}
    
	private void create(GL gl) {
		int[] buf = new int[1];
		gl.glGenTextures(1, buf, 0);
		id = buf[0];
		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		/*
		  gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		*/
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		ByteBuffer data = ByteBuffer.allocate(pixels.length);
		data.put(pixels);
		data.rewind();
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tdim.x, tdim.y, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data);
	}

	public void render(GL gl, Coord c, Coord ul, Coord sz) {
		if(id < 0)
			create(gl);
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		gl.glBegin(GL.GL_QUADS);
		float l = ((float)ul.x) / ((float)tdim.x);
		float t = ((float)ul.y) / ((float)tdim.y);
		float r = ((float)sz.x) / ((float)tdim.x) + l;
		float b = ((float)sz.y) / ((float)tdim.y) + t;
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glTexCoord2f(l, t); gl.glVertex3i(c.x, c.y, 0);
		gl.glTexCoord2f(r, t); gl.glVertex3i(c.x + sz.x, c.y, 0);
		gl.glTexCoord2f(t, b); gl.glVertex3i(c.x + sz.x, c.y + sz.y, 0);
		gl.glTexCoord2f(l, b); gl.glVertex3i(c.x, c.y + sz.y, 0);
		gl.glEnd();
	}

	public void render(GL gl, Coord c) {
		render(gl, c, Coord.z, dim);
	}
    
	public void crender(GL gl, Coord c, Coord ul, Coord sz) {
		Coord t = new Coord(c);
		Coord uld = new Coord(0, 0);
		Coord szd = new Coord(dim);
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
		if(c.x + dim.x > ul.x + sz.x)
			szd.x -= c.x + dim.x - ul.x - sz.x;
		if(c.y + dim.y > ul.y + sz.y)
			szd.y -= c.y + dim.y - ul.y - sz.y;
		render(gl, t, uld, szd);
	}
	
	public void dispose() {
		if(id == -1)
			return;
		synchronized(disposed) {
			disposed.add(id);
		}
		id = -1;
	}
	
	protected void finalize() {
		if(id != -1) {
			synchronized(disposed) {
				disposed.add(id);
			}
			id = -1;
		}
	}
	
	public static void disposeall(GL gl) {
		synchronized(disposed) {
			if(disposed.isEmpty())
				return;
			int[] da = new int[disposed.size()];
			int i = 0;
			for(int id : disposed)
				da[i++] = id;
			gl.glDeleteTextures(da.length, da, 0);
		}
	}
}
