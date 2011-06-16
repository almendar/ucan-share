package kogut.tomasz.ucanShare.networking;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kogut.tomasz.ucanShare.GlobalData;
import kogut.tomasz.ucanShare.files.FileDescription;
import kogut.tomasz.ucanShare.files.SharedFilesManager;
import kogut.tomasz.ucanShare.networking.messages.SearchRequest;
import kogut.tomasz.ucanShare.networking.messages.SearchResultMessage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NetworkingService extends Service {

	private final static String TAG = NetworkingService.class.getName();
	final int MAX_SEARCH_REQUESTS = 10;
	BlockingQueue<SearchRequest> mFileSearchRequests = new ArrayBlockingQueue<SearchRequest>(
			MAX_SEARCH_REQUESTS);

	private final IBinder mBinder = new LocalBinder();
	private MulticastServer mMulticastServer;
	private Hashtable<String, List<SearchResultMessage>> mSearchResults;
	private Runnable rcvFileQueries = new Runnable() {
		@Override
		public void run() {
			mMulticastServer.listenToBroadcast();
		}
	};

	private ProcessFileQueries mQueriesProcessor;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "[Created]");
		GlobalData gd = (GlobalData) getApplication();
		try {
			mMulticastServer = new MulticastServer(getApplicationContext(),
					mFileSearchRequests);

		} catch (IOException e) {
			Log.w(TAG, "Could not create multicast server");
		}
		mQueriesProcessor = new ProcessFileQueries(gd.getFilesManager(),
				mFileSearchRequests);
		new Thread(mQueriesProcessor).start();
		new Thread(rcvFileQueries).start();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public void addSearchResult(SearchResultMessage searchResult) {
		String key = searchResult.getQuery();
		if (mSearchResults.contains(key))
			mSearchResults.get(key).add(searchResult);
		else {
			ArrayList<SearchResultMessage> tmp = new ArrayList<SearchResultMessage>();
			tmp.add(searchResult);
			mSearchResults.put(key, tmp);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "[Destroyed]");
		mMulticastServer.stopListeningToBroadcast();
	}

	public void showServiceToast() {
		Toast.makeText(getApplicationContext(),
				"NetworkingService toast! Cheers!", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public NetworkingService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return NetworkingService.this;
		}
	}

}

class ProcessFileQueries implements Runnable {

	private final String TAG = ProcessFileQueries.class.getName();
	private volatile boolean running = false;
	private SharedFilesManager mFileManager;
	private BlockingQueue<SearchRequest> mSearchRequests;

	public ProcessFileQueries(SharedFilesManager fileManager,
			BlockingQueue<SearchRequest> searchRequests) {
		mFileManager = fileManager;
		mSearchRequests = searchRequests;
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			SearchRequest request=null;
			try {
				request = mSearchRequests.take();
				Log.d(TAG, "Servicing broadcast from: "
						+ request.getSenderAdress().getHostAddress());
				final LinkedList<FileDescription> searchResult = mFileManager
						.findFiles(request.getFileName());
				SendSearchResultTask sendAnswerTask = new SendSearchResultTask(
						request.getSenderAdress(), searchResult,
						request.getFileName());
				Thread t = new Thread(sendAnswerTask);
				t.start();
			} catch(ConnectException e) {
				Log.i(TAG, "Couldn't send search result to client: " + e.getLocalizedMessage());
			}
			
			
			catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) { 
				e.printStackTrace();
			}
		}
	}
}
