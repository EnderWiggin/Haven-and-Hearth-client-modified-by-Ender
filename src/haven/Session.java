package haven;

import java.net.*;
import java.util.*;
import java.io.*;

public class Session {
	public static final int MSG_SESS = 0;
	public static final int MSG_REL = 1;
	public static final int MSG_ACK = 2;
	public static final int MSG_BEAT = 3;
	public static final int MSG_MAPREQ = 4;
	public static final int MSG_MAPDATA = 5;
	public static final int MSG_OBJDATA = 6;
	public static final int MSG_OBJACK = 7;
	public static final int MSG_CLOSE = 8;
	public static final int OD_MOVE = 0;
	public static final int OD_REM = 1;
	public static final int OD_VMOVE = 2;
	public static final int SESSERR_AUTH = 1;
	public static final int SESSERR_BUST = 2;
	public static final int SESSERR_CONN = 3;
	
	public static Session current; /* XXX: Should not exist */
	DatagramSocket sk;
	InetAddress server;
	Thread rworker, sworker, ticker;
	int connfailed = 0;
	boolean connected = false;
	int tseq = 0, rseq = 0;
	MapView mapdispatch; /* XXX */
	LinkedList<Message> received = new LinkedList<Message>();
	Map<Integer, Message> waiting = new TreeMap<Integer, Message>();
	LinkedList<Message> pending = new LinkedList<Message>();
	Map<Integer, ObjAck> objacks = new TreeMap<Integer, ObjAck>();
	OCache oc = new OCache();
	String username, password;
	
	private class ObjAck {
		int id;
		int frame;
		long recv;
		
		public ObjAck(int id, int frame, long recv) {
			this.id = id;
			this.frame = frame;
			this.recv = recv;
		}
	}
    
	private class Ticker extends Thread {
		public Ticker() {
			super(Utils.tg(), "Server time ticker");
			setDaemon(true);
		}
		
		public void run() {
			try {
				long now, then;
				then = System.currentTimeMillis();
				oc.tick();
				now = System.currentTimeMillis();
				if(now - then < 60)
					Thread.sleep(60 - (now - then));
			} catch(InterruptedException e) {}
		}
	}
	
	private class RWorker extends Thread {
		public RWorker() {
			super(Utils.tg(), "Session reader");
			setDaemon(true);
		}
		
		private void sendack(int seq) {
			byte[] msg = {MSG_ACK, 0, 0};
			Utils.uint16e(seq, msg, 1);
			sendmsg(msg);
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
			while(msg.off < msg.blob.length) {
				int type = msg.uint8();
				int id = msg.int32();
				int frame = msg.int32();
				if(type == OD_MOVE) {
					Coord c = msg.coord();
					String res = msg.string();
					oc.move(id, frame, c, res);
				} else if(type == OD_REM) {
					oc.remove(id, frame);
				} else if(type == OD_VMOVE) {
					Coord c = msg.coord();
					Coord v = msg.coord();
					String res = msg.string();
					oc.move(id, frame, c, v, res);
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
				synchronized(sworker) {
					sworker.notifyAll();
				}
			}
		}
		
		private void getrel(Message msg) {
			int seq = msg.uint16();
			msg = new Message(msg.uint8(), msg.blob, msg.off, msg.blob.length - msg.off);
			if(seq == rseq) {
				synchronized(received) {
					received.add(msg);
					while(true) {
						rseq = (rseq + 1) % 65536;
						if(!waiting.containsKey(rseq))
							break;
						received.add(waiting.get(rseq));
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
			while(true) {
				DatagramPacket p = new DatagramPacket(new byte[65536], 65536);
				try {
					sk.receive(p);
				} catch(java.nio.channels.ClosedByInterruptException e) {
					break;
				} catch(IOException e) {
					System.out.println(e);
					continue;
				}
				if(!p.getAddress().equals(server))
					continue;
				Message msg = new Message(p.getData()[0], p.getData(), 1, p.getLength() - 1);
				if(msg.type == MSG_SESS) {
					if(!connected) {
						int error = msg.uint8();
						synchronized(Session.this) {
							if(error == 0) {
								connected = true;
							} else {
								connected = false;
								connfailed = error;
							}
							Session.this.notifyAll();
						}
					}
				}
				if(connected) {
					if(msg.type == MSG_SESS) {
					} else if(msg.type == MSG_REL) {
						getrel(msg);
					} else if(msg.type == MSG_ACK) {
						gotack(msg.uint16());
					} else if(msg.type == MSG_MAPDATA) {
						if(mapdispatch != null)
							mapdispatch.mapdata(msg);
					} else if(msg.type == MSG_OBJDATA) {
						getobjdata(msg);
					} else if(msg.type == MSG_CLOSE) {
						getThreadGroup().interrupt();
					} else {
						for(int i = 0; i < msg.blob.length; i++)
							System.out.format("%02x ", msg.blob[i]);
						System.out.println();
					}
				}
			}
		}		
	}
	
	private class SWorker extends Thread {
		public SWorker() {
			super(Utils.tg(), "Session writer");
			setDaemon(true);
		}
		
		public void run() {
			long to, last = 0, retries = 0;
			
			try {
				while(true) {
					long now = System.currentTimeMillis();
					if(!connected) {
						if(now - last > 500) {
							if(++retries > 5) {
								synchronized(Session.this) {
									connfailed = SESSERR_CONN;
									Session.this.notifyAll();
									return;
								}
							}
							Message msg = new Message(MSG_SESS);
							msg.addstring(username);
							msg.addstring(password);
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
							this.wait(to);
						}
						now = System.currentTimeMillis();
						boolean beat = true;
						synchronized(pending) {
							if(pending.size() > 0) {
								for(Message msg : pending) {
									if(now - msg.last > 60) { /* XXX */
										msg.last = now;
										sendmsg(msg);
									}
								}
								beat = false;
							}
						}
						synchronized(objacks) {
							Message msg = null;
							for(Iterator<ObjAck> i = objacks.values().iterator(); i.hasNext();) {
								ObjAck a = i.next();
								if(now - a.recv > 120) {
									if(msg == null)
										msg = new Message(MSG_OBJACK);
									msg.addint32(a.id);
									msg.addint32(a.frame);
									i.remove();
								}
							}
							if(msg != null)
								sendmsg(msg);
						}
						if(beat) {
							if(now - last > 5000) {
								sendmsg(new byte[] {MSG_BEAT});
								last = now;
							}
						}
					}
				}
			} catch(InterruptedException e) {}
		}
	}
	
	public Session(InetAddress server, String username, String password) {
		this.server = server;
		this.username = username;
		this.password = password;
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
		current = this;
	}
	
	public void close() {
		if(connected)
			sendmsg(new Message(MSG_CLOSE));
		sworker.interrupt();
		rworker.interrupt();
		ticker.interrupt();
		current = null;
	}
	
	public void queuemsg(Message msg) {
		Message rmsg = new Message(MSG_REL);
		rmsg.adduint16(rmsg.seq = tseq);
		rmsg.adduint8(msg.type);
		rmsg.addbytes(msg.blob);
		tseq = (tseq + 1) % 65536;
		synchronized(pending) {
			pending.add(rmsg);
		}
		synchronized(sworker) {
			sworker.notify();
		}
	}
	
	public Message unqueuer() {
		synchronized(received) {
			if(received.size() == 0)
				return(null);
			return(received.remove());
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
}
