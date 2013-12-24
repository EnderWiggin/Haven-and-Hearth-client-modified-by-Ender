/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

public class Session {
    public static final int PVER = 2;
    
    public static final int MSG_SESS = 0;
    public static final int MSG_REL = 1;
    public static final int MSG_ACK = 2;
    public static final int MSG_BEAT = 3;
    public static final int MSG_MAPREQ = 4;
    public static final int MSG_MAPDATA = 5;
    public static final int MSG_OBJDATA = 6;
    public static final int MSG_OBJACK = 7;
    public static final int MSG_CLOSE = 8;
    public static final int OD_REM = 0;
    public static final int OD_MOVE = 1;
    public static final int OD_RES = 2;
    public static final int OD_LINBEG = 3;
    public static final int OD_LINSTEP = 4;
    public static final int OD_SPEECH = 5;
    public static final int OD_LAYERS = 6;
    public static final int OD_DRAWOFF = 7;
    public static final int OD_LUMIN = 8;
    public static final int OD_AVATAR = 9;
    public static final int OD_FOLLOW = 10;
    public static final int OD_HOMING = 11;
    public static final int OD_OVERLAY = 12;
    /* public static final int OD_AUTH = 13; -- Removed */
    public static final int OD_HEALTH = 14;
    public static final int OD_BUDDY = 15;
    public static final int OD_END = 255;
    public static final int SESSERR_AUTH = 1;
    public static final int SESSERR_BUSY = 2;
    public static final int SESSERR_CONN = 3;
    public static final int SESSERR_PVER = 4;
    public static final int SESSERR_EXPR = 5;
    
    static final int ackthresh = 30;
	
    DatagramSocket sk;
    InetAddress server;
    Thread rworker, sworker, ticker, auto;
    public int connfailed = 0;
    public String state = "conn";
    int tseq = 0, rseq = 0;
    int ackseq;
    long acktime = -1;
    LinkedList<Message> uimsgs = new LinkedList<Message>();
    Map<Integer, Message> waiting = new TreeMap<Integer, Message>();
    LinkedList<Message> pending = new LinkedList<Message>();
    Map<Integer, ObjAck> objacks = new TreeMap<Integer, ObjAck>();
    String username;
	public String charname;
    byte[] cookie;
    final Map<Integer, Indir<Resource>> rescache = new TreeMap<Integer, Indir<Resource>>();
    public final Glob glob;
	UI ui;
	
    @SuppressWarnings("serial")
	public class MessageException extends RuntimeException {
	    public Message msg;
		
	    public MessageException(String text, Message msg) {
		super(text);
		this.msg = msg;
	    }
	}
	
    public Indir<Resource> getres(final int id) {
	synchronized(rescache) {
	    Indir<Resource> ret = rescache.get(id);
	    if(ret != null)
		return(ret);
	    ret = new Indir<Resource>() {
		public int resid = id;
		Resource res;
					
		public Resource get() {
		    if(res == null)
			return(null);
		    if(res.loading) {
			res.boostprio(0);
			return(null);
		    }
		    return(res);
		}
					
		public void set(Resource r) {
		    res = r;
		}
				
		public int compareTo(Indir<Resource> x) {
		    return((this.getClass().cast(x)).resid - resid);
		}
		
		public String toString() {
		    if(res == null) {
			return("<res:" + resid + ">");
		    } else {
			if(res.loading)
			    return("<!" + res + ">");
			else
			    return("<" + res + ">");
		    }
		}
	    };
	    rescache.put(id, ret);
	    return(ret);
	}
    }

    private class ObjAck {
	int id;
	int frame;
	long recv;
	long sent;
		
	public ObjAck(int id, int frame, long recv) {
	    this.id = id;
	    this.frame = frame;
	    this.recv = recv;
	    this.sent = 0;
	}
    }
    
    private class Ticker extends HackThread {
	public Ticker() {
	    super("Server time ticker");
	    setDaemon(true);
	}
		
	public void run() {
	    try {
		while(true) {
		    long now, then;
		    then = System.currentTimeMillis();
		    glob.oc.tick();
		    now = System.currentTimeMillis();
		    if(now - then < 70)
			Thread.sleep(70 - (now - then));
		}
	    } catch(InterruptedException e) {}
	}
    }
	
