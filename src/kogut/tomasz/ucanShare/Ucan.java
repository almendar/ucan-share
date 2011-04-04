package kogut.tomasz.ucanShare;

import java.io.IOException;
import java.net.InetAddress;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Ucan extends Activity {
    /** Called when the activity is first created. */
	
	private Context mContext;
	private Button mButton;
	private TextView mTextView;
	private OnClickListener mCorkcListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			try {
				getBroadcastAddress();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.w("", e);
			}
		}
	};
	
	
	void getBroadcastAddress() throws IOException {
	    WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();
	    // handle null somehow
	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    String a = InetAddress.getByAddress(quads).toString();
	    mTextView.setText("This is broadcast adress: " + a.substring(1));
	    mButton.setEnabled(false);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
        bindEvents();
    }

	private void init() {
		mContext = getApplicationContext();
		mButton = (Button) findViewById(R.id.button);
		Log.e(ACCESSIBILITY_SERVICE, "Button is null? " + String.valueOf(mButton==null));
		mTextView = (TextView) findViewById(R.id.textview);
	}

	private void bindEvents() {
        mButton.setOnClickListener(mCorkcListner);
	}
}

