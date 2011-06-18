package kogut.tomasz.ucanShare.tools.files;

import java.io.Serializable;

public class FileDescription implements Serializable {

	private static final long serialVersionUID = -5946887425185642705L;

	public FileDescription(int id, String fileName, long fileSize) {
		mId = id;
		mFileName = fileName;
		mFileSize = fileSize;

	}

	public int getId() {
		return mId;
	}

	public String getFileName() {
		return mFileName;
	}

	public long getFileSize() {
		return mFileSize;
	}
	
	@Override
	public String toString() {
		StringBuilder bs =new StringBuilder();
		bs.append(getFileName()).append("\nsize:").append(getFileSize());
		return bs.toString();
	}

	private final int mId;
	private final String mFileName;
	private final long mFileSize;

}
