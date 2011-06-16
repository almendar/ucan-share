package kogut.tomasz.ucanShare.networking.messages;

import java.io.Serializable;
import java.util.LinkedList;

import kogut.tomasz.ucanShare.files.FileDescription;

public class SearchResultMessage implements Serializable {
	/**
	 * 
	 */

	private static final long serialVersionUID = -4918984036613891605L;
	private final LinkedList<FileDescription> mSearchResult;
	private final String query;
	
	public SearchResultMessage(LinkedList<FileDescription> searchResult, String query) {
		mSearchResult = searchResult;
		this.query = query;
	}

	public LinkedList<FileDescription> getSearchResult() {
		return mSearchResult;
	}

	public String getQuery() {
		return query;
	}
}
