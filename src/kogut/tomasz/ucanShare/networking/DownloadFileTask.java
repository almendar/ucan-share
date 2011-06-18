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

/**
 * @author tomek
 * 
 */
public class DownloadFileTask implements Runnable {

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
				+ File.pathSeparatorChar + mFileDescription.getFileName());
		RandomAccessFile raf = null;
		try {
			mSocket = new Socket(adress, TcpServer.FILE_TRANSFER_PORT);
			oos = new ObjectOutputStream(mSocket.getOutputStream());
			ois = new ObjectInputStream(mSocket.getInputStream());
			raf = new RandomAccessFile(fileToSave, "rw");
			sendRequestForFile();
			Object serverAnswer = ois.readObject();
			if (serverAnswer instanceof NegotationMessage) {
				NegotationMessage answer = (NegotationMessage) serverAnswer;
				if (answer.getType() != NegotationMessage.ACCEPT) {
					// /this is a problem, handle this TODO
				}
			} else {

				// this is also bad
			}

			while (true) {
				DataChunkMessage dataChunk = (DataChunkMessage) ois
						.readObject();
				raf.write(dataChunk.mData, (int) dataChunk.mOffset, dataChunk.mData.length);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				raf.close();
				oos.close();
				ois.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
