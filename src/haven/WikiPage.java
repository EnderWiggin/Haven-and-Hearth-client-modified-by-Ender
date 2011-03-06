package haven;

import haven.RichText.Foundry;

import java.awt.Color;
import java.awt.font.TextAttribute;

import wikilib.Request;
import wikilib.RequestCallback;
import wikilib.WikiLib;

public class WikiPage extends HWindow {
    private static final Foundry fnd = new Foundry(TextAttribute.FOREGROUND, Color.BLACK,TextAttribute.SIZE,12);
    private static final Color busycolor = new Color(255,255,255,128);
    private RichTextBox content;
    private WikiLib reader;
    private RequestCallback callback;
    private Boolean busy;
    
    public WikiPage(Widget parent, String request, boolean closable) {
	super(parent, request, closable);
	content = new RichTextBox(Coord.z, sz, this, "", fnd);
	content.bg = new Color(255, 255, 255, 128);
	content.registerclicks = true;
	
	final HWindow wnd = this;
	busy = false;
	callback = new RequestCallback() {
	    public void run(Request req) {
		synchronized (content) {
		    content.settext(req.result);
		    if(req.title != null) {
			title = req.title;
			ui.wiki.updurgency(wnd, 0);
		    }
		    synchronized(busy) {
			busy = false;
		    }
		}
	    }
	};
	
	reader = new WikiLib();
	open(request);
	if(cbtn != null) {
	    cbtn.raise();
	}
    }
    
    public void setsz(Coord s) {
	super.setsz(s);
	content.setsz(sz);
    }
    
    public void draw(GOut g) {
	super.draw(g);
	if(busy) {
	    g.chcolor(busycolor);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(busy){return;}
	if(sender == content) {
	    String request = (String)args[0];
	    if((Integer)args[1] == 1) {
		open(request);
	    } else {
		new WikiPage(ui.wiki, request, true);
	    }
	} else if(sender == cbtn) {
	    ui.destroy(this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    private void open(String request) {
	Request req = new Request(request, callback);
	if(request.indexOf("/wiki/")>=0) {
	    request = request.replaceAll("/wiki/", "");
	    req.initPage(request);
	}
	synchronized (busy) {
	    busy = true;
	}
	reader.search(req);
    }
    
    public void destroy() {
	super.destroy();
	callback = null;
	reader = null;;
    }
}
