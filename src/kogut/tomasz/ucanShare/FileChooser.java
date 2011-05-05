package kogut.tomasz.ucanShare;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import kogut.tomasz.ucanShare.files.FileArrayAdapter;
import kogut.tomasz.ucanShare.files.LocalFileDescriptor;
import android.app.ListActivity;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooser extends ListActivity {

	private File mCurrentDir;
	FileArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCurrentDir = new File("/sdcard/");
		fill();
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
				dir);
		this.setListAdapter(mAdapter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		LocalFileDescriptor o = mAdapter.getItem(position);
		if (o.getData().equalsIgnoreCase("folder")
				|| o.getData().equalsIgnoreCase("parent directory")) {
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
