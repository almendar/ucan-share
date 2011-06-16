package kogut.tomasz.ucanShare;

import kogut.tomasz.ucanShare.NetworkingService.LocalBinder;
import kogut.tomasz.ucanShare.fileSearch.NetworkSearch;
import kogut.tomasz.ucanShare.fileSharing.FileChooser;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TabHost;

public class Ucan extends TabActivity {

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "[Create]");
		setContentView(R.layout.main);
		createTabs();
		startService(new Intent(this, NetworkingService.class));
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "[Pause]");
		unbindFromNetworkService();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "[Restart]");
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindFromNetworkService();
		Log.d(TAG, "[Stop]");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "[Resume]");
		bindToNetworkService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "[Destroy]");
		stopService(new Intent(this, NetworkingService.class));
	}

	private void unbindFromNetworkService() {
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
			Log.d(TAG, "Networking service was unbound.");
		}

	}

	private void bindToNetworkService() {
		Intent intent = new Intent(this, NetworkingService.class);
		mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "Network service was bound");
	}

	/**
	 * Adds tabs to this activity.
	 */
	private void createTabs() {
		// Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, FileChooser.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("filechooser").setIndicator("File\nChooser")
				.setContent(intent);
		tabHost.addTab(spec);

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, SharedFolders.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("Shared files").setIndicator("Shared\nfiles")
				.setContent(intent);
		tabHost.addTab(spec);

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, LocalFileSearch.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("Local\nsearch").setIndicator("Local\nsearch")
				.setContent(intent);
		tabHost.addTab(spec);

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, NetworkSearch.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("Network search")
				.setIndicator("Network\nsearch").setContent(intent);
		tabHost.addTab(spec);
	}

	private final static String TAG = Ucan.class.getName();
	private NetworkingService mService;
	private boolean mBound;
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

}
