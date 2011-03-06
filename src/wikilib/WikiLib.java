package wikilib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiLib extends Thread {

    private List<Request> requests;
    private Pattern removeHtml = Pattern.compile("\\<.*?\\>");
    private Pattern findKeywords = Pattern.compile("\\[\\[([\\sa-zA-Z0-9]+)(\\|\\s*([\\sa-zA-Z0-9]+))?\\]\\]");
    private Pattern findSpaces = Pattern.compile("\\s");
    private Pattern findTitle = Pattern.compile("<h1.*>(.*)</h1>");

    public WikiLib() {
	super("WikiLib Thread");
	requests = new ArrayList<Request>();
	start();
    }

    // Thread main process
    @Override
    public void run() {
	while(true) {
	    boolean b;
	    synchronized (requests) {
		b = !requests.isEmpty();;
	    }
	    Request req;
	    while (b) {
		synchronized (requests) {
		    req = requests.remove(0);
		}
		searchPage(req);
		synchronized (requests) {
		    b = !requests.isEmpty();
		}
	    }
	    
	    try {
		sleep(250);
	    } catch (InterruptedException e) {}
	}
    }

    public void search(Request req) {
	synchronized (requests) {
	    requests.add(req);
	}
    }

    private void searchPage(Request req) {
	BufferedReader in;
	try {
	    in = new BufferedReader(new InputStreamReader(req.url.openStream()));
	    String inputLine, content = "";
	    while ((inputLine = in.readLine()) != null)
		content += inputLine;
	    in.close();
	    if (content.indexOf("<div class='searchresults'>") != -1) {
		req.result = formatSearchResults(content);
		req.complete();
	    } else {
		Matcher ma = findTitle.matcher(content);
		ma.find();
		req.title = ma.group(1);
		req.result = formatPage(content);
		req.complete();
	    }
	} catch (IOException e) {
	    req.result = e.toString();
	    req.complete();
	}
    }
    
    private String formatPage(String content) {
	// clean up the contents so less content to go through (faster)
	int s = content.indexOf("<!-- start content -->");
	int f = content.indexOf("<div class=\"printfooter\">");
	content = content.substring(s,f);
	content = removeTables(content);
	content = removeScript(content);
	content = content.replaceAll("\\{", "");
	content = content.replaceAll("\\}", "");
	content = content.replaceAll("<br/>", "\n");
	content = content.replaceAll("</p>", "\n");
	content = formatH2(content);
	content = formatLinks(content);
	content = formatOL(content);
	content = formatUL(content);
	content = this.removeHtml.matcher(content).replaceAll("");
	content = content.replaceAll("&gt;",">");
	content = content.replaceAll("&lt;","<");
	return content;
    }
    
    private String formatSearchResults(String content) {
	String buf = "";
	// clean up the contents so less content to go through (faster)
	content = content.substring(
		content.indexOf("<div class='searchresults'>"),
		content.indexOf("<div class=\"printfooter\">"));
	content = content.replaceAll("\\{", "");
	content = content.replaceAll("\\}", "");
	// Create each search section
	String[] h2 = new String[50];
	int idx = 0, cnt = 0;
	while (content.indexOf("<h2>", idx) != -1) {
	    int h2Start = content.indexOf("<h2>", idx);
	    int h2End = content.indexOf("</h2>", h2Start);
	    h2[cnt] = this.removeHtml.matcher(content.substring(h2Start, h2End)).replaceAll("");
	    idx = h2End;
	    cnt++;
	}
	// Get content for each section
	int idxHeader = 0;
	cnt = 0;
	while (content.indexOf("<ul class='mw-search-results'>", idxHeader) != -1) {
	    // Alot of abusing of indexOf cause faster than compiled regex
	    buf += "$size[14]{$b{"+h2[cnt]+":}}\n\n";
	    int ulStart = content.indexOf("<ul class='mw-search-results'>", idxHeader);
	    idxHeader = ulStart + 1;
	    String header = content.substring(ulStart, content.indexOf("</ul>", ulStart));

	    // for each search section create search items
	    int idxItem = header.indexOf("<li>");
	    while (header.indexOf("<li>", idxItem) != -1) {
		int liStart = header.indexOf("<li>", idxItem);
		int liEnd = header.indexOf("</li>", liStart);

		String itemcnt = header.substring(liStart, liEnd);
		int descStart = itemcnt.indexOf("<div");
		int descEnd = itemcnt.indexOf("</div");
		
		String desc = itemcnt.substring(descStart, descEnd);
		desc = this.removeHtml.matcher(desc).replaceAll("");
		desc = formatKeywords(desc);
		String link = itemcnt.substring(itemcnt.indexOf("<a"),
			itemcnt.indexOf("</a>"));
		
		int linkidx = link.indexOf("href=\"");
		String title = this.removeHtml.matcher(link).replaceAll("");
		link = link.substring(linkidx, link.indexOf("\"", linkidx + 6)).replace("href=\"", "");
		
		buf += "$b{$u{$col[0,0,192]{$a["+ link + "]{" + title + "}}}}\n";
		buf += desc + "\n\n";
		
		idxItem = liEnd + 1;
	    }
	    cnt++;
	}
	return buf;
    }
    
    private String removeTables(String content) {
	String buf = "";
	int idx = 0;
	int aStart = 0;
	int aEnd = -8;
	while (content.indexOf("<table", idx) != -1) {
	    aStart = content.indexOf("<table", idx);
	    buf += content.substring(aEnd+8, aStart);
	    aEnd = content.indexOf("</table>", aStart);
	    idx = aEnd;
	}
	buf += content.substring(aEnd+8);
	return buf;
    }
    
    private String removeScript(String content) {
	String buf = "";
	int idx = 0;
	int aStart = 0;
	int aEnd = -9;
	while (content.indexOf("<script", idx) != -1) {
	    aStart = content.indexOf("<script", idx);
	    buf += content.substring(aEnd+9, aStart);
	    aEnd = content.indexOf("</script>", aStart);
	    idx = aEnd;
	}
	buf += content.substring(aEnd+9);
	return buf;
    }
    
    private String formatLinks(String content) {
	String buf = "";
	int idx = 0;
	int aStart = 0;
	int aEnd = -4;
	while (content.indexOf("<a", idx) != -1) {
	    aStart = content.indexOf("<a", idx);
	    buf += content.substring(aEnd+4, aStart);
	    aEnd = content.indexOf("</a>", aStart);
	    
	    String link = content.substring(aStart, aEnd);

	    int linkidx = link.indexOf("href=\"");
	    String title = this.removeHtml.matcher(link).replaceAll("");
	    link = link.substring(linkidx, link.indexOf("\"", linkidx + 6)).replace("href=\"", "");

	    buf += "$u{$col[0,0,192]{$a["+ link + "]{" + title + "}}}";

	    idx = aEnd;
	}
	buf += content.substring(aEnd+4);
	return buf;
    }
    
    private String formatOL(String content) {
	String buf = "";
	int idx = 0;
	int aStart = 0;
	int aEnd = -5;
	while (content.indexOf("<ol>", idx) != -1) {
	    aStart = content.indexOf("<ol>", idx);
	    buf += content.substring(aEnd+5, aStart);
	    aEnd = content.indexOf("</ol>", aStart);
	    
	    buf += formatLI(content.substring(aStart+4, aEnd), true);

	    idx = aEnd;
	}
	buf += content.substring(aEnd+5);
	return buf;
    }
    
    private String formatUL(String content) {
	String buf = "";
	int idx = 0;
	int aStart = 0;
	int aEnd = -5;
	while (content.indexOf("<ul>", idx) != -1) {
	    aStart = content.indexOf("<ul>", idx);
	    buf += content.substring(aEnd+5, aStart);
	    aEnd = content.indexOf("</ul>", aStart);
	    
	    buf += formatLI(content.substring(aStart+4, aEnd), false);

	    idx = aEnd;
	}
	buf += content.substring(aEnd+5);
	return buf;
    }
    
    private String formatLI(String content, boolean ordered) {
	String buf = "";
	int idx = 0;
	int aStart = 0;
	int aEnd = -5;
	int i=0;
	while (content.indexOf("<li>", idx) != -1) {
	    i++;
	    aStart = content.indexOf("<li>", idx);
	    buf += content.substring(aEnd+5, aStart);
	    aEnd = content.indexOf("</li>", aStart);
	    
	    buf += "$size[11]{$b{"+(ordered?i+".":"*")+"}} "+content.substring(aStart+4, aEnd)+"\n";

	    idx = aEnd;
	}
	buf += content.substring(aEnd+5);
	return buf;
    }
    
    private String formatH2(String content) {
	String buf = "";
	int idx = 0;
	int aStart = 0;
	int aEnd = -5;
	while (content.indexOf("<h2>", idx) != -1) {
	    aStart = content.indexOf("<h2>", idx);
	    buf += content.substring(aEnd+5, aStart);
	    aEnd = content.indexOf("</h2>", aStart);
	    
	    buf += "\n$b{"+content.substring(aStart+4, aEnd)+"}\n\n";

	    idx = aEnd;
	}
	buf += content.substring(aEnd+5);
	return buf;
    }
    
    private String formatKeywords(String text) {
	Matcher ma = this.findKeywords.matcher(text);
	StringBuffer buffer = new StringBuffer(text.length());
	while (ma.find()) {
	    String link = "/wiki/";
	    link += this.findSpaces.matcher(ma.group(1)).replaceAll("_");
	    String title = (ma.group(3) == null) ? ma.group(1) : ma.group(3);
	    link = "$u{$col[0,0,192]{$a[" + link + "]{" + title + "}}}";
	    ma.appendReplacement(buffer, Matcher.quoteReplacement(link));
	}
	ma.appendTail(buffer);
	return buffer.toString();
    }
}