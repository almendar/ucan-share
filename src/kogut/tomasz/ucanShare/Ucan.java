package kogut.tomasz.ucanShare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Ucan extends Activity {
	/** Called when the activity is first created. */

	private final static String TAG = Ucan.class.toString();
	private Context mContext;
	private Button mBtnSendBroadcast;
	private Button mBtnReceiveBroadcast;
	private Button mBtnStartFileChooser;
	private OnClickListener mActionBroadcast = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			int id = arg0.getId();
			Intent intent = new Intent(Ucan.this, BroadcastHandle.class);
			switch (id) {
			case R.id.btn_receive_broadcast:
				intent.putExtra(BroadcastHandle.EXTRA_ACTION, BroadcastHandle.ACTION_RCV);
				
				break;
			case R.id.btn_send_broadcast:
				intent.putExtra(BroadcastHandle.EXTRA_ACTION, BroadcastHandle.ACTION_SEND);
				break;
			default:
				break;
			}
			Toast pieceToast = Toast.makeText(mContext, String.valueOf(id),
					Toast.LENGTH_SHORT);
			pieceToast.show();
			startActivity(intent);
		}
	};
	
	
	private OnClickListener mActionStartFileChooser = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Intent myIntent = new Intent(Ucan.this, FileChooser.class);
			startActivity(myIntent);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
		bindEvents();
	}

	private void init() {
		mContext = getApplicationContext();
		mBtnSendBroadcast = (Button) findViewById(R.id.btn_send_broadcast);
		mBtnReceiveBroadcast = (Button) findViewById(R.id.btn_receive_broadcast);
		mBtnStartFileChooser = (Button) findViewById(R.id.btn_start_filechooser);

	}

	private void bindEvents() {
		mBtnReceiveBroadcast.setOnClickListener(mActionBroadcast);
		mBtnSendBroadcast.setOnClickListener(mActionBroadcast);
		mBtnStartFileChooser.setOnClickListener(mActionStartFileChooser);
	}
}
