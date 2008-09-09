package haven.error;

import java.io.*;
import java.util.*;
import java.text.*;

public class HtmlReporter {
    public static final DateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final NumberFormat ifmt = NumberFormat.getInstance();
    public static final String[] idxprops = {
	"java.vendor", "java.version",
	"os.arch", "os.name", "os.version",
	"thnm", "usr",
    };
    
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
	if(html == null)
	    return("(null)");
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

    public static String htmlbt(StackTraceElement[] bt) {
	StringBuilder buf = new StringBuilder();
	buf.append("<table class=\"bt\">\n");
	buf.append("<tr><th>Class</th><th>Function</th><th>File</th><th>Line</th></tr>\n");
	for(StackTraceElement e : bt) {
	    List<String> classes = new LinkedList<String>();
	    String pkg = e.getClassName();
	    if(pkg != null) {
		if(pkg.startsWith("javax.media.opengl.") || pkg.startsWith("com.sun.opengl.")) {
		    classes.add("pkg-jogl");
		} else if(pkg.startsWith("java.") || pkg.startsWith("javax.")) {
		    classes.add("pkg-java");
		} else if(pkg.startsWith("haven.") || pkg.startsWith("dolda.")) {
		    classes.add("own");
		}
	    }
	    if(e.isNativeMethod())
		classes.add("native");
	    buf.append("<tr");
	    if(classes.size() > 0) {
		buf.append(" class=\"");
		boolean f = true;
		for(String cls : classes) {
		    if(!f)
			buf.append(" ");
		    f = false;
		    buf.append(cls);
		}
		buf.append("\"");
	    }
	    buf.append(">");
	    buf.append("<td>" + htmlq(e.getClassName()) + "</td>");
	    buf.append("<td>" + htmlq(e.getMethodName()) + "</td>");
	    buf.append("<td>" + htmlq(e.getFileName()) + "</td>");
	    buf.append("<td>" + htmlq(Integer.toString(e.getLineNumber())) + "</td>");
	    buf.append("</tr>\n");
	}
	buf.append("<table>\n");
	return(buf.toString());
    }

    public static void makereport(OutputStream outs, Report rep) throws IOException {
	PrintWriter out = new PrintWriter(new OutputStreamWriter(outs, "UTF-8"));
	out.print(htmlhead("Error Report"));
	out.println("<h1>Error Report</h1>");
	
	out.println("<p>Reported at: " + htmlq(dfmt.format(new Date(rep.time))) + "</p>");
	out.println("<h2>Properties</h2>");
	out.println("<table>");
	out.println("<tr><th>Name</th><th>Value</th>");
	SortedSet<String> keys = new TreeSet<String>(rep.props.keySet());
	for(String key : keys) {
	    Object val = rep.props.get(key);
	    String vals;
	    if(val instanceof Number) {
		vals = ifmt.format((Number)val);
	    } else if(val instanceof Date) {
		vals = dfmt.format((Number)val);
	    } else {
		vals = val.toString();
	    }
	    out.print("    <tr>");
	    out.print("<td>" + htmlq(key) + "</td><td>" + htmlq(vals) + "</td>");
	    out.println("    </tr>");
	}
	out.println("</table>");
	
	out.println("<h2>Exception chain</h2>");
	for(Throwable t = rep.t; t != null; t = t.getCause()) {
	    out.println("<h3>" + htmlq(t.getClass().getName()) + "</h3>");
	    out.println("<p>" + htmlq(t.getMessage()) + "</p>");
	    out.print(htmlbt(t.getStackTrace()));
	}
	
	out.print(htmltail());
	out.flush();
    }

    public static void makeindex(OutputStream outs, Map<File, Report> reports, Map<File, Exception> failed) throws IOException {
	PrintWriter out = new PrintWriter(new OutputStreamWriter(outs, "UTF-8"));
	out.print(htmlhead("Error Index"));
	out.println("<h1>Error Index</h1>");
	
	Set<String> props = new TreeSet<String>();
	for(String pn : idxprops)
	    props.add(pn);
	
	out.println("<table><tr>");
	out.println("    <th>File</th>");
	out.println("    <th>Time</th>");
	out.println("    <th>Exception</th>");
	for(String pn : props)
	    out.println("    <th>" + htmlq(pn) + "</th>");
	out.println("</tr>");
	
	List<Map.Entry<File, Report>> reps = new ArrayList<Map.Entry<File, Report>>();
	reps.addAll(reports.entrySet());
	Collections.sort(reps, new Comparator<Map.Entry<File, Report>>() {
		public int compare(Map.Entry<File, Report> a, Map.Entry<File, Report> b) {
		    long at = a.getValue().time, bt = b.getValue().time;
		    if(at > bt)
			return(-1);
		    else if(at < bt)
			return(1);
		    else
			return(0);
		}
	    });
	for(Map.Entry<File, Report> rent : reps) {
	    File file = rent.getKey();
	    Report rep = rent.getValue();
	    out.println("    <tr>");
	    out.print("        <td>");
	    out.println("<a href=\"" + htmlq(file.getName()) + ".html\">");
	    out.print(htmlq(file.getName()));
	    out.println("</a></td>");
	    out.println("        <td>" + htmlq(dfmt.format(new Date(rep.time))) + "</td>");
	    out.println("        <td>" + htmlq(rep.t.getClass().getSimpleName()) + "</td>");
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

	if(failed.size() > 0) {
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
	}
	
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
	
	OutputStream out;
	out = new FileOutputStream(new File(outdir, "index.html"));
	try {
	    makeindex(out, reports, failed);
	} finally {
	    out.close();
	}
	
	for(File f : reports.keySet()) {
	    out = new FileOutputStream(new File(outdir, f.getName() + ".html"));
	    try {
		makereport(out, reports.get(f));
	    } finally {
		out.close();
	    }
	}
    }
}
