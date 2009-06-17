package haven;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.KeyEvent;

public class TextEntry extends SSWidget {
    String text;
    int pos, limit = 0;
    boolean prompt = false, pw = false;
    int cw = 0;
	
    static {
	Widget.addtype("text", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new TextEntry(c, (Coord)args[0], parent, (String)args[1]));
		}
	    });
    }
	
    public void settext(String text) {
	this.text = text;
	if(pos > text.length())
	    pos = text.length();
	render();
    }
	
    public void uimsg(String name, Object... args) {
	if(name == "settext") {
	    settext((String)args[0]);
	} else if(name == "get") {
	    wdgmsg("text", text);
	} else if(name == "limit") {
	    limit = (Integer)args[0];
	} else if(name == "pw") {
	    pw = ((Integer)args[0]) == 1;
	    render();
	} else {
	    super.uimsg(name, args);
	}
    }
	
    private void render() {
	String dtext;
	if(pw) {
	    dtext = "";
	    for(int i = 0; i < text.length(); i++)
		dtext += "*";
	} else {
	    dtext = text;
	}
	synchronized(ui) {
	    Graphics g = graphics();
	    g.setColor(Color.WHITE);
	    g.fillRect(0, 0, sz.x, sz.y);
	    g.setColor(Color.BLACK);
	    FontMetrics m = g.getFontMetrics();
	    g.drawString(dtext, 0, m.getAscent());
	    if(hasfocus && prompt) {
		Rectangle2D tm = m.getStringBounds(dtext.substring(0, pos), g);
		g.drawLine((int)tm.getWidth(), 1, (int)tm.getWidth(), m.getHeight() - 1);
	    }
	    Rectangle2D tm = m.getStringBounds(dtext, g);
	    cw = (int)tm.getWidth();
	    update();
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
	text = deftext;
	pos = text.length();
	render();
	setcanfocus(true);
    }
	
    public boolean type(char c, KeyEvent ev) {
	try {
	    if(c == 8) {
		if(pos > 0) {
		    if(pos < text.length())
			text = text.substring(0, pos - 1) + text.substring(pos);
		    else
			text = text.substring(0, pos - 1);
		    pos--;
		}
		return(true);
	    } else if(c == 10) {
		if(!canactivate)
		    return(false);
		wdgmsg("activate", text);
		return(true);
	    } else if(c == 127) {
		if(pos < text.length())
		    text = text.substring(0, pos) + text.substring(pos + 1);
		return(true);
	    } else if(c >= 32) {
		String nt = text.substring(0, pos) + c + text.substring(pos);
		if((limit == 0) || ((limit > 0) && (nt.length() <= limit)) || ((limit == -1) && (cw < sz.x))) {
		    text = nt;
		    pos++;
		}
		return(true);
	    }
	} finally {
	    render();
	}
	return(false);
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
	
    public void draw(GOut g) {
	boolean prompt = System.currentTimeMillis() % 1000 > 500;
	if(prompt != this.prompt) {
	    this.prompt = prompt;
	    render();
	}
	super.draw(g);
    }
}
