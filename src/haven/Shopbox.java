// Decompiled by DJ v3.10.10.93 Copyright 2007 Atanas Neshkov  Date: 11/26/2013 12:41:17 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Shopbox.java
package haven;

import java.awt.Color;
import java.awt.Font;

public class Shopbox extends Widget implements DTarget{
    static Resource res;
    static Tex bg;
    static Coord itemc;
    static Coord inumc;
    static Coord pitemc;
    static Coord pinumc;
    static Coord havec;
    static Coord bbtnc;
    public Indir item;
    public Indir pitem;
    public Tex num;
    public Tex pnum;
    public Tex phave;
    public int itemq;
    public int pitemq;
    public boolean admin;
    public Button bbtn;
    public Button cbtn;
    public String itemtt;
    public String pitemtt;
    static haven.Text.Foundry lf;
	
	public int matsInStand = -1;
	
	static
	{
		res = Resource.load("ui/barterbox", 2);
		res.loadwait();
		
        bg = ((haven.Resource.Image)res.layer(Resource.imgc)).tex();
        itemc = new Coord(10, bg.sz().y / 2 - Inventory.invsq.sz().y / 2);
        inumc = itemc.add(Inventory.invsq.sz());
        pitemc = itemc.add(60, 0);
        pinumc = pitemc.add(Inventory.invsq.sz());
        havec = new Coord(itemc.x + 100, 0);
        bbtnc = new Coord(itemc.x + 100, bg.sz().y / 2);
        lf = new haven.Text.Foundry(new Font("SansSerif", 0, 10), Color.WHITE);
		
		Widget.addtype("ui/barterbox", new WidgetFactory() {
			public Widget create(Coord c, Widget parent, Object[] args) {
				//Resource bg = Resource.load("ui/aim");
				//List<Meter> meters = new LinkedList<Meter>();
				//for(int i = 1; i < args.length; i += 2)
				//meters.add(new Meter((Color)args[i], (Integer)args[i + 1]));
				return(new Shopbox(c, parent));
			}
		});
    }
	
	public Shopbox(Coord coord, Widget widget)
    {
        super(coord, bg.sz(), widget);
        item = null;
        pitem = null;
        admin = false;
    }
	
    public void draw(GOut gout)
    {
        gout.image(bg, Coord.z);
        super.draw(gout);
        synchronized(this)
        {
            Resource resource;
            if(item != null && (resource = (Resource)item.get()) != null)
            {
                gout.image(Inventory.invsq, itemc);
                gout.image(((haven.Resource.Image)resource.layer(Resource.imgc)).tex(), itemc.add(1, 1));
                gout.aimage(num, inumc, 1.0D, 1.0D);
                if(itemtt == null && resource.layer(Resource.tooltip) != null)
                {
                    String s = ((haven.Resource.Tooltip)resource.layer(Resource.tooltip)).t;
                    if(itemq > 0)
                        s = String.format("%s, quality %d", new Object[] {
                            s, Integer.valueOf(itemq)
                        });
                    itemtt = s;
                }
            }
            if(pitem != null && (resource = (Resource)pitem.get()) != null)
            {
                gout.image(Inventory.invsq, pitemc);
                gout.image(((haven.Resource.Image)resource.layer(Resource.imgc)).tex(), pitemc.add(1, 1));
                gout.aimage(pnum, pinumc, 1.0D, 1.0D);
                if(pitemtt == null && resource.layer(Resource.tooltip) != null)
                {
                    String s1 = ((haven.Resource.Tooltip)resource.layer(Resource.tooltip)).t;
                    if(pitemq > 0)
                        s1 = String.format("%s, quality %d+", new Object[] {
                            s1, Integer.valueOf(pitemq)
                        });
                    pitemtt = s1;
                }
                if(phave != null)
                    gout.image(phave, pitemc.add(Inventory.invsq.sz().x + 5, 0));
            }
        }
    }

    public Object tooltip(Coord coord, boolean flag)
    {
        if(coord.isect(itemc, Inventory.invsq.sz()))
            return itemtt;
        if(coord.isect(pitemc, Inventory.invsq.sz()))
            return pitemtt;
        else
            return null;
    }

