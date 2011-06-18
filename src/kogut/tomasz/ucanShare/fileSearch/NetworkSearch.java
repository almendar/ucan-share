package kogut.tomasz.ucanShare.fileSearch;

import java.io.IOException;
import java.io.OptionalDataException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import kogut.tomasz.ucanShare.NetworkingService;
import kogut.tomasz.ucanShare.NetworkingService.LocalBinder;
import kogut.tomasz.ucanShare.R;
import kogut.tomasz.ucanShare.tools.files.FileDescription;
import kogut.tomasz.ucanShare.tools.networking.NetworkConnection;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
	Button mStartSearch;
	ListView mSeachResultDisplay;
	EditText mSearchInput;
	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new ArrayAdapter<FileDescription>(this, R.layout.items_list);
		doLayout();
		mBound = false;
		bindToNetworkService();
		mStartSearch.setOnClickListener(new SearchActionTask());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isBound())
			unbindFromNetworkService();
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
				Log.d(TAG, "Start to wait");
				waitForResults();
				Log.d(TAG, "End waiting");
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						if (mSearchResult.size() > 0) {
							for (SearchResultMessage anser : mSearchResult) {
								for (FileDescription desc : anser.getSearchResult()) {
									mAdapter.add(desc);
								}
							}
							Toast.makeText(NetworkSearch.this, "Adapter has objects:"+mAdapter.getCount(), Toast.LENGTH_SHORT).show();

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
		Intent intent = new Intent(this, NetworkingService.class);
		mBound = getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE);
		Log.d(TAG, "Network service was bound-UCAN");
	}

	private void unbindFromNetworkService() {
		if (isBound()) {
			unbindService(mConnection);
			setBound(false);
			Log.d(TAG, "Networking service was unbound.");
		}

	}

	private ServiceConnection mConnection = new NetworkingServiceConnection();

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
			try {
				mSearchResultServer.stopServer();
				synchronized (lock) {
					mResultsReady = true;
					lock.notify();
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						progressDialog.hide();
					}
				});
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
//			mSearchResult = new LinkedList<SearchResultMessage>();
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
