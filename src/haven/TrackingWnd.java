package haven;

import java.util.ArrayList;
import java.util.List;

public class TrackingWnd extends Window {

    public static List<TrackingWnd> instances = new ArrayList<TrackingWnd>();
    public int broadcastID, a1, a2, direction, delta, ownerid;
    public Coord pos = null;
    
    public TrackingWnd(Gob owner, int bid, int direction, int delta, int a1, int a2, Message sdt){
		super(new Coord(100,100), Coord.z, UI.instance.root, "Direction");
		this.a1 = a1;
		this.a2 = a2;
		this.direction = direction;
		this.delta = delta;
		this.broadcastID = bid;
		this.ownerid = owner.id;
		justclose = true;
		Label l = new Label(Coord.z, this, "Direction: "+direction+"°, delta: "+delta+"°");
		if(sdt != null) getID(sdt);
		new Button(new Coord(l.sz.x+10,0), 60, this, "Broadcast") { public void click() {
			broadcast();
		} };
		pack();
		Gob pl;
		if(/*((pl = ui.sess.glob.oc.getgob(gobid)) != null) &&*/ (owner.sc != null) ){
			pos = owner.getc();
		}
		instances.add(this);
    }
	
	void getID(Message sdt){ // the only way to get the god damn ID :/
		broadcastID = sdt.id;
	}
    
	void broadcast(){
		ui.chat.getawnd().wdgmsg("msg",String.format("@$[%d,%d,%d,%d,%d,%d]", direction, delta, a1, a2, ownerid, broadcastID));
	}
	
    public void destroy(){
	instances.remove(instances.indexOf(this));
	super.destroy();
    }
}
