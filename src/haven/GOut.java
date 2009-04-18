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
	GOut root;
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
	this.sh.root = this;
    }
    
    public static class GLException extends RuntimeException {
	public int code;
	public String str;
	private static javax.media.opengl.glu.GLU glu = new javax.media.opengl.glu.GLU();
	
	public GLException(int code) {
	    super("GL Error: " + code + " (" + glu.gluErrorString(code) + ")");
	    this.code = code;
	    this.str = glu.gluErrorString(code);
	}
    }

    public static void checkerr(GL gl) {
	int err = gl.glGetError();
	if(err != 0)
	    throw(new GLException(err));
    }

    private void checkerr() {
	checkerr(gl);
    }
	
    private void glcolor() {
	gl.glColor4f((float)color.getRed() / 255.0f,
		     (float)color.getGreen() / 255.0f,
		     (float)color.getBlue() / 255.0f,
		     (float)color.getAlpha() / 255.0f);
    }

    public GOut root() {
	return(sh.root);
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
	
    public void aimage(Tex tex, Coord c, double ax, double ay) {
	Coord sz = tex.sz();
	image(tex, c.add((int)((double)sz.x * -ax), (int)((double)sz.y * -ay)));
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
	
    public void frect(Coord c1, Coord c2, Coord c3, Coord c4) {
	glcolor();
	texsel(-1);
	gl.glBegin(GL.GL_QUADS);
	vertex(c1);
	vertex(c2);
	vertex(c3);
	vertex(c4);
	gl.glEnd();
	checkerr();
    }
	
    public void fellipse(Coord c, Coord r, int a1, int a2) {
	glcolor();
	texsel(-1);
	gl.glBegin(GL.GL_TRIANGLE_FAN);
	vertex(c);
	for(int i = a1; i <= a2; i += 5) {
	    double a = (i * Math.PI * 2) / 360.0;
	    vertex(c.add((int)(Math.cos(a) * r.x), -(int)(Math.sin(a) * r.y)));
	}
	gl.glEnd();
	checkerr();
    }
	
    public void fellipse(Coord c, Coord r) {
	fellipse(c, r, 0, 360);
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
    
    public void chcolor(int r, int g, int b, int a) {
	this.color = new Color(r, g, b, a);
    }
	
    public void chcolor() {
	this.color = Color.WHITE;
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
