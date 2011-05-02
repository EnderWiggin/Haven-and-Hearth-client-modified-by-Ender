package haven;

import ender.timer.Callback;
import ender.timer.Timer;

public class TimerWdg extends Widget {

    private Timer timer;
    public Label time, name;
    
    public TimerWdg(Coord c, Widget parent, Timer timer) {
	super(c, new Coord(200, 50), parent);

	this.timer = timer;
	timer.callback =  new Callback() {
	    
	    @Override
	    public void update(Timer timer) {
		synchronized (time) {
		    time.settext(timer.toString());
		}
		
	    }
	    
	    @Override
	    public void finish(Timer timer) {
		// TODO Auto-generated method stub
		
	    }
	};
	name = new Label(Coord.z, this, timer.getName());
	time = new Label(new Coord(0, 25), this, timer.toString());
    }

    @Override
    public void destroy() {
	super.destroy();
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);
    }    
    

}
