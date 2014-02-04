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

import static haven.Utils.getprop;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ender.CurioInfo;
import ender.GoogleTranslator;
import ender.HLInfo;
import ender.SkillAvailability;
import ender.SkillAvailability.Combat;

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
    public static Set<String> hideObjectList;
    public static Set<String> highlightItemList;
    public static Map<String, HLInfo> hlcfg = new HashMap<String, HLInfo>();
    public static Map<String, Set<String>> hlgroups = new HashMap<String, Set<String>>();
    public static Map<String, Set<String>> hlcgroups = new HashMap<String, Set<String>>();
    public static HashMap<Pattern, String> smileys;
    public static boolean nightvision;
    public static String currentCharName;
    public static String currentVersion;
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
    public static boolean showOtherNames;
    public static boolean fastFlowerAnim;
    public static boolean sshot_compress;
    public static boolean sshot_noui;
    public static boolean sshot_nonames;
    public static boolean newclaim;
    public static boolean showq;
    public static boolean showpath;
    public static Map<String, Map<String, Float>> FEPMap = new HashMap<String, Map<String, Float>>();
    public static Map<String, CurioInfo> curios = new HashMap<String, CurioInfo>();
    public static Map<String, SkillAvailability> skills;
    public static Map<String, String> crafts = new HashMap<String, String>();
    public static Map<String, String> beasts = new HashMap<String, String>();
    //public static
    public static boolean highlightSkills;
    public static boolean fps = false;
    public static boolean TEST = false;
    public static boolean simplemap = false;
    public static boolean dontScaleMMIcons = true;
    public static boolean radar;
    public static boolean showViewDistance;
    public static boolean autohearth;
    public static boolean hearthunknown;
    public static boolean hearthred;
    public static boolean muteChat = false;
    public static boolean showgobpath;
    public static boolean showothergobpath = true;
	public static String hhc="255,0,0,128";

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
	    hideObjectList = Collections.synchronizedSet(new HashSet<String>());
	    highlightItemList = Collections.synchronizedSet(new HashSet<String>());
	    loadOptions();
	    loadWindowOptions();
	    loadSmileys();
	    loadFEP();
	    loadCurios();
	    loadSkills();
	    loadCraft();
	    loadHighlight();
	    loadCurrentHighlight();
	    loadBeasts();
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

    public static void saveCurrentHighlights(){
	try {
	    JSONObject cfg = new JSONObject();
	    for(String group : hlcgroups.keySet()){
		cfg.put(group, hlcgroups.get(group));
	    }
	    try {
		FileWriter fw = new FileWriter("highlight.cfg");
		cfg.write(fw);
		fw.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	} catch (JSONException e) {
	    e.printStackTrace();
	}
    }

    private static void loadBeasts() {
	//bear
	String pat = "kritter/bear";
	HLInfo inf = new HLInfo(pat, "mmap/bear");
	Color col = new Color(0xff797c);
	inf.setColor(col);
	beasts.put(pat, "Bear");
	hlcfg.put(pat, inf);
	//boar
	pat = "kritter/boar";
	inf = new HLInfo(pat, "mmap/boar");
	inf.setColor(col);
	beasts.put(pat, "Boar");
	hlcfg.put(pat, inf);
	//deer
	pat = "kritter/deer";
	inf = new HLInfo(pat, "mmap/deer");
	inf.setColor(new Color(0x7BAF8E));
	beasts.put(pat, "Deer");
	hlcfg.put(pat, inf);
	//fox
	pat = "kritter/fox";
	inf = new HLInfo(pat, "mmap/fox");
	inf.setColor(new Color(0xAF8E5B));
	beasts.put(pat, "Fox");
	hlcfg.put(pat, inf);
	//rabbit
	pat = "kritter/hare";
	inf = new HLInfo(pat, "mmap/rabbit");
	inf.setColor(new Color(0x8E8E8E));
	beasts.put(pat, "Rabbit");
	hlcfg.put(pat, inf);
    }

    private static void loadHighlight() {
	try {
	    FileInputStream fstream;
	    fstream = new FileInputStream("highlight.conf");
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
	    String data = "";
	    String strLine;
	    while ((strLine = br.readLine()) != null)   {
		data += strLine;
	    }
	    try {
		JSONObject cfg = new JSONObject(data);
		Iterator<String> keys = cfg.keys();
		while(keys.hasNext()){
		    String key = keys.next();
		    Set<String> group = new HashSet<String>();
		    Set<String> group2 = new HashSet<String>();
		    hlgroups.put(key, group);
		    hlcgroups.put(key, group2);
		    JSONArray arr = cfg.getJSONArray(key);
		    for(int i=0; i<arr.length(); i++){
			JSONObject o = arr.getJSONObject(i);
			String name = o.getString("name");
			String icon = null;
			if(!o.isNull("icon")){
			    icon = o.getString("icon");
			}
			HLInfo inf = new HLInfo(name, icon);
			if(!o.isNull("color")){
			    inf.setColor(new Color(Integer.parseInt(o.getString("color"), 16)));
			}
			hlcfg.put(name, inf);
			group.add(name);
			group2.add(name);
		    }
		}

	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	    br.close();
	    fstream.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}
    }

    private static void loadCurrentHighlight() {
	try {
	    FileInputStream fstream;
	    fstream = new FileInputStream("highlight.cfg");
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
	    String data = "";
	    String strLine;
	    while ((strLine = br.readLine()) != null)   {
		data += strLine;
	    }
	    try {
		JSONObject cfg = new JSONObject(data);
		Iterator<String> keys = cfg.keys();
		while(keys.hasNext()){
		    String key = keys.next();
		    Set<String> group = new HashSet<String>();
		    hlcgroups.put(key, group);
		    JSONArray arr = cfg.getJSONArray(key);
		    for(int i=0; i<arr.length(); i++){
			String name = arr.getString(i);
			group.add(name);
		    }
		}

	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	    br.close();
	    fstream.close();
	} catch (FileNotFoundException e) {
	    //e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static void loadCraft() {
	try {
	    FileInputStream fstream;
	    fstream = new FileInputStream("craft.conf");
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
	    String strLine;
	    while ((strLine = br.readLine()) != null)   {
		String [] tmp = strLine.split("=");
		String name = tmp[0].toLowerCase(), resources = tmp[1].replace("\\n", "\n");
		crafts.put(name, resources);
	    }
	    br.close();
	    fstream.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}

    }

    private static void loadSkills() {
	skills = new HashMap<String, SkillAvailability>();

	//Attacks
	skills.put("paginae/atk/knockteeth", new Combat(6));
	skills.put("paginae/atk/axe", new Combat(4));
	skills.put("paginae/atk/cleave", new Combat(8));
	skills.put("paginae/atk/sting", new Combat(2));
	skills.put("paginae/atk/strangle", new Combat().maxINT(3));
	skills.put("paginae/atk/valstr", new Combat(6));

	//Moves
	skills.put("paginae/atk/feignflight", new Combat().maxDEF(10));
	skills.put("paginae/atk/flex", new Combat(6));
	skills.put("paginae/atk/padv", new Combat().minBAL(3));
	skills.put("paginae/atk/seize", new Combat().minINT(5).minATK(75));
	skills.put("paginae/atk/throwsand", new Combat(1));

	//Special Moves
	skills.put("paginae/atk/roar", new Combat(14).minINT(10));
	skills.put("paginae/atk/bloodshot", new Combat(2));
	skills.put("paginae/atk/skuld", new Combat(10));
	skills.put("paginae/atk/oppknock", new Combat(5));
	skills.put("paginae/atk/sidestep", new Combat(4));
	skills.put("paginae/atk/sternorder", new Combat(5));
	skills.put("paginae/atk/bee", new Combat(6));
	skills.put("paginae/atk/toarms", new Combat(3));

	skills.put("paginae/atk/quell", new Combat(2).maxINT(0).minBAL(3));

    }

    private static void loadCurios() {
	try {
	    FileInputStream fstream;
	    fstream = new FileInputStream("curio.conf");
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
	    String strLine;
	    while ((strLine = br.readLine()) != null)   {
		CurioInfo curio = new CurioInfo();
		String [] tmp = strLine.split(":");
		String name = tmp[0].toLowerCase();
		curio.LP = Integer.parseInt(tmp[1]);
		curio.time = (int) (60*Float.parseFloat(tmp[2]));
		curio.weight = Integer.parseInt(tmp[3]);
		curios.put(name, curio);
	    }
	    br.close();
	    fstream.close();
	} catch (Exception e) {}
    }

    private static void loadFEP() {
	try {
	    FileInputStream fstream;
	    fstream = new FileInputStream("fep.conf");
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
	    String strLine;
	    while ((strLine = br.readLine()) != null)   {
		Map<String, Float> fep = new HashMap<String, Float>();
		String [] tmp = strLine.split("=");
		String name;
		name = tmp[0].toLowerCase();
		if(name.charAt(0)=='@'){
		    name = name.substring(1);
		    fep.put("isItem",(float) 1.0);
		}
		tmp = tmp[1].split(" ");
		for(String itm : tmp){
		    String tmp2[] = itm.split(":");
		    fep.put(tmp2[0], Float.valueOf(tmp2[1]).floatValue());
		}
		FEPMap.put(name, fep);
	    }
	    br.close();
	    fstream.close();
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	}

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
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
	    String strLine;
	    while ((strLine = br.readLine()) != null)   {
		String [] tmp = strLine.split("\t");
		String smile, res;
		smile = tmp[0];
		res = "\\$img\\[smiley\\/"+tmp[1]+"\\]";
		smileys.put(Pattern.compile(smile, Pattern.CASE_INSENSITIVE|Pattern.LITERAL), res);
	    }
	    br.close();
	    fstream.close();
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
            try {
		inputFile.createNewFile();
	    } catch (IOException e) {
		return;
	    }
        }
        try {
            options.load(new FileInputStream("haven.conf"));
        }
        catch (IOException e) {
            System.out.println(e);
        }
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
        showOtherNames = options.getProperty("showOtherNames", "false").equals("true");
        showBeast = options.getProperty("showBeast", "false").equals("true");
        showRadius = options.getProperty("showRadius", "false").equals("true");
        showHidden = options.getProperty("showHidden", "false").equals("true");
        simple_plants = options.getProperty("simple_plants", "false").equals("true");
        fastFlowerAnim = options.getProperty("fastFlowerAnim", "false").equals("true");
        sshot_compress = options.getProperty("sshot_compress", "false").equals("true");
        sshot_noui = options.getProperty("sshot_noui", "false").equals("true");
        sshot_nonames = options.getProperty("sshot_nonames", "false").equals("true");
        newclaim = options.getProperty("newclaim", "true").equals("true");
        showq = options.getProperty("showq", "true").equals("true");
        showpath = options.getProperty("showpath", "false").equals("true");
        showgobpath = options.getProperty("showgobpath", "false").equals("true");
        showothergobpath = options.getProperty("showothergobpath", "false").equals("true");
        highlightSkills = options.getProperty("highlightSkills", "false").equals("true");
        dontScaleMMIcons = options.getProperty("dontScaleMMIcons", "false").equals("true");
        radar = options.getProperty("radar", "true").equals("true");
        showViewDistance = options.getProperty("showViewDistance", "false").equals("true");
        sfxVol = Integer.parseInt(options.getProperty("sfx_vol", "100"));
        musicVol = Integer.parseInt(options.getProperty("music_vol", "100"));
        currentVersion = options.getProperty("version", "");
        autohearth = options.getProperty("autohearth", "false").equals("true");
        hearthunknown = options.getProperty("hearthunknown", "false").equals("true");
        hearthred = options.getProperty("hearthred", "false").equals("true");
        hideObjectList.clear();
        String hideObjects = options.getProperty("hideObjects", "");
        if (!hideObjects.isEmpty()) {
            for (String objectName : hideObjects.split(",")) {
                if (!objectName.isEmpty()) {
                    hideObjectList.add(objectName);
                }
            }
        }
        String highlightObjects = options.getProperty("highlightObjects", "");
        if (!highlightObjects.isEmpty()) {
            for (String objectName : highlightObjects.split(",")) {
                if (!objectName.isEmpty()) {
                    highlightItemList.add(objectName);
                }
            }
        }
        Resource.checkhide();
        timestamp = options.getProperty("timestamp","false").equals("true");
        hhc = options.getProperty("hhc", "255,0,0,128");
    }

    public static synchronized void setWindowOpt(String key, String value) {
	synchronized (window_props) {
	    String prev_val =window_props.getProperty(key);
	    if((prev_val != null)&&prev_val.equals(value))
		return;
	    window_props.setProperty(key, value);
	}
	saveWindowOpt();
    }

    public static synchronized void setWindowOpt(String key, Boolean value) {
	setWindowOpt(key, value?"true":"false");
    }

    public static void saveWindowOpt() {
	synchronized (window_props) {
	    try {
		window_props.store(new FileOutputStream("windows.conf"), "Window config options");
	    } catch (IOException e) {
		System.out.println(e);
	    }
	}
    }

    public static void addhide(String str){
	hideObjectList.add(str);
	Resource.checkhide();
    }

    public static void remhide(String str){
	hideObjectList.remove(str);
	Resource.checkhide();
    }

    public static void saveOptions() {
        String hideObjects = "";
        for (String objectName : hideObjectList) {
            hideObjects += objectName+",";
        }
        String highlightObjects = "";
        for (String objectName : highlightItemList) {
            highlightObjects += objectName+",";
        }
        options.setProperty("hideObjects", hideObjects);
        options.setProperty("highlightObjects", highlightObjects);
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
        options.setProperty("showOtherNames", showOtherNames?"true":"false");
        options.setProperty("showBeast", showBeast?"true":"false");
        options.setProperty("showRadius", showRadius?"true":"false");
        options.setProperty("showHidden", showHidden?"true":"false");
        options.setProperty("simple_plants", simple_plants?"true":"false");
        options.setProperty("fastFlowerAnim", fastFlowerAnim?"true":"false");
        options.setProperty("sshot_compress", sshot_compress?"true":"false");
        options.setProperty("sshot_noui", sshot_noui?"true":"false");
        options.setProperty("sshot_nonames", sshot_nonames?"true":"false");
        options.setProperty("newclaim", newclaim?"true":"false");
        options.setProperty("showq", showq?"true":"false");
        options.setProperty("showpath", showpath?"true":"false");
        options.setProperty("showgobpath", showgobpath?"true":"false");
        options.setProperty("showothergobpath", showothergobpath?"true":"false");
        options.setProperty("highlightSkills", highlightSkills?"true":"false");
        options.setProperty("dontScaleMMIcons", dontScaleMMIcons?"true":"false");
        options.setProperty("radar", radar?"true":"false");
        options.setProperty("autohearth", autohearth?"true":"false");
        options.setProperty("hearthunknown", hearthunknown?"true":"false");
        options.setProperty("hearthred", hearthred?"true":"false");
        options.setProperty("showViewDistance", showViewDistance?"true":"false");
        options.setProperty("version", currentVersion);
        options.setProperty("hhc", hhc);

        try {
            options.store(new FileOutputStream("haven.conf"), "Custom config options");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
