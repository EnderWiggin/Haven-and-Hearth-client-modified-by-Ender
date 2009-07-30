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

public class Logout extends Window {
    Button y, n;
	
    public Logout(Coord c, Widget parent) {
	super(c, new Coord(125, 50), parent, "Haven & Hearth");
	new Label(Coord.z, this, "Do you want to log out?");
	y = new Button(new Coord(0, 30), 50, this, "Yes");
	n = new Button(new Coord(75, 30), 50, this, "No");
	canactivate = true;
    }
	
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == y) {
	    ui.sess.close();
	} else if(sender == n) {
	    ui.destroy(this);
	} else if(sender == this) {
	    if(msg == "close")
		ui.destroy(this);
	    if(msg == "activate")
		ui.sess.close();
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
}
