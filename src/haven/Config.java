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

import java.util.HashMap;
import java.util.HashSet;
import java.io.*;
import java.util.Properties;
import java.util.regex.Pattern;
import java.net.URL;
import java.io.PrintStream;

import ender.GoogleTranslator;
import static haven.Utils.getprop;

public class Config {
    public static byte[] authck;
    public static String authuser;
    public static String authserv;
    public static String defserv;
    public static URL resurl, mapurl;
    public static boolean fullscreen;
    public static boolean dbtext;
    public static boolean bounddb;
    public static boolean profile;
    public static boolean nolocalres;
    public static String resdir;
    public static boolean nopreload;
    public static String loadwaited, allused;
    public static boolean xray;
    public static boolean hide;
    public static boolean grid;
    public static boolean timestamp;
    public static boolean new_chat;
    public static boolean highlight = false;
    public static boolean use_smileys;
    public static boolean zoom;
    public static boolean noborders;
    public static boolean new_minimap;
    public static boolean simple_plants = false;
    public static HashSet<String> hideObjectList;
    public static HashMap<Pattern, String> smileys;
    public static boolean nightvision;
    public static String currentCharName;
    public static Properties options, window_props;
    public static int sfxVol;
    public static int musicVol;
    public static boolean isMusicOn = false;
    public static boolean isSoundOn = false;
    public static boolean showRadius = false;
    public static boolean showHidden = false;
    public static boolean showBeast = false;
    public static boolean showDirection;
    public static boolean showNames;
    public static boolean fastFlowerAnim;
    
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
	    if(!(p = getprop("haven.mapurl", "http://www.havenandhearth.com/mm/")).equals(""))
		mapurl = new URL(p);
	    fullscreen = getprop("haven.fullscreen", "off").equals("on");
	    loadwaited = getprop("haven.loadwaited", null);
	    allused = getprop("haven.allused", null);
	    dbtext = getprop("haven.dbtext", "off").equals("on");
	    bounddb = getprop("haven.bounddb", "off").equals("on");
	    profile = getprop("haven.profile", "off").equals("on");
	    nolocalres = getprop("haven.nolocalres", "").equals("yesimsure");
	    resdir = getprop("haven.resdir", null);
	    nopreload = getprop("haven.nopreload", "no").equals("yes");
	    xray = false;
	    hide = false;
	    grid = false;
	    timestamp = false;
	    nightvision = false;
	    zoom = false;
	    new_minimap = true;
	    GoogleTranslator.lang = "en";
	    GoogleTranslator.turnedon = false;
	    currentCharName = "";
	    options = new Properties();
	    window_props = new Properties();
	    hideObjectList = new HashSet<String>();
	    loadOptions();
	    loadWindowOptions();
	    loadSmileys();
	} catch(java.net.MalformedURLException e) {
	    throw(new RuntimeException(e));
	}
    }
    
    public static String mksmiley(String str){
	synchronized (smileys) {
	    for(Pattern p : Config.smileys.keySet()){
		String res = Config.smileys.get(p);
		str = p.matcher(str).replaceAll(res);
	    }
	}
	return str;
    }
    
    private static void usage(PrintStream out) {
	out.println("usage: haven.jar [-hdPf] [-u USER] [-C HEXCOOKIE] [-r RESDIR] [-U RESURL] [-A AUTHSERV] [SERVER]");
    }

    public static void cmdline(String[] args) {
	PosixArgs opt = PosixArgs.getopt(args, "hdPU:fr:A:u:C:");
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
	    case 'P':
		profile = true;
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
	    case 'u':
		authuser = opt.arg;
		break;
	    case 'C':
		authck = Utils.hex2byte(opt.arg);
		break;
	    }
	}
	if(opt.rest.length > 0)
	    defserv = opt.rest[0];
    }
    
    public static double getSFXVolume()
    {
    	return (double)sfxVol/100;
    }
    
    public static int getMusicVolume()
    {
    	return isMusicOn?musicVol:0;
    }
    
    private static void loadSmileys() {
	smileys = new HashMap<Pattern, String>();
	try {
	    FileInputStream fstream;
	    fstream = new FileInputStream("smileys.conf");
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    while ((strLine = br.readLine()) != null)   {
		String [] tmp = strLine.split("\t");
		String smile, res;
		smile = tmp[0];
		res = "\\$img\\[smiley\\/"+tmp[1]+"\\]";
		smileys.put(Pattern.compile(smile, Pattern.CASE_INSENSITIVE|Pattern.LITERAL), res);
	    }
	    in.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
	
    }
    
    private static void loadWindowOptions() {
	File inputFile = new File("windows.conf");
        if (!inputFile.exists()) {
            return;
        }
        try {
            window_props.load(new FileInputStream(inputFile));
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    private static void loadOptions() {
        File inputFile = new File("haven.conf");
        if (!inputFile.exists()) {
            return;
        }
        try {
            options.load(new FileInputStream("haven.conf"));
        }
        catch (IOException e) {
            System.out.println(e);
        }
        String hideObjects = options.getProperty("hideObjects", "");
        GoogleTranslator.apikey = options.getProperty("GoogleAPIKey", "AIzaSyCuo-ukzI_J5n-inniu2U7729ZfadP16_0");
        zoom = options.getProperty("zoom", "false").equals("true");
        noborders = options.getProperty("noborders", "false").equals("true");
        new_minimap = options.getProperty("new_minimap", "true").equals("true");
        new_chat = options.getProperty("new_chat", "true").equals("true");
        use_smileys = options.getProperty("use_smileys", "true").equals("true");
        isMusicOn = options.getProperty("music_on", "true").equals("true");
        isSoundOn = options.getProperty("sound_on", "true").equals("true");
        showDirection = options.getProperty("show_direction", "true").equals("true");
        showNames = options.getProperty("showNames", "true").equals("true");
        showBeast = options.getProperty("showBeast", "false").equals("true");
        showRadius = options.getProperty("showRadius", "false").equals("true");
        showHidden = options.getProperty("showHidden", "false").equals("true");
        simple_plants = options.getProperty("simple_plants", "false").equals("true");
        fastFlowerAnim = options.getProperty("fastFlowerAnim", "false").equals("true");
        sfxVol = Integer.parseInt(options.getProperty("sfx_vol", "100"));
        musicVol = Integer.parseInt(options.getProperty("music_vol", "100"));
        hideObjectList.clear();
        if (!hideObjects.isEmpty()) {
            for (String objectName : hideObjects.split(",")) {
                if (!objectName.isEmpty()) {
                    hideObjectList.add(objectName);
                }
            }
        }
        timestamp = options.getProperty("timestamp","false").equals("true");
    }

    public static synchronized void setWindowOpt(String key, String value) {
	String prev_val =window_props.getProperty(key); 
	if((prev_val != null)&&prev_val.equals(value))
	    return;
	window_props.setProperty(key, value);
	saveWindowOpt();
    }
    
    public static synchronized void setWindowOpt(String key, Boolean value) {
	setWindowOpt(key, value?"true":"false");
    }
    
    public static void saveWindowOpt() {
	try {
	    window_props.store(new FileOutputStream("windows.conf"), "Window config options");
	} catch (IOException e) {
	    System.out.println(e);
	}
    }
    
    public static void saveOptions() {
        String hideObjects = "";
        for (String objectName : hideObjectList) {
            hideObjects += objectName+",";
        }
        options.setProperty("hideObjects", hideObjects);
        options.setProperty("GoogleAPIKey", GoogleTranslator.apikey);
        options.setProperty("timestamp", (timestamp)?"true":"false");
        options.setProperty("zoom", zoom?"true":"false");
        options.setProperty("noborders", noborders?"true":"false");
        options.setProperty("new_minimap", new_minimap?"true":"false");
        options.setProperty("new_chat", new_chat?"true":"false");
        options.setProperty("use_smileys", use_smileys?"true":"false");
        options.setProperty("sfx_vol", String.valueOf(sfxVol));
        options.setProperty("music_vol", String.valueOf(musicVol));
        options.setProperty("music_on", isMusicOn?"true":"false");
        options.setProperty("sound_on", isSoundOn?"true":"false");
        options.setProperty("show_direction", showDirection?"true":"false");
        options.setProperty("showNames", showNames?"true":"false");
        options.setProperty("showBeast", showBeast?"true":"false");
        options.setProperty("showRadius", showRadius?"true":"false");
        options.setProperty("showHidden", showHidden?"true":"false");
        options.setProperty("simple_plants", simple_plants?"true":"false");
        options.setProperty("fastFlowerAnim", fastFlowerAnim?"true":"false");
        try {
            options.store(new FileOutputStream("haven.conf"), "Custom config options");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
