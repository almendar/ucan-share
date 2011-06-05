package kogut.tomasz.ucanShare;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import kogut.tomasz.ucanShare.files.FileArrayAdapter;
import kogut.tomasz.ucanShare.files.FileDescription;
import kogut.tomasz.ucanShare.files.LocalFileDescriptor;
import android.app.ListActivity;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooser extends ListActivity {

	private final int MENU_ADD = Menu.FIRST;
	private final int MENU_DISCARD = Menu.FIRST + 1;
	private ArrayList<LocalFileDescriptor> mToAdd;
	private File mCurrentDir;
	FileArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCurrentDir = new File("/sdcard/");
		mToAdd = new ArrayList<LocalFileDescriptor>();
		fill();

		// ListView lView = getListView();

	}

	private void fill() {
		File[] dirs = mCurrentDir.listFiles();
		this.setTitle("Current dir:" + mCurrentDir.getName());

		List<LocalFileDescriptor> dir = new ArrayList<LocalFileDescriptor>();
		List<LocalFileDescriptor> fls = new ArrayList<LocalFileDescriptor>();
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

		mAdapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view,
				dir, mToAdd);
		this.setListAdapter(mAdapter);

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
			mToAdd.add(o);
			break;
		case MENU_DISCARD:
			mToAdd.remove(o);
			break;
		}
		fill();
		return ret;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		LocalFileDescriptor o = mAdapter.getItem(position);
		if (o.getData().equalsIgnoreCase("folder")
				|| o.getData().equalsIgnoreCase("parent directory")) {
			o.setChecked(!o.isChecked());
			mCurrentDir = new File(o.getPath());
			fill();
		} else {
			onFileClick(o);
		}
	}

	private void onFileClick(LocalFileDescriptor o) {
		Toast.makeText(this, "File Clicked: " + o.getName(), Toast.LENGTH_SHORT)
				.show();
	}

}
