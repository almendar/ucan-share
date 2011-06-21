package kogut.tomasz.ucanShare.fileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.RandomAccessFile;

import kogut.tomasz.ucanShare.GlobalData;
import kogut.tomasz.ucanShare.networking.messages.DataChunkMessage;
import kogut.tomasz.ucanShare.networking.messages.NegotationMessage;
import kogut.tomasz.ucanShare.tools.networking.NetworkConnection;
import kogut.tomasz.ucanShare.tools.networking.TcpServer;
import android.util.Log;

/**
 * 
 * @author tomek
 * 
 */
class FileUploadTask extends NetworkConnection {

	/**
	 * 
	 */
	private final FileUploader fileUploader;

	public FileUploadTask(FileUploader fileUploader) {
		super(fileUploader);
		this.fileUploader = fileUploader;
	}

	@Override
	public void run() {
		try {
			NegotationMessage request = (NegotationMessage) ois.readObject();
			final int fileId = request.getId();
			NegotationMessage negMsg;
			switch (request.getType()) {
			case NegotationMessage.ASK:
				File fileToUpload = GlobalData.getFileById(fileId);
				if (fileToUpload != null && fileToUpload.exists()) {
					ackAndSendFile(fileToUpload);
					sendGoodbye();
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
				if (ois != null) {
					ois.close();
				}
				if (oos != null) {
					oos.close();
				}
				if (mSocket != null) {
					mSocket.close();
				}
			}
			catch (IOException e) {
				//consume and ommit
			}
			finally {
				mServer.removeFromActiveConnectionList(this);
			}

		}
	}

	private void sendGoodbye() throws IOException {
		DataChunkMessage goodbyeMsg = new DataChunkMessage(null, null, 0, 0);
		oos.writeObject(goodbyeMsg);
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
			negMsg.setData(String.valueOf(fileToUpload.length()));
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
				Log.d(this.fileUploader.TAG, "Pointer:" + pointer);
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