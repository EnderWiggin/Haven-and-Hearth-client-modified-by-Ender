package haven;

import java.awt.Color;
import java.nio.*;
import java.util.*;
import javax.media.opengl.*;

public abstract class TexGL extends Tex {
    protected int id = -1;
    protected GL mygl = null;
    private Object idmon = new Object();
    protected Coord tdim;
    protected static Map<GL, Collection<Integer>> disposed = new HashMap<GL, Collection<Integer>>();
    public static boolean disableall = false;
    
    public TexGL(Coord sz) {
	super(sz);
	tdim = new Coord(nextp2(sz.x), nextp2(sz.y));
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
	mygl = gl;
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
	
    Color blend(GOut g, Color amb) {
	Color c = g.getcolor();
	Color n = new Color((c.getRed() * amb.getRed()) / 255,
			    (c.getGreen() * amb.getGreen()) / 255,
			    (c.getBlue() * amb.getBlue()) / 255,
			    (c.getAlpha() * amb.getAlpha()) / 255);
	return(n);
    }
	
    public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
	GL gl = g.gl;
	synchronized(idmon) {
	    if(mygl != gl) {
		dispose(mygl, id);
		id = -1;
	    }
	    if(id < 0)
		create(g);
	    g.texsel(id);
	}
	Color amb = blend(g, setenv(gl));
	checkerr(gl);
	if(!disableall) {
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
    }
	
    private static void dispose(GL gl, int id) {
	Collection<Integer> dc;
	synchronized(disposed) {
	    dc = disposed.get(gl);
	    if(dc == null) {
		dc = new LinkedList<Integer>();
		disposed.put(gl, dc);
	    }
	}
	synchronized(dc) {
	    dc.add(id);
	}
    }
	
    public void dispose() {
	synchronized(idmon) {
	    if(id == -1)
		return;
	    dispose(mygl, id);
	    id = -1;
	}
    }
	
    protected void finalize() {
	dispose();
    }
	
    public static void disposeall(GL gl) {
	Collection<Integer> dc;
	synchronized(disposed) {
	    dc = disposed.get(gl);
	    if(dc == null)
		return;
	}
	synchronized(dc) {
	    if(dc.isEmpty())
		return;
	    int[] da = new int[dc.size()];
	    int i = 0;
	    for(int id : dc)
		da[i++] = id;
	    dc.clear();
	    gl.glDeleteTextures(da.length, da, 0);
	}
    }
}
