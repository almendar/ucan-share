package kogut.tomasz.ucanShare.files;

import java.util.zip.CheckedInputStream;

import android.os.Parcel;
import android.os.Parcelable;

public class LocalFileDescriptor implements Comparable<LocalFileDescriptor> {

	private final String mName;
	private final String mData;
	private final String mPath;
	private boolean mChecked;

	public LocalFileDescriptor(String name, String data, String path) {
		mName = name;
		mData = data;
		mPath = path;
		mChecked = false;
	}

	@Override
	public int compareTo(LocalFileDescriptor o) {
		if (this.mName != null) {
			return this.getPath().toLowerCase()
					.compareTo(o.getPath().toLowerCase());
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

	public void setChecked(boolean checked) {
		this.mChecked = checked;
	}

	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LocalFileDescriptor) {
			LocalFileDescriptor other = (LocalFileDescriptor) o;
			if (other.getPath().equals(this.getPath())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}
}
