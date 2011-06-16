package kogut.tomasz.ucanShare.networking.messages;

import java.io.Serializable;

public class NegotationMessage implements Serializable {
	private static final long serialVersionUID = 8941314351093784827L;
	public static final int ACCEPT = 0x00;
	public static final int REJECT = 0x01;
	public static final int ASK = 0x02;
	private int type;
	private int id;
	private String data;
	
	public NegotationMessage(int type) {
		this.setType(type);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
	
	

}
