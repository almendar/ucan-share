package kogut.tomasz.ucanShare.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import kogut.tomasz.ucanShare.files.FileDescription;
import kogut.tomasz.ucanShare.networking.messages.SearchResultMessage;

public class SendSearchResultTask implements Runnable {

	private LinkedList<FileDescription> mSearchResult;
	ObjectOutputStream oos = null;
	Socket mSocket = null;
	private InetAddress adress;
	private final String query;
	public SendSearchResultTask(InetAddress adress,
			LinkedList<FileDescription> searchResult,String query) throws IOException {
		mSearchResult = searchResult;
		this.adress = adress;
		this.query = query;
	}

	@Override
	public void run() {
		try {
			mSocket = new Socket(adress, TcpServer.SEARCH_PORT);
			SearchResultMessage srm = new SearchResultMessage(mSearchResult,this.query);
			oos.writeObject(srm);
			oos = new ObjectOutputStream(mSocket.getOutputStream());
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
