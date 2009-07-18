package haven;

public class Logout extends Window {
    Button y, n;
	
    public Logout(Coord c, Widget parent) {
	super(c, new Coord(125, 50), parent, "Haven & Hearth");
	new Label(Coord.z, this, "Do you want to log out?");
	y = new Button(new Coord(0, 30), 50, this, "Yes");
	n = new Button(new Coord(75, 30), 50, this, "No");
	canactivate = true;
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == y) {
	    ui.sess.close();
	} else if(sender == n) {
	    ui.destroy(this);
	} else if(sender == this) {
	    if(msg == "close")
		ui.destroy(this);
	    if(msg == "activate")
		ui.sess.close();
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
}
