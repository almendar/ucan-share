package kogut.tomasz.ucanShare;

import java.util.ArrayList;

import kogut.tomasz.ucanShare.files.FileDescription;
import kogut.tomasz.ucanShare.files.LocalFileDescriptor;
import kogut.tomasz.ucanShare.files.SharedFilesManager;
import android.R.anim;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class SearchLocalFiles extends Activity {

	private final static String TAG = SearchLocalFiles.class.getName();
	ArrayAdapter<String> adapter;
	Button mStartSearch;
	ListView mSeachResult;
	EditText mSearchInput;
	SharedFilesManager mFileSearcher;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "[Create]");
		setContentView(R.layout.search_result);
		mSearchInput = (EditText) findViewById(R.id.search_box);
		mStartSearch = (Button) findViewById(R.id.button1);
		mSeachResult = (ListView) findViewById(R.id.list);
		mFileSearcher = new SharedFilesManager();
		adapter = new ArrayAdapter<String>(
				SearchLocalFiles.this, R.layout.shared_files_list);
		adapter.setNotifyOnChange(true);
		mSeachResult.setAdapter(adapter);
		fill();
		mStartSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String text = mSearchInput.getText().toString();
				Log.d(TAG,"Clicked on button. Text on texedit:" + text);
				adapter.clear();
				adapter.add("Search for: " + text);
				for (FileDescription s : mFileSearcher.findFiles(text)) {
					adapter.add(s.getFileName());
				}
				Log.d(TAG,"Is adapter empty:" + adapter.isEmpty());
				adapter.notifyDataSetChanged();
			}
		});

	}

	private void fill() {
		GlobalData gd = (GlobalData) getApplication();
		ArrayList<LocalFileDescriptor> sharedPaths = gd.getSharedFolders();
		for (LocalFileDescriptor path : sharedPaths) {
			Log.d(TAG, "Added path:" + path.getPath());
			mFileSearcher.addLocation(path.getPath());
		}
		mFileSearcher.buildDatabase();
	}

}
