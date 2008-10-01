package haven.test;

public class RobotException extends RuntimeException {
    public Robot bot;
    
    public RobotException(Robot bot, String msg, Throwable cause) {
	super(bot.c.user + ": " + msg, cause);
	this.bot = bot;
    }

    public RobotException(Robot bot, String msg) {
	this(bot, msg, null);
    }
}