    private class RWorker extends HackThread {
	boolean alive;
		
	public RWorker() {
	    super("Session reader");
	    setDaemon(true);
	}
		
	private void gotack(int seq) {
	    synchronized(pending) {
		for(ListIterator<Message> i = pending.listIterator(); i.hasNext(); ) {
		    Message msg = i.next();
		    if(msg.seq <= seq)
			i.remove();
		}
	    }
	}
		
	private void getobjdata(Message msg) {
	    OCache oc = glob.oc;
	    while(msg.off < msg.blob.length) {
		int fl = msg.uint8();
		int id = msg.int32();
		int frame = msg.int32();
		if((fl & 1) != 0) {
		    oc.remove(id, frame - 1);
		}
		synchronized(oc) {
		    while(true) {
			int type = msg.uint8();
			if(type == OD_REM) {
			    oc.remove(id, frame);
			} else if(type == OD_MOVE) {
			    Coord c = msg.coord();
			    oc.move(id, frame, c);
			} else if(type == OD_RES) {
			    int resid = msg.uint16();
			    Message sdt;
			    if((resid & 0x8000) != 0) {
				resid &= ~0x8000;
				sdt = msg.derive(0, msg.uint8());
			    } else {
				sdt = new Message(0);
			    }
			    oc.cres(id, frame, getres(resid), sdt);
			} else if(type == OD_LINBEG) {
			    Coord s = msg.coord();
			    Coord t = msg.coord();
			    int c = msg.int32();
			    oc.linbeg(id, frame, s, t, c);
			} else if(type == OD_LINSTEP) {
			    int l = msg.int32();
			    oc.linstep(id, frame, l);
			} else if(type == OD_SPEECH) {
			    Coord off = msg.coord();
			    String text = msg.string();
			    oc.speak(id, frame, off, text);
			} else if((type == OD_LAYERS) || (type == OD_AVATAR)) {
			    Indir<Resource> baseres = null;
			    if(type == OD_LAYERS)
				baseres = getres(msg.uint16());
			    List<Indir<Resource>> layers = new LinkedList<Indir<Resource>>();
			    while(true) {
				int layer = msg.uint16();
				if(layer == 65535)
				    break;
				layers.add(getres(layer));
			    }
			    if(type == OD_LAYERS)
				oc.layers(id, frame, baseres, layers);
			    else
				oc.avatar(id, frame, layers);
			} else if(type == OD_DRAWOFF) {
			    Coord off = msg.coord();
			    oc.drawoff(id, frame, off);
			} else if(type == OD_LUMIN) {
			    oc.lumin(id, frame, msg.coord(), msg.uint16(), msg.uint8());
			} else if(type == OD_FOLLOW) {
			    int oid = msg.int32();
			    Coord off = Coord.z;
			    int szo = 0;
			    if(oid != -1) {
				szo = msg.int8();
				off = msg.coord();
			    }
			    oc.follow(id, frame, oid, off, szo);
			} else if(type == OD_HOMING) {
			    int oid = msg.int32();
			    if(oid == -1) {
				oc.homostop(id, frame);
			    } else if(oid == -2) {
				Coord tgtc = msg.coord();
				int v = msg.uint16();
				oc.homocoord(id, frame, tgtc, v);
			    } else {
				Coord tgtc = msg.coord();
				int v = msg.uint16();
				oc.homing(id, frame, oid, tgtc, v);
			    }
			} else if(type == OD_OVERLAY) {
			    int olid = msg.int32();
			    boolean prs = (olid & 1) != 0;
			    olid >>= 1;
			    int resid = msg.uint16();
			    Indir<Resource> res;
			    Message sdt;
			    if(resid == 65535) {
				res = null;
				sdt = null;
			    } else {
				if((resid & 0x8000) != 0) {
				    resid &= ~0x8000;
				    sdt = msg.derive(0, msg.uint8());
				} else {
				    sdt = new Message(0);
				}
				res = getres(resid);
			    }
			    oc.overlay(id, frame, olid, prs, res, sdt);
			} else if(type == OD_HEALTH) {
			    int hp = msg.uint8();
			    oc.health(id, frame, hp);
			} else if(type == OD_BUDDY) {
			    String name = msg.string();
			    int group = msg.uint8();
			    int btype = msg.uint8();
			    oc.buddy(id, frame, name, group, btype);
			} else if(type == OD_END) {
			    break;
			} else {
			    throw(new MessageException("Unknown objdelta type: " + type, msg));
			}
		    }
		    Gob g = oc.getgob(id, frame);
		    if(g != null)
			g.frame = frame;
		}
		synchronized(objacks) {
		    if(objacks.containsKey(id)) {
			ObjAck a = objacks.get(id);
			a.frame = frame;
			a.recv = System.currentTimeMillis();
		    } else {
			objacks.put(id, new ObjAck(id, frame, System.currentTimeMillis()));
		    }
		}
	    }
	    synchronized(sworker) {
		sworker.notifyAll();
	    }
	}
		
