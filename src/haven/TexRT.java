package haven;

import javax.media.opengl.*;

public abstract class TexRT extends Tex {
	private GLPbuffer pbuf;
	
	public TexRT(Coord sz) {
		super(sz);
		pbuf = null;
	}
	
	public void dispose() {
		if(pbuf != null)
			pbuf.destroy();
		super.dispose();
	}
	
	protected abstract void subrend(GL gl);
	
	protected void fill(GOut g) {
		if(pbuf == null) {
			GLDrawableFactory df = GLDrawableFactory.getFactory();
			if(!df.canCreateGLPbuffer())
				throw(new RuntimeException("No pbuffer support"));
			GLCapabilities caps = new GLCapabilities();
			caps.setDoubleBuffered(false);
			pbuf = df.createGLPbuffer(caps, null, dim.x, dim.y, g.ctx);
		}
	}
}
