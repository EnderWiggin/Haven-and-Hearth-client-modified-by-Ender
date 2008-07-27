package haven;

public class SprData extends GAttrib {
	private Message data;
	
	public SprData(Gob gob, Message data) {
		super(gob);
		this.data = data;
	}
	
	public Message data() {
		return(data.clone());
	}
}
