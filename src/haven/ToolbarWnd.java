package haven;

import haven.Resource.Image;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class ToolbarWnd extends Window implements DTarget, DropTarget {
    private static final Color pressedColor = new Color(196, 196, 196, 196);
    public final static Tex bg = Resource.loadtex("gfx/hud/invsq");
    private static final int BELTS_NUM = 15;
    private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
    private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
    private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
    private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
    @SuppressWarnings("unchecked")
    private static final Indir<Resource>[] defbelt = new Indir[10];
    public final static Coord bgsz = bg.sz().add(-1, -1);
    private static final Properties beltsConfig = new Properties();
    private Coord gsz, off, beltNumC;
    public Slot pressed, dragging, layout[];
    private IButton lockbtn, flipbtn, minus, plus;
    public boolean flipped = false, locked = false;
    public int belt, key;
    private Tex[] nums;
    private static Tex[] beltNums;
    public String name;
    
    public final static RichText.Foundry ttfnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    
    static {
	/* Text rendering is slow, so pre-cache the belt numbers. */
	beltNums = new Tex[BELTS_NUM];
	for(int i = 0; i < BELTS_NUM; i++) {
	    beltNums[i] = new TexI(Utils.outline2(Text.render(Integer.toString(i)).img, Color.BLACK, true));
	}
    }
    
    public ToolbarWnd(Coord c, Widget parent, String name) {
	super( c, Coord.z,  parent, null);
	this.name = name;
	init(1, 10, new Coord(5, 10), KeyEvent.VK_0);
    }
    
    public ToolbarWnd(Coord c, Widget parent, String name, int belt, int key, int sz, Coord off) {
	super( c, Coord.z,  parent, null);
	this.name = name;
	init(belt, sz, off, key);
    }
    
    public ToolbarWnd(Coord c, Widget parent, String name, int belt, int key) {
	super( c, Coord.z,  parent, null);
	this.name = name;
	init(belt, 10, new Coord(5, 10), key);
    }

    private void loadOpts() {
	synchronized (Config.window_props) {
	    if(Config.window_props.getProperty(name+"_locked", "false").equals("true")) {
		locked = true;
	    }
	    if(Config.window_props.getProperty(name+"_flipped", "false").equals("true")) {
		flip();
	    }
	    if(Config.window_props.getProperty(name+"_folded", "false").equals("true")) {
		folded = true;
		checkfold();
	    }
	    visible = Config.window_props.getProperty(name, "true").equals("true");
	    c = new Coord(Config.window_props.getProperty(name+"_pos", c.toString()));
	}
    }
    
    private void init(int belt, int sz, Coord off, int key) {
	gsz = new Coord(1, sz);
	this.off = off;
	fbtn.show();
	mrgn = new Coord(2,18);
	layout = new Slot[sz];
	loadOpts();
	cbtn.visible = false;
	lockbtn = new IButton(Coord.z, this, locked?ilockc:ilocko, locked?ilocko:ilockc, locked?ilockch:ilockoh) {
		
		public void click() {
		    locked = !locked;
		    if(locked) {
			up = ilockc;
			down = ilocko;
			hover = ilockch;
		    } else {
			up = ilocko;
			down = ilockc;
			hover = ilockoh;
		    }
		    Config.setWindowOpt(name+"_locked", locked);
		}
	};
	lockbtn.recthit = true;
	flipbtn = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flip"), Resource.loadimg("gfx/hud/flipo")) {
		public void click() {
		    flip();
		}
	};
	minus = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/charsh/minusup"), Resource.loadimg("gfx/hud/charsh/minusdown")) {
	    public void click() {
		    prevBelt();
		}
	};
	plus = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/charsh/plusup"), Resource.loadimg("gfx/hud/charsh/plusdown")) {
	    public void click() {
		    nextBelt();
		}
	};
	flipbtn.recthit = true;
	loadBelt(belt);
	this.key = key;
	pack();
	/* Text rendering is slow, so pre-cache the hotbar numbers. */
	nums = new Tex[sz];
	for(int i = 0; i < sz; i++) {
	    String slot;
	    if(key == KeyEvent.VK_0){
		slot = Integer.toString(i);
	    } else if(key == KeyEvent.VK_F1){
		slot = "F"+Integer.toString(i+1);
	    } else {
		slot = "N"+Integer.toString(i);
	    }
	    nums[i] = new TexI(Utils.outline2(Text.render(slot).img, Color.BLACK, true));
	}
    }
    
    protected void nextBelt() {
	loadBelt(belt + 2);
    }
    
    protected void prevBelt() {
	loadBelt(belt - 2);
    }
    
    public static void loadBelts() {
	
	String configFileName = "belts_" + Config.currentCharName.replaceAll("[^a-zA-Z()]", "_") + ".conf";
	try {
	    synchronized (beltsConfig) {
		beltsConfig.load(new FileInputStream(configFileName));
	    }
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
    }
    
    protected void loadBelt(int beltNr) {
	belt = beltNr % BELTS_NUM;
	if(belt < 0)
	    belt += BELTS_NUM;
	synchronized (beltsConfig) {
	    for (int slot = 0; slot < layout.length; slot++) {
		String icon = beltsConfig.getProperty("belt_" + belt + "_" + slot, "");
		if (icon.length() > 0) {
		    layout[slot] = new Slot(icon, belt, slot);
		} else {
		    layout[slot] = null;
		}
	    }
	}
    }
    
    public static void saveBelts() {
	synchronized (beltsConfig) {
	    String configFileName = "belts_" + Config.currentCharName.replaceAll("[^a-zA-Z()]", "_") + ".conf";
	    try {
		beltsConfig.store(new FileOutputStream(configFileName), "Belts actions for " + Config.currentCharName);
	    } catch (FileNotFoundException e) {
	    } catch (IOException e) {
	    }
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn)
	    ui.destroy(this);
	Boolean _folded = folded;
	if(sender == fbtn)
	    super.wdgmsg(sender, msg, args);
	if(_folded != folded) {
	    Config.setWindowOpt(name+"_folded", folded);
	}
    }
    
    public void draw(GOut g) {
	super.draw(g);
	if(folded)
	    return;
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		Coord p = getcoord(x, y);
		g.image(bg, p);
		int slot = x+y;
		if(key == KeyEvent.VK_0)
		    slot = (slot + 1) % 10;
		Slot s = layout[x+y];
		Resource btn = (s==null)?null:s.getres();
		if(btn != null) {
		    Image img = btn.layer(Resource.imgc);
		    if(img != null){
			Tex btex = img.tex();
			if(s == pressed) {
			    g.chcolor(pressedColor);
			}
			if(Config.highlightSkills)
			    g.chcolor(btn.getStateColor());
			g.image(btex, p.add(1, 1));
		    } else {
			System.out.println(btn.name);
		    }
		}
		g.aimage(nums[slot], p.add(bg.sz()), 1, 1);
		g.chcolor();
	    }
	}
	g.chcolor();
	Resource res;
	if((dragging != null)&&((res = dragging.getres()) != null)) {
	    final Tex dt = res.layer(Resource.imgc).tex();
	    ui.drawafter(new UI.AfterDraw() {
		    public void draw(GOut g) {
			g.image(dt, ui.mc.add(dt.sz().div(2).inv()));
		    }
		});
	}
	g.aimage(beltNums[belt], beltNumC, 1, 1);
    } 
    
    private Coord getcoord(int x, int y) {
	Coord p = xlate(bgsz.mul(new Coord(x, y)),true);
	if (off.x > 0)
	    if (flipped) {
		p.x += off.y*(x/off.x);
	    } else {
		p.y += off.y*(y/off.x);
	    }
	return p;
    }
    
    public void checkfold() {
	super.checkfold();
	Coord max = new Coord(ssz);
	if((folded)&&(flipped)) {
	    max.x = 0;
	    recalcsz(max);
	}
	placecbtn();
    }
    
    protected void recalcsz(Coord max)
    {
	sz = max.add(wbox.bsz().add(mrgn.mul(2)).add(tlo).add(rbo)).add(-1, -1);
	wsz = sz.sub(tlo).sub(rbo);
	if(folded)
	    if (flipped)
		wsz.x = wsz.x/2;
	    else
		wsz.y = wsz.y/2;
	asz = wsz.sub(wbox.bl.sz()).sub(wbox.br.sz()).sub(mrgn.mul(2));
    }
    
    public void flip() {
	flipped = !flipped;
	gsz = new Coord(gsz.y, gsz.x);
	mrgn = new Coord(mrgn.y, mrgn.x);
	pack();
	Config.setWindowOpt(name+"_flipped", flipped);
    }
    
    protected void placecbtn() {
	cbtn.c = new Coord(wsz.x - 3 - Utils.imgsz(cbtni[0]).x, 3).sub(mrgn).sub(wbox.tloff());
	if(flipped) {
	    fbtn.c = new Coord(cbtn.c.x, wsz.y - 3 - Utils.imgsz(fbtni[0]).y - mrgn.y - wbox.tloff().y);
	    if(lockbtn != null)
		lockbtn.c = new Coord(3 - wbox.tloff().x - mrgn.x, cbtn.c.y );
	    if(flipbtn != null)
		flipbtn.c = new Coord(5 - wbox.tloff().x - mrgn.x, fbtn.c.y);
	    if(plus != null)
		plus.c = cbtn.c.sub(16,0);
	    if(minus != null) {
		minus.c = fbtn.c.sub(16,0);
	    	beltNumC = minus.c.add(plus.c).div(2).add(36, 22);
	    }
	} else {
	    fbtn.c = new Coord(3 - wbox.tloff().x, cbtn.c.y);
	    if(lockbtn != null)
		lockbtn.c = new Coord(fbtn.c.x, wsz.y - 21 - mrgn.y - wbox.tloff().y );
	    if(flipbtn != null)
		flipbtn.c = new Coord(cbtn.c.x - 2, wsz.y - 21 - mrgn.y - wbox.tloff().y);
	    if(plus != null)
		plus.c = flipbtn.c.sub(0, 16);
	    if(minus != null) {
		minus.c = lockbtn.c.sub(0, 16);
	    	beltNumC = minus.c.add(plus.c).div(2).add(20, 38);
	    }
	}
    }
    
    public void pack() {
	ssz = bgsz.mul(gsz);
	if (off.x > 0)
	    if (flipped) {
		ssz.x += off.y*((gsz.x/off.x) - ((gsz.x%off.x == 0)?1:0)) + 16;
	    } else {
		ssz.y += off.y*((gsz.y/off.x) - ((gsz.y%off.x == 0)?1:0)) + 16;
	    }
	checkfold();
	placecbtn();
    }
    
    private Slot bhit(Coord c) {
	int i = index(c);
	if (i >= 0)
	    return (layout[i]);
	else
	    return (null);
    }

    private int index(Coord c) {
	for(int y = 0; y < gsz.y; y++) {
	    for(int x = 0; x < gsz.x; x++) {
		if (c.isect(getcoord(x, y), bgsz))
		    return x+y;
	    }
	}
	return -1;
    }
    
    public boolean mousedown(Coord c, int button) {
	Slot h = bhit(c);
	if (button == 1) {
	    if (h != null) {
		pressed = h;
		ui.grabmouse(this);
	    } else {
		super.mousedown(c, button);
	    }
	} else if((button == 3)&&(!locked)){
	    clearslot(index(c));
	}
	return (true);
    }

    public boolean mouseup(Coord c, int button) {
	Slot h = bhit(c);
	if (button == 1) {
	    if(dragging != null) {
		ui.dropthing(ui.root, ui.mc, dragging.getres());
		dragging = pressed = null;
	    } else if (pressed != null) {
		if (pressed == h)
		    h.use();
		pressed = null;
	    }
	    ui.grabmouse(null);
	}
	if(dm) {
	    Config.setWindowOpt(name+"_pos", this.c.toString());
	}
	super.mouseup(c, button);
	
	return (true);
    }
    
    public void clearslot(int slot){
	if((slot<0)||(slot>=layout.length)){return;}
	Slot s = layout[slot];
	layout[slot] = null;
	setBeltSlot(slot, "");
	if((s != null) && (s.isitem)){
	    ui.slen.wdgmsg("belt", s.slot, 3, ui.modflags());
	}
    }
    
    public void mousemove(Coord c) {
	if ((!locked)&&(dragging == null) && (pressed != null)) {
	    dragging = pressed;
	    int slot = index(c);
	    if(slot >= 0){
		clearslot(slot);
	    }
	    pressed = null;
	} else {
	    super.mousemove(c);
	}
	    
    }
    
    public boolean drop(Coord cc, Coord ul) {
	if(!locked){
	    int s = getbeltslot();
	    if(s<0){
		String msg = "No empty item slots!";
		ui.cons.out.println(msg);
		ui.slen.error(msg);
	    } else {
		int slot = index(cc);
		if(slot >= 0){
		    String val = "@"+s;
		    layout[slot] = new Slot(val, belt, slot);
		    ui.slen.wdgmsg("setbelt", s, 0);
		    setbeltslot(belt, slot, val);
		}
	    }
	}
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
    
    public boolean dropthing(Coord c, Object thing) {
	if ((!locked)&&(thing instanceof Resource)) {
	    int slot = index(c);
	    if(slot < 0){return false;}
	    Resource res = (Resource)thing;
	    setBeltSlot(slot, res.name);
	    layout[slot] = new Slot(res.name, belt, slot );
	    return true;
	}
	return false;
    }
    
    private void setBeltSlot(int slot, String icon) {
	setbeltslot(belt, slot, icon);
    }
    
    private Resource curttr = null;
    private boolean curttl = false;
    private Text curtt = null;
    private long hoverstart;
    public Object tooltip(Coord c, boolean again) {
	Slot slot = bhit(c);
	Resource res = (slot==null)?null:slot.getres();
	long now = System.currentTimeMillis();
	if((res != null) && ((res.layer(Resource.action) != null)||(res.layer(Resource.tooltip) != null))) {
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
    
    private static Text rendertt(Resource res, boolean withpg) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt;
	if(ad != null){
	    tt = ad.name;
	} else {
	    tt = res.layer(Resource.tooltip).t;
	}
	if(withpg && (pg != null)) {
	    tt += "\n\n" + pg.text;
	}
	return(ttfnd.render(tt, 0));
    }
    
    private boolean checkKey(char ch, KeyEvent ev) {
	if(!visible){return false;}
	int code = ev.getKeyCode();
	int slot = code - key;
	boolean alt = ev.isAltDown();
	boolean ctrl = ev.isControlDown();
	if(alt && key == KeyEvent.VK_F1){
	    slot = code - KeyEvent.VK_0;
	    if((slot>0)&&(slot<=5)){
		loadBelt(slot*2);
		return true;
	    }
	} else if (ctrl && key == KeyEvent.VK_0) {
	    slot = code - KeyEvent.VK_0;
	    if((slot>0)&&(slot<=5)){
		slot = ((slot-1)<<1) + 1;
		loadBelt(slot);
		return true;
	    }
	} else	if(!alt && !ctrl && (slot >= 0)&&(slot < gsz.x*gsz.y)) {
	    if(key == KeyEvent.VK_0)
		    slot = (slot == 0)?9:slot-1;
	    Slot h = layout[slot];
	    if(h!=null)
		h.use();
	    return true;
	}
	return false;
    }
    
    public boolean globtype(char ch, KeyEvent ev) {
	if(!checkKey(ch, ev))
	    return(super.globtype(ch, ev));
	else
	    return true;
    }
    
    public boolean type(char key, KeyEvent ev) {
	if(key == 27) {
	    wdgmsg(fbtn, "click");
	    return(true);
	}
	if(!checkKey(key, ev))
	    return(super.type(key, ev));
	else
	    return true;
    }
    
    public void removedef(int slot){
	for(int i=0; i<layout.length; i++){
	    Slot s = layout[i];
	    if((s != null) && s.isitem && (s.slot == slot)){
		clearslot(i);
	    }
	}
    }
    
    public static void setbelt(int slot, Indir<Resource> res){
	synchronized (defbelt) {
	    defbelt[slot] = res;
	}
	if(res == null){
	    MenuGrid mnu = UI.instance.mnu;
	    mnu.digitbar.removedef(slot);
	    mnu.functionbar.removedef(slot);
	    mnu.numpadbar.removedef(slot);
	}
    }
    
    public static Indir<Resource>getbelt(int slot){
	Indir<Resource> res;
	synchronized (defbelt) {
	    res = defbelt[slot];
	}
	return res;
    }
    
    public static int getbeltslot(){
	synchronized (defbelt) {
	    for(int i = 0; i<defbelt.length; i++){
		if(defbelt[i] == null){
		    return i;
		}
	    }
	}
	return -1;
    }
    
    private static void setbeltslot(int belt, int slot, String value){
	synchronized (beltsConfig) {
	    beltsConfig.setProperty("belt_"+belt+"_"+slot, value);
	}
	saveBelts();
    }
    
    public static class Slot {
	public boolean isitem;
	public String action;
	public int slot;
	public Resource res;
	public int belt, ind;
	
	public Slot(String str, int belt, int ind){
	    this.ind = ind;
	    this.belt = belt;
	    if(str.charAt(0) == '@'){
		isitem = true;
		slot = Integer.decode(str.substring(1));
	    } else {
		isitem = false;
		action = str;
		res = Resource.load(action);
	    }
	}
	
	public Resource getres(){
	    if((res == null) && (isitem))
	    {
		Indir<Resource> indir = getbelt(slot);
		if(indir == null){
		    res = null;
		} else {
		    res = indir.get();
		}
	    }
	    return res;
	}
	
	public void use(){
	    UI ui = UI.instance;
	    if(isitem){
		if(slot>=0){
		    ui.slen.wdgmsg("belt", slot, 1, ui.modflags());
		}
	    } else if(ui.mnu != null){
		ui.mnu.use(res);
	    }
	}
    }
}