    public boolean mousewheel(Coord coord, int i)
    {
        if(coord.isect(itemc, Inventory.invsq.sz()))
        {
            if(i < 0)
                wdgmsg("xfer", new Object[] {
                    Integer.valueOf(-1), Integer.valueOf(ui.modflags())
                });
            if(i > 0)
                wdgmsg("xfer", new Object[] {
                    Integer.valueOf(1), Integer.valueOf(ui.modflags())
                });
        } else
        if(coord.isect(pitemc, Inventory.invsq.sz()))
        {
            if(i < 0)
                wdgmsg("pxfer", new Object[] {
                    Integer.valueOf(-1), Integer.valueOf(ui.modflags())
                });
            if(i > 0)
                wdgmsg("pxfer", new Object[] {
                    Integer.valueOf(1), Integer.valueOf(ui.modflags())
                });
        }
        return true;
    }

    public boolean mousedown(Coord coord, int button)
    {
        if(super.mousedown(coord, button))
            return true;
        if(coord.isect(itemc, Inventory.invsq.sz())){
            if(button == 1){
                if(ui.modshift){
                    wdgmsg("transfer", new Object[] { coord });
                }else{
                    wdgmsg("take", new Object[] { coord });
				}
			}
        } else if(coord.isect(pitemc, Inventory.invsq.sz()) && button == 1){
            if(ui.modshift){
                wdgmsg("ptransfer", new Object[] { coord });
            }else{
                wdgmsg("ptake", new Object[] { coord });
			}
		} else if(coord.isect(pitemc, Inventory.invsq.sz()) && button == 3 && ui.modmeta){
			for(int i = 0; i < 56; i++)
					wdgmsg("ptransfer", new Object[] { coord });
		}
        return true;
    }

    private void setsubs()
    {
        if(bbtn != null)
            ui.destroy(bbtn);
        if(cbtn != null)
            ui.destroy(cbtn);
        bbtn = null;
        cbtn = null;
        if(!admin)
            bbtn = new Button(bbtnc, Integer.valueOf(60), this, "Buy");
        else
            cbtn = new Button(bbtnc, Integer.valueOf(60), this, "Change");
    }

    public void wdgmsg(Widget widget, String s, Object aobj[])
    {
        if(widget == bbtn)
        {
            wdgmsg("buy", new Object[0]);
            return;
        }
        if(widget == cbtn)
        {
            wdgmsg("change", new Object[0]);
            return;
        } else
        {
            super.wdgmsg(widget, s, aobj);
            return;
        }
    }

    public void uimsg(String s, Object aobj[])
    {
        if(s == "item")
        {
            int i = ((Integer)aobj[0]).intValue();
            int i1 = ((Integer)aobj[1]).intValue();
            int k1 = ((Integer)aobj[2]).intValue();
            String s1 = null;
            if(aobj.length > 3)
                s1 = (String)aobj[3];
            synchronized(this)
            {
                num = new TexI(Utils.outline2(lf.render(String.format("%d", new Object[] {
                    Integer.valueOf(i1)
                })).img, Color.BLACK));
                itemq = k1;
                itemtt = s1;
                item = i >= 0 ? ui.sess.getres(i) : null;
            }
        } else
        if(s == "price")
        {
            int j = ((Integer)aobj[0]).intValue();
            int j1 = ((Integer)aobj[1]).intValue();
            int l1 = ((Integer)aobj[2]).intValue();
            synchronized(this)
            {
                pnum = new TexI(Utils.outline2(lf.render(String.format("%d", new Object[] {
                    Integer.valueOf(j1)
                })).img, Color.BLACK));
                pitemq = l1;
                pitemtt = null;
                pitem = j >= 0 ? ui.sess.getres(j) : null;
            }
        } else
        if(s == "phave")
        {
            int k = ((Integer)aobj[0]).intValue();
            if(k > 0){
				matsInStand = k;
                phave = lf.render(String.format("Have %d", new Object[] {
                    Integer.valueOf(k)
                })).tex();
            }else{
                phave = null;
			}
        } else
        if(s == "mode")
        {
            int l = ((Integer)aobj[0]).intValue();
            admin = l != 0;
            setsubs();
        }
    }

    public boolean drop(Coord coord, Coord coord1)
    {
        wdgmsg("drop", new Object[0]);
        return true;
    }

    public boolean iteminteract(Coord coord, Coord coord1)
    {
        return false;
    }
}