	private void handlerel(Message msg) {
	    if(msg.type == Message.RMSG_NEWWDG) {
		synchronized(uimsgs) {
		    uimsgs.add(msg);
		}
	    } else if(msg.type == Message.RMSG_WDGMSG) {
		synchronized(uimsgs) {
		    uimsgs.add(msg);
		}
	    } else if(msg.type == Message.RMSG_DSTWDG) {
		synchronized(uimsgs) {
		    uimsgs.add(msg);
		}
	    } else if(msg.type == Message.RMSG_MAPIV) {
		glob.map.invalblob(msg);
	    } else if(msg.type == Message.RMSG_GLOBLOB) {
		glob.blob(msg);
	    } else if(msg.type == Message.RMSG_PAGINAE) {
		glob.paginae(msg);
	    } else if(msg.type == Message.RMSG_RESID) {
		int resid = msg.uint16();
		String resname = msg.string();
		int resver = msg.uint16();
		synchronized(rescache) {
		    getres(resid).set(Resource.load(resname, resver, -5));
		}
	    } else if(msg.type == Message.RMSG_PARTY) {
		glob.party.msg(msg);
	    } else if(msg.type == Message.RMSG_SFX) {
	    	if(!Config.isSoundOn)
	    	    return;//	Sound effects disabled
		Indir<Resource> res = getres(msg.uint16());
		double vol = ((double)msg.uint16()) / 256.0;
		double spd = ((double)msg.uint16()) / 256.0;
		Audio.play(res);
	    } else if(msg.type == Message.RMSG_CATTR) {
		glob.cattr(msg);
	    } else if(msg.type == Message.RMSG_MUSIC) {
		String resnm = msg.string();
		int resver = msg.uint16();
		boolean loop = !msg.eom() && (msg.uint8() != 0);
		if(Music.enabled) {
		    if(resnm.equals(""))
			Music.play(null, false);
		    else
			Music.play(Resource.load(resnm, resver), loop);
		}
	    } else if(msg.type == Message.RMSG_TILES) {
		glob.map.tilemap(msg);
	    } else if(msg.type == Message.RMSG_BUFF) {
		glob.buffmsg(msg);
	    } else {
		throw(new MessageException("Unknown rmsg type: " + msg.type, msg));
	    }
	}
		
	private void getrel(int seq, Message msg) {
	    if(seq == rseq) {
		synchronized(uimsgs) {
		    handlerel(msg);
		    while(true) {
			rseq = (rseq + 1) % 65536;
			if(!waiting.containsKey(rseq))
			    break;
			handlerel(waiting.get(rseq));
			waiting.remove(rseq);
		    }
		}
		sendack(rseq - 1);
		synchronized(Session.this) {
		    Session.this.notifyAll();
		}
	    } else if(seq > rseq) {
		waiting.put(seq, msg);
	    }
	}
		
