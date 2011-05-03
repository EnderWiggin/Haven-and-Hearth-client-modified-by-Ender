package haven;

import ender.timer.Timer;
import ender.timer.TimerController;

public class TimerPanel extends Window {
    
    private static TimerPanel instance;
    private Button btnnew;
    
    public static void toggle(){
	if(instance == null){
	    instance = new TimerPanel(UI.instance.root);
	} else {
	    UI.instance.destroy(instance);
	}
    }
    
    public TimerPanel(Widget parent){
	super(new Coord(250, 100), Coord.z, parent, "Timers");
	justclose = true;
	btnnew = new Button(Coord.z, 100, this, "Add timer");
	
	synchronized (TimerController.getInstance().timers){
	    for(Timer timer : TimerController.getInstance().timers){
		new TimerWdg(Coord.z, this, timer);
	    }
	}
	pack();
    }

    @Override
    public void pack() {
	int n, i=0, h = 0;
	synchronized (TimerController.getInstance().timers){
	    n = TimerController.getInstance().timers.size();
	}
	n = (int) Math.ceil(Math.sqrt((double)n/3));
	for(Widget wdg = child; wdg != null; wdg = wdg.next) {
	    if(!(wdg instanceof TimerWdg))
		continue;
	    wdg.c = new Coord((i%n)*wdg.sz.x, ((int)(i/n))*wdg.sz.y);
	    h = wdg.c.y + wdg.sz.y;
	    i++;
	}
	
	btnnew.c = new Coord(0,h);
	super.pack();
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == btnnew){
	    new TimerAddWdg(c, ui.root, this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }

    class TimerAddWdg extends Window{
	
	private TextEntry name, hours, minutes, seconds;
	private Button btnadd;
	private TimerPanel panel;
	
	public TimerAddWdg(Coord c, Widget parent, TimerPanel panel) {
	    super(c, Coord.z, parent, "Add timer");
	    justclose = true;
	    this.panel = panel;
	    name = new TextEntry(Coord.z, new Coord(150,18), this, "timer");
	    new Label(new Coord(0, 25),this,"hours");
	    new Label(new Coord(50, 25),this,"min");
	    new Label(new Coord(100, 25),this,"sec");
	    hours = new TextEntry(new Coord(0, 40), new Coord(45,18), this, "0");
	    minutes = new TextEntry(new Coord(50, 40), new Coord(45,18), this, "00");
	    seconds = new TextEntry(new Coord(100, 40), new Coord(45,18), this, "00");
	    btnadd = new Button(new Coord(0, 60), 100, this, "Add");
	    pack();
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
	    if(sender == btnadd){
		try{
		    long time = 0;
		    time += Integer.parseInt(seconds.text);
		    time += Integer.parseInt(minutes.text)*60;
		    time += Integer.parseInt(hours.text)*3600;
		    Timer timer = new Timer(time, name.text);
		    TimerController.getInstance().save();
		    new TimerWdg(Coord.z, panel, timer);
		    panel.pack();
		    ui.destroy(this);
		} catch(Exception e){
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		}
	    } else {
		super.wdgmsg(sender, msg, args);
	    }
	}

	@Override
	public void destroy() {
	    panel = null;
	    super.destroy();
	}
	
    }
}
