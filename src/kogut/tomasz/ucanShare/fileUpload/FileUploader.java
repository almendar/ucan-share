package kogut.tomasz.ucanShare.fileUpload;

import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;

import kogut.tomasz.ucanShare.GlobalData;
import kogut.tomasz.ucanShare.networking.messages.DataChunkMessage;
import kogut.tomasz.ucanShare.networking.messages.NegotationMessage;
import kogut.tomasz.ucanShare.tools.files.SharedFilesManager;
import kogut.tomasz.ucanShare.tools.networking.NetworkConnection;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;
import android.content.Context;
import android.util.Log;

/**
 * 
 * @author tomek
 * 
 */
public class FileUploader extends TcpServer {

	private final String TAG = FileUploader.class.getName();
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

	/**
	 * 
	 * @author tomek
	 * 
	 */
	class FileUploadTask extends NetworkConnection {

		public FileUploadTask(TcpServer server) {
			super(server);

		}

		@Override
		public void run() {
			try {
				NegotationMessage request = (NegotationMessage) ois
						.readObject();
				final int fileId = request.getId();
				NegotationMessage negMsg;
				switch (request.getType()) {
				case NegotationMessage.ASK:
					File fileToUpload = GlobalData.getFileById(fileId);
					if (fileToUpload != null && fileToUpload.exists()) {
						ackAndSendFile(fileToUpload);
					}
					else {
						nckSend();
					}
					break;
				default:
					throw new IOException(
							"Protocol not understood. Needed 'NegotationMessage.ASK' message");
				}
				Thread.sleep(200);

			}
			catch (OptionalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (InterruptedException e) {
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

		/**
		 * @throws IOException
		 */
		private void nckSend() throws IOException {
			NegotationMessage negMsg;
			negMsg = new NegotationMessage(NegotationMessage.REJECT);
			oos.writeObject(negMsg);
		}

		/**
		 * @param fileToUpload
		 * @throws IOException
		 * @throws FileNotFoundException
		 */
		private void ackAndSendFile(File fileToUpload) throws IOException,
				FileNotFoundException {
			NegotationMessage negMsg;
			{
				negMsg = new NegotationMessage(NegotationMessage.ACCEPT);
				oos.writeObject(negMsg);
				RandomAccessFile f = new RandomAccessFile(fileToUpload, "rw");
				f.seek(0);
				String fileName = fileToUpload.getName();
				long leftBytes = fileToUpload.length();
				long pointer = 0;
				final int CHUNK_SIZE = 5000;
				int read = -1;
				byte[] bytes = new byte[CHUNK_SIZE];
				while (leftBytes > 0) {
					pointer = f.getFilePointer();
					Log.d(TAG,"Pointer:" + pointer);
					read = f.read(bytes);
					leftBytes -= (long) read;
					DataChunkMessage m = new DataChunkMessage(fileName, bytes,
							read, pointer);
					oos.reset();
					oos.writeObject(m);
					// out.println("Left bytes to send:" + leftBytes +
					// " pointer:"
					// + pointer);
				}
			}
		}
	}

}
