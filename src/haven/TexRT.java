package haven;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

public abstract class TexRT extends TexGL {
    private GLPbuffer pbuf;
    private boolean inited = false;
    public Profile prof = new Profile(300);
    private Profile.Frame curf;
	
    public TexRT(Coord sz) {
	super(sz);
	pbuf = null;
    }
	
    public void dispose() {
	if(pbuf != null)
	    pbuf.destroy();
	inited = false;
	super.dispose();
    }
	
    protected abstract void init(GL gl);
    protected abstract void subrend(GOut g);
	
    protected void fill(GOut g) {
	if(pbuf == null) {
	    GLDrawableFactory df = GLDrawableFactory.getFactory();
	    if(!df.canCreateGLPbuffer())
		throw(new RuntimeException("No pbuffer support"));
	    GLCapabilities caps = new GLCapabilities();
	    caps.setDoubleBuffered(false);
	    caps.setAlphaBits(8);
	    caps.setRedBits(8);
	    caps.setGreenBits(8);
	    caps.setBlueBits(8);
	    pbuf = df.createGLPbuffer(caps, null, tdim.x, tdim.y, g.ctx);
	    pbuf.addGLEventListener(new GLEventListener() {
		    public void display(GLAutoDrawable d) {
			GL gl = d.getGL();
			subrend2(gl);
		    }
			
		    public void init(GLAutoDrawable d) {
			GL gl = d.getGL();
			gl.glClearColor(0, 0, 0, 1);
			gl.glColor3f(1, 1, 1);
			gl.glPointSize(4);
			gl.glEnable(GL.GL_BLEND);
			//gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			GOut.checkerr(gl);
			reshape(d, 0, 0, tdim.x, tdim.y);
			GOut.checkerr(gl);
			TexRT.this.init(gl);
			GOut.checkerr(gl);
		    }

		    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
			GL gl = d.getGL();
			GLU glu = new GLU();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluOrtho2D(0, w, 0, h);
		    }
			
		    public void displayChanged(GLAutoDrawable d, boolean cp1, boolean cp2) {}
		});
	}
	update();
    }
	
    public void update() {
	curf = prof.new Frame();
	if(pbuf != null)
	    pbuf.display();
	curf.tick("gl-out");
	curf.fin();
    }
	
    private void subrend2(GL gl) {
	curf.tick("gl-in");
	GOut g = new GOut(gl, pbuf.getContext(), dim);
	subrend(g);
	curf.tick("render");
	if(id < 0)
	    throw(new RuntimeException("Negative tex id when updating pbuf texture"));
	gl.glBindTexture(GL.GL_TEXTURE_2D, id);
	if(!inited) {
	    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tdim.x, tdim.y, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
	    inited = true;
	}
	gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, dim.x, dim.y);
	GOut.checkerr(gl);
	curf.tick("copy");
    }
}
