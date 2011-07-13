package haven;

public interface IHWindowParent {
    public void addwnd(HWindow wnd);
    public void remwnd(HWindow wnd);
    public void updurgency(HWindow wnd, int level);
    public void setawnd(HWindow wnd);
    public void setawnd(HWindow wnd, boolean focus);
    public HWindow getawnd();
}
