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

import java.lang.reflect.*;
import java.io.*;
import java.net.URL;
import javax.jnlp.*;

public class JnlpCache implements ResCache {
    private PersistenceService back;
    private URL base;
    
    private JnlpCache(PersistenceService back, URL base) {
	this.back = back;
	this.base = base;
    }
    
    public static JnlpCache create() {
	try {
	    Class<? extends ServiceManager> cl = Class.forName("javax.jnlp.ServiceManager").asSubclass(ServiceManager.class);
	    Method m = cl.getMethod("lookup", String.class);
	    BasicService basic = (BasicService)m.invoke(null, "javax.jnlp.BasicService");
	    PersistenceService prs = (PersistenceService)m.invoke(null, "javax.jnlp.PersistenceService");
	    return(new JnlpCache(prs, basic.getCodeBase()));
	} catch(Exception e) {
	    return(null);
	}
    }
    
    private static String mangle(String nm) {
	StringBuilder buf = new StringBuilder();
	for(int i = 0; i < nm.length(); i++) {
	    char c = nm.charAt(i);
	    if(c == '/')
		buf.append("_");
	    else
		buf.append(c);
	}
	return(buf.toString());
    }

    private void put(URL loc, byte[] data) {
	FileContents file;
	try {
	    try {
		file = back.get(loc);
	    } catch(FileNotFoundException e) {
		back.create(loc, data.length);
		file = back.get(loc);
	    }
	    if(file.getMaxLength() < data.length) {
		if(file.setMaxLength(data.length) < data.length) {
		    back.delete(loc);
		    return;
		}
	    }
	    OutputStream s = file.getOutputStream(true);
	    try {
		s.write(data);
	    } finally {
		s.close();
	    }
	} catch(IOException e) {
	    return;
	} catch(Exception e) {
	    /* There seems to be a strange bug in NetX. */
	    return;
	}
    }

    public OutputStream store(final String name) throws IOException {
	OutputStream ret = new ByteArrayOutputStream() {
		public void close() {
		    byte[] res = toByteArray();
		    try {
			put(new URL(base, mangle(name)), res);
		    } catch(java.net.MalformedURLException e) {
			throw(new RuntimeException(e));
		    }
		}
	    };
	return(ret);
    }
    
    public InputStream fetch(String name) throws IOException {
	try {
	    URL loc = new URL(base, mangle(name));
	    FileContents file = back.get(loc);
	    InputStream in = file.getInputStream();
	    return(in);
	} catch(IOException e) {
	    throw(e);
	} catch(Exception e) {
	    /* There seems to be a weird bug in NetX */
	    throw((IOException)(new IOException("Virtual NetX IO exception").initCause(e)));
	}
    }
}
