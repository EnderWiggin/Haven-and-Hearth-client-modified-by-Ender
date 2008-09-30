package haven.test;

import haven.*;

public class Robot {
    public TestClient c;
    
    public Robot(TestClient cl) {
	this.c = cl;
	cl.addbot(this);
    }
    
    public void destroy() {
	c.rembot(this);
    }
    
    public void newwdg(int id, Widget w, Object... args) {
    }
    
    public void dstwdg(int id, Widget w) {
    }
    
    public void uimsg(int id, Widget w, String msg, Object... args) {
    }
}
