package kogut.tomasz.ucanShare;

import java.util.ArrayList;

import kogut.tomasz.ucanShare.fileSharing.FileChooser;
import kogut.tomasz.ucanShare.tools.files.LocalFileDescriptor;
import kogut.tomasz.ucanShare.tools.files.SharedFilesManager;

import android.app.Application;
import android.util.Log;

public class GlobalData extends Application {

	private static ArrayList<LocalFileDescriptor> mSharedFolders = new ArrayList<LocalFileDescriptor>();
	private static SharedFilesManager sharedFilesManger = new SharedFilesManager();
	private final String TAG = GlobalData.class.getName();

	public GlobalData() {
		mSharedFolders = FileChooser.readSharedPaths();
		rebuildSharedFiles();
	}

	private void rebuildSharedFiles() {
		sharedFilesManger.reset();
		for (LocalFileDescriptor location : mSharedFolders) {
			sharedFilesManger.addLocation(location.getPath());
		}
		sharedFilesManger.buildDatabase();
	}

	public synchronized void setSharedFolders(
			ArrayList<LocalFileDescriptor> sharedFolders) {
		mSharedFolders.clear();
		mSharedFolders.addAll(sharedFolders);
		rebuildSharedFiles();
		String msg = "Shared folders object set: " + mSharedFolders + " has "
				+ mSharedFolders.size() + " length";
		Log.d(TAG, msg);
	}

	public synchronized ArrayList<LocalFileDescriptor> getSharedFolders() {
		
		ArrayList<LocalFileDescriptor> tmp = FileChooser.readSharedPaths();
		if(!mSharedFolders.equals(tmp)) {
			mSharedFolders = tmp;
			rebuildSharedFiles();
		}
		String msg = "Shared folders object returned: " + mSharedFolders
				+ " has " + mSharedFolders.size() + " length";
		Log.d(TAG, msg);
		return new ArrayList<LocalFileDescriptor>(mSharedFolders);
	}

	public synchronized SharedFilesManager getFilesManager() {
		return sharedFilesManger;
	}

}
