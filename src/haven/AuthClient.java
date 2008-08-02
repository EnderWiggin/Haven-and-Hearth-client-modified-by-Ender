package haven;

import java.util.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;

public class AuthClient {
    private static final SslHelper ssl;
    private static final int CMD_USR = 1;
    private static final int CMD_PASSWD = 2;
    private Socket sk;
    private InputStream skin;
    private OutputStream skout;
    public byte[] cookie;
    
    static {
	ssl = new SslHelper();
	try {
	    ssl.trust(ssl.loadX509(Resource.class.getResourceAsStream("authsrv.crt")));
	} catch(Exception e) {
	    throw(new RuntimeException(e));
	}
    }

    public AuthClient(String host, String username) throws IOException {
	sk = ssl.connect(host, 1871);
	skin = sk.getInputStream();
	skout = sk.getOutputStream();
	binduser(username);
    }
    
    public void binduser(String username) throws IOException {
	Message msg = new Message(CMD_USR);
	msg.addstring2(username);
	sendmsg(msg);
	Message rpl = recvmsg();
	if(rpl.type != 0)
	    throw(new IOException("Unhandled reply " + rpl.type + " when binding username"));
    }
    
    private static byte[] digest(String pw) {
	MessageDigest dig;
	byte[] buf;
	try {
	    dig = MessageDigest.getInstance("SHA-256");
	    buf = pw.getBytes("utf-8");
	} catch(java.security.NoSuchAlgorithmException e) {
	    throw(new RuntimeException(e));
	} catch(java.io.UnsupportedEncodingException e) {
	    throw(new RuntimeException(e));
	}
	dig.update(buf);
	for(int i = 0; i < buf.length; i++)
	    buf[i] = 0;
	return(dig.digest());
    }

    public boolean trypasswd(String pw) throws IOException {
	byte[] phash = digest(pw);
	sendmsg(new Message(CMD_PASSWD, phash));
	Message rpl = recvmsg();
	if(rpl.type == 0) {
	    cookie = rpl.blob;
	    return(true);
	} else {
	    return(false);
	}
    }
    
    public void close() throws IOException {
	sk.close();
    }

    private void sendmsg(Message msg) throws IOException {
	if(msg.blob.length > 255)
	    throw(new RuntimeException("Too long message in AuthClient (" + msg.blob.length + " bytes)"));
	byte[] buf = new byte[msg.blob.length + 2];
	buf[0] = (byte)msg.type;
	buf[1] = (byte)msg.blob.length;
	System.arraycopy(msg.blob, 0, buf, 2, msg.blob.length);
	skout.write(buf);
    }
    
    private static void readall(InputStream in, byte[] buf) throws IOException {
	int rv;
	for(int i = 0; i < buf.length; i += rv) {
	    rv = in.read(buf, i, buf.length - i);
	    if(rv < 0)
		throw(new IOException("Premature end of input"));
	}
    }

    private Message recvmsg() throws IOException {
	byte[] header = new byte[2];
	readall(skin, header);
	byte[] buf = new byte[header[1]];
	readall(skin, buf);
	return(new Message(header[0], buf));
    }
    
    public static void main(String[] args) throws Exception {
	AuthClient test = new AuthClient("127.0.0.1", args[0]);
	System.out.println(test.trypasswd(args[1]));
	if(test.cookie != null) {
	    for(byte b : test.cookie)
		System.out.print(String.format("%02X ", b));
	    System.out.println();
	}
	test.close();
    }
}
