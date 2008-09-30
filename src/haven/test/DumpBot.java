package haven.test;

import haven.*;

public class DumpBot extends Robot {
    public DumpBot(TestClient c) {
	super(c);
    }
    
    public void newwdg(int id, Widget w, Object... args) {
	System.out.println(c + ": new widget: " + w + " (" + id + ")");
    }
    
    public void dstwdg(int id, Widget w) {
	System.out.println(c + ": destroyed: " + w + " (" + id + ")");
    }
    
    public void uimsg(int id, Widget w, String msg, Object... args) {
	System.out.println(c + ": uimsg: " + w + " (" + id + "): " + msg);
    }
}
