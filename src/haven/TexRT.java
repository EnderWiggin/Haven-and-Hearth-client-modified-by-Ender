package haven;

import java.util.*;
import java.lang.ref.WeakReference;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

public abstract class TexRT extends TexGL {
    private static Map<GL, Collection<TexRT>> current = new WeakHashMap<GL, Collection<TexRT>>();
    private boolean inited = false, incurrent = false;
    public Profile prof = new Profile(300);
    private Profile.Frame curf;
	
    public TexRT(Coord sz) {
	super(sz);
    }
	
    public void dispose() {
	if(incurrent) {
	    synchronized(current) {
		current.remove(this);
	    }
	}
	incurrent = false;
	inited = false;
	super.dispose();
    }
	
    protected abstract void subrend(GOut g);
	
    protected void fill(GOut g) {
	if(!incurrent) {
	    GL gl = g.gl;
	    Collection<TexRT> tc;
	    synchronized(current) {
		tc = current.get(gl);
		if(tc == null) {
		    tc = new HashSet<TexRT>();
		    current.put(gl, tc);
		}
	    }
	    synchronized(tc) {
		tc.add(this);
	    }
	    incurrent = true;
	}
    }
	
    private void subrend2(GOut g) {
	if(id < 0)
	    return;
	GL gl = g.gl;
	curf = prof.new Frame();
	subrend(g);
	curf.tick("render");
	g.texsel(id);
	GOut.checkerr(gl);
	if(!inited) {
	    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tdim.x, tdim.y, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
	    GOut.checkerr(gl);
	    inited = true;
	}
	gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, dim.x, dim.y);
	GOut.checkerr(gl);
	curf.tick("copy");
	curf.fin();
    }
    
    public static void renderall(GOut g) {
	GL gl = g.gl;
	Collection<TexRT> tc;
	synchronized(current) {
	    tc = current.get(gl);
	}
	if(tc != null) {
	    synchronized(tc) {
		for(TexRT t : tc)
		    t.subrend2(g);
	    }
	}
    }
}
