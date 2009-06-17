package haven;

import java.awt.Color;

public class Label extends Widget {
    Text.Foundry f;
    Text text;
    String texts;
    Color col = Color.WHITE;
	
    static {
	Widget.addtype("lbl", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Label(c, parent, (String)args[0]));
		}
	    });
    }
	
    public void draw(GOut g) {
	g.image(text.tex(), Coord.z);
    }
	
    public Label(Coord c, Widget parent, String text, Text.Foundry f) {
	super(c, Coord.z, parent);
	this.f = f;
	this.text = f.render(texts = text, this.col);
	sz = this.text.sz();
    }

    public Label(Coord c, Widget parent, String text) {
	this(c, parent, text, Text.std);
    }
	
    public void settext(String text) {
	this.text = f.render(texts = text, col);
	sz = this.text.sz();
    }
	
    public void setcolor(Color color) {
	col = color;
	this.text = f.render(texts, col);
	sz = this.text.sz();
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "set")
	    settext((String)args[0]);
    }
}
