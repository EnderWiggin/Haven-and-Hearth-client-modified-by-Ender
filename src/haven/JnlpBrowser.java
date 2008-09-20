package haven;

import java.lang.reflect.*;
import java.net.URL;
import javax.jnlp.*;

public class JnlpBrowser extends WebBrowser {
    BasicService basic;
    
    private JnlpBrowser(BasicService basic) {
	this.basic = basic;
    }
    
    public static JnlpBrowser create() {
	try {
	    Class<? extends ServiceManager> cl = Class.forName("javax.jnlp.ServiceManager").asSubclass(ServiceManager.class);
	    Method m = cl.getMethod("lookup", String.class);
	    BasicService basic = (BasicService)m.invoke(null, "javax.jnlp.BasicService");
	    return(new JnlpBrowser(basic));
	} catch(Exception e) {
	    return(null);
	}
    }
    
    public void show(URL url) {
	basic.showDocument(url);
    }
}
