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

public class Tex {
    private int id = -1;
    private byte[] pixels;
    private Coord dim, tdim;
    private static ComponentColorModel glcm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    
    public Tex(BufferedImage img) {
	dim = Utils.imgsz(img);
	tdim = new Coord(nextp2(dim.x), nextp2(dim.y));
	pixels = convert(img, tdim);
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
	gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	ByteBuffer data = ByteBuffer.allocate(pixels.length);
	data.put(pixels);
	data.rewind();
	gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tdim.x, tdim.y, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data);
    }

    public void render(GL gl, Coord c) {
	if(id < 0)
	    create(gl);
	gl.glEnable(gl.GL_TEXTURE_2D);
	gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
	gl.glBindTexture(GL.GL_TEXTURE_2D, id);
	gl.glBegin(GL.GL_QUADS);
	float w = ((float)dim.x) / ((float)tdim.x);
	float h = ((float)dim.y) / ((float)tdim.y);
	gl.glTexCoord2f(0, 0); gl.glVertex3i(c.x, c.y, 0);
	gl.glTexCoord2f(w, 0); gl.glVertex3i(c.x + dim.x, c.y, 0);
	gl.glTexCoord2f(w, h); gl.glVertex3i(c.x + dim.x, c.y + dim.y, 0);
	gl.glTexCoord2f(0, h); gl.glVertex3i(c.x, c.y + dim.y, 0);
	gl.glEnd();
    }
}
