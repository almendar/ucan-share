package kogut.tomasz.ucanShare.tools.files;

import java.io.Serializable;

import android.test.IsolatedContext;

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
		StringBuilder bs = new StringBuilder();
		bs.append(getFileName()).append("\nsize:").append(getFileSize());
		return bs.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof FileDescription)) {
			return false;
		}

		FileDescription other = (FileDescription) o;
		boolean test1 = this.mFileName.equals(other.mFileName);
		boolean test2 = this.mFileSize == other.mFileSize;
		boolean test3 = this.mId == other.mId;

		if (test1 && test2 && test3) {
			return true;
		}
		else {
			return false;
		}

	}

	private final int mId;
	private final String mFileName;
	private final long mFileSize;

}
