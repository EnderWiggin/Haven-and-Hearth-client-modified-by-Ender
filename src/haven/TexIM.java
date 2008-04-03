package haven;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.Graphics2D;

public class TexIM extends TexI {
	BufferedImage bufw;
	WritableRaster buf;
	Graphics2D cg = null;
	
	public TexIM(Coord sz) {
		super(sz);
		clear();
	}
	
	public Graphics2D graphics() {
		if(cg != null)
			throw(new RuntimeException("Multiple TexIM Graphics created"));
		return(cg = bufw.createGraphics());
	}
	
	public void update() {
		cg.dispose();
		cg = null;
		super.update(((DataBufferByte)buf.getDataBuffer()).getData());
	}
	
	public void clear() {
		buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, tdim.x, tdim.y, 4, null);
		bufw = new BufferedImage(glcm, buf, false, null);
	}
}
