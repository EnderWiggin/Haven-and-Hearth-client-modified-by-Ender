package haven;

import java.util.*;

public class PosixArgs {
    private List<Arg> parsed;
    public String[] rest;
    public String arg = null;
    
    private static class Arg {
	private char ch;
	private String arg;
	
	private Arg(char ch, String arg) {
	    this.ch = ch;
	    this.arg = arg;
	}
    }
    
    private PosixArgs() {
	parsed = new ArrayList<Arg>();
    }

    public static PosixArgs getopt(String[] argv, String desc) {
	PosixArgs ret = new PosixArgs();
	List<Character> fl = new ArrayList<Character>(), fla = new ArrayList<Character>();
	List<String> rest = new ArrayList<String>();
	for(int i = 0; i < desc.length();) {
	    char ch = desc.charAt(i++);
	    if((i < desc.length() - 1) && (desc.charAt(i) == ':')) {
		i++;
		fla.add(ch);
	    } else {
		fl.add(ch);
	    }
	}
	boolean acc = true;
	for(int i = 0; i < argv.length;) {
	    String arg = argv[i++];
	    if(acc && arg.equals("--")) {
		acc = false;
	    } if(acc && (arg.charAt(0) == '-')) {
		for(int o = 1; o < arg.length();) {
		    char ch = arg.charAt(o++);
		    if(fl.contains(ch)) {
			ret.parsed.add(new Arg(ch, null));
		    } else if(fla.contains(ch)) {
			if(o < arg.length()) {
			    ret.parsed.add(new Arg(ch, arg.substring(o)));
			    break;
			} else if(i < argv.length) {
			    ret.parsed.add(new Arg(ch, argv[i++]));
			    break;
			} else {
			    System.err.println("option requires an argument -- '" + ch + "'");
			    return(null);
			}
		    } else {
			System.err.println("invalid option -- '" + ch + "'");
			return(null);
		    }
		}
	    } else {
		rest.add(arg);
	    }
	}
	ret.rest = rest.toArray(new String[0]);
	return(ret);
    }
    
    public Iterable<Character> parsed() {
	return(new Iterable<Character>() {
		public Iterator<Character> iterator() {
		    return(new Iterator<Character>() {
			    private int i = 0;
			    
			    public boolean hasNext() {
				return(i < parsed.size());
			    }
			    
			    public Character next() {
				if(i >= parsed.size())
				    throw(new NoSuchElementException());
				Arg a = parsed.get(i++);
				arg = a.arg;
				return(a.ch);
			    }
			    
			    public void remove() {
				throw(new UnsupportedOperationException());
			    }
			});
		}
	    });
    }
}
