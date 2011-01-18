package ender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleTranslator {
    
    static public String apikey = "API-KEY";
    static public String apiurl = "https://www.googleapis.com/language/translate/v2?key=";
    static public String lang = "en";
    
    public static String translate(String str) {
	String res = "";
	URL url = url(str);
	if(url == null)
	    return str;
	try {
	    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	    String tmp;
	    while((tmp = in.readLine())!=null)
		res += tmp;
	    try {
		JSONObject o = new JSONObject(res);
		res = o.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
	    } catch (JSONException e) {
		return str;
	    }
	} catch (IOException e) {
	    return str;
	}
	
	return res;
    }
    
    private static URL url(String str) {
	try {
	    str = URLEncoder.encode(str, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    return null;
	}
	URL url = null;
	try {
	    url = new URL(apiurl + apikey + "&target=" + lang + "&q=" + str);
	} catch (MalformedURLException e) {
	    return null;
	}
	return url;
    }
}
