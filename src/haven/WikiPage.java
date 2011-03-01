package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.util.List;

import wikireader.header;
import wikireader.searchItem;
import wikireader.wikiReader;

import haven.RichText.Foundry;

public class WikiPage extends HWindow {
    private static final Foundry fnd = new Foundry(TextAttribute.FOREGROUND, Color.BLACK);
    private RichTextBox content;
    private wikiReader reader;
    
    public WikiPage(Widget parent, String title, boolean closable) {
	super(parent, title, closable);
	content = new RichTextBox(Coord.z, sz, this, "", fnd);
	content.bg = new Color(255, 255, 255, 128);
	reader = new wikiReader();
	reader.start();
	reader.setSearch(title);
    }
    
    public void setsz(Coord s) {
	super.setsz(s);
	content.setsz(sz);
    }
    
    public void draw(GOut g) {
	if(reader.isReady()){
	    showResults();
	}
	super.draw(g);
    }
    
    private void showResults(){
        List<header> results = reader.getSearchResults();
        String buf = "";
        //Wasn't search results ready but it was page
        if(results != null) {
            //go through the array list
            for(header head : results){
        	if(head.title != null)  System.out.println(head.title);
        	buf+="***********************\n";
        	for(searchItem item : head.results){
        	    buf+="$size[14]{$b{$u{$col[70,70,200]{$a["+item.link+"]{"+item.title+"}}}}}\n";
        	    buf+=item.desc+"\n\n";
        	}
            }
        } else {
            buf = reader.getResults();
        }
        content.settext(buf);
    }
}
