/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import ender.screen.Bitmap;
import ender.screen.Screen;

public class TexI extends Tex {
    public static ComponentColorModel glcm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    protected byte[] pixels;
    public BufferedImage back;
//    public Bitmap bmp;
    protected Graphics2D mygl = null;
    protected Coord tdim;
    private Object fmt;
    private Bitmap bmp;
    public static boolean disableall = false;

    public TexI(BufferedImage img) {
	super(Utils.imgsz(img));
	tdim = new Coord(nextp2(sz().x), nextp2(sz().y));
	back = img;
	bmp = new Bitmap(back);
	pixels = convert(img, tdim);
	mygl = img.createGraphics();
    }

    public TexI(Coord sz) {
	super(sz);
	bmp = new Bitmap(sz.x, sz.y);
	tdim = new Coord(nextp2(sz.x), nextp2(sz.y));
	pixels = new byte[tdim.x * tdim.y * 4];
    }
    
    protected Color setenv(Screen gl) {
	return(Color.WHITE);
    }
	
    Color blend(GOut g, Color amb) {
	Color c = g.getcolor();
	Color n = new Color((c.getRed() * amb.getRed()) / 255,
			    (c.getGreen() * amb.getGreen()) / 255,
			    (c.getBlue() * amb.getBlue()) / 255,
			    (c.getAlpha() * amb.getAlpha()) / 255);
	return(n);
    }
	
    public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
	Screen gl = g.gl;
	Color amb = blend(g, setenv(gl));
//	checkerr(gl);
	if(!disableall) {
	    //gl.gl.drawImage(back, c.x, c.y, c.x+sz.x, c.y+sz.y, ul.x, ul.y, br.x, br.y, null);
	    
	    gl.blit(bmp, c.x, c.y);
	}
    }
	
    public void dispose() {
	    //dispose(mygl, id);
    }
	
    protected void finalize() {
	dispose();
    }
    
    protected void update(byte[] n) {
	if(n.length != pixels.length)
	    throw(new RuntimeException("Illegal new texbuf size (" + n.length + " != " + pixels.length + ")"));
	pixels = n;
	dispose();
    }
	
    public int getRGB(Coord c) {
	return(back.getRGB(c.x, c.y));
    }
	
    public TexI mkmask() {
	TexI n = new TexI(dim);
	n.pixels = new byte[pixels.length];
	System.arraycopy(pixels, 0, n.pixels, 0, pixels.length);
	return(n);
    }
	
    public static BufferedImage mkbuf(Coord sz) {
	WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, sz.x, sz.y, 4, null);
	BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
	return(tgt);
    }
	
    public static byte[] convert(BufferedImage img, Coord tsz) {
	WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, tsz.x, tsz.y, 4, null);
	BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
	Graphics g = tgt.createGraphics();
	g.drawImage(img, 0, 0, null);
	g.dispose();
	return(((DataBufferByte)buf.getDataBuffer()).getData());
    }
}
