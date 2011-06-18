package kogut.tomasz.ucanShare.fileSharing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;

import kogut.tomasz.ucanShare.GlobalData;
import kogut.tomasz.ucanShare.R;
import kogut.tomasz.ucanShare.R.layout;
import kogut.tomasz.ucanShare.R.string;
import kogut.tomasz.ucanShare.tools.files.LocalFileDescriptor;
import android.app.Application;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooser extends ListActivity {

	private final static String CACHE_DIR = "/Android/data/kogut.tomasz.ucanShare/cache/";
	private final static String SHARED_PATHS_FILE_NAME = "shared_files.qwr";

	private final static String TAG = FileChooser.class.getName();
	BroadcastReceiver mExternalStorageReceiver;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private final int MENU_ADD = Menu.FIRST;
	private final int MENU_DISCARD = Menu.FIRST + 1;
	private File mCurrentDir;
	FileArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "[Create] Activity created");
		getListView().setEmptyView(findViewById(R.layout.empty_list));
		updateExternalStorageState();
		startWatchingExternalStorage();
		mAdapter = new FileArrayAdapter(this, R.layout.file_view);
		GlobalData gData = (GlobalData)getApplication();
		mAdapter.setMarkedFilesCopy(gData.getSharedFolders());
		mAdapter.setMarkedFilesCopy(readSharedPaths());
		mAdapter.notifyDataSetChanged();
		this.setListAdapter(mAdapter);
		fill();

	}

	@Override
	public void onStop() {
		super.onStop();
	
		Log.d(TAG,"[Stop]");
	}

	/** Called when the activity looses focus **/
	@Override
	public void onPause() {
		super.onPause();
		storeSharedPaths(mAdapter.getMarkedFilesCopy());
		Log.d(TAG,"[Pause]");
		
	}

//	@Override
//	protected void onResume() {
//		super.onResume();
//		mAdapter.setMarkedFilesCopy(readSharedPaths());
//		Log.d(TAG,"[Resume]");
//	}

	private void fill() {
		mAdapter.clear();
		if (mCurrentDir == null) {
			return;
		}
		File[] dirs = mCurrentDir.listFiles();
		// this.setTitle("Current dir:" + mCurrentDir.getName());
		ArrayList<LocalFileDescriptor> dir = new ArrayList<LocalFileDescriptor>();
		ArrayList<LocalFileDescriptor> fls = new ArrayList<LocalFileDescriptor>();
		for (File f : dirs) {
			if (f.isDirectory()) {

				dir.add(new LocalFileDescriptor(f.getName(), "Folder", f
						.getAbsolutePath()));
			} else {
				fls.add(new LocalFileDescriptor(f.getName(), "File size:"
						+ f.length(), f.getAbsolutePath()));
			}
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (!mCurrentDir.getName().equalsIgnoreCase("sdcard")) {
			dir.add(0, new LocalFileDescriptor("..", "Parent directory",
					mCurrentDir.getParent()));
		}
		for (LocalFileDescriptor desc : dir) {
			mAdapter.add(desc);
		}
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(getString(R.string.menu_add_folder_to_share));
		menu.add(0, MENU_ADD, 0, getString(R.string.yes));
		menu.add(0, MENU_DISCARD, 0, getString(R.string.no));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean ret = super.onContextItemSelected(item);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final LocalFileDescriptor o = mAdapter.getItem(info.position);
		switch (item.getItemId()) {
		case MENU_ADD:
			mAdapter.addToMarked(o);
			break;
		case MENU_DISCARD:
			mAdapter.removedFromMarked(o);
			break;
		}
		return ret;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		LocalFileDescriptor descriptor = mAdapter.getItem(position);
		if (descriptor.getData().equalsIgnoreCase("folder")
				|| descriptor.getData().equalsIgnoreCase("parent directory")) {
			descriptor.setChecked(!descriptor.isChecked());
			mCurrentDir = new File(descriptor.getPath());
			fill();
		} else {
			onFileClick(descriptor);
		}
	}

	void handleExternalStorageState(boolean available, boolean writeable) {
		if (!available) {
			mCurrentDir = null;
			fill();
		} else {
			mCurrentDir = Environment.getExternalStorageDirectory();
		}
	}

	void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		handleExternalStorageState(mExternalStorageAvailable,
				mExternalStorageWriteable);
	}

	void startWatchingExternalStorage() {
		mExternalStorageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i("test", "Storage: " + intent.getData());
				updateExternalStorageState();
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		registerReceiver(mExternalStorageReceiver, filter);
		updateExternalStorageState();
	}

	public static ArrayList<LocalFileDescriptor> readSharedPaths() {

		File f = android.os.Environment.getExternalStorageDirectory();
		File shared_file = new File(f.getAbsoluteFile() + CACHE_DIR
				+ SHARED_PATHS_FILE_NAME);
		ArrayList<LocalFileDescriptor> paths = null;
		InputStream in;
		try {
			in = new FileInputStream(shared_file);
			InputStream buffer = new BufferedInputStream(in);
			ObjectInput ois = new ObjectInputStream(buffer);
			paths = (ArrayList<LocalFileDescriptor>) ois.readObject();
			ois.close();
			Log.i(TAG, "Shared paths were succesfully loaded. Number of records loaded: "+paths.size());
		} catch (FileNotFoundException e) {
			Log.w(TAG, e.getMessage());
		} catch (StreamCorruptedException e) {
			Log.w(TAG, e.getMessage());
		} catch (IOException e) {
			Log.w(TAG, e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.w(TAG, e.getMessage());

		} finally {
			if (paths == null) {
				paths = new ArrayList<LocalFileDescriptor>();
				Log.i(TAG, "Empty shared locations returned");
			}

		}
		return paths;

	}

	
	
	
	static void storeSharedPaths(ArrayList<LocalFileDescriptor> paths) {
		File f = Environment.getExternalStorageDirectory();
		File shared_file = new File(f.getAbsoluteFile() + CACHE_DIR
				+ SHARED_PATHS_FILE_NAME);
		try {

			if (!shared_file.getParentFile().exists()) {
				boolean ret = shared_file.getParentFile().mkdirs();
				Log.i(TAG, "Attempt to create directory for data was: "+ (ret ? "succes" : "failed"));
			}

			if (!shared_file.exists()) {
				shared_file.createNewFile();
			}
			OutputStream out;
			out = new FileOutputStream(shared_file);
			OutputStream buffer = new BufferedOutputStream(out);
			ObjectOutputStream oos = new ObjectOutputStream(buffer);
			oos.writeObject(paths);
			oos.close();
			Log.i(TAG, "Paths file was saved. Number of records saved: " + paths.size());
		} catch (FileNotFoundException e) {
			Log.w(TAG, e.getMessage());
		} catch (IOException e) {
			Log.w(TAG, e.getMessage());
		} finally {
		}

	}

	void stopWatchingExternalStorage() {
		unregisterReceiver(mExternalStorageReceiver);
	}

	private void onFileClick(LocalFileDescriptor o) {
		Toast.makeText(this, "File Clicked: " + o.getName(), Toast.LENGTH_SHORT)
				.show();
	}

}