package ender.screen;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.*;

public class Bitmap {
    public int w, h;
    public int[] pixels;

    public Bitmap(int w, int h) {
        this.w = w;
        this.h = h;
        pixels = new int[w * h];
    }

    public Bitmap(BufferedImage image) {
	w = image.getWidth();
	h = image.getHeight();
	//pixels = new int[w*h];
	int type = image.getType();
	BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	img.createGraphics().drawImage(image, null, 0, 0);
	pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
//	byte px[] = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//	if(type == BufferedImage.TYPE_3BYTE_BGR){
//	    for(int i=0; i<pixels.length; i++){
//		int k = 3*i;
//		pixels[i] = ((0xff000000) | (px[k+2]<<16) | (px[k+1]<<8) | (px[k+0])); 
//	    }
//	} else if(type == BufferedImage.TYPE_4BYTE_ABGR){
//	    for(int i=0; i<pixels.length; i++){
//		int k = 4*i;
//		pixels[i] = ((px[k+0]<<24) | (px[k+3]<<16) | (px[k+2]<<8) | (px[k+1])); 
//	    }
//	} else {
//	    for(int i=0; i<pixels.length; i++){
//		int k = 3*i;
//		pixels[i] = ((0xff000000) | (px[k+1]<<16) | (px[k+2]<<8) | (px[k+0])); 
//	    }
//	}
    }

    public void clear(int color) {
        Arrays.fill(pixels, color);
    }

    public void blit(Bitmap bitmap, int x, int y) {
        int x0 = x;
        int x1 = x + bitmap.w;
        int y0 = y;
        int y1 = y + bitmap.h;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            int sp = (yy - y) * bitmap.w + (x0 - x);
            tp -= sp;
            for (int xx = sp; xx < sp + ww; xx++) {
        	int col = bitmap.pixels[xx];
        	int a = (col>>24)&0xff;
        	if(a == 255){
        	    pixels[tp + xx] = col;
        	} else if (a > 0){
        	    pixels[tp + xx] = mix(pixels[tp + xx], col, a);
        	}
            }
        }
    }
    
    private int mix(int c1, int c2, int a1){
	int a2 = 256 - a1;
	int r = (c1 & 0x00FF0000);
	int g = (c1 & 0x0000FF00);
	int b = (c1 & 0x000000FF);

	int rr = (c2 & 0x00FF0000);
	int gg = (c2 & 0x0000FF00);
	int bb = (c2 & 0x000000FF);

	r = ((r * a2 + rr * a1) >> 8) & 0xff0000;
	g = ((g * a2 + gg * a1) >> 8) & 0xff00;
	b = ((b * a2 + bb * a1) >> 8) & 0xff;
	return 0xff000000 | r | g | b;
    }
    
    public void blit(Bitmap bitmap, int x, int y, int www, int hhh) {
        int x0 = x;
        int x1 = x + www;
        int y0 = y;
        int y1 = y + hhh;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            int sp = (yy - y) * bitmap.w + (x0 - x);
            tp -= sp;
            for (int xx = sp; xx < sp + ww; xx++) {
                int col = bitmap.pixels[xx];
                int a = (col>>24)&0xff;
        	if(a == 255){
        	    pixels[tp + xx] = col;
        	} else if (a > 0){
        	    pixels[tp + xx] = mix(pixels[tp + xx], col, a);
        	}
            }
        }
    }

    public void colorBlit(Bitmap bitmap, int x, int y, int color) {
        int x0 = x;
        int x1 = x + bitmap.w;
        int y0 = y;
        int y1 = y + bitmap.h;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        int a2 = (color >> 24) & 0xff;
        int a1 = 256 - a2;

        int rr = color & 0xff0000;
        int gg = color & 0xff00;
        int bb = color & 0xff;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            int sp = (yy - y) * bitmap.w + (x0 - x);
            for (int xx = 0; xx < ww; xx++) {
                int col = bitmap.pixels[sp + xx];
                if (col < 0) {
                    int r = (col & 0xff0000);
                    int g = (col & 0xff00);
                    int b = (col & 0xff);

                    r = ((r * a1 + rr * a2) >> 8) & 0xff0000;
                    g = ((g * a1 + gg * a2) >> 8) & 0xff00;
                    b = ((b * a1 + bb * a2) >> 8) & 0xff;
                    pixels[tp + xx] = 0xff000000 | r | g | b;
                }
            }
        }
    }

    public void fill(int x, int y, int bw, int bh, int color) {
        int x0 = x;
        int x1 = x + bw;
        int y0 = y;
        int y1 = y + bh;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 > w) x1 = w;
        if (y1 > h) y1 = h;
        int ww = x1 - x0;

        for (int yy = y0; yy < y1; yy++) {
            int tp = yy * w + x0;
            for (int xx = 0; xx < ww; xx++) {
                pixels[tp + xx] = color;
            }
        }
    }
}