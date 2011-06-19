package kogut.tomasz.ucanShare.fileSearch;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

import kogut.tomasz.ucanShare.tools.files.FileDescription;

public class SearchResultMessage implements Serializable {
	/**
	 * 
	 */

	private static final long serialVersionUID = -4918984036613891605L;
	private final LinkedList<FileDescription> mSearchResult;
	private final String mQuery;
	private final InetAddress mFrom;
	


	public SearchResultMessage(InetAddress from ,LinkedList<FileDescription> searchResult, String query) {
		mSearchResult = searchResult;
		this.mQuery = query;
		this.mFrom = from;
	}

	public LinkedList<FileDescription> getSearchResult() {
		return mSearchResult;
	}

	public String getQuery() {
		return mQuery;
	}
	
	public InetAddress getFrom() {
		return mFrom;
	}
}
