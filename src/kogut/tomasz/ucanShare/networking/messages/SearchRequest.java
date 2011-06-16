package kogut.tomasz.ucanShare.networking.messages;

import java.io.Serializable;
import java.net.InetAddress;

public class SearchRequest implements Serializable {

	public SearchRequest(String fileName, InetAddress sender) {
		this.mFileName = fileName;
		this.mSenderAdress = sender;
	}

	public String getFileName() {
		return mFileName;
	}

	public InetAddress getSenderAdress() {
		return mSenderAdress;
	}

	private static final long serialVersionUID = 6894854650565694763L;
	private final String mFileName;
	private final InetAddress mSenderAdress;
}
