package kogut.tomasz.ucanShare;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kogut.tomasz.ucanShare.fileSearch.ProcessFileQueries;
import kogut.tomasz.ucanShare.fileSearch.SearchRequest;
import kogut.tomasz.ucanShare.fileSearch.SearchResultMessage;
import kogut.tomasz.ucanShare.fileSearch.SendSearchResultTask;
import kogut.tomasz.ucanShare.tools.files.FileDescription;
import kogut.tomasz.ucanShare.tools.files.SharedFilesManager;
import kogut.tomasz.ucanShare.tools.networking.MulticastServer;
import kogut.tomasz.ucanShare.tools.networking.NetworkInfo;

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
	private NetworkInfo mNetworkInfo;
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
		mNetworkInfo = new NetworkInfo(getApplicationContext());
		new Thread(mQueriesProcessor).start();
		new Thread(rcvFileQueries).start();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG,"Bind request");
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

	public void sendSearchRequest(String fileNameSearch) {
		SearchRequest sr = new SearchRequest(fileNameSearch,
				mNetworkInfo.getLocalIpAdress());
		mMulticastServer.sendBroadcast(sr);
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


