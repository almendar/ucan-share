package kogut.tomasz.ucanShare.networking.messages;

import java.io.Serializable;

public class DataChunkMessage implements Serializable {
	private static final long serialVersionUID = -364426092756113007L;



	public DataChunkMessage(String filename, byte[] data, int nrOfBytes,
			long offset) {
		mFilename = filename;
		mOffset = offset;
		if (data.length != nrOfBytes) {
			mData = new byte[nrOfBytes];
			for (int i = 0; i < nrOfBytes; i++) {
				mData[i] = data[i];
			}
		} else {
			mData = data;
		}
	}

	public final byte[] mData;
	public final String mFilename;
	public final long mOffset;
}
