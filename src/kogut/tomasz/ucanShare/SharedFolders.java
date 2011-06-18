package kogut.tomasz.ucanShare;

import java.util.ArrayList;

import kogut.tomasz.ucanShare.tools.files.LocalFileDescriptor;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class SharedFolders extends ListActivity {
	ArrayList<LocalFileDescriptor> sharedFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refreshPaths();
	}

	/**
	 * 
	 */
	private void refreshPaths() {
		GlobalData gData = (GlobalData) getApplication();
		ArrayList<LocalFileDescriptor> sharedFolders = gData.getSharedFolders();
		ArrayList<String> sharedFilesStringRepresentation = new ArrayList<String>();
		if (sharedFolders != null) {
			for (LocalFileDescriptor desc : sharedFolders) {
				sharedFilesStringRepresentation.add(desc.getPath());
			}
		}
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this,
				R.layout.items_list,
				sharedFilesStringRepresentation);
		setListAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshPaths();
	}
}
