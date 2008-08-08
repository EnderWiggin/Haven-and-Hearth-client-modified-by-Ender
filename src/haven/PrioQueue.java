package haven;

import java.util.*;

public class PrioQueue<E extends Prioritized> extends LinkedList<E> {
    public E peek() {
	E rv = null;
	int mp = 0;
	for(E e : this) {
	    int ep = e.priority();
	    if((rv == null) || (ep > mp)) {
		mp = ep;
		rv = e;
	    }
	}
	return(rv);
    }
    
    public E element() {
	E rv;
	if((rv = peek()) == null)
	    throw(new NoSuchElementException());
	return(rv);
    }
    
    public E poll() {
	E rv = peek();
	remove(rv);
	return(rv);
    }
    
    public E remove() {
	E rv;
	if((rv = poll()) == null)
	    throw(new NoSuchElementException());
	return(rv);
    }
}
