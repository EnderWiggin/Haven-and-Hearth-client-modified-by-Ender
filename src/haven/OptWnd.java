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

import java.util.*;
import java.awt.font.TextAttribute;

public class OptWnd extends Window {
    public static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    private Tabs body;
    private String curcam;
    private Map<String, CamInfo> caminfomap = new HashMap<String, CamInfo>();

    static {
	Widget.addtype("opt", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new OptWnd(c, parent));
		}
	    });
    }
    
    private static class CamInfo {
	String name, desc;
	Tabs.Tab args;
	
	public CamInfo(String name, String desc, Tabs.Tab args) {
	    this.name = name;
	    this.desc = desc;
	    this.args = args;
	}
    }

    public OptWnd(Coord c, Widget parent) {
	super(c, new Coord(400, 340), parent, "Options");

	body = new Tabs(Coord.z, new Coord(400, 300), this) {
		public void changed(Tab from, Tab to) {
		    Utils.setpref("optwndtab", to.btn.text.text);
		    from.btn.c.y = 0;
		    to.btn.c.y = -2;
		}};

	/* GENERAL TAB */
	Widget general = body.new Tab(new Coord(0, 0), 60, "General");

	new Button(new Coord(10, 40), 125, general, "Quit") {
	    public void click() {
		HackThread.tg().interrupt();
	    }};
	new Button(new Coord(10, 70), 125, general, "Log out") {
	    public void click() {
		ui.sess.close();
	    }};
	new Button(new Coord(10, 100), 125, general, "Toggle fullscreen") {
	    public void click() {
		if(ui.fsm != null) {
		    if(ui.fsm.hasfs()) ui.fsm.setwnd();
		    else               ui.fsm.setfs();
		}
	    }};

	/* CAMERA TAB */
	getcamera();
	Widget cam = body.new Tab(new Coord(70, 0), 60, "Camera"); // är det här verkligen nödvändigt?

	new Label(new Coord(10, 40), cam, "Camera type:");
	final RichTextBox caminfo = new RichTextBox(new Coord(180, 70), new Coord(210, 180), cam, "", foundry);
	caminfo.bg = new java.awt.Color(0, 0, 0, 64);
	addinfo("orig",    "Original Camera", "This camera centers where you left-click.", null);
	addinfo("border",  "The Borderizer", "", null);
	addinfo("predict", "The Predictor", "", null);

	final Tabs cambox = new Tabs(new Coord(100, 60), new Coord(300, 200), cam);
	Tabs.Tab ctab;
	/* clicktgt arg */
	ctab = cambox.new Tab();
	new Label(new Coord(10, 10),  ctab, "Fast");
	new Label(new Coord(10, 180), ctab, "Slow");
	new Scrollbar(new Coord(25, 20), 160, ctab, 0, 20) {{ val = Integer.parseInt(Utils.getpref("clicktgtarg1", "10")); }
	    public boolean mouseup(Coord c, int button) {
		if(super.mouseup(c, button)) {
		    setcamera(curcam, String.valueOf(Math.cbrt(Math.cbrt(val / 24.0))));
		    Utils.setpref("clicktgtarg1", String.valueOf(val));
		    return(true);
		}
		return(false);
	    }};
	addinfo("clicktgt", "Target Seeker", "", ctab);
	/* fixedcake arg */
	ctab = cambox.new Tab();
	new Label(new Coord(10, 10),  ctab, "Fast");
	new Label(new Coord(10, 180), ctab, "Slow");
	new Scrollbar(new Coord(25, 20), 160, ctab, 0, 20) {{ val = Integer.parseInt(Utils.getpref("fixedcakearg1", "10")); }
	    public boolean mouseup(Coord c, int button) {
		if(super.mouseup(c, button)) {
		    setcamera(curcam, String.valueOf(Math.pow(1 - (val / 20.0), 2)));
		    Utils.setpref("fixedcakearg1", String.valueOf(val));
		    return(true);
		}
		return(false);
	    }};
	addinfo("fixedcake", "Fixcake", "", ctab);

	final RadioGroup cameras = new RadioGroup(cam) {
		public void changed(int btn, String lbl) {
		    if(!lbl.equals(curcam))
			setcamera(lbl, "0.2");
		    CamInfo inf = caminfomap.get(lbl);
		    if(inf == null) {
			cambox.showtab(null);
		    } else {
			cambox.showtab(inf.args);
			caminfo.settext(String.format("$size[12]{%s}\n\n%s", inf.name, inf.desc));
		    }
		}};
	String[] camlist = new String[] {"orig", "clicktgt", "kingsquest", "border", "predict", "fixed", "cake", "fixedcake"};
	for(int i = 0; i < camlist.length; i++)
	    cameras.add(camlist[i], new Coord(10, 50 + i * 25));
	cameras.check(curcam);

	/* SOUND TAB */
	Widget snd = body.new Tab(new Coord(140, 0), 60, "Sound");

	new Label(new Coord(10, 40), snd, "Sound volume:");
	new Frame(new Coord(10, 65), new Coord(20, 206), snd);
	final Label sfxvol = new Label(new Coord(35, 69 + (int)(getsfxvol() * 1.86)),  snd, String.valueOf(100 - getsfxvol()) + " %");
	new Scrollbar(new Coord(25, 70), 196, snd, 0, 100) {{ val = getsfxvol(); }
	    public void changed() {
		setsfxvol(val);
		sfxvol.c.y = 69 + (int)(val * 1.86);
		sfxvol.settext(String.valueOf(100 - val) + " %");
	    }};

	new Frame(new Coord(-10, 20), new Coord(420, 330), this);
	String last = Utils.getpref("optwndtab", "");
	for(Tabs.Tab tab : body.tabs) {
	    if(tab.btn.text.text.equals(last))
		body.showtab(tab);
	}
    }

    private void getcamera() {
	curcam = Utils.getpref("defcam", "border");
    }
    private void setcamera(String camtype, String... args) {
	curcam = camtype;
	Utils.setpref("defcam", curcam);
	Utils.setprefb("camargs", Utils.serialize(args));

	MapView mv = ui.root.findchild(MapView.class);
	if(mv != null) {
	    if     (curcam.equals("orig"))       mv.cam = new MapView.OrigCam();
	    else if(curcam.equals("clicktgt"))   mv.cam = new MapView.OrigCam2(args);
	    else if(curcam.equals("kingsquest")) mv.cam = new MapView.WrapCam();
	    else if(curcam.equals("border"))     mv.cam = new MapView.BorderCam();
	    else if(curcam.equals("predict"))    mv.cam = new MapView.PredictCam();
	    else if(curcam.equals("fixed"))      mv.cam = new MapView.FixedCam();
	    else if(curcam.equals("cake"))       mv.cam = new MapView.CakeCam();
	    else if(curcam.equals("fixedcake"))  mv.cam = new MapView.FixedCakeCam(args);
	}
    }

    private int getsfxvol() {
	return((int)(100 - Double.parseDouble(Utils.getpref("sfxvol", "1.0")) * 100));
    }
    private void setsfxvol(int vol) {
	Audio.setvolume((100 - vol) / 100.0);
    }

    private void addinfo(String camtype, String title, String text, Tabs.Tab args) {
	caminfomap.put(camtype, new CamInfo(title, text, args));
    }

    public class Frame extends Widget {
	private IBox box;

	public Frame(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	    box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
	}

	public void draw(GOut og) {
	    super.draw(og);
	    GOut g = og.reclip(Coord.z, sz);
	    g.chcolor(150, 200, 125, 255);
	    box.draw(g, Coord.z, sz);
	}
    }
}
