package kogut.tomasz.ucanShare.files;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SharedFilesManager {
	private final static Logger log = Logger.getLogger(SharedFilesManager.class
			.toString());
	private final static String[] VIDEO_EXTENSIONS = { ".avi", ".mkv", ".mp4",
			".3gp" };
	private final static String[] MUSIC_EXTENSTIONS = { ".mp3", ".wav", ".ogg" };
	private final static String[] IMAGE_EXTENSTIONS = { ".jpg", ".jpeg", ".bmp" };
	private final static String[] DOCUMENT_EXTENSIONS = { "docx", "doc", "pdf",
			"txt", "xls", "ppt" };

	final LinkedList<String> mSharedLocations = new LinkedList<String>();;
	final Hashtable<Integer, File> mSharedFiles = new Hashtable<Integer, File>();
	final FileFilter[] mFilters = { new ExtenstionFilter(VIDEO_EXTENSIONS),
			new ExtenstionFilter(MUSIC_EXTENSTIONS),
			new ExtenstionFilter(DOCUMENT_EXTENSIONS),
			new ExtenstionFilter(IMAGE_EXTENSTIONS) };

	Stack<File> filesToProcess = new Stack<File>();

	public SharedFilesManager() {

	}

	
	public void reset() {
		mSharedFiles.clear();
		mSharedLocations.clear();
		filesToProcess.clear();
	}
	
	public void addLocation(String location) {
		// Check if path is sub-path of any other already added.
		LinkedList<String> toRemove = new LinkedList<String>();
		if (mSharedLocations.isEmpty()) {
			mSharedLocations.add(location);
		} else {
			for (String addedLocations : mSharedLocations) {
				if (location.contains(addedLocations)) {
					System.out.printf("Location discarded:%s\n", location);
					return;
				} else if (addedLocations.contains(location)) {
					toRemove.add(addedLocations);
				}
			}
			mSharedLocations.add(location);
		}
		for (String locationToRemove : toRemove) {
			mSharedLocations.remove(locationToRemove);
//			System.out.printf("Location removed:%s\n", locationToRemove);
		}
	}

	public void printout() {
		for (File f : mSharedFiles.values()) {
			System.out.println(f.getAbsolutePath());
		}
	}

	private boolean filterFile(File res) {
		boolean ret = false;
		for (FileFilter filter : mFilters) {
			ret |= filter.accept(res);
		}
		return ret;
	}

	public LinkedList<FileDescription> findFiles(String fileName) {
		LinkedList<FileDescription> ret = new LinkedList<FileDescription>();
		String regexAsterix = fileName;
		Pattern pattern = Pattern.compile(regexAsterix,
				Pattern.CASE_INSENSITIVE);
		for (Entry<Integer, File> entry : mSharedFiles.entrySet()) {
			Matcher matcher = pattern.matcher(entry.getValue().getName());
			if (matcher.find()) {
				int id = entry.getKey();
				File matchingFile = entry.getValue();
				ret.add(new FileDescription(id, matchingFile.getName(),
						matchingFile.length()));
			}
		}
		return ret;
	}

	private void processDirectory() {
		while (!filesToProcess.isEmpty()) {
			File f = filesToProcess.pop();
			if (f.isDirectory()) {
				filesToProcess.addAll(Arrays.asList(f.listFiles()));
			} else if (f.isFile() && filterFile(f)) {
				mSharedFiles.put(f.hashCode(), f);
			}
		}
	}

	public File getById(int id) {
		return mSharedFiles.get(id);
	}

	public void buildDatabase() {
		LinkedList<String> missingLocations = new LinkedList<String>();
		filesToProcess = new Stack<File>();
		for (String location : mSharedLocations) {
			File resource = new File(location);
			if (!resource.exists()) {
				missingLocations.add(location);
				continue;
			}
			if (resource.isFile()) {
				mSharedFiles.put(resource.hashCode(), resource);
			} else if (resource.isDirectory()) {
				filesToProcess.addAll(Arrays.asList(resource.listFiles()));
				processDirectory();
			}
		}

		for (String locationToRemove : missingLocations) {
			mSharedLocations.remove(locationToRemove);
		}
		filesToProcess = null;
	}
}

class ExtenstionFilter implements FileFilter {
	public ExtenstionFilter(String[] extenstionsArray) {
		mExtensions = extenstionsArray;
	}

	private final String[] mExtensions;

	@Override
	public boolean accept(File pathname) {
		boolean ret = false;
		for (String ext : mExtensions) {
			ret |= pathname.getName().endsWith(ext);
		}
		return ret;
	}
}
