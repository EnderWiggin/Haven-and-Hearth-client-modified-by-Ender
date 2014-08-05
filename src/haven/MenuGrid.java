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

import haven.Resource.AButton;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class MenuGrid extends Widget {
    private static final Color pressedColor = new Color(196, 196, 196, 196);
    public final static Tex bg = Resource.loadtex("gfx/hud/invsq");
    public final static Coord bgsz = bg.sz().add(-1, -1);
    public final static Resource next = Resource.load("gfx/hud/sc-next");
    public final static Resource bk = Resource.load("gfx/hud/sc-back");
    public final static RichText.Foundry ttfnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    private static Coord gsz = new Coord(4, 4);
    private Resource cur, pressed, dragging, layout[][] = new Resource[gsz.x][gsz.y];
    private int curoff = 0;
    private Map<Character, Resource> hotmap = new TreeMap<Character, Resource>();
    public ToolbarWnd digitbar;
    public ToolbarWnd functionbar;
    public ToolbarWnd numpadbar;
	public ToolbarWnd qwertypadbar;
	
    static {
	Widget.addtype("scm", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new MenuGrid(c, parent));
		}
	    });
    }
	
    public class PaginaException extends RuntimeException {
	public Resource res;
	
	public PaginaException(Resource r) {
	    super("Invalid pagina: " + r.name);
	    res = r;
	}
    }

    private Resource[] cons(Resource p) {
	Resource[] cp = new Resource[0];
	Resource[] all;
	{
	    Collection<Resource> ta = new HashSet<Resource>();
	    Collection<Resource> open;
	    synchronized(ui.sess.glob.paginae) {
		open = new HashSet<Resource>(ui.sess.glob.paginae);
	    }
	    while(!open.isEmpty()) {
		for(Resource r : open.toArray(cp)) {
		    if(!r.loading) {
			AButton ad = r.layer(Resource.action);
			if(ad == null)
			    throw(new PaginaException(r));
			if((ad.parent != null) && !ta.contains(ad.parent))
			    open.add(ad.parent);
			ta.add(r);
			open.remove(r);
		    }
		}
	    }
	    all = ta.toArray(cp);
	}
	Collection<Resource> tobe = new HashSet<Resource>();
	for(Resource r : all) {
	    if(r.layer(Resource.action).parent == p)
		tobe.add(r);
	}
	return(tobe.toArray(cp));
    }
	
    public MenuGrid(Coord c, Widget parent) {
	super(c, bgsz.mul(gsz).add(1, 1), parent);
	cons(null);
	ui.mnu = this;
	ToolbarWnd.loadBelts();
	digitbar = new ToolbarWnd(new Coord(0,300), ui.root, "toolbar1");
	functionbar = new ToolbarWnd(new Coord(50,300), ui.root, "toolbar2", 2, KeyEvent.VK_F1, 12, new Coord(4, 10));
	numpadbar = new ToolbarWnd(new Coord(100,300), ui.root, "toolbar3", 10, KeyEvent.VK_NUMPAD0){
	    protected void nextBelt(){
		loadBelt((belt+1)%5+10);
	    }
	    protected void prevBelt(){
		loadBelt((belt-1)%5+10);
	    }
	};
	qwertypadbar = new ToolbarWnd(new Coord(150,300), ui.root, "toolbar4", 14, KeyEvent.VK_Q);
	ui.spd.setspeed(Config.speed, true);
    }
	
    private static Comparator<Resource> sorter = new Comparator<Resource>() {
	public int compare(Resource a, Resource b) {
	    AButton aa = a.layer(Resource.action), ab = b.layer(Resource.action);
	    if((aa.ad.length == 0) && (ab.ad.length > 0))
		return(-1);
	    if((aa.ad.length > 0) && (ab.ad.length == 0))
		return(1);
	    return(aa.name.compareTo(ab.name));
	}
    };

    private void updlayout() {
	Resource[] cur = cons(this.cur);
	Arrays.sort(cur, sorter);
	int i;
	hotmap.clear();
	for (i = 0; i< cur.length; i++){
	    Resource.AButton ad = cur[i].layer(Resource.action);
	    if(ad.hk != 0)
		hotmap.put(Character.toUpperCase(ad.hk), cur[i]);
	}
	i = curoff;
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		Resource btn = null;
		if((this.cur != null) && (x == gsz.x - 1) && (y == gsz.y - 1)) {
		    btn = bk;
		} else if((cur.length > ((gsz.x * gsz.y) - 1)) && (x == gsz.x - 2) && (y == gsz.y - 1)) {
		    btn = next;
		} else if(i < cur.length) {
		    btn = cur[i++];
		}
		layout[x][y] = btn;
	    }
	}
    }
    
    private static Text rendertt(Resource res, boolean withpg) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = ad.name;
	int pos = tt.toUpperCase().indexOf(Character.toUpperCase(ad.hk));
	String format = "$col[255,255,0]{%s}";
	if(pos >= 0)
	    tt = tt.substring(0, pos) + String.format(format, tt.charAt(pos)) + tt.substring(pos + 1);
	else if(ad.hk != 0)
	    tt += " [" + String.format(format, ad.hk) + "]";
	if(withpg && (pg != null)) {
	    tt += "\n\n" + pg.text;
	}
	String mats, name = ad.name.toLowerCase();
	if((mats = Config.crafts.get(name)) != null){
	    tt += "\n\n" + mats;
	}
	return(ttfnd.render(tt, 200));
    }

    public void draw(GOut g) {
	updlayout();
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		Coord p = bgsz.mul(new Coord(x, y));
		g.image(bg, p);
		Resource btn = layout[x][y];
		if(btn != null) {
		    Tex btex = btn.layer(Resource.imgc).tex();
		    if(btn == pressed) {
			g.chcolor(pressedColor);
		    }
		    if(Config.highlightSkills)
			g.chcolor(btn.getStateColor());
		    g.image(btex, p.add(1, 1));
		    g.chcolor();
		}
	    }
	}
	if(dragging != null) {
	    final Tex dt = dragging.layer(Resource.imgc).tex();
	    ui.drawafter(new UI.AfterDraw() {
		    public void draw(GOut g) {
			g.image(dt, ui.mc.add(dt.sz().div(2).inv()));
		    }
		});
	}
    }
	
    private Resource curttr = null;
    private boolean curttl = false;
    private Text curtt = null;
    private long hoverstart;
    public Object tooltip(Coord c, boolean again) {
	Resource res = bhit(c);
	long now = System.currentTimeMillis();
	if((res != null) && (res.layer(Resource.action) != null)) {
	    if(!again)
		hoverstart = now;
	    boolean ttl = (now - hoverstart) > 500;
	    if((res != curttr) || (ttl != curttl)) {
		curtt = rendertt(res, ttl);
		curttr = res;
		curttl = ttl;
	    }
	    return(curtt);
	} else {
	    hoverstart = now;
	    return("");
	}
    }

    private Resource bhit(Coord c) {
	Coord bc = c.div(bgsz);
	if((bc.x >= 0) && (bc.y >= 0) && (bc.x < gsz.x) && (bc.y < gsz.y))
	    return(layout[bc.x][bc.y]);
	else
	    return(null);
    }
	
    public boolean mousedown(Coord c, int button) {
	Resource h = bhit(c);
	if((button == 1) && (h != null)) {
	    pressed = h;
	    ui.grabmouse(this);
	}
	return(true);
    }
	
    public void mousemove(Coord c) {
	if((dragging == null) && (pressed != null)) {
	    Resource h = bhit(c);
	    if(h != pressed)
		dragging = pressed;
	}
    }
	
    public void use(Resource r) {
	if(cons(r).length > 0) {
	    cur = r;
	    curoff = 0;
	} else if(r == bk) {
	    cur = cur.layer(Resource.action).parent;
	    curoff = 0;
	} else if(r == next) {
	    if((curoff + 14) >= cons(cur).length)
		curoff = 0;
	    else
		curoff += 14;
	} else {
	    AButton act = r.layer(Resource.action);
	    if(act != null){
		String [] ad = act.ad;
		if((ad == null) || (ad.length < 1)){return;}
		if(ad[0].equals("@")) {
		    usecustom(ad);
		} else {
		    int k = 0;
		    if (ad[0].equals("crime")){k = -1;}
		    if (ad[0].equals("tracking")){k = -2;}
		    if (ad[0].equals("swim")){k = -3;}
		    if(k<0){
			synchronized (ui.sess.glob.buffs) {
			    if(ui.sess.glob.buffs.containsKey(k)){
				ui.sess.glob.buffs.remove(k);
			    } else {
				Buff buff = new Buff(k, r.indir());
				buff.major = true;
				ui.sess.glob.buffs.put(k, buff);
			    }
			}
		    }
			
			for(int i = 0; i < ad.length; i++){ // new
				if(ad[i].contains("atk") ){
					if(!Config.singleAttack && doubleTapAttack(ad)) return;
					if(Config.singleAttack && singleTapAttack(ad) ) return;
				}if(ui.modflags() == 1 && autoLandscape(landscape(ad[i]) ) ){
					return;
				}
			}
			
		    wdgmsg("act", (Object[])ad);
		}
	    } else {
		String str = "Error while using belt item! Looks like inventory item got to be used as menu item. If you know steps to reproduce this - please report.";
		ui.cons.out.println(str);
		ui.slen.error(str);
	    }
	}
    }
    
    private void usecustom(String[] list) {
	if(list[1].equals("radius")) {
	    Config.showRadius = !Config.showRadius;
	    String str = "Radius highlight is turned "+((Config.showRadius)?"ON":"OFF");
	    ui.cons.out.println(str);
	    ui.slen.error(str);
	    Config.saveOptions();
	} else if(list[1].equals("hidden")) {
	    Config.showHidden = !Config.showHidden;
	    String str = "Hidden object highlight is turned "+((Config.showHidden)?"ON":"OFF");
	    ui.cons.out.println(str);
	    ui.slen.error(str);
	    Config.saveOptions();
	} else if(list[1].equals("hide")) {
	    for(int i=2;i<list.length;i++){
		String item = list[i];
		if(Config.hideObjectList.contains(item)){
		    Config.remhide(item);
		} else {
		    Config.addhide(item);
		}
	    }
	} else if(list[1].equals("simple plants")) {
	    Config.simple_plants = !Config.simple_plants;
	    String str = "Simplified plants is turned "+((Config.simple_plants)?"ON":"OFF");
	    ui.cons.out.println(str);
	    ui.slen.error(str);
	    Config.saveOptions();
	} else if(list[1].equals("timers")) {
	    TimerPanel.toggle();
	} else if(list[1].equals("animal")) {
	    Config.showBeast = !Config.showBeast;
	    String str = "Animal highlight is turned "+((Config.showBeast)?"ON":"OFF");
	    ui.cons.out.println(str);
	    ui.slen.error(str);
	    Config.saveOptions();
	} else if(list[1].equals("radar")) {
	    Config.radar = !Config.radar;
	    String str = "Radar is turned "+((Config.radar)?"ON":"OFF");
	    ui.cons.out.println(str);
	    ui.slen.error(str);
	    Config.saveOptions();
	} else if(list[1].equals("study")) {
	    ui.study.toggle();
	} else if(list[1].equals("globalchat")) {
	    IRChatHW.open();
	} else if(list[1].equals("wiki")) {
	    if(ui.wiki == null) {
		new WikiBrowser(MainFrame.getCenterPoint().sub(115, 75), Coord.z, ui.root);
	    } else {
		ui.wiki.wdgmsg(ui.wiki.cbtn, "click");
	    }
	} else if(list[1].equals("pickup")) {
		addons.MainScript.cleanupItems(1000, ui.mainview.gobAtMouse);
	} else if(list[1].equals("msafe")) {
		Config.minerSafety = !Config.minerSafety;
		String str = "Mining safety: "+((Config.minerSafety)?"ON":"OFF");
		ui.cons.out.println(str);
		ui.slen.error(str);
	} else if(list[1].equals("runflask")) {
		Config.pathDrinker = !Config.pathDrinker;
		String str = "Auto drinker: "+((Config.pathDrinker)?"ON":"OFF");
		ui.cons.out.println(str);
		ui.slen.error(str);
		addons.MainScript.flaskScript();
	} else if(list[1].equals("animaltag")) {
		Config.animalTags = !Config.animalTags;
		String str = "Turn animal tags: "+((Config.animalTags)?"ON":"OFF");
		ui.cons.out.println(str);
		ui.slen.error(str);
		Config.saveOptions();
	} else if(list[1].equals("focushide")) {
		if(ui.mainview.gobAtMouse != null){
			String name = ui.mainview.gobAtMouse.resname();
			if(Config.hideObjectList.contains(name)){
				Config.remhide(name);
			} else {
				Config.addhide(name);
			}
			Config.saveOptions();
			String str = "Hide: " + name;
			ui.cons.out.println(str);
			ui.slen.error(str);
			Config.saveOptions();
		}
	}
	use(null);
    }
    
    public boolean mouseup(Coord c, int button) {
	Resource h = bhit(c);
	if(button == 1) {
	    if(dragging != null) {
		ui.dropthing(ui.root, ui.mc, dragging);
		dragging = pressed = null;
	    } else if(pressed != null) {
		if(pressed == h)
		    use(h);
		pressed = null;
	    }
	    ui.grabmouse(null);
	}
	updlayout();
	return(true);
    }
	
    public void uimsg(String msg, Object... args){
	if(msg == "goto") {
	    String res = (String)args[0];
	    if(res.equals(""))
		cur = null;
	    else
		cur = Resource.load(res);
	    curoff = 0;
	}
    }
	
    public boolean globtype(char k, KeyEvent ev){
	if(ev.isAltDown() || ev.isControlDown()){
	    return false;
	}
	if(qwertypadbar.visible && ToolbarWnd.keypadNum((int)Character.toUpperCase(k)) != -1 ){
		return false;
	}
	if((k == 27) && (this.cur != null)) {
	    this.cur = null;
	    curoff = 0;
	    updlayout();
	    return(true);
	} else if((k == 'N') && (layout[gsz.x - 2][gsz.y - 1] == next)) {
	    use(next);
	    return(true);
	}
	Resource r = hotmap.get(Character.toUpperCase(k));
	if(r != null) {
	    use(r);
	    return(true);
	}
	return(false);
    }
	
	///////////
	
	long doubleTapTime = 0; // new
	boolean soakAttack = false;
	
	public static String[] moveAttacks = {
		"thunder",
		"berserk",
		"dash",
		"feignflight",
		"flex",
		"butterfly",
		"jump",
		"advpush",
		"seize",
		"slide",
		"throwsand"
	};
	
	boolean doubleTapAttack(String[] ad){ // new
		Config.runFlaskSuppression = true;
		long tapTime = 400;
		
		//String attackName = getAttackName(ad);
		
		if(System.currentTimeMillis() - doubleTapTime < tapTime){
			//System.out.println("double tapped");
			if(soakAttack) return true;
			
			if(ui.fight != null){
				soakAttack = true;
				ui.fight.attackCurrent(/*attackName*/);
			}
			
			/*for(Widget w = ui.root.child; w != null; w = w.next){
				if(w instanceof Fightview){
					if(!Config.singleAttack) soakAttack = true;
					((Fightview)w).attackCurrent(/*attackName*);
				}
			}*/
			
			//doubleTapTime = 0;
			return true;
		}
		
		soakAttack = false;
		doubleTapTime = System.currentTimeMillis();
		return false;
    }
	
	boolean singleTapAttack(String[] ad){ // new
		Config.runFlaskSuppression = true;
		long tapTime = 400;
		
		if(soakAttack && System.currentTimeMillis() - doubleTapTime < tapTime) return false;
		
		soakAttack = false;
		
		if(ui.modflags() != 1){
			wdgmsg("act", (Object[])ad);
			
			if(ui.fight != null){
				ui.fight.attackCurrent();
			}
			
			doubleTapTime = System.currentTimeMillis();
			soakAttack = getAttackName(ad) != null;
			
			return true;
		}
		
		return false;
    }
	
	String getAttackName(String[] ad){
		String name = null;
		for(int i = 0; i < ad.length; i++){ // new
			if(!ad[i].contains("atk") ){
				name = soakAttackCandidates(ad[i]);
			}
		}
		
		return name;
	}
	
	String soakAttackCandidates(String name){
		for(int i = 0; i < moveAttacks.length; i++){ // new
			if(moveAttacks[i].contains(name) ){
				return name;
			}
		}
		return null;
	}
	
	int landscape(String name){
		if(name.equals("harvest") ){
			return 1;
		}else if(name.equals("stone") ){
			return 2;
		}else if(name.equals("grass") ){
			return 3;
		}else if(name.equals("dirt") ){
			return 4;
		}
		
		return 0;
	}
	
	boolean autoLandscape(int type){
		if(type == 0){
			return false;
		}
		addons.MainScript.m_alType = type;
		Config.autoLand = true;
		
		return true;
	}
}
