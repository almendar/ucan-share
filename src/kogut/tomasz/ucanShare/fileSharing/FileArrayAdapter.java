package kogut.tomasz.ucanShare.fileSharing;

import java.util.ArrayList;
import java.util.List;

import kogut.tomasz.ucanShare.R;
import kogut.tomasz.ucanShare.tools.files.LocalFileDescriptor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.MergeCursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileArrayAdapter extends ArrayAdapter<LocalFileDescriptor> {

	private Context c;
	private int id;
	private ArrayList<LocalFileDescriptor> mMarked;



	public FileArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		c = context;
		id = textViewResourceId;
		mMarked = new ArrayList<LocalFileDescriptor>();
	}

	public void addToMarked(LocalFileDescriptor toBeMarked) {
		mMarked.add(toBeMarked);
		notifyDataSetChanged();
	}

	public void removedFromMarked(LocalFileDescriptor toBeUnmarked) {
		mMarked.remove(toBeUnmarked);
		notifyDataSetChanged();
	}
	
	public ArrayList<LocalFileDescriptor> getMarkedFilesCopy() {
		ArrayList<LocalFileDescriptor> ret = new ArrayList<LocalFileDescriptor>();
		for(LocalFileDescriptor desc : mMarked) {
			ret.add(desc);
		}
		return ret;
	}
	
	public void setMarkedFilesCopy(ArrayList<LocalFileDescriptor> markedFiles) {
		mMarked.clear();
		for(LocalFileDescriptor desc : markedFiles) {
			mMarked.add(desc);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		LayoutInflater vi = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(id, null);
		final LocalFileDescriptor o = getItem(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.TextView01);
			TextView t2 = (TextView) v.findViewById(R.id.TextView02);

			if (t1 != null) {
				t1.setText(o.getName());
				if (mMarked.contains(o)) {
					t1.setBackgroundResource(R.color.red);
					Toast.makeText(getContext(), mMarked.size() + " ",
							Toast.LENGTH_SHORT).show();
				}

			}
			if (t2 != null) {
				t2.setText(o.getData());
			}
		}
		return v;
	}
}
