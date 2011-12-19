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

import haven.SelectorWnd.Callback;

import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptWnd extends Window {
    public static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    private static final BufferedImage cfgimgu = Resource.loadimg("gfx/hud/buttons/centeru");
    private static final BufferedImage cfgimgd = Resource.loadimg("gfx/hud/buttons/centerd");
    private Tabs body;
    private String curcam;
    private Map<String, CamInfo> caminfomap = new HashMap<String, CamInfo>();
    private Map<String, String> camname2type = new HashMap<String, String>();
    private Map<String, String[]> camargs = new HashMap<String, String[]>();
    private Comparator<String> camcomp = new Comparator<String>() {
	public int compare(String a, String b) {
	    if(a.startsWith("The ")) a = a.substring(4);
	    if(b.startsWith("The ")) b = b.substring(4);
	    return (a.compareTo(b));
	}
    };

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
	super(c, new Coord(550, 445), parent, "Options");

	body = new Tabs(Coord.z, new Coord(530, 445), this) {
	    public void changed(Tab from, Tab to) {
		Utils.setpref("optwndtab", to.btn.text.text);
		from.btn.c.y = 0;
		to.btn.c.y = -2;
		}};
	Widget tab;

	{ /* GENERAL TAB */
	    tab = body.new Tab(new Coord(0, 0), 60, "General");

	    new Button(new Coord(10, 40), 125, tab, "Quit") {
		public void click() {
		    HackThread.tg().interrupt();
		}};
	    new Button(new Coord(10, 70), 125, tab, "Log out") {
		public void click() {
		    ui.sess.close();
		}};
	    new Button(new Coord(10, 100), 125, tab, "Toggle fullscreen") {
		public void click() {
		    if (ui.fsm != null) {
			if(ui.fsm.hasfs()) ui.fsm.setwnd();
			else               ui.fsm.setfs();
		    }
		}};

	    (new CheckBox(new Coord(10, 130), tab, "Use new minimap (restart required)") {
		public void changed(boolean val) {
		    Config.new_minimap = val;
		    Config.saveOptions();
		}
	    }).a = Config.new_minimap;
	    
	    (new CheckBox(new Coord(10, 165), tab, "Use new chat (restart required)") {
		public void changed(boolean val) {
		    Config.new_chat = val;
		    Config.saveOptions();
		}
	    }).a = Config.new_chat;
	    
	    (new CheckBox(new Coord(10, 200), tab, "Add timestamp in chat") {
		public void changed(boolean val) {
		    Config.timestamp = val;
		    Config.saveOptions();
		}
	    }).a = Config.timestamp;
	    
	    (new CheckBox(new Coord(10, 235), tab, "Show dowsing direcion") {
		public void changed(boolean val) {
		    Config.showDirection = val;
		    Config.saveOptions();
		}
	    }).a = Config.showDirection;
	    
	    (new CheckBox(new Coord(10, 270), tab, "Always show heartling names") {
		public void changed(boolean val) {
		    Config.showNames = val;
		    Config.saveOptions();
		}
	    }).a = Config.showNames;
	    
	    (new CheckBox(new Coord(10, 305), tab, "Always show other kin names") {
		public void changed(boolean val) {
		    Config.showOtherNames = val;
		    Config.saveOptions();
		}
	    }).a = Config.showOtherNames;
	    
	    (new CheckBox(new Coord(10, 340), tab, "Show smileys in chat") {
		public void changed(boolean val) {
		    Config.use_smileys = val;
		    Config.saveOptions();
		}
	    }).a = Config.use_smileys;
	    
	    (new CheckBox(new Coord(10, 375), tab, "Show item quality") {
		public void changed(boolean val) {
		    Config.showq = val;
		    Config.saveOptions();
		}
	    }).a = Config.showq;
	    
	    (new CheckBox(new Coord(10, 410), tab, "Show player path") {
		public void changed(boolean val) {
		    Config.showpath = val;
		    Config.saveOptions();
		}
	    }).a = Config.showpath;
	    
	    (new CheckBox(new Coord(220, 130), tab, "Fast menu") {
		public void changed(boolean val) {
		    Config.fastFlowerAnim = val;
		    Config.saveOptions();
		}
	    }).a = Config.fastFlowerAnim;
	    
	    (new CheckBox(new Coord(220, 165), tab, "Compress screenshots") {
		public void changed(boolean val) {
		    Config.sshot_compress = val;
		    Config.saveOptions();
		}
	    }).a = Config.sshot_compress;
	    
	    (new CheckBox(new Coord(220, 200), tab, "Exclude UI from screenshot") {
		public void changed(boolean val) {
		    Config.sshot_noui = val;
		    Config.saveOptions();
		}
	    }).a = Config.sshot_noui;
	    
	    (new CheckBox(new Coord(220, 235), tab, "Exclude names from screenshot") {
		public void changed(boolean val) {
		    Config.sshot_nonames = val;
		    Config.saveOptions();
		}
	    }).a = Config.sshot_nonames;
	    
	    (new CheckBox(new Coord(220, 270), tab, "Use optimized claim higlighting") {
		public void changed(boolean val) {
		    Config.newclaim = val;
		    Config.saveOptions();
		}
	    }).a = Config.newclaim;
	    
	    (new CheckBox(new Coord(220, 305), tab, "Show digit toolbar") {
		public void changed(boolean val) {
		    ui.mnu.digitbar.visible = val;
		    Config.setWindowOpt(ui.mnu.digitbar.name, val);
		}
	    }).a = ui.mnu.digitbar.visible;
	    
	    (new CheckBox(new Coord(220, 340), tab, "Show F-button toolbar") {
		public void changed(boolean val) {
		    ui.mnu.functionbar.visible = val;
		    Config.setWindowOpt(ui.mnu.functionbar.name, val);
		}
	    }).a = ui.mnu.functionbar.visible;
	    
	    (new CheckBox(new Coord(220, 375), tab, "Show numpad toolbar") {
		public void changed(boolean val) {
		    ui.mnu.numpadbar.visible = val;
		    Config.setWindowOpt(ui.mnu.numpadbar.name, val);
		}
	    }).a = ui.mnu.numpadbar.visible;
	    
	    (new CheckBox(new Coord(220, 410), tab, "Highlight combat skills") {
		public void changed(boolean val) {
		    Config.highlightSkills = val;
		    Config.saveOptions();
		}
	    }).a = Config.highlightSkills;
		
		(new CheckBox(new Coord(440, 130), tab, "Auto-hearth") {
		public void changed(boolean val) {
		    Config.autohearth = val;
		    Config.saveOptions();
		}
	    }).a = Config.autohearth;
		
		(new CheckBox(new Coord(455, 165), tab, "Unknown") {
			public void changed(boolean val) {
			    Config.hearthunknown = val;
			    Config.saveOptions();
			}
		    }).a = Config.hearthunknown;
	    
		(new CheckBox(new Coord(455, 190), tab, "Red") {
			public void changed(boolean val) {
			    Config.hearthred = val;
			    Config.saveOptions();
			}
		    }).a = Config.hearthred;
		
	    Widget editbox = new Frame(new Coord(310, 30), new Coord(90, 100), tab);
	    new Label(new Coord(20, 10), editbox, "Edit mode:");
	    RadioGroup editmode = new RadioGroup(editbox) {
		public void changed(int btn, String lbl) {
		    Utils.setpref("editmode", lbl.toLowerCase());
		    }};
	    editmode.add("Emacs", new Coord(10, 25));
	    editmode.add("PC", new Coord(10, 50));
	    if(Utils.getpref("editmode", "pc").equals("emacs")) editmode.check("Emacs");
	    else                                                editmode.check("PC");
	}

	{ /* CAMERA TAB */
	    curcam = Utils.getpref("defcam", "border");
	    tab = body.new Tab(new Coord(70, 0), 60, "Camera");

	    new Label(new Coord(10, 40), tab, "Camera type:");
	    final RichTextBox caminfo = new RichTextBox(new Coord(180, 70), new Coord(210, 180), tab, "", foundry);
	    caminfo.bg = new java.awt.Color(0, 0, 0, 64);
	    String dragcam = "\n\n$col[225,200,100,255]{You can drag and recenter with the middle mouse button.}";
	    String fscam = "\n\n$col[225,200,100,255]{Should be used in full-screen mode.}";
	    addinfo("orig",       "The Original",  "The camera centers where you left-click.", null);
	    addinfo("predict",    "The Predictor", "The camera tries to predict where your character is heading - à la Super Mario World - and moves ahead of your character. Works unlike a charm." + dragcam, null);
	    addinfo("border",     "Freestyle",     "You can move around freely within the larger area of the window; the camera only moves along to ensure the character does not reach the edge of the window. Boom chakalak!" + dragcam, null);
	    addinfo("fixed",      "The Fixator",   "The camera is fixed, relative to your character." + dragcam, null);
	    addinfo("kingsquest", "King's Quest",  "The camera is static until your character comes close enough to the edge of the screen, at which point the camera snaps around the edge.", null);
	    addinfo("cake",       "Pan-O-Rama",    "The camera centers at the point between your character and the mouse cursor. It's pantastic!", null);

	    final Tabs cambox = new Tabs(new Coord(100, 60), new Coord(300, 200), tab);
	    Tabs.Tab ctab;
	    /* clicktgt arg */
	    ctab = cambox.new Tab();
	    new Label(new Coord(45, 10), ctab, "Fast");
	    new Label(new Coord(45, 180), ctab, "Slow");
	    new Scrollbar(new Coord(60, 20), 160, ctab, 0, 20) {
		{
		    val = Integer.parseInt(Utils.getpref("clicktgtarg1", "10"));
		    setcamargs("clicktgt", calcarg());
		}
		public boolean mouseup(Coord c, int button) {
		    if (super.mouseup(c, button)) {
			setcamargs(curcam, calcarg());
			setcamera(curcam);
			Utils.setpref("clicktgtarg1", String.valueOf(val));
			return (true);
		    }
		    return (false);
		}
		private String calcarg() {
		    return (String.valueOf(Math.cbrt(Math.cbrt(val / 24.0))));
		}};
	    addinfo("clicktgt", "The Target Seeker", "The camera recenters smoothly where you left-click." + dragcam, ctab);
	    /* fixedcake arg */
	    ctab = cambox.new Tab();
	    new Label(new Coord(45, 10), ctab, "Fast");
	    new Label(new Coord(45, 180), ctab, "Slow");
	    new Scrollbar(new Coord(60, 20), 160, ctab, 0, 20) {
		{
			val = Integer.parseInt(Utils.getpref("fixedcakearg1", "10"));
		    setcamargs("fixedcake", calcarg());
		}
		public boolean mouseup(Coord c, int button) {
		    if (super.mouseup(c, button)) {
			setcamargs(curcam, calcarg());
			setcamera(curcam);
			Utils.setpref("fixedcakearg1", String.valueOf(val));
			return (true);
		    }
		    return (false);
		}
		private String calcarg() {
		    return (String.valueOf(Math.pow(1 - (val / 20.0), 2)));
		}};
	    addinfo("fixedcake", "The Borderizer", "The camera is fixed, relative to your character unless you touch one of the screen's edges with the mouse, in which case the camera peeks in that direction." + dragcam + fscam, ctab);

	    final RadioGroup cameras = new RadioGroup(tab) {
		public void changed(int btn, String lbl) {
		    if (camname2type.containsKey(lbl))
			lbl = camname2type.get(lbl);
		    if (!lbl.equals(curcam)) {
			if (camargs.containsKey(lbl))
			    setcamargs(lbl, camargs.get(lbl));
			setcamera(lbl);
		    }
		    CamInfo inf = caminfomap.get(lbl);
		    if (inf == null) {
			cambox.showtab(null);
			caminfo.settext("");
		    } else {
			cambox.showtab(inf.args);
			    caminfo.settext(String.format("$size[12]{%s}\n\n$col[200,175,150,255]{%s}", inf.name, inf.desc));
		    }
		    }};
	    List<String> clist = new ArrayList<String>();
	    for (String camtype : MapView.camtypes.keySet())
		clist.add(caminfomap.containsKey(camtype) ? caminfomap.get(camtype).name : camtype);
	    Collections.sort(clist, camcomp);
	    int y = 25;
	    for (String camname : clist)
		cameras.add(camname, new Coord(10, y += 25));
	    cameras.check(caminfomap.containsKey(curcam) ? caminfomap.get(curcam).name : curcam);
	    (new CheckBox(new Coord(50, 270), tab, "Allow zooming with mouse wheel") {
		public void changed(boolean val) {
		    Config.zoom = val;
		    Config.saveOptions();
		}
	    }).a = Config.zoom;
	    (new CheckBox(new Coord(50, 300), tab, "Disable camera borders") {
		public void changed(boolean val) {
		    Config.noborders = val;
		    Config.saveOptions();
		}
	    }).a = Config.noborders;
	}

	{ /* AUDIO TAB */
	    tab = body.new Tab(new Coord(140, 0), 60, "Audio");

	    new Label(new Coord(10, 40), tab, "Sound volume:");
	    new Frame(new Coord(10, 65), new Coord(20, 206), tab);
	    new Label(new Coord(210, 40), tab, "Music volume:");
	    new Frame(new Coord(210, 65), new Coord(20, 206), tab);
	    final Label sfxvol = new Label(new Coord(35, 69 + (int)(Config.sfxVol * 1.86)),  tab, String.valueOf(100 - getsfxvol()) + " %");
	    final Label musicvol = new Label(new Coord(235, 69 + (int)(Config.musicVol * 1.86)),  tab, String.valueOf(100 - getsfxvol()) + " %");
	    (new Scrollbar(new Coord(25, 70), 196, tab, 0, 100) {{ val = 100 - Config.sfxVol; }
		public void changed() {
		    //Audio.setvolume((100 - val) / 100.0);
		    Config.sfxVol = 100 - val;
		    sfxvol.c.y = 69 + (int) (val * 1.86);
		    sfxvol.settext(String.valueOf(100 - val) + " %");
		    Config.saveOptions();
		}
		public boolean mousewheel(Coord c, int amount) {
		    val = Utils.clip(val + amount, min, max);
		    changed();
		    return (true);
		}
	    }).changed();
	    (new Scrollbar(new Coord(225, 70), 196, tab, 0, 100) {{ val = 100 - Config.musicVol; }
		public void changed() {
		    //Audio.setvolume((100 - val) / 100.0);
		    Config.musicVol = 100 - val;
		    Music.setVolume(Config.getMusicVolume());
		    musicvol.c.y = 69 + (int) (val * 1.86);
		    musicvol.settext(String.valueOf(100 - val) + " %");
		    Config.saveOptions();
		}
		public boolean mousewheel(Coord c, int amount) {
		    val = Utils.clip(val + amount, min, max);
		    changed();
		    return (true);
		}
	    }).changed();
	    (new CheckBox(new Coord(10, 270), tab, "Sound enabled") {
		public void changed(boolean val) {
		    Config.isSoundOn = val;
		}}).a = Config.isSoundOn;
		
	    (new CheckBox(new Coord(210, 270), tab, "Music enabled") {
		public void changed(boolean val) {
		    Config.isMusicOn = val;
		    Music.setVolume(Config.getMusicVolume());
		}}).a = Config.isMusicOn;
	}

	{ /* HIDE OBJECTS TAB */
	    tab = body.new Tab(new Coord(210, 0), 80, "Hide Objects");

	    String[][] checkboxesList = { { "Walls", "gfx/arch/walls" },
		    { "Gates", "gfx/arch/gates" },
		    { "Wooden Houses", "gfx/arch/cabin" },
		    { "Stone Mansions", "gfx/arch/inn" },
		    { "Plants", "gfx/terobjs/plants" },
		    { "Trees", "gfx/terobjs/trees" },
		    { "Stones", "gfx/terobjs/bumlings" },
		    { "Flavor objects", "flavobjs" },
		    { "Bushes", "gfx/tiles/wald" },
		    { "Thicket", "gfx/tiles/dwald" } };
	    int y = 0;
	    for (final String[] checkbox : checkboxesList) {
		CheckBox chkbox = new CheckBox(new Coord(10, y += 30), tab,
			checkbox[0]) {

		    public void changed(boolean val) {
			if (val) {
			    Config.addhide(checkbox[1]);
			} else {
			    Config.remhide(checkbox[1]);
			}
			Config.saveOptions();
		    }
		};
		chkbox.a = Config.hideObjectList.contains(checkbox[1]);
	    }
	}

	{ /* HIGHLIGHT OPTIONS TAB */
	    tab = body.new Tab(new Coord(300, 0), 80, "Highlight");
	    int i = 1;
	    for (final String group : Config.hlgroups.keySet()) {
		final CheckBox chkbox = new CheckBox(new Coord(20, 30*i), tab, group) {
		    public void changed(boolean val) {
			if (val) {
			    Config.highlightItemList.addAll(Config.hlcgroups.get(group));
			} else {
			    Config.highlightItemList.removeAll(Config.hlgroups.get(group));
			}
			Config.saveOptions();
		    }
		};
		chkbox.a = Config.highlightItemList.containsAll(Config.hlcgroups.get(group));
		
		new IButton(new Coord(1, 30*i + 17), tab, cfgimgu, cfgimgd){
		    private boolean v = false;
		    public void click() {
			if(v){return;}
			v = true;
			SelectorWnd wnd = new SelectorWnd(ui.root, group);
			wnd.setData(Config.hlgroups.get(group), Config.hlcgroups.get(group), new Callback() {
			    
			    @Override
			    public void callback() {
				v = false;
				Config.highlightItemList.removeAll(Config.hlgroups.get(group));
				if(chkbox.a){
				    Config.highlightItemList.addAll(Config.hlcgroups.get(group));
				}
				Config.saveCurrentHighlights();
				Config.saveOptions();
			    }
			});
		    }

		    private Text tooltip = Text.render("Config group");
		    @Override
		    public Object tooltip(Coord c, boolean again) {
			return checkhit(c)?tooltip:null;
		    }
		};
		
		i++;
	    }
	    
	    CheckBox chkbox = new CheckBox(new Coord(150, 30), tab, "Don't scale minimap icons") {
		public void changed(boolean val) {
		    Config.dontScaleMMIcons = val;
		    Config.saveOptions();
		}
	    };
	    chkbox.a = Config.dontScaleMMIcons;
	    chkbox = new CheckBox(new Coord(150, 60), tab, "Show view distance") {
		public void changed(boolean val) {
		    Config.showViewDistance = val;
		    Config.saveOptions();
		}
	    };
	    chkbox.a = Config.showViewDistance;
	}

	new Frame(new Coord(-10, 20), new Coord(550, 430), this);
	String last = Utils.getpref("optwndtab", "");
	for (Tabs.Tab t : body.tabs) {
	    if (t.btn.text.text.equals(last))
		body.showtab(t);
	}
    }

    private void setcamera(String camtype) {
	curcam = camtype;
	Utils.setpref("defcam", curcam);
	String[] args = camargs.get(curcam);
	if(args == null) args = new String[0];

	MapView mv = ui.mainview;
	if (mv != null) {
	    if     (curcam.equals("clicktgt"))   mv.cam = new MapView.OrigCam2(args);
	    else if(curcam.equals("fixedcake"))  mv.cam = new MapView.FixedCakeCam(args);
	    else {
		try {
		    mv.cam = MapView.camtypes.get(curcam).newInstance();
		} catch (InstantiationException e) {
		} catch(IllegalAccessException e) {}
	    }
	}
    }

    private void setcamargs(String camtype, String... args) {
	camargs.put(camtype, args);
	if (args.length > 0 && curcam.equals(camtype))
	    Utils.setprefb("camargs", Utils.serialize(args));
    }

    private int getsfxvol() {
	return ((int) (100 - Double.parseDouble(Utils.getpref("sfxvol", "1.0")) * 100));
    }

    private void addinfo(String camtype, String title, String text, Tabs.Tab args) {
	caminfomap.put(camtype, new CamInfo(title, text, args));
	camname2type.put(title, camtype);
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if ((sender == cbtn) || (sender == fbtn))
	    super.wdgmsg(sender, msg, args);
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
