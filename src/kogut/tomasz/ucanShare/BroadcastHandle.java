package kogut.tomasz.ucanShare;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;

import kogut.tomasz.ucanShare.networking.NetworkInfo;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class BroadcastHandle extends Activity {

	private static final String LOG_TAG = BroadcastHandle.class.toString();
	private final int PORT = 10001;
	MulticastSocket mMulticastSocket;
	Context mContext;
	NetworkInfo mNetworkInfo;
	TextView mTextView;
	Handler mHandler = new Handler();
	int mCounter = 0;
	String mMsg;
	byte[] mRcvData;
	public final static String EXTRA_ACTION = "ACTION";
	public final static int ACTION_SEND = 0x0;
	public final static int ACTION_RCV = 0x1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.broadcast_handle);
		init();
		bind();
	}

	private void bind() {
		int action = getIntent().getExtras().getInt(EXTRA_ACTION);
		switch (action) {
		case ACTION_SEND:
			new Thread(mSendRunnable).start();
			break;
		case ACTION_RCV:
			new Thread(mRcvRunnable).start();
			break;
		default:
			break;
		}
	}

	private void init() {
		mContext = getApplicationContext();
		mNetworkInfo = new NetworkInfo(mContext);
		try {
			mMulticastSocket = new MulticastSocket(PORT);
		} catch (IOException e) {
			Log.d(LOG_TAG, e.toString());
		}
		try {
			mMulticastSocket.joinGroup(mNetworkInfo.getBroadcastAdress());
		} catch (IOException e) {
			Log.d(LOG_TAG, e.toString());
		}
		mTextView = (TextView) findViewById(R.id.brodcast_log);
	}

	private Runnable mSendRunnable = new Runnable() {

		@Override
		public void run() {
			final String myIpAdress = mNetworkInfo
					.getLocalIpAdressTextRepresentation();
			while (true) {
				mCounter = new Random().nextInt();
				DatagramPacket packet = new DatagramPacket(Integer.toString(
						mCounter).getBytes(), 4,
						mNetworkInfo.getBroadcastAdress(), PORT);
				try {
					mMulticastSocket.send(packet);
					Thread.sleep(2000);

					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mTextView.append(String.valueOf(mCounter) + ' ');

						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private Runnable mRcvRunnable = new Runnable() {

		@Override
		public void run() {
			final byte[] data = new byte[4];
			while (true) {

				final DatagramPacket packet = new DatagramPacket(data,
						data.length, mNetworkInfo.getBroadcastAdress(), PORT);
				try {
					mMulticastSocket.setSoTimeout(1500);
					mMulticastSocket.receive(packet);
					// mMsg = packet.getAddress().getHostAddress();
					mMsg = new String(data);
				} catch (SocketTimeoutException timout) {
					mMsg = "Tomuœ znikn¹³";
				}

				catch (SocketException SockE) {
					SockE.printStackTrace();
				} catch (IOException IOe) {
					// TODO Auto-generated catch block
					IOe.printStackTrace();
				}

				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mTextView.setText(mMsg);
					}
				});
			}
		}
	};

}
