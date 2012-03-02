package ender.screen;

import haven.Coord;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Screen extends Bitmap {
    public BufferedImage image;
    private int xOffset, yOffset;
    public Graphics2D gl;

    public Screen(int w, int h) {
        super(w, h);
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        gl = image.createGraphics();
    }

    public void setOffset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public void blit(Bitmap bitmap, double x, double y) {
        blit(bitmap, (int) x, (int) y);
    }
    
    public void blit(Bitmap bitmap, Coord c) {
        blit(bitmap, c.x, c.y);
    }

    public void blit(Bitmap bitmap, int x, int y) {
        super.blit(bitmap, x + xOffset, y + yOffset);
    }

    public void blit(Bitmap bitmap, int x, int y, int w, int h) {
        super.blit(bitmap, x + xOffset, y + yOffset, w, h);
    }

    public void colorBlit(Bitmap bitmap, double x, double y, int color) {
        colorBlit(bitmap, (int) x, (int) y, color);
    }

    public void colorBlit(Bitmap bitmap, int x, int y, int color) {
        super.colorBlit(bitmap, x + xOffset, y + yOffset, color);
    }

    public void fill(int x, int y, int width, int height, int color) {
        super.fill(x + xOffset, y + yOffset, width, height, color);
    }

    public void setOffset(Coord off) {
	xOffset = off.x;
	yOffset = off.y;
    }
}