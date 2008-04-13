package haven;

import java.util.*;

public class WeightList<T> {
    List<T> c;
    List<Integer> w;
    int tw = 0;
    
    public WeightList() {
	c = new ArrayList<T>();
	w = new ArrayList<Integer>();
    }
    
    public void add(T c, int w) {
	this.c.add(c);
	this.w.add(w);
	tw += w;
    }
    
    public T pick(Random gen) {
	int p = gen.nextInt() % tw;
	int i = 0;
	while(true) {
	    if((p -= w.get(i)) <= 0)
		break;
	    i++;
	}
	return(c.get(i));
    }
}
