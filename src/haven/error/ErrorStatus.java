package haven.error;

public interface ErrorStatus {
    public void goterror(Throwable t);
    public void connecting();
    public void sending();
    public void done();
    public void senderror(Exception e);
}
