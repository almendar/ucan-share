package kogut.tomasz.ucanShare;

import java.util.ArrayList;

import kogut.tomasz.ucanShare.files.LocalFileDescriptor;

import android.app.Application;
import android.util.Log;

public class GlobalData extends Application {
	
	private static ArrayList<LocalFileDescriptor> mSharedFolders = new ArrayList<LocalFileDescriptor>();
	private final String TAG = GlobalData.class.getName();

	public GlobalData() {
		mSharedFolders = FileChooser.readSharedPaths();
	}
	
	public synchronized void  setSharedFolders(ArrayList<LocalFileDescriptor> sharedFolders) {
		mSharedFolders.clear();
		mSharedFolders.addAll(sharedFolders);
		String msg = "Shared folders object set: " + mSharedFolders + " has " + mSharedFolders.size() + " length"; 
		Log.d(TAG,msg);
	}

	public synchronized ArrayList<LocalFileDescriptor> getSharedFolders() {
		String msg = "Shared folders object returned: " + mSharedFolders + " has " + mSharedFolders.size() + " length";
		Log.d(TAG,msg);
		return new ArrayList<LocalFileDescriptor>(mSharedFolders);
	}

}
