package kogut.tomasz.ucanShare;

import java.util.ArrayList;

import kogut.tomasz.ucanShare.files.LocalFileDescriptor;

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

		ArrayList<LocalFileDescriptor> sharedFolders = FileChooser
				.readSharedPaths();
		ArrayList<String> sharedFilesStringRepresentation = new ArrayList<String>();
		if (sharedFolders != null) {
			for (LocalFileDescriptor desc : sharedFolders) {
				sharedFilesStringRepresentation.add(desc.getPath());
			}
		}
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this,
				R.layout.shared_files_list, R.id.share_files,
				sharedFilesStringRepresentation);
		setListAdapter(mAdapter);
	}
}
