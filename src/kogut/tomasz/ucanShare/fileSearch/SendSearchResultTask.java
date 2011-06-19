package kogut.tomasz.ucanShare.fileSearch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import kogut.tomasz.ucanShare.tools.files.FileDescription;
import kogut.tomasz.ucanShare.tools.networking.NetworkInfo;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;

public class SendSearchResultTask implements Runnable {

	private final static String TAG = SendSearchResultTask.class.getName();
	private LinkedList<FileDescription> mSearchResult;
	NetworkInfo mNetworkInfo;
	ObjectOutputStream oos = null;
	Socket mSocket = null;
	private InetAddress mToAdress;
	private final String query;

	public SendSearchResultTask(InetAddress toAdress,
			LinkedList<FileDescription> searchResult, String query)
			throws IOException {
		mSearchResult = searchResult;
		this.mToAdress = toAdress;
		this.query = query;
	}

	@Override
	public void run() {
		try {
			mSocket = new Socket(mToAdress, TcpServer.SEARCH_PORT);
			SearchResultMessage srm = new SearchResultMessage(mSocket.getLocalAddress(),mSearchResult,
					this.query);
			oos = new ObjectOutputStream(mSocket.getOutputStream());
			oos.writeObject(srm);
			oos.flush();
			Log.d(TAG, "Sent search result to:"+mToAdress.getHostAddress()+ " with matches:"+mSearchResult.size());
			Thread.sleep(300);
		}
		catch (IOException e) {
			Log.i(TAG,
					"Connection could not be stablished to send search result:"
							+ mToAdress.getHostAddress());
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (mSocket != null) {
					mSocket.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
