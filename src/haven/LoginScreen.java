package haven;

public class LoginScreen extends Widget {
	TextEntry user, pass;
	CheckBox savepass;
	Label userl, passl;
	Text error;
	IButton btn;
	int state = 0;
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

    private void mklogin() {
    	synchronized(ui) {
    		userl = new Label(new Coord(345, 310), this, "User name", textf);
    		userl.setcolor(java.awt.Color.WHITE);
    		user = new TextEntry(new Coord(345, 330), new Coord(150, 20), this, "");
    		passl = new Label(new Coord(345, 370), this, "Password", textf);
    		passl.setcolor(java.awt.Color.WHITE);
    		pass = new TextEntry(new Coord(345, 390), new Coord(150, 20), this, "");
    		pass.pw = true;
    		btn = new IButton(new Coord(373, 460), this, Resource.loadimg("gfx/hud/buttons/loginu"), Resource.loadimg("gfx/hud/buttons/logind"));
    		savepass = new CheckBox(new Coord(345, 420), this, "Save password");
    		setfocus(user);
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
    
    private void nologin() {
    	ui.destroy(user);
    	ui.destroy(pass);
    	ui.destroy(btn);
    	ui.destroy(userl);
    	ui.destroy(passl);
    	ui.destroy(savepass);
    	user = null;
    	pass = null;
    	btn = null;
    	userl = null;
    	passl = null;
    	savepass = null;
    	error(null);
    }
    
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == btn) {
			super.wdgmsg("login", user.text, pass.text);
			return;
		} else if(sender == savepass) {
			super.wdgmsg("savepw", savepass.a);
			return;
		}
		super.wdgmsg(sender, msg, args);
	}
	
	public void uimsg(String msg, Object... args) {
		if(msg == "state") {
			int ns = (Integer)args[0];
			if(ns != state) {
				if(ns == 0)
					mklogin();
				else if(ns == 1)
					nologin();
				state = ns;
			}
		} else if(msg == "error") {
			error((String)args[0]);
		} else if(msg == "prg") {
			progress((String)args[0]);
		} else if(msg == "ld") {
			if(state == 0) {
				user.settext((String)args[0]);
				pass.settext((String)args[1]);
				savepass.a = (Boolean)args[2];
				if(user.text.equals(""))
					setfocus(user);
				else if(pass.text.equals(""))
					setfocus(pass);
				else
					setfocus(user);
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
		if((k == 10) && (state == 0)) {
			if(user.text.equals(""))
				setfocus(user);
			else if(pass.text.equals(""))
				setfocus(pass);
			else
				wdgmsg("login", user.text, pass.text);
			return(true);
		}
		return(super.type(k, ev));
	}
}
