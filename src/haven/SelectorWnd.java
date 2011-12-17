package haven;

import java.awt.event.KeyEvent;
import java.util.Set;

public class SelectorWnd extends Window {
    
    private Callback callback;
    
    public static abstract class Callback {
	public abstract void callback();
    }
    
    public SelectorWnd(Widget parent, String cap) {
	super(new Coord(400,300), Coord.z, parent, cap);
	justclose = true;
    }

    public void setData(final Set<String> list, final Set<String> selected, Callback callback){
	this.callback = callback;
	int y = -30;
	for(final String item : list){
	    (new CheckBox(new Coord(0, y += 30), this, item) {
		public void changed(boolean val) {
		    if(val){
			selected.add(item);
		    } else {
			selected.remove(item);
		    }
		}
	    }).a = selected.contains(item);
	}
	pack();
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	callback.callback();
	super.wdgmsg(sender, msg, args);
    }

    @Override
    public boolean type(char key, KeyEvent ev) {
	callback.callback();
	return super.type(key, ev);
    }
}
