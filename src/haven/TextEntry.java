package haven;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.KeyEvent;

public class TextEntry extends SSWidget {
	static int barda = 1;
	String text;
	int pos;
	boolean prompt = false;
	
	static {
		Widget.addtype("text", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				return(new TextEntry(c, (Coord)args[0], parent, (String)args[1]));
			}
		});
	}
	
	public void uimsg(String name, Object... args) {
		if(name == "settext") {
			text = (String)args[0];
			if(pos > text.length())
				pos = text.length();
			render();
		} else if(name == "get") {
			ui.wdgmsg(this, "text", text);
		} else {
			super.uimsg(name, args);
		}
	}
	
	private void render() {
		Graphics g = surf.getGraphics();
		Utils.AA(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, sz.x, sz.y);
		g.setColor(Color.BLACK);
		FontMetrics m = g.getFontMetrics();
		g.drawString(text, 0, m.getAscent());
		if(hasfocus && prompt) {
			Rectangle2D tm = m.getStringBounds(text.substring(0, pos), g);
			g.drawLine((int)tm.getWidth(), 1, (int)tm.getWidth(), m.getHeight() - 1);
		}
	}
	
	public void gotfocus() {
		render();
	}
	
	public void lostfocus() {
		render();
	}
	
	public TextEntry(Coord c, Coord sz, Widget parent, String deftext) {
		super(c, sz, parent);
		canfocus = true;
		text = deftext;
		pos = text.length();
		render();
	}
	
	public boolean type(char c, KeyEvent ev) {
		if(c == 8) {
			if(pos > 0) {
				if(pos < text.length())
					text = text.substring(0, pos - 1) + text.substring(pos);
				else
					text = text.substring(0, pos - 1);
				pos--;
			}
		} else if(c == 10) {
			if(!canactivate)
				return(false);
			ui.wdgmsg(this, "activate", text);
		} else if(c == 127) {
			if(pos < text.length())
				text = text.substring(0, pos) + text.substring(pos + 1);
		} else if(c == '\t') {
			return(false);
		} else {
			text = text.substring(0, pos) + c + text.substring(pos);
			pos++;
		}
		render();
		return(true);
	}
	
	public boolean keydown(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			if(pos > 0)
				pos--;
		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			if(pos < text.length())
				pos++;
		} else if(e.getKeyCode() == KeyEvent.VK_HOME) {
			pos = 0;
		} else if(e.getKeyCode() == KeyEvent.VK_END) {
			pos = text.length();
		}
		render();
		return(true);
	}
	
	public boolean mousedown(Coord c, int button) {
		parent.setfocus(this);
		render();
		return(true);
	}
	
	public void draw(Graphics g) {
		boolean prompt = System.currentTimeMillis() % 1000 > 500;
		if(prompt != this.prompt) {
			this.prompt = prompt;
			render();
		}
		super.draw(g);
	}
}
