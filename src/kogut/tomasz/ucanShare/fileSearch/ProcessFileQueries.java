package kogut.tomasz.ucanShare.fileSearch;

import java.io.IOException;
import java.net.ConnectException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import kogut.tomasz.ucanShare.tools.files.FileDescription;
import kogut.tomasz.ucanShare.tools.files.SharedFilesManager;
import android.util.Log;

public class ProcessFileQueries implements Runnable {

	private final String TAG = ProcessFileQueries.class.getName();
	private volatile boolean running = false;
	private SharedFilesManager mFileManager;
	private BlockingQueue<SearchRequest> mSearchRequests;

	public ProcessFileQueries(SharedFilesManager fileManager,
			BlockingQueue<SearchRequest> searchRequests) {
		mFileManager = fileManager;
		mSearchRequests = searchRequests;
	}

	public void stop() {
		running = false;
		Thread.currentThread().interrupt();

	}

	@Override
	public void run() {
		running = true;
		while (running) {
			SearchRequest request = null;
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
			}
			catch (ConnectException e) {
				Log.i(TAG,
						"Couldn't send search result to client: "
								+ e.getLocalizedMessage());
			}

			catch (IOException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}