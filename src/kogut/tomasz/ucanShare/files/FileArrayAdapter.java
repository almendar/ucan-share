package kogut.tomasz.ucanShare.files;

import java.util.List;

import kogut.tomasz.ucanShare.R;

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
	private List<LocalFileDescriptor> items;
	public  List<LocalFileDescriptor> marked;
	public FileArrayAdapter(Context context, int textViewResourceId,
			List<LocalFileDescriptor> objects, List<LocalFileDescriptor> markedObjects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
		marked = markedObjects;
	}

	@Override
	public LocalFileDescriptor getItem(int position) {
		return items.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		final LocalFileDescriptor o = items.get(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.TextView01);
			TextView t2 = (TextView) v.findViewById(R.id.TextView02);

			if (t1 != null) {
				t1.setText(o.getName());
				if(marked.contains(o)) {
					t1.setBackgroundResource(R.color.red);
				}

			}
			if (t2 != null) {
				t2.setText(o.getData());
			}

		}
		return v;
	}
}
