import haven.Config;
import haven.Coord;
import haven.FreeSprite;
import haven.GOut;
import haven.Message;
import haven.Resource;
import haven.Sprite;
import haven.TrackingWnd;

public class DowseFx extends FreeSprite
{
  double a = 0.0D;
  static final int ms = 4000;
  static final int r = 100;
  int a1;
  int a2;

  public DowseFx(Sprite.Owner owner, Resource res, Message msg)
  {
    super(owner, res, -15, 0);
    a2 = (msg.uint8() * 360 / 200);
    a1 = (msg.uint8() * 360 / 200);
    a1 = (-45 - a1);
    a2 = (-45 - a2);
    int a0 = (a1 + a2)/2;
    int d = Math.max(Math.abs(a1 - a2)%360, 5);
    a2 = a1 + d;
    if(Config.showDirection) {
	new TrackingWnd(a0+270, d, a1, a2);
    }
  }

  public void draw(GOut g, Coord c) {
    if (this.a < 0.25D) {
      g.chcolor(255, 0, 0, 128);
      g.fellipse(c, new Coord((int)(this.a / 0.25D * 100.0D), (int)(this.a / 0.25D * 100.0D / 2.0D)));
    } else if (this.a < 0.75D) {
      g.chcolor(255, 0, 0, (int)((0.75D - this.a) / 0.5D * 128.0D));
      g.fellipse(c, new Coord((int)(this.a / 0.25D * 100.0D), (int)(this.a / 0.25D * 100.0D / 2.0D)));
      g.chcolor(255, 0, 0, 128);
      g.fellipse(c, new Coord(100, 50), this.a1, this.a2);
    } else {
      g.chcolor(255, 0, 0, (int)((1.0D - this.a) / 0.25D * 128.0D));
      g.fellipse(c, new Coord(100, 50), this.a1, this.a2);
    }
    g.chcolor();
  }

  public boolean tick(int paramInt) {
    this.a += paramInt / 2000.0D;
    return this.a >= 1.0D;
  }
}