package kogut.tomasz.ucanShare;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class Ucan extends TabActivity {
	/** Called when the activity is first created. */

	private final static String TAG = Ucan.class.toString();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, FileChooser.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("filechooser").setIndicator("FileChooser")
				.setContent(intent);
		tabHost.addTab(spec);
		
		
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, SharedFolders.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("filechooser").setIndicator("Broadcasthandle")
				.setContent(intent);
		tabHost.addTab(spec);
	}

}
