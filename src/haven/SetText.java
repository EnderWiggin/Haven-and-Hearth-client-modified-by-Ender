package haven;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class SetText extends Widget {
	TexIM texi;
	Font deffont = new Font("SansSerif", Font.PLAIN, 11);
	Color defcol = Color.BLACK;
	
	static class Text implements Cloneable {
		String text;
		int off;
		Coord cur = new Coord(0, 0);
		Coord sz;
		
		Text(String text, int off, Coord sz) {
			this.text = text;
			this.off = off;
			this.sz = sz;
		}
		
		boolean end() {
			return(off < text.length());
		}
		
		boolean match(String s) {
			return(text.substring(off, off + s.length()).equals(s));
		}
		
		public Text clone() {
			try {
				return((Text)super.clone());
			} catch(CloneNotSupportedException e) {
				throw(new RuntimeException(e));
			}
		}
	}
	
	static class Result {
		BufferedImage cnt;
		Coord rsz = new Coord();
		Text t;
		Graphics g;
		
		Result(Text t) {
			cnt = TexI.mkbuf(t.sz);
			g = cnt.getGraphics();
		}
		
		void merge(Result sub) {
			g.drawImage(sub.cnt, 0, 0, null);
		}
	}
	
	static class LOverflow extends Throwable {}
	static class POverflow extends Throwable {}
	
	Result renderel(Text t) throws LOverflow, POverflow {
		Result r = new Result(t.clone());
		while(true) {
			if(r.t.end()) {
				break;
			} else if(r.t.match("\n")) {
				break;
			}
		}
		return(r);
	}
	
	Result renderline(Text t) throws POverflow {
		Result r = new Result(t.clone());
		while(!r.t.end()) {
			try {
				Text tb = r.t.clone();
				Result el = renderel(tb);
				r.t = el.t;
				r.merge(el);
			} catch(LOverflow e) {
				break;
			}
		}
		return(r);
	}
	
	Result renderpar(Text t) throws POverflow {
		Result r = new Result(t.clone());
		
		while(!r.t.end()) {
			try {
				Result line = renderline(r.t);
				r.t = line.t;
				r.merge(line);
			} catch(POverflow e) {
				break;
			}
		}
		return(r);
	}
	
	void render(String text) {
		Result r = new Result(new Text(text, 0, sz));
		while(!r.t.end()) {
			try {
				Text tb = r.t.clone();
				Result par = renderpar(tb);
				r.t = r.t;
				r.merge(par);
			} catch(POverflow e) {
				break;
			}
		}
		texi.update();
	}
	
	public SetText(Coord c, Coord sz, Widget parent, String text) {
		super(c, sz, parent);
		texi = new TexIM(sz);
		render(text);
	}
}
