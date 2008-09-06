package dolda.xiphutil;

public class FormatException extends java.io.IOException {
    public FormatException() {
	super("Invalid data format");
    }
	
    public FormatException(String msg) {
	super(msg);
    }
}
