package kogut.tomasz.ucanShare.tools.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import kogut.tomasz.ucanShare.tools.networking.NetworkConnection;
import android.util.Log;

public class TcpServer implements Runnable {

	private final static String TAG = TcpServer.class.getName();
	public final static int FILE_TRANSFER_PORT = 4000;
	public final static int SEARCH_PORT = 4500;
	protected ServerSocket mSocket;
	private LinkedList<NetworkConnection> activeConnections;

	public TcpServer(int port) throws IOException {
		mSocket = new ServerSocket(port);
		activeConnections = new LinkedList<NetworkConnection>();
	}

	public void stopServer() {
		try {
			for (NetworkConnection c : activeConnections) {
				c.close();
			}
			activeConnections.clear();
		}catch (IOException e) {
			//nothing to do with this
		}
		finally {
			try {
				mSocket.close();
			}
			catch (IOException e) {
				//nothing to do with this 
			}
		}
	}

	public void startConnectionJob(NetworkConnection connection) {
		activeConnections.add(connection);
		new Thread(connection).start();
	}

	public void removeFromActiveConnectionList(NetworkConnection connection) {
		activeConnections.remove(connection);
	}

	@Override
	public void run() {
		Log.d(TAG,
				String.format("TCP server started at port %d",
						mSocket.getLocalPort()));
		while (true) {
			Log.d(TAG, "Waiting for connections...");
			Socket clientConnection;
			try {
				clientConnection = mSocket.accept();
				Log.d(TAG,
						"Connection from client:"
								+ clientConnection.getInetAddress());
				NetworkConnection NC = new NetworkConnection(this);
				NC.setSocket(clientConnection);
				startConnectionJob(NC);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
