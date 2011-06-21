package kogut.tomasz.ucanShare.fileUpload;

import static java.lang.System.out;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import kogut.tomasz.ucanShare.tools.files.SharedFilesManager;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;
import android.content.Context;
import android.util.Log;

/**
 * 
 * @author tomek
 * 
 */
public class FileUploader extends TcpServer {

	final String TAG = FileUploader.class.getName();
	private Context mContext;

	public FileUploader() throws IOException {
		super(TcpServer.FILE_TRANSFER_PORT);
	}

	@Override
	public void run() {
		while (true) {
			Log.d(TAG, "Waiting file upload requests...");
			Socket clientConnection;
			try {
				clientConnection = mSocket.accept();
				InetAddress clientAdress = clientConnection.getInetAddress();
				String log_msg = String.format("Connection from client:%s",
						clientAdress);
				Log.d(TAG, log_msg);
				FileUploadTask searchResultTask = new FileUploadTask(this);
				searchResultTask.setSocket(clientConnection);
				startConnectionJob(searchResultTask);
			}
			catch (IOException e) {
				break;
			}
		}

	}

}
