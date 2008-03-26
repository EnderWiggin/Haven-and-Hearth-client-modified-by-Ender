package haven;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.Graphics;

public class Tex2 extends Tex {
	BufferedImage bufw;
	WritableRaster buf;
	Graphics cg = null;
	
	public Tex2(Coord sz) {
		super(sz);
		buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, tdim.x, tdim.y, 4, null);
		bufw = new BufferedImage(Tex.glcm, buf, false, null);
	}
	
	public Graphics graphics() {
		if(cg != null)
			throw(new RuntimeException("Multiple Tex2 Graphics created"));
		return(cg = bufw.getGraphics());
	}
	
	public void update() {
		cg.dispose();
		cg = null;
		super.update(((DataBufferByte)buf.getDataBuffer()).getData());
	}
}
