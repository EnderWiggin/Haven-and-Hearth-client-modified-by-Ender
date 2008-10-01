package haven;

import java.util.*;

public class I2<T> implements Iterator<T> {
    private Iterator<Iterator<T>> is;
    private Iterator<T> cur;
    private T co;
    private boolean hco;
	
    public I2(Iterator<T>... is) {
	this.is = Arrays.asList(is).iterator();
	f();
    }

    public I2(Collection<Iterator<T>> is) {
	this.is = is.iterator();
	f();
    }
	
    private void f() {
	while(true) {
	    if((cur == null) && !is.hasNext()) {
		co = null;
		hco = false;
		return;
	    }
	    if(cur == null) {
		cur = is.next();
		continue;
	    }
	    if(!cur.hasNext()) {
		cur = null;
	    } else {
		co = cur.next();
		hco = true;
		return;
	    }
	}
    }
	
    public boolean hasNext() {
	return(hco);
    }
	
    public T next() {
	if(!hco)
	    throw(new NoSuchElementException());
	T ret = co;
	f();
	return(ret);
    }
	
    public void remove() {
	throw(new UnsupportedOperationException());
    }
}
