package haven;

import java.net.URL;

public abstract class WebBrowser {
    public static WebBrowser self;
    
    public WebBrowser() {}
    
    public abstract void show(URL url);
}
