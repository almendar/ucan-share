/**
 * 
 */
package kogut.tomasz.ucanShare.fileUpload;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import kogut.tomasz.ucanShare.NotifyMe;
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
	public static final String FINISHED = "Finished";
	public static final String FAILED = "Failed";
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	long mFileSize = 0;
	final int id;
	long mAlreadyDownloaded = 0;
	Socket mSocket = null;
	FileDescription mFileDescription;
	private InetAddress adress;
	private String mStatusText = "New";
	private NotifyMe listner;

	public DownloadFileTask(InetAddress adress,
			FileDescription fileDescription, int id) {
		this.adress = adress;
		this.mFileDescription = fileDescription;
		this.id = id;
	}

	public void setListner(NotifyMe listner) {
		this.listner = listner;
	}

	public void breakDownload() {
		try {
			if (oos != null) {
				oos.close();
			}
			if (ois != null) {
				ois.close();
			}

			if (mSocket != null) {
				mSocket.close();
			}

		}
		catch (IOException e) {
			// ignore
		}
	}

	public synchronized String getStatusString() {
		return mStatusText;
	}

	public int getPercentageTaskDone() {
		double done;
		double all = (double) mFileSize;
		int ret = 0;
		if (mFileSize != 0) {
			synchronized (this) {
				done = (double) mAlreadyDownloaded;
			}
			double result = done / all;
			result *= 100;
			ret = (int) result;
		}
		return ret;
	}

	public String getFileName() {
		return mFileDescription.getFileName();
	}

	@Override
	public void run() {

		File downloadDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}
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
					mStatusText = "Active";
					mFileSize = Long.parseLong(answer.getData());
					while (true) {
						DataChunkMessage dataChunk = (DataChunkMessage) ois
								.readObject();
						if (dataChunk.mFilename != null) {
							raf.seek(dataChunk.mOffset);
							raf.write(dataChunk.mData, 0,
									dataChunk.mData.length);
							synchronized (this) {
								mAlreadyDownloaded += dataChunk.mData.length;
							}
						}
						else {
							// everything was transfered
							Log.d(TAG, "Download completed");
							listner.updateMe(this, DownloadFileTask.FINISHED);

							mStatusText = "Done";
							break;
						}

						try {
							Thread.sleep(30);
						}
						catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
				else {
					Log.d(TAG, "Answer was not good");
					// A missunderstanding
				}
			}
			else {
				Log.d(TAG, "Wrongf object");
				// this is also bad
			}

		}
		catch (IOException e) {
			Log.i(TAG, "Download of file " + mFileDescription.getFileName()
					+ " failed.");
			// Close stream before tryng to delete
			if (raf != null)
				try {
					raf.close();
					// cleanup, some messed up things could be left here
					if (fileToSave.exists()) {
						fileToSave.delete();
					}
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			breakDownload();
			listner.updateMe(this, DownloadFileTask.FAILED);
			mStatusText = "Failed";

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
				if (oos != null) {
					oos.close();
				}
				if (ois != null) {
					ois.close();
				}
				if (mSocket != null) {
					mSocket.close();
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, e.toString());
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

	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
