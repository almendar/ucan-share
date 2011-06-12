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
	     Intent myIntent = new Intent();
        myIntent = this.getIntent();
        Bundle extras = myIntent.getExtras();
        if(extras!=null)
        	Log.d(SharedFolders.class.toString(), "" + extras.size());
        else
        	Log.d(SharedFolders.class.toString(), "Extras are empty");
        	
        
        try {
        	this.sharedFiles = (ArrayList<LocalFileDescriptor>) extras.get("sharedFiles");	
        }catch (NullPointerException e) {
			sharedFiles = new ArrayList<LocalFileDescriptor>();
		}
        
        ArrayAdapter<LocalFileDescriptor> mAdapter = new ArrayAdapter<LocalFileDescriptor>(this, R.layout.shared_files_list,sharedFiles);
        

	}
	
	

}
