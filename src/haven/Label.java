package haven;

import java.awt.Color;

public class Label extends Widget {
	Text text;
	String texts;
	Color c = Color.BLACK;
	
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
	
	public Label(Coord c, Widget parent, String text) {
		super(c, Coord.z, parent);
		this.text = Text.render(texts = text, this.c);
		sz = this.text.sz();
	}
	
	public void settext(String text) {
		this.text = Text.render(texts = text, c);
		sz = this.text.sz();
	}
	
	public void setcolor(Color color) {
		c = color;
		this.text = Text.render(texts, c);
		sz = this.text.sz();
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "set")
			settext((String)args[0]);
	}
}
