package kogut.tomasz.ucanShare;

import java.util.ArrayList;
import java.util.List;

import kogut.tomasz.ucanShare.NetworkingService.LocalBinder;
import kogut.tomasz.ucanShare.fileUpload.DownloadFileTask;
import kogut.tomasz.ucanShare.tools.files.LocalFileDescriptor;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ActiveDownloads extends Activity {

	private final String TAG = ActiveDownloads.class.getName();
	private NetworkingService mService;
	private volatile boolean mBound;
	private ArrayAdapter<ActiveDownloadDescription> downloadAdapter;
	private NetworkingServiceConnection mConnection = new NetworkingServiceConnection();
	private ListView mDownloadList;
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.active_downloads);
		bindToNetworkService();
		mDownloadList = (ListView) findViewById(R.id.listView1);
		mDownloadList.setEmptyView(findViewById(R.id.emptyView));
		downloadAdapter = new ArrayAdapter<ActiveDownloadDescription>(this,
				R.layout.items_list);
		downloadAdapter.setNotifyOnChange(true);
		mDownloadList.setAdapter(downloadAdapter);
		registerForContextMenu(mDownloadList);
		new Thread(new Runnable() {

			@Override
			public void run() {

				while (isBound()) {
					while (mService == null) {

					}
					final List<ActiveDownloadDescription> ongoing = mService
							.getOngoingDownload();
					mHandler.post(new Runnable() {
						@Override
						public void run() {

							synchronized (downloadAdapter) {
								downloadAdapter.clear();
								for (ActiveDownloadDescription descNew : ongoing) {
									downloadAdapter.add(descNew);
								}

								downloadAdapter.notifyDataSetChanged();
							}
						}
					});

					try {
						Thread.sleep(300);
					}
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindFromNetworkService();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle("Clear position?");
		String[] menuItems = { "Yes", "No" };
		for (int i = 0; i < menuItems.length; i++) {
			menu.add(0, Menu.FIRST + i, i, menuItems[i]);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean ret = super.onContextItemSelected(item);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int itemId = item.getItemId();
		if (itemId == Menu.FIRST) { // Yes
			ActiveDownloadDescription dft = downloadAdapter
					.getItem(info.position);
			Log.d(TAG, "Discard download of file " + dft.mFileName);
			downloadAdapter.remove(dft);
			mService.discardDownloadTask(dft.getId());

		}
		else {// No
			Log.d(TAG, "Context menu canceled");
		}
		return ret;
	}

	private void bindToNetworkService() {
		if (!isBound()) {
			Intent intent = new Intent(this, NetworkingService.class);
			getApplicationContext().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);
			setBound(true);
		}
		Log.d(TAG, "Network service was bound-UCAN");
	}

	private void unbindFromNetworkService() {
		if (isBound()) {
			getApplicationContext().unbindService(mConnection);
			setBound(false);
			Log.d(TAG, "Networking service was unbound.");
		}

	}

	public void setBound(boolean mBound) {
		this.mBound = mBound;
	}

	public boolean isBound() {
		return mBound;
	}

	private final class NetworkingServiceConnection implements
			ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			setBound(true);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			setBound(false);
		}
	}
}
