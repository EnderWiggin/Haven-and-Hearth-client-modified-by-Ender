package haven.test;

import haven.*;

public class CharSelector extends Robot {
    Runnable cb;
    String chr;
    Listbox chrlist;
    Button selbtn;
    
    public CharSelector(TestClient c, String chr, Runnable cb) {
	super(c);
	this.chr = chr;
	this.cb = cb;
    }
    
    public void check() {
	if(chrlist == null)
	    return;
	if(selbtn == null)
	    return;
	
	if(chr == null) {
	    chr = chrlist.opts.get(0).name;
	} else {
	    String nm = null;
	    for(Listbox.Option opt : chrlist.opts) {
		if(opt.disp.equals(chr)) {
		    nm = opt.name;
		    break;
		}
	    }
	    if(nm == null)
		throw(new RobotException(this, "requested character not found: " + chr));
	    chr = nm;
	}
	chrlist.wdgmsg("chose", chr);
	selbtn.wdgmsg("activate");
    }
    
    public void newwdg(int id, Widget w, Object... args) {
	if(w instanceof Listbox) {
	    chrlist = (Listbox)w;
	} else if(w instanceof Button) {
	    if(((Button)w).text.text.equals("I choose you!"))
		selbtn = (Button)w;
	}
	check();
    }
    
    public void dstwdg(int id, Widget w) {
	if(w == chrlist) {
	    destroy();
	    succeed();
	}
    }
    
    public void uimsg(int id, Widget w, String msg, Object... args) {
    }
    
    public void succeed() {
	if(cb != null)
	    cb.run();
    }
}
