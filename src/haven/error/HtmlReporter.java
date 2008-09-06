package haven.error;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class HtmlReporter {
    public static final DateFormat dfmt = new SimpleDateFormat();
    
    public static String htmlhead(String title) {
	StringBuilder buf = new StringBuilder();
	buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
	buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-US\">\n");
	buf.append("<head>\n");
	buf.append("<title>" + title + "</title>\n");
	buf.append("<link rel=\"stylesheet\" title=\"Haven error report\" type=\"text/css\" href=\"base.css\" />");
	buf.append("</head>\n");
	buf.append("<body>\n");
	return(buf.toString());
    }

    public static String htmltail() {
	StringBuilder buf = new StringBuilder();
	buf.append("</body>\n");
	buf.append("</html>\n");
	return(buf.toString());
    }
    
    public static String htmlq(String html) {
	StringBuilder buf = new StringBuilder();
	for(int i = 0; i < html.length(); i++) {
	    char c = html.charAt(i);
	    if(c == '&')
		buf.append("&amp;");
	    else if(c == '<')
		buf.append("&lt;");
	    else if(c == '>')
		buf.append("&gt;");
	    else
		buf.append(c);
	}
	return(buf.toString());
    }

    public static void makeindex(OutputStream outs, Map<File, Report> reports, Map<File, Exception> failed) throws IOException {
	PrintWriter out = new PrintWriter(new OutputStreamWriter(outs, "UTF-8"));
	out.print(htmlhead("Error Index"));
	out.println("<h1>Error Index</h1>");
	
	Set<String> props = new TreeSet<String>();
	for(Report r : reports.values()) {
	    for(String pn : r.props.keySet())
		props.add(pn);
	}
	
	out.println("<table><tr>");
	out.println("    <th>File</th>");
	out.println("    <th>Time</th>");
	for(String pn : props)
	    out.println("    <th>" + htmlq(pn) + "</th>");
	out.println("</tr>");
	
	for(File file : reports.keySet()) {
	    Report rep = reports.get(file);
	    out.println("    <tr>");
	    out.print("        <td>");
	    out.println("<a href=\"" + htmlq(file.getName()) + ".html\">");
	    out.print(htmlq(file.getName()));
	    out.println("</a></td>");
	    out.println("        <td>" + htmlq(dfmt.format(new Date(rep.time))) + "</td>");
	    for(String pn : props) {
		out.print("        <td>");
		if(rep.props.containsKey(pn)) {
		    out.print(htmlq(rep.props.get(pn).toString()));
		}
		out.println("</td>");
	    }
	    out.println("    </tr>");
	}
	out.println("</table>");

	out.println("<h2>Unreadable reports</h2>");
	out.println("<table>");
	out.println("<tr><th>File</th><th>Exception</th>");
	for(File file : failed.keySet()) {
	    Exception exc = failed.get(file);
	    out.print("    <tr>");
	    out.print("<td>" + htmlq(file.getName()) + "</td><td>" + htmlq(exc.getClass().getName()) + ": " + htmlq(exc.getMessage()) + "</td>");
	    out.println("    </tr>");
	}
	out.println("</table>");
	
	out.print(htmltail());
	out.flush();
    }

    public static void main(String[] args) throws Exception {
	File indir = new File("/srv/haven/errors");
	File outdir = new File("/srv/www/haven/errors");
	Map<File, Report> reports = new HashMap<File, Report>();
	Map<File, Exception> failed = new HashMap<File, Exception>();
	
	for(File f : indir.listFiles()) {
	    if(f.getName().startsWith("err")) {
		try {
		    ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
		    try {
			reports.put(f, (Report)in.readObject());
		    } finally {
			in.close();
		    }
		} catch(Exception e) {
		    failed.put(f, e);
		}
	    }
	}
	
	OutputStream out = new FileOutputStream(new File(outdir, "index.html"));
	try {
	    makeindex(out, reports, failed);
	} finally {
	    out.close();
	}
    }
}
