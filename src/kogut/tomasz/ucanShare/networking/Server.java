package kogut.tomasz.ucanShare.networking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;


import static java.lang.System.out;

public class Server implements Runnable {



	private final static int PORT = 4000;
	ServerSocket mSocket;

	public Server() throws IOException {
		mSocket = new ServerSocket(PORT);
	}

	@Override
	public void run() {
		out.println("Server started at port:" + PORT);
		while (true) {
			out.println("Waiting for connections...");
			try {
				Socket clientConnection = mSocket.accept();
				out.println("Connection from client:"
						+ clientConnection.getInetAddress());
				(new Thread(new Connection(this, clientConnection))).start();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

class Connection implements Runnable {
	Server mServer;
	Socket mSocket;
	final static Random r = new Random(System.currentTimeMillis());
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;

	public Connection(Server server, Socket socket) {
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
				Message m = new Message(fileName, bytes, read, pointer);
//				oos.reset();
				oos.writeUnshared(m);
				out.println("Left bytes to send:" + leftBytes + " pointer:"
						+ pointer);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			catch (OutOfMemoryError e2) {
				try {
					oos.reset();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		 finally {
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
