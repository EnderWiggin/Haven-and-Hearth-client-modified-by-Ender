package haven;

import java.util.*;
import java.lang.ref.WeakReference;

public class ArrayIdentity {
    private static HashMap<Entry<?>, Entry<?>> set = new HashMap<Entry<?>, Entry<?>>();
    private static int cleanint = 0;
    
    private static class Entry<T> {
	WeakReference<T[]> arr;
	
	private Entry(T[] arr) {
	    this.arr = new WeakReference<T[]>(arr);
	}
	
	public boolean equals(Object x) {
	    if(!(x instanceof Entry))
	       return(false);
	    T[] a = arr.get();
	    if(a == null)
		return(false);
	    Entry<?> e = (Entry<?>)x;
	    Object[] ea = e.arr.get();
	    if(ea == null)
		return(false);
	    if(ea.length != a.length)
		return(false);
	    for(int i = 0; i < a.length; i++) {
		if(a[i] != ea[i])
		    return(false);
	    }
	    return(true);
	}
	
	public int hashCode() {
	    T[] a = arr.get();
	    if(a == null)
		return(0);
	    int ret = 1;
	    for(T o : a)
		ret = (ret * 31) + System.identityHashCode(o);
	    return(ret);
	}
    }
    
    private static synchronized void clean() {
	for(Iterator<Entry<?>> i = set.keySet().iterator(); i.hasNext();) {
	    Entry<?> e = i.next();
	    if(e.arr.get() == null)
		i.remove();
	}
    }
    
    public static <T> T[] intern(T[] arr) {
	synchronized(ArrayIdentity.class) {
	    if(cleanint++ > 100) {
		clean();
		cleanint = 0;
	    }
	}
	Entry<T> e = new Entry(arr);
	synchronized(ArrayIdentity.class) {
	    Entry<T> e2 = (Entry<T>)set.get(e);
	    T[] ret;
	    if(e2 == null) {
		set.put(e, e);
		ret = arr;
	    } else {
		ret = e2.arr.get();
		if(ret == null) {
		    set.remove(e2);
		    set.put(e, e);
		    ret = arr;
		}
	    }
	    return(ret);
	}
    }
}
