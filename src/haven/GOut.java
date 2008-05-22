package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.media.opengl.*;

public class GOut {
	GL gl;
	Coord ul, sz;
	private Color color = Color.WHITE;
	final GLContext ctx;
	private Shared sh;
	
	private static class Shared {
		int curtex = -1;
	}
	
	private GOut(GOut o) {
		this.gl = o.gl;
		this.ul = o.ul;
		this.sz = o.sz;
		this.color = o.color;
		this.ctx = o.ctx;
		this.sh = o.sh;
	}

	public GOut(GL gl, GLContext ctx, Coord sz) {
		this.gl = gl;
		this.ul = Coord.z;
		this.sz = sz;
		this.ctx = ctx;
		this.sh = new Shared();
	}
    
	private void checkerr() {
		int err = gl.glGetError();
		if(err != 0)
			throw(new RuntimeException("GL Error: " + err));
	}
	
	private void glcolor() {
		gl.glColor4f((float)color.getRed() / 255.0f,
			     (float)color.getGreen() / 255.0f,
			     (float)color.getBlue() / 255.0f,
			     (float)color.getAlpha() / 255.0f);
	}

	public void image(BufferedImage img, Coord c) {
		if(img == null)
			return;
		Tex tex = new TexI(img);
		image(tex, c);
		tex.dispose();
	}
	
	public void image(Tex tex, Coord c) {
		if(tex == null)
			return;
		tex.crender(this, c.add(ul), ul, sz);
		checkerr();
	}
	
	public void image(Tex tex, Coord c, Coord sz) {
		if(tex == null)
			return;
		tex.crender(this, c.add(ul), ul, this.sz, sz);
		checkerr();
	}
	
	public void image(Tex tex, Coord c, Coord ul, Coord sz) {
		if(tex == null)
			return;
		tex.crender(this, c.add(this.ul), this.ul.add(ul), sz);
		checkerr();
	}
	
	private void vertex(Coord c) {
		gl.glVertex2i(c.x + ul.x, c.y + ul.y);
	}
	
	void texsel(int id) {
		if(id != sh.curtex) {
			HavenPanel.texmiss++;
			if(id == -1) {
				gl.glDisable(GL.GL_TEXTURE_2D);
			} else {
				gl.glEnable(GL.GL_TEXTURE_2D);
				gl.glBindTexture(GL.GL_TEXTURE_2D, id);
			}
			sh.curtex = id;
		} else {
			HavenPanel.texhit++;
		}
	}
	
	public void line(Coord c1, Coord c2, double w) {
		texsel(-1);
		gl.glLineWidth((float)w);
		gl.glBegin(GL.GL_LINES);
		glcolor();
		vertex(c1);
		vertex(c2);
		gl.glEnd();
		checkerr();
	}
    
	public void text(String text, Coord c) {
		atext(text, c, 0, 0);
	}
	
	public void atext(String text, Coord c, double ax, double ay) {
		Text t = Text.render(text);
		Tex T = t.tex();
		Coord sz = t.sz();
		image(T, c.add((int)((double)sz.x * -ax), (int)((double)sz.y * -ay)));
		T.dispose();
		checkerr();
	}
    
	public void frect(Coord ul, Coord sz) {
		glcolor();
		texsel(-1);
		gl.glBegin(GL.GL_QUADS);
		vertex(ul);
		vertex(ul.add(new Coord(sz.x, 0)));
		vertex(ul.add(sz));
		vertex(ul.add(new Coord(0, sz.y)));
		gl.glEnd();
		checkerr();
	}
	
	public void rect(Coord ul, Coord sz) {
		Coord ur, bl, br;
		ur = new Coord(ul.x + sz.x - 1, ul.y);
		bl = new Coord(ul.x, ul.y + sz.y - 1);
		br = new Coord(ur.x, bl.y);
		line(ul, ur, 1);
		line(ur, br, 1);
		line(br, bl, 1);
		line(bl, ul, 1);
	}
	
	public void chcolor(Color c) {
		this.color = c;
	}
    
	Color getcolor() {
		return(color);
	}
	
	public GOut reclip(Coord ul, Coord sz) {
		GOut g = new GOut(this);
		g.ul = this.ul.add(ul);
		g.sz = sz;
		return(g);
	}
}
