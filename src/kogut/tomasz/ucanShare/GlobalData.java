package kogut.tomasz.ucanShare;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

import kogut.tomasz.ucanShare.fileSharing.FileChooser;
import kogut.tomasz.ucanShare.tools.files.LocalFileDescriptor;
import kogut.tomasz.ucanShare.tools.files.SharedFilesManager;
import kogut.tomasz.ucanShare.tools.networking.NetworkingInformation;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class GlobalData extends Application {

	private static ArrayList<LocalFileDescriptor> mSharedFolders = new ArrayList<LocalFileDescriptor>();
	private static SharedFilesManager sharedFilesManger = new SharedFilesManager();
	private final static String TAG = GlobalData.class.getName();

	
	@Override
	public void onCreate() {
		super.onCreate();


//		startService(new Intent(this, NetworkingService.class));
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		stopService(new Intent(this, NetworkingService.class));
	}
	
	
	public GlobalData() {
		mSharedFolders = FileChooser.readSharedPaths();

		rebuildSharedFiles();
	}

	private static void rebuildSharedFiles() {
		sharedFilesManger.reset();
		for (LocalFileDescriptor location : mSharedFolders) {
			sharedFilesManger.addLocation(location.getPath());
		}
		sharedFilesManger.buildDatabase();
	}

	
	public static synchronized void setSharedFolders(
			ArrayList<LocalFileDescriptor> sharedFolders) {
		mSharedFolders.clear();
		mSharedFolders.addAll(sharedFolders);
		rebuildSharedFiles();
		String msg = "Shared folders object set: " + mSharedFolders + " has "
				+ mSharedFolders.size() + " length";
		Log.d(TAG, msg);
	}
	
	public static synchronized File getFileById(int id) {
		return sharedFilesManger.getById(id);
	}

	public static synchronized ArrayList<LocalFileDescriptor> getSharedFolders() {
		
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
