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

import java.io.*;
import dolda.xiphutil.VorbisStream;

/*
 * Not that I'm ungrateful for the work the JCraft has done with
 * writing an Ogg/Vorbis decoder in Java, they really should try and
 * learn some rational API design.
 */
public class VorbisDecoder extends InputStream {
    public VorbisStream in;
    private byte[] buf;
    private int bufp;
    public final int chn, rate;
    
    public VorbisDecoder(InputStream in) throws IOException {
	this.in = new VorbisStream(in);
	chn = this.in.chn;
	rate = this.in.rate;
    }
    
    private boolean decode() throws IOException {
	float[][] inb = in.decode();
	if(inb == null) {
	    buf = new byte[0];
	    return(false);
	}
	buf = new byte[2 * chn * inb[0].length];
	int p = 0;
	for(int i = 0; i < inb[0].length; i++) {
	    for(int c = 0; c < chn; c++) {
		int s = (int)(inb[c][i] * 32767);
		buf[p++] = (byte)s;
		buf[p++] = (byte)(s >> 8);
	    }
	}
	bufp = 0;
	return(true);
    }

    public int read() throws IOException {
	byte[] rb = new byte[1];
	int ret;
	do {
	    ret = read(rb);
	    if(ret < 0)
		return(-1);
	} while(ret == 0);
	return(rb[0]);
    }
    
    public int read(byte[] dst, int off, int len) throws IOException {
	if((buf == null) && !decode())
	    return(-1);
	if(buf.length - bufp < len)
	    len = buf.length - bufp;
	System.arraycopy(buf, bufp, dst, off, len);
	if((bufp += len) == buf.length)
	    buf = null;
	return(len);
    }
    
    public void close() throws IOException {
	in.close();
    }
    
    public static void main(String[] args) throws Exception {
	InputStream dec = new VorbisDecoder(new FileInputStream(args[0]));
	byte[] buf = new byte[4096];
	int ret;
	while((ret = dec.read(buf)) >= 0)
	    System.out.write(buf, 0, ret);
    }
}
