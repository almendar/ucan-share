/**
 * 
 */
package kogut.tomasz.ucanShare.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;


import static java.lang.System.out;

/**
 * @author tomek
 * 
 */
public class Client implements Runnable {

	final static Random r = new Random(System.currentTimeMillis());

	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	Socket mSocket = null;
	int mId;

	public Client() throws UnknownHostException, IOException {
		mId = r.nextInt();
		mSocket = new Socket("localhost", 4000);
		oos = new ObjectOutputStream(mSocket.getOutputStream());
		ois = new ObjectInputStream(mSocket.getInputStream());
		out.println("Created client:" + mId);
	}

	@Override
	public void run() {
		RandomAccessFile raf = null;
		try {
			while (true) {
				Message m = (Message) ois.readObject();
				if (raf == null) {
					raf = new RandomAccessFile("d:\\" + m.mFilename, "rw");
				}
				raf.seek(m.mOffset);
				long fp = raf.getFilePointer();
				raf.write(m.mData);
				out.println("Wrote " + m.mData.length + " bytes");
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

}
