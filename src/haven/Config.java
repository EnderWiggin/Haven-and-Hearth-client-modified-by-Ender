package haven;

import java.net.URL;
import java.io.PrintStream;
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
    
    private static void usage(PrintStream out) {
	out.println("usage: haven.jar [-hdf] [-r RESDIR] [-U RESURL] [-A AUTHSERV] [SERVER]");
    }

    public static void cmdline(String[] args) {
	PosixArgs opt = PosixArgs.getopt(args, "hdU:fr:A:");
	if(opt == null) {
	    usage(System.err);
	    System.exit(1);
	}
	for(char c : opt.parsed()) {
	    switch(c) {
	    case 'h':
		usage(System.out);
		System.exit(0);
		break;
	    case 'd':
		dbtext = true;
		break;
	    case 'f':
		fullscreen = true;
		break;
	    case 'r':
		resdir = opt.arg;
		break;
	    case 'A':
		authserv = opt.arg;
		break;
	    case 'U':
		try {
		    resurl = new URL(opt.arg);
		} catch(java.net.MalformedURLException e) {
		    System.err.println(e);
		    System.exit(1);
		}
		break;
	    }
	}
	if(opt.rest.length > 0)
	    defserv = opt.rest[0];
    }
}
