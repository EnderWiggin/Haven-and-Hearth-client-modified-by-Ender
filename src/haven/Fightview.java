package haven;

public class Fightview extends Widget {
    static {
        Widget.addtype("frv", new WidgetFactory() {
            public Widget create(Coord c, Widget parent, Object[] args) {
                return(new Fightview(c, parent));
            }
        });
    }
    
    public Fightview(Coord c, Widget parent) {
        super(c, new Coord(100, 140), parent);
    }
}
