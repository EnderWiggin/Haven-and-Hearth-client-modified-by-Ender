package haven;

public class FormatException extends RuntimeException {
	String res;
	
	public FormatException(String msg, String res) {
		super(msg);
		this.res = res;
	}
}
