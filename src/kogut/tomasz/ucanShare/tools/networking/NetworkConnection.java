package kogut.tomasz.ucanShare.tools.networking;

import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.Random;

import kogut.tomasz.ucanShare.networking.messages.DataChunkMessage;

public class NetworkConnection implements Runnable {
	protected Socket mSocket;
	protected ObjectInputStream ois = null;
	protected ObjectOutputStream oos = null;
	protected TcpServer mServer;

	public NetworkConnection(TcpServer server) {
		mServer = server;
	}

	public void setSocket(Socket socket) throws StreamCorruptedException,
			IOException {

		mSocket = socket;

		if (mSocket != null) {
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
		}

	}

	public void close() throws IOException {
		ois.close();
		oos.close();
		mSocket.close();
	}

	@Override
	public void run() {
		// try{
		// Object obj = ois.readObject();
		// if(obj instanceof NegotationMessage) {
		// NegotationMessage negoMesg = (NegotationMessage) obj;
		// if(negoMesg.getType()==NegotationMessage.ASK) {
		// NegotationMessage anser = new
		// NegotationMessage(NegotationMessage.ACCEPT);
		// oos.writeObject(anser);
		// negoMesg.getId();
		//
		// }
		// }
		// else if(obj instanceof SearchResultMessage) {
		// SearchResultMessage searchResults = (SearchResultMessage) obj;
		//
		// }
		// } catch (Exception e) {
		//
		// }

		String filePath = "D:\\tomek\\instalki\\jdk-6u23-windows-i586.exe";
		File file = new File(filePath);
		long time;
		time = System.currentTimeMillis();
		try {
			RandomAccessFile f = new RandomAccessFile(file, "rw");
			f.seek(0);
			String fileName = file.getName();
			long leftBytes = file.length();
			long pointer = 0;
			final int chunk = 5000;
			int read = -1;
			byte[] bytes = new byte[chunk];
			while (leftBytes > 0) {
				pointer = f.getFilePointer();
				read = f.read(bytes);
				out.println("read:" + read);
				// pointer += (long)read;
				leftBytes -= (long) read;
				DataChunkMessage m = new DataChunkMessage(fileName, bytes,
						read, pointer);
				// oos.reset();
				oos.writeUnshared(m);
				out.println("Left bytes to send:" + leftBytes + " pointer:"
						+ pointer);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e2) {
			try {
				oos.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			try {
				oos.flush();
				out.println("Total time:" + (System.currentTimeMillis() - time));
				oos.close();
				ois.close();
				mServer.removeFromActiveConnectionList(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}