import haven.Config;
import haven.Coord;
import haven.FreeSprite;
import haven.GOut;
import haven.Message;
import haven.Resource;
import haven.Sprite;
import haven.Sprite.Owner;
import haven.Window;
import haven.Label;
import haven.UI;

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
    int a0 = -45 - ((a1 + a2)>>1);
    int d = Math.max(Math.abs(a1 - a2), 5);
    a1 = a0 - (d>>1);
    a2 = a0 + (d>>1);
    if(Config.showDirection) {
	Window wnd = new Window(new Coord(100,100),Coord.z,UI.instance.root,"Direction");
	wnd.justclose = true;
	new Label(Coord.z, wnd, "Direction: "+(a0 + 270)+", delta: "+d);
	wnd.pack();
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