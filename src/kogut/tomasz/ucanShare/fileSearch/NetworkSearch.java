package kogut.tomasz.ucanShare.fileSearch;

import java.io.IOException;
import java.io.OptionalDataException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import kogut.tomasz.ucanShare.NetworkingService;
import kogut.tomasz.ucanShare.NetworkingService.LocalBinder;
import kogut.tomasz.ucanShare.R;
import kogut.tomasz.ucanShare.tools.files.FileDescription;
import kogut.tomasz.ucanShare.tools.networking.NetworkConnection;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;
import android.app.Activity;
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

public class NetworkSearch extends Activity {
	private final static String TAG = NetworkSearch.class.getName();
	private NetworkingService mService;
	private boolean mBound;
	private ArrayAdapter<FileDescription> mAdapter;
	private List<SearchResultMessage> searchResult;
	ProgressBar mProgress;
	private Handler mHandler = new Handler();
	Button mStartSearch;
	ListView mSeachResultDisplay;
	EditText mSearchInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doLayout();
		mAdapter = new ArrayAdapter<FileDescription>(this, R.layout.empty_list);
		mBound = false;
		bindToNetworkService();
		mStartSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				try {
					/* First start server to be ready for answers
					 * then send broadcast with query */
					SearchResultsGatherer searchResultServer = new SearchResultsGatherer();
					Thread t = new Thread(searchResultServer);
					t.start();
					String searchQuery = mSearchInput.getText().toString();
					mService.sendSearchRequest(searchQuery);
					
					//odczekaj pewien kwant czasu i ubij sieć
					
					searchResultServer.stopServer();
					
					//przeetwórz wyniki i je wyświetl
					// obsłuż przyciski
					
					
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
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

	private void doLayout() {
		setContentView(R.layout.search_result);
		setTitle(getString(R.string.search_net));
		mSearchInput = (EditText) findViewById(R.id.search_box);
		mStartSearch = (Button) findViewById(R.id.button1);
		mSeachResultDisplay = (ListView) findViewById(R.id.list);
	}

	private void bindToNetworkService() {
		Intent intent = new Intent(this, NetworkingService.class);
		setBound(bindService(intent, mConnection, Context.BIND_AUTO_CREATE));
		Log.d(TAG, "Network service was bound");
	}

	private void unbindFromNetworkService() {
		if (isBound()) {
			unbindService(mConnection);
			setBound(false);
			Log.d(TAG, "Networking service was unbound.");
		}

	}

	private ServiceConnection mConnection = new ServiceConnection() {

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
	};

	class SearchResultsGatherer extends TcpServer {
		public SearchResultsGatherer() throws IOException {
			super(TcpServer.SEARCH_PORT);
		}

		@Override
		public void run() {
			while (true) {
				Log.d(TAG, "Waiting for connections...");
				Socket clientConnection;
				int count=0;
				try {
					
					
					
					clientConnection = mSocket.accept();
					InetAddress clientAdress = clientConnection
							.getInetAddress();
					String log_msg = String.format("Connection from client:%s",
							clientAdress);
					Log.d(TAG, log_msg);
					FetchSearchResults searchResultTask = new FetchSearchResults(
							this, searchResult);
					searchResultTask.setSocket(clientConnection);
					startConnectionJob(searchResultTask);
					// tutaj można by do głównego UI raportować liczbe
					// odpowiedzi
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}

class FetchSearchResults extends NetworkConnection {
	private List<SearchResultMessage> resultList;

	public FetchSearchResults(TcpServer server,
			List<SearchResultMessage> resultsList) {
		super(server);
	}

	public void run() {
		try {
			SearchResultMessage searchResult = (SearchResultMessage) ois
					.readObject();
			synchronized (resultList) {
				resultList.add(searchResult);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ois.close();
				oos.close();
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				mServer.removeFromActiveConnectionList(this);
			}

		}
	}

}
