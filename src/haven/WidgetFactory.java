package haven;

public interface WidgetFactory {
    public Widget create(Coord c, Widget parent, Object[] par);
}
