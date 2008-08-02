package haven;

public class LoginScreen extends Widget {
	Login cur;
	Text error;
	IButton btn;
	static Text.Foundry textf;
	Tex bg = Resource.loadtex("gfx/loginscr");
	Tex logo = Resource.loadtex("gfx/logo");
	Text progress = null;
	
	static {
		textf = new Text.Foundry(new java.awt.Font("Sans", java.awt.Font.PLAIN, 16));
	}
	
	public LoginScreen(Widget parent) {
		super(Coord.z, new Coord(800, 600), parent);
		setfocustab(true);
		parent.setfocus(this);
		new Img(Coord.z, bg, this);
		new Img(new Coord(420, 215).add(logo.sz().div(2).inv()), logo, this);
		mklogin();
	}

	private static abstract class Login extends Widget {
		private Login(Coord c, Coord sz, Widget parent) {
			super(c, sz, parent);
		}
		
		abstract Object[] data();
		abstract boolean enter();
	}

	private class Pwbox extends Login {
		TextEntry user, pass;
		CheckBox savepass;
		
		private Pwbox(String username, boolean save) {
			super(new Coord(345, 310), new Coord(150, 100), LoginScreen.this);
			setfocustab(true);
			Label lbl;
			lbl = new Label(new Coord(0, 0), this, "User name", textf);
			user = new TextEntry(new Coord(0, 20), new Coord(150, 20), this, username);
			lbl = new Label(new Coord(0, 60), this, "Password", textf);
			pass = new TextEntry(new Coord(0, 80), new Coord(150, 20), this, "");
			pass.pw = true;
			savepass = new CheckBox(new Coord(0, 110), this, "Save identity");
			if(user.text.equals(""))
				setfocus(user);
			else
				setfocus(pass);
		}
		
		Object[] data() {
			return(new Object[] {user.text, pass.text, savepass.a});
		}
		
		boolean enter() {
			if(user.text.equals("")) {
				setfocus(user);
				return(false);
			} else if(pass.text.equals("")) {
				setfocus(pass);
				return(false);
			} else {
				return(true);
			}
		}
	}

	private void mklogin() {
		synchronized(ui) {
			btn = new IButton(new Coord(373, 460), this, Resource.loadimg("gfx/hud/buttons/loginu"), Resource.loadimg("gfx/hud/buttons/logind"));
			progress(null);
		}
	}
	
	private void error(String error) {
		synchronized(ui) {
			if(this.error != null)
				this.error = null;
			if(error != null)
				this.error = textf.render(error, java.awt.Color.RED);
		}
	}
    
	private void progress(String p) {
		synchronized(ui) {
			if(progress != null)
				progress = null;
			if(p != null)
				progress = textf.render(p, java.awt.Color.WHITE);
		}
	}
    
	private void clear() {
		if(cur != null) {
			ui.destroy(cur);
			cur = null;
			ui.destroy(btn);
			btn = null;
		}
		progress(null);
	}
    
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == btn) {
			if(cur.enter())
				super.wdgmsg("login", cur.data());
			return;
		}
		super.wdgmsg(sender, msg, args);
	}
	
	public void uimsg(String msg, Object... args) {
		synchronized(ui) {
			if(msg == "passwd") {
				clear();
				cur = new Pwbox((String)args[0], (Boolean)args[1]);
				mklogin();
			} else if(msg == "error") {
				error((String)args[0]);
			} else if(msg == "prg") {
				error(null);
				clear();
				progress((String)args[0]);
			}
		}
	}
	
	public void draw(GOut g) {
		super.draw(g);
		if(error != null)
			g.image(error.tex(), new Coord(420 - (error.sz().x / 2), 500));
		if(progress != null)
			g.image(progress.tex(), new Coord(420 - (progress.sz().x / 2), 350));
	}
	
	public boolean type(char k, java.awt.event.KeyEvent ev) {
		if(k == 10) {
			if((cur != null) && cur.enter())
				wdgmsg("login", cur.data());
			return(true);
		}
		return(super.type(k, ev));
	}
}
