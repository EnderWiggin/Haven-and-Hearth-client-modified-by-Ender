package haven.error;

public interface ErrorStatus {
    public void goterror(Throwable t);
    public void connecting();
    public void sending();
    public void done();
    public void senderror(Exception e);
    
    public static class Simple implements ErrorStatus {
	public void goterror(Throwable t) {
	    System.err.println("Caught error: " + t);
	}
	
	public void connecting() {
	    System.err.println("Connecting to error server");
	}
	
	public void sending() {
	    System.err.println("Sending error");
	}
	
	public void done() {
	    System.err.println("Done");
	}
	
	public void senderror(Exception e) {
	    System.err.println("Error while sending error:");
	    e.printStackTrace(System.err);
	}
    }
}
