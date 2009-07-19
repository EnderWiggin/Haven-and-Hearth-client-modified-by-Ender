package haven;

public interface Indir<T> extends Comparable<Indir<T>> {
    public T get();
    public void set(T val);
}
