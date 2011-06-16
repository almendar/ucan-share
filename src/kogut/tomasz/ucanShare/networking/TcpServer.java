package kogut.tomasz.ucanShare.networking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import android.util.Log;

import kogut.tomasz.ucanShare.networking.messages.DataChunkMessage;
import kogut.tomasz.ucanShare.networking.messages.NegotationMessage;
import kogut.tomasz.ucanShare.networking.messages.SearchResultMessage;

import static java.lang.System.out;

public class TcpServer implements Runnable {

	private final static String TAG = TcpServer.class.getName();
	public final static int FILE_TRANSFER_PORT = 4000;
	public final static int SEARCH_PORT = 4500;
	ServerSocket mSocket;
	volatile boolean isRunning = false;

	public TcpServer(int port) throws IOException {
		isRunning = true;
		mSocket = new ServerSocket(port);
	}

	public void stopServer() {
		isRunning = false;
	}

	@Override
	public void run() {
		Log.d(TAG, String.format("TCP server started at port %d", mSocket.getLocalPort()));
		while (isRunning) {
			Log.d(TAG, "Waiting for connections...");
			try {
				Socket clientConnection = mSocket.accept();
				out.println(String.format("Connection from client: %s",
						clientConnection.getInetAddress()));
				(new Thread(new Connection(this, clientConnection))).start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

class Connection implements Runnable {
	TcpServer mServer;
	Socket mSocket;
	final static Random r = new Random(System.currentTimeMillis());
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;

	public Connection(TcpServer server, Socket socket) {
		mSocket = socket;
		mServer = server;
		try {
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			try {
				mSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	@Override
	public void run() {
		try{
		Object obj = ois.readObject();
		if(obj instanceof NegotationMessage) {
			NegotationMessage negoMesg = (NegotationMessage) obj;
			if(negoMesg.getType()==NegotationMessage.ASK) {
				NegotationMessage anser = new NegotationMessage(NegotationMessage.ACCEPT);
				oos.writeObject(anser);
				negoMesg.getId();
				
			}
		}
		else if(obj instanceof SearchResultMessage) {
			SearchResultMessage searchResults = (SearchResultMessage) obj;
			
		}
		} catch (Exception e) {
		
		}
		
		
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
