package ender;

import haven.Coord;
import haven.Resource;
import haven.Tex;
import haven.TexI;
import haven.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class HLInfo {
    private Tex icon = null;
    private String name;
    private String icnname = null;
    public Color col = Color.GRAY;
    
    public HLInfo(String name, String icon){
	this.name = name;
	icnname = icon;
    }
    
    public Tex geticon(){
	if(icon == null){
	    BufferedImage img;
	    if(icnname != null){
		img = Resource.loadimg(icnname);
	    } else {
		img = Resource.loadimg(name);
	    }
	    BufferedImage icnimg = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = icnimg.createGraphics();
	    g.setRenderingHint(
		    RenderingHints.KEY_INTERPOLATION,
		    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	    // icon background
	    g.setColor(col);
	    g.fillOval(0, 0, icnimg.getWidth() - 1, icnimg.getHeight() - 1);
	    g.setColor(Color.DARK_GRAY);
	    g.drawOval(0, 0, icnimg.getWidth() - 1, icnimg.getHeight() - 1);

	    Coord isz = Utils.imgsz(img);
	    isz = isz.mul(16.0 / Math.max(16, Math.max(isz.x, isz.y)));
	    g.drawImage(img, 10-isz.x/2, 10-isz.y/2, isz.x, isz.y ,null);

	    icon = new TexI(icnimg);
	}
	return icon;
    }
    
    public void setColor(Color c){
	col = c;
    }
}
