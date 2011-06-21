package kogut.tomasz.ucanShare;

import kogut.tomasz.ucanShare.fileUpload.DownloadFileTask;

public class ActiveDownloadDescription {
	final String mFileName;

	final int mId;

	private DownloadFileTask task;

	public ActiveDownloadDescription(DownloadFileTask task, String fileName,
			int percentageDone, int id) {
		this.mFileName = fileName;
		this.task = task;
		this.mId = id;
	}

	@Override
	public String toString() {
		return task.getStatusString() + ": " + mFileName + " - "
				+ task.getPercentageTaskDone() + "%";
	}

	public int getId() {
		return mId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ActiveDownloadDescription))
			return false;

		ActiveDownloadDescription other = (ActiveDownloadDescription) o;
		if (other.getId() == getId()) {
			return true;
		}
		else {
			return false;
		}

	}
}
