package haven;

public class ISBox extends Widget implements DTarget {
    static Tex bg = Resource.loadtex("gfx/hud/bosq");
    static Text.Foundry lf;
    private Resource res;
    private Text label;
    static {
        lf = new Text.Foundry(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 18), java.awt.Color.WHITE);
        lf.aa = true;
    }
    
    static {
        Widget.addtype("isbox", new WidgetFactory() {
            public Widget create(Coord c, Widget parent, Object[] args) {
                return(new ISBox(c, parent, Resource.load((String)args[0]), (Integer)args[1], (Integer)args[2], (Integer)args[3]));
            }
        });
    }
    
    private void setlabel(int rem, int av, int bi) {
        label = lf.renderf("%d/%d/%d", rem, av, bi);
    }
    
    public ISBox(Coord c, Widget parent, Resource res, int rem, int av, int bi) {
        super(c, bg.sz(), parent);
        this.res = res;
        setlabel(rem, av, bi);
    }
    
    public void draw(GOut g) {
        g.image(bg, Coord.z);
        if(!res.loading) {
            Tex t = res.layer(Resource.imgc).tex();
            Coord dc = new Coord(6, (bg.sz().y / 2) - (t.sz().y / 2));
            g.image(t, dc);
        }
        g.image(label.tex(), new Coord(40, (bg.sz().y / 2) - (label.tex().sz().y / 2)));
    }
    
    public boolean mousedown(Coord c, int button) {
        if(button == 1) {
            if(ui.modshift)
                wdgmsg("xfer");
            else
                wdgmsg("click");
            return(true);
        }
        return(false);
    }
    
    public boolean drop(Coord cc, Coord ul) {
        wdgmsg("drop");
        return(true);
    }
    
    public boolean iteminteract(Coord cc, Coord ul) {
        wdgmsg("iact");
        return(true);
    }
    
    public void uimsg(String msg, Object... args) {
        if(msg == "chnum") {
            setlabel((Integer)args[0], (Integer)args[1], (Integer)args[2]);
        } else {
            super.uimsg(msg, args);
        }
    }
}
