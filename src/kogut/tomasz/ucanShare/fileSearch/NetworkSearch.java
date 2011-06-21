package kogut.tomasz.ucanShare.fileSearch;

import java.io.IOException;
import java.io.OptionalDataException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import kogut.tomasz.ucanShare.NetworkingService;
import kogut.tomasz.ucanShare.NetworkingService.LocalBinder;
import kogut.tomasz.ucanShare.R;
import kogut.tomasz.ucanShare.fileUpload.DownloadFileTask;
import kogut.tomasz.ucanShare.tools.files.FileDescription;
import kogut.tomasz.ucanShare.tools.networking.NetworkConnection;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

public class NetworkSearch extends Activity {
	private final static String TAG = NetworkSearch.class.getName();
	private NetworkingService mService;
	private boolean mBound;
	private ArrayAdapter<FileDescription> mAdapter;
	public static List<SearchResultMessage> mSearchResult = new LinkedList<SearchResultMessage>();
	private volatile Boolean mResultsReady = false;
	private volatile Object lock = new Object();

	ProgressBar mProgress;
	private Handler mHandler = new Handler();
	private ServiceConnection mConnection = new NetworkingServiceConnection();
	Button mStartSearch;
	ListView mSeachResultDisplay;
	EditText mSearchInput;
	ProgressDialog progressDialog;
	AtomicInteger id = new AtomicInteger(0);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new ArrayAdapter<FileDescription>(this, R.layout.items_list);
		doLayout();
		mBound = false;
		bindToNetworkService();
		mStartSearch.setOnClickListener(new SearchActionTask());
		mSeachResultDisplay.setOnItemClickListener(new SearchResultPick());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindFromNetworkService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (progressDialog != null)
			progressDialog.dismiss();
	}

	public void setBound(boolean value) {
		mBound = value;
	}

	public boolean isBound() {
		return mBound;
	}

	protected void populateWithSearchResult() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				waitForResults();
				updateGUI();
			}

			/**
			 * 
			 */
			private void updateGUI() {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						fillAdapter();
					}

					/**
					 * 
					 */
					private void fillAdapter() {
						mAdapter.clear();
						if (mSearchResult.size() > 0) {
							for (SearchResultMessage anser : mSearchResult) {
								for (FileDescription desc : anser
										.getSearchResult()) {
									mAdapter.add(desc);
								}
							}

						}
					}
				});
			}

			/**
			 * 
			 */
			private void waitForResults() {

				try {
					while (!mResultsReady) {
						synchronized (lock) {
							lock.wait();
						}
					}

				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}).start();

	}

	private void doLayout() {
		setContentView(R.layout.search_result);
		setTitle(getString(R.string.search_net));
		mSearchInput = (EditText) findViewById(R.id.search_box);
		mStartSearch = (Button) findViewById(R.id.button1);
		mSeachResultDisplay = (ListView) findViewById(R.id.list);
		mSeachResultDisplay.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();

	}

	private void bindToNetworkService() {
		if (!isBound()) {
			Intent intent = new Intent(this, NetworkingService.class);
			getApplicationContext().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);
			mBound = true;
			Log.d(TAG, "Network service was bound-Network search");
		}
	}

	private void unbindFromNetworkService() {
		if (isBound()) {
			getApplicationContext().unbindService(mConnection);
			setBound(false);
			Log.d(TAG, "Networking service was unbound.");
		}

	}

	private final class NetworkingServiceConnection implements
			ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	}

	private final class SearchActionTask implements OnClickListener {

		private SearchResultsGatherer mSearchResultServer;

		@Override
		public void onClick(View arg0) {

			try {
				startListeningForAnswers();

				sendQuery();

				showSearchWaitDialog();

				waitForResults();

				populateWithSearchResult();

			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 
		 */
		private void waitForResults() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					updateProgressDialog();
					stopListeningForAnswers();
				}
			}).start();
		}

		/**
		 * 
		 */
		private void updateProgressDialog() {
			final int PROGRESS_UPDATES = 100;
			final int PROGRESS_TIME_STEP = 33;
			int i = 0;
			while (i < PROGRESS_UPDATES) {
				++i;
				final int a = i;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						progressDialog.setProgress(a);
					}
				});

				try {
					Thread.sleep(PROGRESS_TIME_STEP);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * 
		 */
		private void stopListeningForAnswers() {
			mSearchResultServer.stopServer();
			synchronized (lock) {
				mResultsReady = true;
				lock.notify();
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					progressDialog.dismiss();
				}
			});
		}

		/**
		 * 
		 */
		private void sendQuery() {
			String searchQuery = mSearchInput.getText().toString();
			mService.sendSearchRequest(searchQuery);
		}

		/**
		 * @throws IOException
		 */
		private void startListeningForAnswers() throws IOException {
			// mSearchResult = new LinkedList<SearchResultMessage>();
			mResultsReady = false;
			mSearchResultServer = new SearchResultsGatherer();
			Thread t = new Thread(mSearchResultServer);
			t.start();
		}

		/**
		 * 
		 */
		private void showSearchWaitDialog() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					progressDialog = ProgressDialog.show(NetworkSearch.this,
							"Searching", "Waiting for answers", false, false);
					progressDialog.setMax(100);
					progressDialog.show();

				}
			});
		}
	}

	private final class SearchResultPick implements OnItemClickListener {

		private final class DownloadFileCancel implements
				DialogInterface.OnClickListener {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// Cancel, does nothing
			}
		}

		private final class DownloadFileConfirm implements
				DialogInterface.OnClickListener {

			private final FileDescription fd;

			private DownloadFileConfirm(FileDescription fd) {
				this.fd = fd;

			}

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				for (SearchResultMessage ns : mSearchResult) {
					for (FileDescription iterFd : ns.getSearchResult()) {
						if (iterFd.equals(fd)) {
							DownloadFileTask dft;
							int newId = id.incrementAndGet();
							Log.d(TAG,"Current task id:" + newId);
							dft = new DownloadFileTask(ns.getFrom(), fd,
									newId);
							mService.addDownloadTask(dft);
						}
					}
				}
			}
		}

		@Override
		public void onItemClick(AdapterView<?> adapter, View textView,
				int postition, long id) {
			StringBuilder sb = new StringBuilder();
			final FileDescription fd = mAdapter.getItem(postition);
			sb.append("Position:").append(postition).append(" id:").append(id)
					.append(" item:").append(fd.getFileName());
			AlertDialog.Builder builder = buildConfigDownloadDialog(fd);
			builder.create().show();
		}

		/**
		 * @param fd
		 * @return
		 */
		private AlertDialog.Builder buildConfigDownloadDialog(
				final FileDescription fd) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					NetworkSearch.this)
					.setTitle(
							"Do you want to download file " + fd.getFileName()
									+ "?")
					.setCancelable(false)
					.setPositiveButton(getText(R.string.yes),
							new DownloadFileConfirm(fd))
					.setNegativeButton(R.string.no, new DownloadFileCancel());
			return builder;
		}
	}

	class SearchResultsGatherer extends TcpServer {
		public SearchResultsGatherer() throws IOException {
			super(TcpServer.SEARCH_PORT);
		}

		@Override
		public void run() {
			while (true) {
				Log.d(TAG, "Waiting for connections...");
				Socket clientConnection;
				try {
					clientConnection = mSocket.accept();
					InetAddress clientAdress = clientConnection
							.getInetAddress();
					String log_msg = String.format("Connection from client:%s",
							clientAdress);
					Log.d(TAG, log_msg);
					NetworkSearch.this.mSearchResult.clear();
					FetchSearchResults searchResultTask = new FetchSearchResults(
							this, NetworkSearch.this.mSearchResult);
					searchResultTask.setSocket(clientConnection);
					startConnectionJob(searchResultTask);
				}
				catch (IOException e) {
					break;
				}
			}

		}
	}
}

class FetchSearchResults extends NetworkConnection {
	private List<SearchResultMessage> mResultList;
	private final static String TAG = FetchSearchResults.class.getName();

	public FetchSearchResults(TcpServer server,
			List<SearchResultMessage> resultsList) {
		super(server);
		this.mResultList = resultsList;
	}

	public void run() {
		try {

			SearchResultMessage searchResult = (SearchResultMessage) ois
					.readObject();
			Log.d(TAG, "Received answer with matches:"
					+ searchResult.getSearchResult().size());
			synchronized (NetworkSearch.mSearchResult) {
				NetworkSearch.mSearchResult.add(searchResult);
			}
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				ois.close();
				oos.close();
				mSocket.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				mServer.removeFromActiveConnectionList(this);
			}

		}
	}

}
