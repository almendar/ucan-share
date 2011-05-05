package kogut.tomasz.ucanShare.files;

public class LocalFileDescriptor implements Comparable<LocalFileDescriptor> {

	private final String mName;
	private final String mData;
	private final String mPath;

	public LocalFileDescriptor(String name, String data, String path) {
		mName = name;
		mData = data;
		mPath = path;
	}

	@Override
	public int compareTo(LocalFileDescriptor o) {
		if (this.mName != null) {
			return this.mName.toLowerCase()
					.compareTo(o.getName().toLowerCase());
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String getName() {
		return mName;
	}

	public String getData() {
		return mData;
	}

	public String getPath() {
		return mPath;
	}

}
