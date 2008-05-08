package haven;

import java.awt.Color;
import java.nio.*;
import java.util.*;
import javax.media.opengl.*;

public abstract class TexGL extends Tex {
	protected int id = -1;
	protected Coord tdim;
	protected static Collection<Integer> disposed = new LinkedList<Integer>();
    
	public TexGL(Coord sz) {
		super(sz);
		tdim = new Coord(nextp2(sz.x), nextp2(sz.y));
	}
	
	public static int nextp2(int in) {
		int ret;
	
		for(ret = 1; ret < in; ret <<= 1);
		return(ret);
	}

	private void checkerr(GL gl) {
		int err = gl.glGetError();
		if(err != 0)
			throw(new RuntimeException("GL Error: " + err));
	}
	
	protected abstract void fill(GOut gl);

	private void create(GOut g) {
		GL gl = g.gl;
		int[] buf = new int[1];
		gl.glGenTextures(1, buf, 0);
		id = buf[0];
		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		fill(g);
		checkerr(gl);
	}
	
	protected Color setenv(GL gl) {
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		return(Color.WHITE);
	}
	
	public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
		GL gl = g.gl;
		if(id < 0)
			create(g);
		g.texsel(id);
		Color amb = setenv(gl);
		checkerr(gl);
		gl.glBegin(GL.GL_QUADS);
		float l = ((float)ul.x) / ((float)tdim.x);
		float t = ((float)ul.y) / ((float)tdim.y);
		float r = ((float)br.x) / ((float)tdim.x);
		float b = ((float)br.y) / ((float)tdim.y);
		gl.glColor4f((float)amb.getRed() / 255.0f,
			     (float)amb.getGreen() / 255.0f,
			     (float)amb.getBlue() / 255.0f,
			     (float)amb.getAlpha() / 255.0f);
		gl.glTexCoord2f(l, t); gl.glVertex3i(c.x, c.y, 0);
		gl.glTexCoord2f(r, t); gl.glVertex3i(c.x + sz.x, c.y, 0);
		gl.glTexCoord2f(r, b); gl.glVertex3i(c.x + sz.x, c.y + sz.y, 0);
		gl.glTexCoord2f(l, b); gl.glVertex3i(c.x, c.y + sz.y, 0);
		gl.glEnd();
		checkerr(gl);
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
		dispose();
	}
	
	public static void disposeall(GL gl) {
		synchronized(disposed) {
			if(disposed.isEmpty())
				return;
			int[] da = new int[disposed.size()];
			int i = 0;
			for(int id : disposed)
				da[i++] = id;
			disposed.clear();
			gl.glDeleteTextures(da.length, da, 0);
		}
	}
}
