package kogut.tomasz.ucanShare.networking;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkInfo {

	private Context mContext;
	private final static String LOG_TAG = NetworkInfo.class.toString();
	private InetAddress mBroadcastAdress;
	private String mLocalIpAdress;

	public NetworkInfo(Context context) {
		mContext = context;
		resolveBroadcastAddress();
		resolveLocalIpAddress();
	}

	public InetAddress getBroadcastAdress() {
		return mBroadcastAdress;
	}

	public String getLocalIpAdress() {
		return mLocalIpAdress;
	}

	private void resolveBroadcastAddress() {
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		if (dhcp != null) {
			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			byte[] quads = new byte[4];
			for (int k = 0; k < 4; k++)
				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			try {
				mBroadcastAdress = InetAddress.getByAddress(quads);
			} catch (UnknownHostException e) {
				Log.d(LOG_TAG, e.toString());
				mBroadcastAdress = null;
			}
		} else {
			mBroadcastAdress = null;
		}

	}

	private void resolveLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						mLocalIpAdress = inetAddress.getHostAddress()
								.toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(LOG_TAG, ex.toString());
		}
		mLocalIpAdress = null;
	}
}
