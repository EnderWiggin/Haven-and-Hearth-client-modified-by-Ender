package haven;

import java.net.URL;
import static haven.Utils.getprop;

public class Config {
    public static byte[] authck;
    public static String authuser;
    public static String authserv;
    public static String defserv;
    public static URL resurl;
    public static boolean fullscreen;
    public static boolean dbtext;
    public static boolean bounddb;
    public static boolean nolocalres;
    public static String resdir;
    public static boolean nopreload;
    public static String loadwaited, allused;
    
    static {
	try {
	    String p;
	    if((p = getprop("haven.authck", null)) != null)
		authck = Utils.hex2byte(p);
	    authuser = getprop("haven.authuser", null);
	    authserv = getprop("haven.authserv", null);
	    defserv = getprop("haven.defserv", null);
	    if(!(p = getprop("haven.resurl", "http://www.havenandhearth.com/res/")).equals(""))
		resurl = new URL(p);
	    fullscreen = getprop("haven.fullscreen", "off").equals("on");
	    loadwaited = getprop("haven.loadwaited", null);
	    allused = getprop("haven.allused", null);
	    dbtext = getprop("haven.dbtext", "off").equals("on");
	    bounddb = getprop("haven.bounddb", "off").equals("on");
	    nolocalres = getprop("haven.nolocalres", "").equals("yesimsure");
	    resdir = getprop("haven.resdir", null);
	    nopreload = getprop("haven.nopreload", "no").equals("yes");
	} catch(java.net.MalformedURLException e) {
	    throw(new RuntimeException(e));
	}
    }
    
    public static void cmdline(String[] args) {
	
    }
}
