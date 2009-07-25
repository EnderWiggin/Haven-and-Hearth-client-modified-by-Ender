package haven;

import java.util.*;
import java.awt.Font;

public class Makewindow extends HWindow {
    Widget btn;
    List<Widget> inputs;
    List<Widget> outputs;
    static Coord boff = new Coord(7, 9);
    public static final Text.Foundry nmf = new Text.Foundry(new Font("Serif", Font.PLAIN, 20));

    static {
	Widget.addtype("make", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Makewindow(parent, (String)args[0]));
		}
	    });
    }
	
    public Makewindow(Widget parent, String rcpnm) {
	super(parent, "Crafting", true);
	Label nm = new Label(new Coord(10, 10), this, rcpnm, nmf);
	nm.c = new Coord(sz.x - 10 - nm.sz.x, 10);
	new Label(new Coord(10, 18), this, "Input:");
	new Label(new Coord(10, 73), this, "Result:");
	btn = new Button(new Coord(370, 71), 50, this, "Craft");
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "pop") {
	    final int xoff = 50;
	    if(inputs != null) {
		for(Widget w : inputs)
		    w.unlink();
		for(Widget w : outputs)
		    w.unlink();
	    }
	    inputs = new LinkedList<Widget>();
	    outputs = new LinkedList<Widget>();
	    int i;
	    Coord c = new Coord(xoff, 10);
	    for(i = 0; (Integer)args[i] >= 0; i += 2) {
		Widget box = new Inventory(c, new Coord(1, 1), this);
		inputs.add(box);
		c = c.add(new Coord(31, 0));
		new Item(Coord.z, (Integer)args[i], box, null, (Integer)args[i + 1]);
	    }
	    c = new Coord(xoff, 65);
	    for(i++; (i < args.length) && ((Integer)args[i] >= 0); i += 2) {
		Widget box = new Inventory(c, new Coord(1, 1), this);
		outputs.add(box);
		c = c.add(new Coord(31, 0));
		new Item(Coord.z, (Integer)args[i], box, null, (Integer)args[i + 1]);
	    }
	}
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == btn) {
	    if(msg == "activate") {
		wdgmsg("make");
	    }
	    return;
	}
	if(sender instanceof Item)
	    return;
	if(sender instanceof Inventory)
	    return;
	super.wdgmsg(sender, msg, args);
    }
    
    public boolean globtype(char ch, java.awt.event.KeyEvent ev) {
	if(ch == '\n') {
	    wdgmsg("make");
	    return(true);
	}
	return(super.globtype(ch, ev));
    }
}