	public void run() {
	    try {
		alive = true;
		try {
		    sk.setSoTimeout(1000);
		} catch(SocketException e) {
		    throw(new RuntimeException(e));
		}
		while(alive) {
		    DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
		    try {
			sk.receive(p);
		    } catch(java.nio.channels.ClosedByInterruptException e) {
			/* Except apparently Sun's J2SE doesn't throw this when interrupted :P*/
			break;
		    } catch(SocketTimeoutException e) {
			continue;
		    } catch(IOException e) {
			throw(new RuntimeException(e));
		    }
		    if(!p.getAddress().equals(server))
			continue;
		    Message msg = new Message(p.getData()[0], p.getData(), 1, p.getLength() - 1);
		    if(msg.type == MSG_SESS) {
			if(state == "conn") {
			    int error = msg.uint8();
			    synchronized(Session.this) {
				if(error == 0) {
				    state = "";
				} else {
				    connfailed = error;
				    Session.this.close();
				}
				Session.this.notifyAll();
			    }
			}
		    }
		    if(state != "conn") {
			if(msg.type == MSG_SESS) {
			} else if(msg.type == MSG_REL) {
			    int seq = msg.uint16();
			    while(!msg.eom()) {
				int type = msg.uint8();
				int len;
				if((type & 0x80) != 0) {
				    type &= 0x7f;
				    len = msg.uint16();
				} else {
				    len = msg.blob.length - msg.off;
				}
				getrel(seq, new Message(type, msg.blob, msg.off, len));
				msg.off += len;
				seq++;
			    }
			} else if(msg.type == MSG_ACK) {
			    gotack(msg.uint16());
			} else if(msg.type == MSG_MAPDATA) {
			    glob.map.mapdata(msg);
			} else if(msg.type == MSG_OBJDATA) {
			    getobjdata(msg);
			} else if(msg.type == MSG_CLOSE) {
			    synchronized(Session.this) {
				state = "fin";
				Session.this.notifyAll();
			    }
			    Session.this.close();
			} else {
			    //throw(new MessageException("Unknown message type: " + msg.type, msg));
			    System.out.println("Unknown message type: " + msg.type+"\nMSG: " + msg.toString());
			}
		    }
		}
	    } finally {
		synchronized(Session.this) {
		    state = "dead";
		    Session.this.notifyAll();
		}
	    }
	}
		
	public void interrupt() {
	    alive = false;
	    super.interrupt();
	}
    }
	
    private class SWorker extends HackThread {
		
	public SWorker() {
	    super("Session writer");
	    setDaemon(true);
	}
		
