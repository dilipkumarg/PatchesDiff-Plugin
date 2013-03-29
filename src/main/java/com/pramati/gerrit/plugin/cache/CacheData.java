package com.pramati.gerrit.plugin.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.pramati.gerrit.plugin.entities.ChangeIdDetails;

/**
 * It contains functions to get the data from the cache and write the data to
 * cache.
 * 
 * @author dilip
 * 
 */
public class CacheData {
	private final java.io.File homeDir;
	private final File cachedDataFile;

	public CacheData(java.io.File myDir) {
		this.homeDir = myDir;
		this.cachedDataFile = new File(this.homeDir, "cacheddata.json");
	}

	/**
	 * Creates cache data file on the disk if not exists
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean createCacheFileIfnotExists() throws IOException {
		if (!cachedDataFile.exists()) {
			if (!cachedDataFile.createNewFile()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fetches the Data from File
	 * 
	 * @return
	 * @throws IOException
	 */
	private ChangeIdsData fetchDataFromCache() throws IOException {
		Gson gson = new Gson();
		ChangeIdsData changeIdsData = new ChangeIdsData();
		try {
			ChangeIdsData temp = new ChangeIdsData();
			BufferedReader br = new BufferedReader(new FileReader(
					cachedDataFile));
			temp = gson.fromJson(br, ChangeIdsData.class);
			if (temp != null) {
				changeIdsData = temp;
			}

		} catch (FileNotFoundException e) {
			createCacheFileIfnotExists();
			// e.printStackTrace();
		}
		return changeIdsData;
	}

	/**
	 * Simply writes the given object into the cache file;
	 * 
	 * @param cidData
	 */
	private void writeDataToCache(ChangeIdsData cidData) {
		Gson gson = new Gson();
		String json = gson.toJson(cidData);
		try {
			// write converted json data to a file named "file.json"
			FileWriter writer = new FileWriter(cachedDataFile);
			writer.write(json);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * returns the ChangeIdDetails object for given change id. returns null if
	 * change id not exists in the cache
	 * 
	 * @param changeId
	 * @return
	 * @throws IOException
	 */
	public ChangeIdDetails getChangeIdDetails(int changeId) throws IOException {
		ChangeIdsData cidData = this.fetchDataFromCache();
		return cidData.getChangeIdDetails(changeId);
	}

	/**
	 * add the given changeIdDetails object in the cache. if change id already
	 * exists in the cache it updates with new data
	 * 
	 * @param cid
	 * @throws IOException
	 */
	public void addUpdateChangeIdDetails(ChangeIdDetails cid)
			throws IOException {
		ChangeIdsData cidData = this.fetchDataFromCache();
		cidData.addOrUpdateChangeIdDetails(cid);
		this.writeDataToCache(cidData);
	}
}

/**
 * This class contains List of all change id's and details of that change id
 * containing list of patch sets and list of patches corresponding that and also
 * information of md5 . Data stored in the cache uses this object notation
 * 
 * @author dilip
 * 
 */
class ChangeIdsData {
	private List<ChangeIdDetails> changeIds;

	ChangeIdsData() {
		changeIds = new ArrayList<ChangeIdDetails>();
	}

	public void addOrUpdateChangeIdDetails(ChangeIdDetails cid) {
		// removes already existed change id details
		removeChangeIdDetails(cid);
		changeIds.add(cid);
	}

	/**
	 * removes the change id details of given change Id
	 * 
	 * @param cid
	 */
	public void removeChangeIdDetails(ChangeIdDetails cid) {
		for (ChangeIdDetails changeId : changeIds) {
			if (changeId.changeId == cid.changeId) {
				changeIds.remove(changeId);
			}
		}
	}

	/**
	 * returns the changeIdDetails object of given change id. if given id not
	 * exists on the cache returns null
	 * 
	 * @param cid
	 * @return
	 */
	public ChangeIdDetails getChangeIdDetails(int cid) {
		for (ChangeIdDetails changeId : changeIds) {
			if (changeId.changeId == cid) {
				return changeId;
			}
		}
		return null;
	}
}
