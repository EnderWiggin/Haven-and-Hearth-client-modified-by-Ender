package haven.sslproxy;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import javax.net.ssl.*;

public class SslHelper {
    private KeyStore creds, trusted;
    private SSLContext ctx = null;
    private int tserial = 0;
    private char[] pw;
    private HostnameVerifier ver = null;
    
    public SslHelper() {
	creds = null;
	try {
	    trusted = KeyStore.getInstance(KeyStore.getDefaultType());
	    trusted.load(null, null);
	} catch(Exception e) {
	    throw(new Error(e));
	}
    }
    
    private synchronized SSLContext ctx() {
	if(ctx == null) {
	    TrustManagerFactory tmf;
	    KeyManagerFactory kmf;
	    try {
		ctx = SSLContext.getInstance("TLS");
		tmf = TrustManagerFactory.getInstance("PKIX");
		kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyManager[] kms = null;
		tmf.init(trusted);
		if(creds != null) {
		    kmf.init(creds, pw);
		    kms = kmf.getKeyManagers();
		}
		ctx.init(kms, tmf.getTrustManagers(), new SecureRandom());
	    } catch(NoSuchAlgorithmException e) {
		throw(new Error(e));
	    } catch(KeyStoreException e) {
		throw(new RuntimeException(e));
	    } catch(UnrecoverableKeyException e) {
		/* The key should be recoverable at this stage, since
		 * it was loaded successfully. */
		throw(new RuntimeException(e));
	    } catch(KeyManagementException e) {
		throw(new RuntimeException(e));
	    }
	}
	return(ctx);
    }
    
    public synchronized void trust(Certificate cert) {
	ctx = null;
	try {
	    trusted.setCertificateEntry("cert-" + tserial++, cert);
	} catch(KeyStoreException e) {
	    /* The keystore should have been initialized and should
	     * not have the generated alias, so this should not
	     * happen. */
	    throw(new RuntimeException(e));
	}
    }
    
    public static Certificate loadX509(InputStream in) throws IOException, CertificateException {
	CertificateFactory fac = CertificateFactory.getInstance("X.509");
	return(fac.generateCertificate(in));
    }
    
    public synchronized void loadCredsPkcs12(InputStream in, char[] pw) throws IOException, CertificateException {
	ctx = null;
	try {
	    creds = KeyStore.getInstance("PKCS12");
	    creds.load(in, pw);
	    this.pw = pw;
	} catch(KeyStoreException e) {
	    throw(new Error(e));
	} catch(NoSuchAlgorithmException e) {
	    throw(new Error(e));
	}
    }
    
    public HttpsURLConnection connect(URL url) throws IOException {
	if(!url.getProtocol().equals("https"))
	    throw(new MalformedURLException("Can only be used to connect to HTTPS servers"));
	HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
	conn.setSSLSocketFactory(ctx().getSocketFactory());
	if(ver != null)
	    conn.setHostnameVerifier(ver);
	return(conn);
    }
    
    public HttpsURLConnection connect(String url) throws IOException {
	return(connect(new URL(url)));
    }
    
    public void ignoreName() {
	ver = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession sess) {
		    return(true);
		}
	    };
    }
    
    public SSLSocket connect(Socket sk, String host, int port, boolean autoclose) throws IOException {
	return((SSLSocket)ctx().getSocketFactory().createSocket(sk, host, port, autoclose));
    }
    
    public SSLSocket connect(String host, int port) throws IOException {
	Socket sk = new Socket(host, port);
	return(connect(sk, host, port, true));
    }
    
    public boolean hasCreds() {
	return(creds != null);
    }
}
