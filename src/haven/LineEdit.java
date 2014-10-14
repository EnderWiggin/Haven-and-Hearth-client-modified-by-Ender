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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class LineEdit {
    public String line = "";
    public int point = 0;
	public int selStart = 0;
	public int selEnd = 0;
    private static Text tcache = null;
	private static final int S = 1;
    private static final int C = 2;
    private static final int M = 4;
    public KeyHandler mode;

    public abstract class KeyHandler {
	public abstract boolean key(char c, int code, int mod);
    }

    public class PCMode extends KeyHandler {
	public boolean key(char c, int code, int mod) {
		if((code == KeyEvent.VK_LEFT) && (mod == S)) {
			if(point > 0){
				changeSel(-1);
				point--;
			}
			return true;
	    } else if((code == KeyEvent.VK_RIGHT) && (mod == S)) {
			if(point < line.length()){
				changeSel(1);
				point++;
			}
			return true;
	    }
		
		if(((c == 'c')||(c == 'C')) && (mod == C) ){
			if(selStart != selEnd)
				Utils.setClipboard(line.substring(selStart(), selEnd() ) );
			return true;
	    } else if(((c == 'a')||(c == 'A')) && (mod == C) ) {
			changeSelect(0, -1);
			return true;
		}
		
		if((code == KeyEvent.VK_LEFT) && (mod == 0)) {
			if(point > 0)
				point--;
			if(selStart != selEnd) point = selStart();
				selDes();
	    } else if((code == KeyEvent.VK_LEFT) && (mod == C)) {
			point = wordstart(point);
			if(selStart != selEnd) point = selStart();
				selDes();
	    } else if((code == KeyEvent.VK_RIGHT) && (mod == 0)) {
			if(point < line.length())
				point++;
			if(selStart != selEnd) point = selEnd();
				selDes();
	    } else if((code == KeyEvent.VK_RIGHT) && (mod == C)) {
			point = wordend(point);
			if(selStart != selEnd) point = selEnd();
				selDes();
	    } else if((code == KeyEvent.VK_HOME) && (mod == 0)) {
			point = 0;
			selDes();
	    } else if((code == KeyEvent.VK_END) && (mod == 0)) {
			point = line.length();
			selDes();
	    }else{
			if((c == 8) && (mod == 0)) {
				if(selStart == selEnd){
					if(point > 0) {
						line = line.substring(0, point - 1) + line.substring(point);
						point--;
					}
				}else{
					removeSelection();
				}
			}else if((c == 8) && (mod == C)) {
				removeSelection();
				int b = wordstart(point);
				line = line.substring(0, b) + line.substring(point);
				point = b;
			} else if(c == 10) {
				removeSelection();
				done(line);
			} else if((c == 127) && (mod == 0)) {
				removeSelection();
				if(point < line.length())
					line = line.substring(0, point) + line.substring(point + 1);
			} else if((c == 127) && (mod == C)) {
				removeSelection();
				int b = wordend(point);
				line = line.substring(0, point) + line.substring(b);
			} else if((c >= 32) && (mod <= 1)) {
				removeSelection();
				line = line.substring(0, point) + c + line.substring(point);
				point++;
			} else if(((c == 'v')||(c == 'V')) && (mod == C)) {
				removeSelection();
				String str = Utils.getClipboard();
				line = line.substring(0, point) + str + line.substring(point);
				point += str.length();
			} else {
				if((int)c > 0) selDes();
					return(false);
			}
			selDes();
		}
	    return(true);
	}
    }
	
	void sortSelection(){
		if(selStart > selEnd){
			int temp = selStart;
			selStart = selEnd;
			selEnd = temp;
		}
	}
	
	int selStart(){
		if(selStart > selEnd) return selEnd;
		
		return selStart;
	}
	
	int selEnd(){
		if(selStart > selEnd) return selStart;
		
		return selEnd;
	}
	
	void changeSel(int increment){
		if(selStart == selEnd){
			selStart = point;
			selEnd = point + increment;
			changeSelect(selStart, selEnd);
		}else{
			changeSelect(selStart, point + increment);
		}
	}
	
	void selDes(){
		deselect();
		selEnd = selStart;
	}
	
	void removeSelection(){
		if(selStart != selEnd){
			line = line.substring(0, selStart() ) + line.substring(selEnd() );
			point = selStart();
		}
	}
    
    public class EmacsMode extends KeyHandler {
	private int mark, yankpos, undopos;
	private String last = "";
	private List<String> yanklist = new ArrayList<String>();
	private List<UndoState> undolist = new ArrayList<UndoState>();
	{undolist.add(new UndoState());}
	
	private class UndoState {
	    private String line;
	    private int point;
	    
	    private UndoState() {
		this.line = LineEdit.this.line;
		this.point = LineEdit.this.point;
	    }
	}
	
	private void save() {
	    if(!undolist.get(undolist.size() - 1).line.equals(line))
		undolist.add(new UndoState());
	}
	
	private void mode(String mode) {
	    if((mode == "") || (last != mode))
		save();
	    last = mode;
	}

	private void kill(String text) {
	    yanklist.add(text);
	}
	
	public boolean key(char c, int code, int mod) {
	    if(mark > line.length())
		mark = line.length();
	    String last = this.last;
	    if((c == 8) && (mod == 0)) {
		mode("erase");
		if(point > 0) {
		    line = line.substring(0, point - 1) + line.substring(point);
		    point--;
		}
	    } else if((c == 8) && ((mod == C) || (mod == M))) {
		mode("backward-kill-word");
		save();
		int b = wordstart(point);
		if(last == "backward-kill-word")
		    yanklist.set(yanklist.size() - 1, line.substring(b, point) + yanklist.get(yanklist.size() - 1));
		else
		    kill(line.substring(b, point));
		line = line.substring(0, b) + line.substring(point);
		point = b;
	    } else if(c == 10) {
		done(line);
	    } else if((c == 'd') && (mod == C)) {
		mode("erase");
		if(point < line.length())
		    line = line.substring(0, point) + line.substring(point + 1);
	    } else if((c == 'd') && (mod == M)) {
		mode("kill-word");
		save();
		int b = wordend(point);
		if(last == "kill-word")
		    yanklist.set(yanklist.size() - 1, yanklist.get(yanklist.size() - 1) + line.substring(point, b));
		else
		    kill(line.substring(point, b));
		line = line.substring(0, point) + line.substring(b);
	    } else if((c == 'b') && (mod == C)) {
		mode("move");
		if(point > 0)
		    point--;
	    } else if((c == 'b') && (mod == M)) {
		mode("move");
		point = wordstart(point);
	    } else if((c == 'f') && (mod == C)) {
		mode("move");
		if(point < line.length())
		    point++;
	    } else if((c == 'f') && (mod == M)) {
		mode("move");
		point = wordend(point);
	    } else if((c == 'a') && (mod == C)) {
		mode("move");
		point = 0;
	    } else if((c == 'e') && (mod == C)) {
		mode("move");
		point = line.length();
	    } else if((c == 't') && (mod == C)) {
		mode("transpose");
		if((line.length() >= 2) && (point > 0)) {
		    if(point < line.length()) {
			line = line.substring(0, point - 1) + line.charAt(point) + line.charAt(point - 1) + line.substring(point + 1);
			point++;
		    } else {
			line = line.substring(0, point - 2) + line.charAt(point - 1) + line.charAt(point - 2);
		    }
		}
	    } else if((c == 'k') && (mod == C)) {
		mode("");
		kill(line.substring(point));
		line = line.substring(0, point);
	    } else if((c == 'w') && (mod == M)) {
		mode("");
		if(mark < point) {
		    kill(line.substring(mark, point));
		} else {
		    kill(line.substring(point, mark));
		}
	    } else if((c == 'w') && (mod == C)) {
		mode("");
		if(mark < point) {
		    kill(line.substring(mark, point));
		    line = line.substring(0, mark) + line.substring(point);
		} else {
		    kill(line.substring(point, mark));
		    line = line.substring(0, point) + line.substring(mark);
		}
	    } else if((c == 'y') && (mod == C)) {
		mode("yank");
		save();
		yankpos = yanklist.size();
		if(yankpos > 0) {
		    String yank = yanklist.get(--yankpos);
		    mark = point;
		    line = line.substring(0, point) + yank + line.substring(point);
		    point = mark + yank.length();
		}
	    } else if((c == 'y') && (mod == M)) {
		mode("yank");
		save();
		if((last == "yank") && (yankpos > 0)) {
		    String yank = yanklist.get(--yankpos);
		    line = line.substring(0, mark) + yank + line.substring(point);
		    point = mark + yank.length();
		}
	    } else if((c == ' ') && (mod == C)) {
		mode("");
		mark = point;
	    } else if((c == '_') && (mod == C)) {
		mode("undo");
		save();
		if(last != "undo")
		    undopos = undolist.size() - 1;
		if(undopos > 0) {
		    UndoState s = undolist.get(--undopos);
		    line = s.line;
		    point = s.point;
		}
	    } else if((c >= 32) && (mod == 0)) {
		mode("type");
		line = line.substring(0, point) + c + line.substring(point);
		point++;
	    } else {
		return(false);
	    }
	    return(true);
	}
    }

    public LineEdit() {
	String mode = Utils.getpref("editmode", "pc");
	if(mode.equals("emacs")) {
	    this.mode = new EmacsMode();
	} else {
	    this.mode = new PCMode();
	}
    }
    
    public LineEdit(String line) {
	this();
	this.line = line;
	this.point = line.length();
    }
    
    public void setline(String line) {
	String prev = this.line;
	this.line = line;
	if(point > line.length())
	    point = line.length();
	if(!prev.equals(line))
	    changed();
    }

    public boolean key(char c, int code, int mod) {
	String prev = line;
	boolean ret = mode.key(c, code, mod);
	if(!prev.equals(line))
	    changed();
	return(ret);
    }
	
    public boolean key(KeyEvent ev) {
	int mod = 0;
	if((ev.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) mod |= S;
	if((ev.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) mod |= C;
	if((ev.getModifiersEx() & (InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK)) != 0) mod |= M;
	if(ev.getID() == KeyEvent.KEY_TYPED) {
	    char c = ev.getKeyChar();
	    if(((mod & C) != 0) && (c < 32)) {
		/* Undo Java's TTY Control-code mangling */
		if(ev.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
		} else if(ev.getKeyCode() == KeyEvent.VK_ENTER) {
		} else if(ev.getKeyCode() == KeyEvent.VK_TAB) {
		} else if(ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
		} else {
		    if((ev.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)
			c = (char)(c + 'A' - 1);
		    else
			c = (char)(c + 'a' - 1);
		}
	    }
	    return(key(c, ev.getKeyCode(), mod));
	} else if(ev.getID() == KeyEvent.KEY_PRESSED) {
	    if(ev.getKeyChar() == KeyEvent.CHAR_UNDEFINED)
		return(key('\0', ev.getKeyCode(), mod));
	}
	return(false);
    }
    
    private static boolean wordchar(char c) {
	return(Character.isLetterOrDigit(c));
    }

    private int wordstart(int from) {
	while((from > 0) && !wordchar(line.charAt(from - 1))) from--;
	while((from > 0) && wordchar(line.charAt(from - 1))) from--;
	return(from);
    }
    
    private int wordend(int from) {
	while((from < line.length()) && !wordchar(line.charAt(from))) from++;
	while((from < line.length()) && wordchar(line.charAt(from))) from++;
	return(from);
    }

    protected void done(String line) {}
    protected void changed() {}
	protected void deselect() {}
	protected void changeSelect(int start, int end) {}
    
    public Text render(Text.Foundry f) {
	if((tcache == null) || (tcache.text != line))
	    tcache = f.render(line);
	return(tcache);
    }
    
    static {
	Console.setscmd("editmode", new Console.Command() {
		public void run(Console cons, String[] args) {
		    Utils.setpref("editmode", args[1]);
		}
	    });
    }
}