	public void run() {
	    try {
		long to, last = 0, retries = 0;
		while(true) {
					
		    long now = System.currentTimeMillis();
		    if(state == "conn") {
			if(now - last > 2000) {
			    if(++retries > 5) {
				synchronized(Session.this) {
				    connfailed = SESSERR_CONN;
				    Session.this.notifyAll();
				    return;
				}
			    }
			    Message msg = new Message(MSG_SESS);
			    msg.adduint16(1);
			    msg.addstring("Haven");
			    msg.adduint16(PVER);
			    msg.addstring(username);
			    msg.addbytes(cookie);
			    sendmsg(msg);
			    last = now;
			}
			Thread.sleep(100);
		    } else {
			to = 5000;
			synchronized(pending) {
			    if(pending.size() > 0)
				to = 60;
			}
			synchronized(objacks) {
			    if((objacks.size() > 0) && (to > 120))
				to = 200;
			}
			synchronized(this) {
			    if(acktime > 0)
				to = acktime + ackthresh - now;
			    if(to > 0)
				this.wait(to);
			}
			now = System.currentTimeMillis();
			boolean beat = true;
			/*
			  if((closing != -1) && (now - closing > 500)) {
			  Message cm = new Message(MSG_CLOSE);
			  sendmsg(cm);
			  closing = now;
			  if(++ctries > 5)
			  getThreadGroup().interrupt();
			  }
			*/
			synchronized(pending) {
			    if(pending.size() > 0) {
				for(Message msg : pending) {
				    int txtime;
				    if(msg.retx == 0)
					txtime = 0;
				    else if(msg.retx == 1)
					txtime = 80;
				    else if(msg.retx < 4)
					txtime = 200;
				    else if(msg.retx < 10)
					txtime = 620;
				    else
					txtime = 2000;
				    if(now - msg.last > txtime) { /* XXX */
					msg.last = now;
					msg.retx++;
					Message rmsg = new Message(MSG_REL);
					rmsg.adduint16(msg.seq);
					rmsg.adduint8(msg.type);
					rmsg.addbytes(msg.blob);
					sendmsg(rmsg);
				    }
				}
				beat = false;
			    }
			}
			synchronized(objacks) {
			    Message msg = null;
			    for(Iterator<ObjAck> i = objacks.values().iterator(); i.hasNext();) {
				ObjAck a = i.next();
				boolean send = false, del = false;
				if(now - a.sent > 200)
				    send = true;
				if(now - a.recv > 120)
				    send = del = true;
				if(send) {
				    if(msg == null)
					msg = new Message(MSG_OBJACK);
				    msg.addint32(a.id);
				    msg.addint32(a.frame);
				    a.sent = now;
				}
				if(del)
				    i.remove();
			    }
			    if(msg != null) {
				sendmsg(msg);
				beat = false;
			    }
			}
			synchronized(this) {
			    if((acktime > 0) && (now - acktime >= ackthresh)) {
				byte[] msg = {MSG_ACK, 0, 0};
				Utils.uint16e(ackseq, msg, 1);
				sendmsg(msg);
				acktime = -1;
				beat = false;
			    }
			}
			if(beat) {
			    if(now - last > 5000) {
				sendmsg(new byte[] {MSG_BEAT});
				last = now;
			    }
			}
		    }
		}
	    } catch(InterruptedException e) {
		for(int i = 0; i < 5; i++) {
		    sendmsg(new Message(MSG_CLOSE));
		    long f = System.currentTimeMillis();
		    while(true) {
			synchronized(Session.this) {
			    if((state == "conn") || (state == "fin") || (state == "dead"))
				break;
			    state = "close";
			    long now = System.currentTimeMillis();
			    if(now - f > 500)
				break;
			    try {
				Session.this.wait(500 - (now - f));
			    } catch(InterruptedException e2) {}
			}
		    }
		}
	    } finally {
		ticker.interrupt();
		rworker.interrupt();
	    }
	}
    }
	
    public Session(InetAddress server, String username, byte[] cookie) {
	this.server = server;
	this.username = username;
	this.cookie = cookie;
	glob = new Glob(this);
	try {
	    sk = new DatagramSocket();
	} catch(SocketException e) {
	    throw(new RuntimeException(e));
	}
	rworker = new RWorker();
	rworker.start();
	sworker = new SWorker();
	sworker.start();
	ticker = new Ticker();
	ticker.start();
    }
		
    private void sendack(int seq) {
	synchronized(sworker) {
	    if(acktime < 0)
		acktime = System.currentTimeMillis();
	    ackseq = seq;
	    sworker.notifyAll();
	}
    }
	
    public void close() {
	sworker.interrupt();
	ticker.interrupt(); // new
	rworker.interrupt(); // new
    }
	
    public synchronized boolean alive() {
	return(state != "dead");
    }
	
    public void queuemsg(Message msg) {
	msg.seq = tseq;
	tseq = (tseq + 1) % 65536;
	synchronized(pending) {
	    pending.add(msg);
	}
	synchronized(sworker) {
	    sworker.notify();
	}
    }
	
    public Message getuimsg() {
	synchronized(uimsgs) {
	    if(uimsgs.size() == 0)
		return(null);
	    return(uimsgs.remove());
	}
    }
	
    public void sendmsg(Message msg) {
	byte[] buf = new byte[msg.blob.length + 1];
	buf[0] = (byte)msg.type;
	System.arraycopy(msg.blob, 0, buf, 1, msg.blob.length);
	sendmsg(buf);
    }
	
    public void sendmsg(byte[] msg) {
	try {
	    sk.send(new DatagramPacket(msg, msg.length, server, 1870));
	} catch(IOException e) {
	}
    }
	
	public void autoLogin(int t, String c){
		if(t >= 0){
			auto = new LoginAuto(t, username, c, this);
			auto.start();
		}
	}
}
