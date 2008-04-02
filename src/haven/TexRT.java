package haven;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

public abstract class TexRT extends TexGL {
	private GLPbuffer pbuf;
	private boolean inited = false;
	
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
			pbuf = df.createGLPbuffer(caps, null, dim.x, dim.y, g.ctx);
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
						TexRT.this.init(gl);
					}

					public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
						GL gl = d.getGL();
						GLU glu = new GLU();
						gl.glMatrixMode(GL.GL_PROJECTION);
						gl.glLoadIdentity();
						glu.gluOrtho2D(0, w, h, 0);
					}
			
					public void displayChanged(GLAutoDrawable d, boolean cp1, boolean cp2) {}
				});
		}
		update();
	}
	
	public void update() {
		if(pbuf != null)
			pbuf.display();
	}
	
	private void subrend2(GL gl) {
		GOut g = new GOut(gl, pbuf.getContext(), dim);
		subrend(g);
		if(id < 0)
			throw(new RuntimeException("Negative tex id when updating pbuf texture"));
		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		if(!inited) {
			gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, 0, 0, dim.x, dim.y, 0);
			inited = true;
		} else {
			gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, dim.x, dim.y);
		}
	}
}
