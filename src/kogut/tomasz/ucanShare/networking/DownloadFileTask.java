/**
 * 
 */
package kogut.tomasz.ucanShare.networking;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import kogut.tomasz.ucanShare.networking.messages.DataChunkMessage;
import kogut.tomasz.ucanShare.networking.messages.NegotationMessage;
import kogut.tomasz.ucanShare.tools.files.FileDescription;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;
import android.os.Environment;
import android.util.Log;

/**
 * @author tomek
 * 
 */
public class DownloadFileTask implements Runnable {

	private final static String TAG = DownloadFileTask.class.getName();
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	Socket mSocket = null;
	FileDescription mFileDescription;
	private InetAddress adress;

	public DownloadFileTask(InetAddress adress, FileDescription fileDescription)
			throws UnknownHostException, IOException {
		this.adress = adress;
		this.mFileDescription = fileDescription;
	}

	@Override
	public void run() {

		File downloadDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File fileToSave = new File(downloadDir.getAbsolutePath()
				+ File.separatorChar + mFileDescription.getFileName());
		RandomAccessFile raf = null;
		try {
			Log.d(TAG,
					"Trying to download file:" + fileToSave.getAbsolutePath());
			mSocket = new Socket(adress, TcpServer.FILE_TRANSFER_PORT);
			oos = new ObjectOutputStream(mSocket.getOutputStream());
			ois = new ObjectInputStream(mSocket.getInputStream());
			raf = new RandomAccessFile(fileToSave, "rw");
			Log.d(TAG, "Streams opened");
			sendRequestForFile();
			Object serverAnswer = ois.readObject();
			Log.d(TAG, "Request for file send and have answer");
			if (serverAnswer instanceof NegotationMessage) {
				NegotationMessage answer = (NegotationMessage) serverAnswer;
				if (answer.getType() == NegotationMessage.ACCEPT) {
					Log.d(TAG, "Answer was ACCEPT");
					while (true) {
						DataChunkMessage dataChunk = (DataChunkMessage) ois
								.readObject();
						Log.d(TAG, "Length" + dataChunk.mData.length + " offset" + dataChunk.mOffset);
						raf.seek(dataChunk.mOffset);
						raf.write(dataChunk.mData, 0,
								dataChunk.mData.length);
					}
				}
				else {
					Log.d(TAG, "Answer was not good");
					// A missunderstanding
				}
			}
			else {
				Log.d(TAG, "Wrongf object");
				// this is also bad TODO
			}

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "Download completed");
			//end
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getLocalizedMessage());
		}
		finally {
			try {
				if (raf != null) {
					raf.close();
				}
				oos.close();
				ois.close();
				mSocket.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, e.getLocalizedMessage());
			}
		}
	}

	/**
	 * @throws IOException
	 */
	private void sendRequestForFile() throws IOException {
		NegotationMessage negMsg = new NegotationMessage(NegotationMessage.ASK);
		final int id = mFileDescription.getId();
		negMsg.setId(id);
		oos.writeObject(negMsg);
	}

}
