package kogut.tomasz.ucanShare.tools.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import kogut.tomasz.ucanShare.fileSearch.SearchRequest;

import android.content.Context;
import android.test.IsolatedContext;
import android.util.Log;

public class MulticastServer {

	private MulticastSocket mSocket;
	private String mName;
	private NetworkingInformation mNetworkInfo;
	private Context mContext;
	volatile boolean listeningToBroadcast = false;
	private final static int PORT = 4445;
	private static final String TAG = MulticastServer.class.getName();
	private final BlockingQueue<SearchRequest> mSeachQueries;
	private final InetAddress mGroup  = InetAddress.getByName("224.0.0.1");
	public MulticastServer(Context context,
			BlockingQueue<SearchRequest> msgQueue) throws IOException {
		this(context, "MulticastServer", msgQueue);
	}

	public MulticastServer(Context context, String name,
			BlockingQueue<SearchRequest> searchQueries) throws IOException {
		super();
		mName = name;
		mSocket = new MulticastSocket(PORT);
		mNetworkInfo = new NetworkingInformation(context);
		mSocket.joinGroup(mGroup);
		mContext = context;
		mSeachQueries = searchQueries;

	}

	byte[] convertToBytes(Object object) throws IOException {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ObjectOutputStream oStream = new ObjectOutputStream(bStream);
		oStream.writeObject(object);
		byte[] byteVal = bStream.toByteArray();
		return byteVal;
	}

	Object convertToObject(final byte[] binaryData)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		ByteArrayInputStream bStream = new ByteArrayInputStream(binaryData);
		ObjectInputStream iStream = new ObjectInputStream(bStream);
		Object ret = iStream.readObject();
		return ret;
	}

	public void listenToBroadcast() throws UnknownHostException {
		if (listeningToBroadcast)
			return;
		listeningToBroadcast = true;
		byte[] data = new byte[1000];
		final InetAddress myIp = mNetworkInfo.getLocalIpAdress();
		while (listeningToBroadcast) {
			final DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				// mSocket.setSoTimeout(1500);
				mSocket.receive(packet);
				final InetAddress senderIp = packet.getAddress();
				if (myIp.equals(senderIp)) {
					Log.d(TAG, "Ignoring broadcast from myself.");
					continue;
				}
				else {
					SearchRequest bsm = (SearchRequest) convertToObject(packet
							.getData());
					mSeachQueries.put(bsm);
				}
			}
			catch (SocketException e) {
				// e.printStackTrace();
			}
			catch (IOException e) {
				// e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				// e.printStackTrace();
			}
			catch (InterruptedException e) {
				// e.printStackTrace();
			}
		}

	}

	public void stopListeningToBroadcast() {
		listeningToBroadcast = false;
		mSocket.close();
	}

	public void sendBroadcast(SearchRequest msg) {

		try {
			byte[] binaryData = convertToBytes(msg);
			final DatagramPacket packet = new DatagramPacket(binaryData,
					binaryData.length,mGroup,PORT);
			mSocket.send(packet);
		}
		catch (IOException e) {
			Log.w(TAG, "Couldn't send multicast message");
		}
	}
}
