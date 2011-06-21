package kogut.tomasz.ucanShare;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kogut.tomasz.ucanShare.fileSearch.ProcessFileQueries;
import kogut.tomasz.ucanShare.fileSearch.SearchRequest;
import kogut.tomasz.ucanShare.fileSearch.SearchResultMessage;
import kogut.tomasz.ucanShare.fileUpload.DownloadFileTask;
import kogut.tomasz.ucanShare.fileUpload.FileUploader;
import kogut.tomasz.ucanShare.tools.networking.MulticastServer;
import kogut.tomasz.ucanShare.tools.networking.NetworkingInformation;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class NetworkingService extends Service implements NotifyMe {

	private final static String TAG = NetworkingService.class.getName();
	final int MAX_SEARCH_REQUESTS = 10;
	final int STARTING_MAX_DOWNLOADS = 100;
	BlockingQueue<SearchRequest> mFileSearchRequests = new ArrayBlockingQueue<SearchRequest>(
			MAX_SEARCH_REQUESTS);
	BlockingQueue<DownloadFileTask> activeDownloads = new ArrayBlockingQueue<DownloadFileTask>(
			STARTING_MAX_DOWNLOADS);
	private NetworkingInformation mNetworkInfo;
	private final IBinder mBinder = new LocalBinder();
	private MulticastServer mMulticastServer;
	private Hashtable<String, List<SearchResultMessage>> mSearchResults;

	private Runnable rcvFileQueries = new Runnable() {
		@Override
		public void run() {
			try {
				mMulticastServer.listenToBroadcast();
			}
			catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private ProcessFileQueries mQueriesProcessor;
	private FileUploader mFileUploder;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "[Created]");
		GlobalData gd = (GlobalData) getApplication();
		try {
			mMulticastServer = new MulticastServer(getApplicationContext(),
					mFileSearchRequests);
			mFileUploder = new FileUploader();
		}
		catch (IOException e) {
			Log.w(TAG, "Could not create servers");
		}
		mQueriesProcessor = new ProcessFileQueries(gd.getFilesManager(),
				mFileSearchRequests);
		mNetworkInfo = new NetworkingInformation(getApplicationContext());
		new Thread(mQueriesProcessor).start();
		new Thread(rcvFileQueries).start();
		new Thread(mFileUploder).start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "Bind request");
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

	public synchronized void addDownloadTask(DownloadFileTask task) {
		task.setListner(this);
		activeDownloads.add(task);
		new Thread(task).start();
	}

	public synchronized void discardDownloadTask(int id) {
		DownloadFileTask lookFor = null;
		for (DownloadFileTask task : activeDownloads) {
			if (task.getId() == id) {
				lookFor = task;
				break;
			}
		}
		if (lookFor != null) {
			lookFor.breakDownload();
			activeDownloads.remove(lookFor);

		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "[Destroyed]");
		mMulticastServer.stopListeningToBroadcast();
		mFileSearchRequests.clear();
		mFileUploder.stopServer();
		mQueriesProcessor.stop();
		for (DownloadFileTask downloadTask : activeDownloads) {
			downloadTask.breakDownload();
		}
	}

	public void sendSearchRequest(String fileNameSearch) {
		SearchRequest sr = new SearchRequest(fileNameSearch,
				mNetworkInfo.getLocalIpAdress());
		mMulticastServer.sendBroadcast(sr);
		Log.d(TAG, "Broadcast with search query" + fileNameSearch + " was send");
	}

	public synchronized List<ActiveDownloadDescription> getOngoingDownload() {
		LinkedList<ActiveDownloadDescription> ret = new LinkedList<ActiveDownloadDescription>();
		if (activeDownloads != null) {
			for (DownloadFileTask desc : activeDownloads) {
				ActiveDownloadDescription q = new ActiveDownloadDescription(
						desc, desc.getFileName(), desc.getPercentageTaskDone(),
						desc.getId());
				ret.add(q);
			}
		}
		return ret;
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

	@Override
	public synchronized void updateMe(Object sender, Object arg) {
		Log.d(TAG, "Observer update");
		DownloadFileTask dft = (DownloadFileTask) sender;
		String msg = (String) arg;
		if (msg == DownloadFileTask.FINISHED) {
			synchronized (this) {
				// activeDownloads.remove(dft);
			}

		}
		else if (msg == DownloadFileTask.FAILED) {
		}

	}

}
