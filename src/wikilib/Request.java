package wikilib;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Request {
	public String wiki = "http://ringofbrodgar.com";
	public RequestCallback callback;
	public String result;
	public String search;
	public String title;
	public URL url;
	
	public Request() {
	    super();
	}

	public Request(String reqString) {
	    this();
	    initSearch(reqString);
	}

	public Request(String reqString, RequestCallback reqCallback) {
	    this(reqString);
	    callback = reqCallback;
	}

	public void initSearch(String req) {
	    search = req;
	    try {
		url = new URL(wiki+"/w/index.php?search=" + URLEncoder.encode(search, "UTF-8"));
	    } catch (MalformedURLException e) {
	    } catch (UnsupportedEncodingException e) {
	    }
	}

	public void initPage(String req) {
	    search = req;
	    search.replaceAll(" ", "_");
	    try {
		url = new URL(wiki+"/wiki/"+search);
	    } catch (MalformedURLException e) {}
	}

	public void complete() {
	    if(callback != null)
		callback.run(this);
	}
}
