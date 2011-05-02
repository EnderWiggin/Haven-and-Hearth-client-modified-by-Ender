package haven;

import ender.timer.Timer;

public class TimerPanel extends Window {
    public TimerPanel(Widget parent){
	super(Coord.z, Coord.z, parent, "Timers");
	Glob g = ui.sess.glob;
	Timer timer = new Timer(g.time, g.local, 120, "test timer");
	g.timers.add(timer);
	new TimerWdg(Coord.z, this, timer);
	pack();
	justclose = true;
    }
}
