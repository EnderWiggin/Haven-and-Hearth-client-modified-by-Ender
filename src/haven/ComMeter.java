package haven;

public class ComMeter extends Widget {
    static Tex sword = Resource.loadtex("gfx/hud/combat/com/sword");
    static Tex scales[];
    int bal, intns;
    
    static {
        scales = new Tex[11];
        for(int i = 0; i <= 10; i++)
            scales[i] = Resource.loadtex(String.format("gfx/hud/combat/com/%02d", i));
        Widget.addtype("com", new WidgetFactory() {
            public Widget create(Coord c, Widget parent, Object[] args) {
                return(new ComMeter(c, parent));
            }
        });
    }
    
    public ComMeter(Coord c, Widget parent) {
        super(c, sword.sz(), parent);
    }
    
    public void draw(GOut g) {
        g.image(sword, Coord.z);
        g.image(scales[(-bal) + 5], Coord.z);
	g.atext(String.format("%d", intns), sword.sz().div(new Coord(2, 1)), 0.5, 1);
    }
    
    public void uimsg(String msg, Object... args) {
        if(msg == "upd") {
            bal = (Integer)args[0];
            intns = (Integer)args[1];
            return;
        }
        super.uimsg(msg, args);
    }
}
