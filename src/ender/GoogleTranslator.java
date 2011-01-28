package ender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleTranslator {
    
    static public String apikey = "API-KEY";
    static public String apiurl = "https://www.googleapis.com/language/translate/v2?key=";
    static public String lang = "en";
    static public boolean turnedon = false;
    protected static final String ENCODING = "UTF-8";
    
    public static String translate(String str) {
	if(!turnedon)
	    return str;
	String res = "";
	URL url = url(str);
	if(url == null)
	    return str;
	try {
	    final HttpURLConnection uc = (HttpURLConnection) url
		    .openConnection();
	    uc.setRequestMethod("GET");
	    uc.setDoOutput(true);
	    try {
		String result;
		try {
		    result = inputStreamToString(uc.getInputStream());
		} catch (Exception e) {
		    return str;
		}
		JSONObject o = new JSONObject(result);
		res = o.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
		str = res;
		res = o.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("detectedSourceLanguage");
		res = "[" + res + "] " + str;
	    } catch (JSONException e) {
		return str;
	    } finally { // http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
		uc.getInputStream().close();
		if (uc.getErrorStream() != null) {
		    uc.getErrorStream().close();
		}
	    }
	} catch (IOException e) {
	    return str;
	}
	
	return res;
    }
    
    private static String inputStreamToString(final InputStream inputStream) throws Exception {
    	final StringBuilder outputBuilder = new StringBuilder();
    	
    	try {
    		String string;
    		if (inputStream != null) {
    			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
    			while (null != (string = reader.readLine())) {
    				outputBuilder.append(string).append('\n');
    			}
    		}
    	} catch (Exception ex) {
    		throw new Exception("[google-api-translate-java] Error reading translation stream.", ex);
    	}
    	
    	return outputBuilder.toString();
    }
    
    private static URL url(String str) {
	try {
	    str = URLEncoder.encode(str, ENCODING);
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
