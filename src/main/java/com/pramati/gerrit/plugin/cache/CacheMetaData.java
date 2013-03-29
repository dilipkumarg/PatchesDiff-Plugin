package com.pramati.gerrit.plugin.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;

/**
 * This Class contains the functions to access cache meta file.
 * 
 * @author dilip
 * 
 */
public class CacheMetaData {
	private final java.io.File homeDir;
	private final File cachedMetaFile;

	public CacheMetaData(java.io.File myDir) {
		this.homeDir = myDir;
		this.cachedMetaFile = new File(this.homeDir, "cachedmeta.json");
	}

	/**
	 * Creates Cache Meta file on the disk if it not exists
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean createCacheMetaIfNotExists() throws IOException {
		if (!cachedMetaFile.exists()) {
			if (!cachedMetaFile.createNewFile()) {
				return false;
			}
		}
		return false;
	}

	public String getHomeDirPath() {
		return homeDir.getPath();
	}

	/**
	 * returns the ChangeIdMeta Object from the cache file.
	 * 
	 * @return
	 * @throws IOException
	 */
	private ChangeIdsMeta getChangeIdsFromCache() throws IOException {
		Gson gson = new Gson();
		ChangeIdsMeta changeIds = new ChangeIdsMeta();
		try {
			ChangeIdsMeta changeId = new ChangeIdsMeta();
			BufferedReader br = new BufferedReader(new FileReader(
					cachedMetaFile));
			changeId = gson.fromJson(br, ChangeIdsMeta.class);
			if (changeId != null) {
				changeIds = changeId;
			}
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			createCacheMetaIfNotExists();
		}
		return changeIds;
	}

	/**
	 * writes the changeIdMeta object in cache file
	 * 
	 * @param cid
	 */
	private void writeChangeIdsToCache(ChangeIdsMeta cid) {
		Gson gson = new Gson();
		String json = gson.toJson(cid);
		try {
			// write converted json data to a file named "file.json"
			FileWriter writer = new FileWriter(cachedMetaFile);
			writer.write(json);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * returns no of patch sets in the cache. if given change id not exists
	 * returns -1
	 * 
	 * @param cid
	 * @return
	 * @throws IOException
	 */
	public int getNoOfPatcheSets(int cid) throws IOException {
		ChangeIdsMeta changeIds = getChangeIdsFromCache();
		int patches = changeIds.getNoOfPatchsets(cid);
		return patches;
	}

	public void addOrUpdateChangeId(int cid, int patchsets) throws IOException {
		ChangeIdsMeta changeIds = getChangeIdsFromCache();
		if (changeIds.isChangeIdExists(cid)) {
			changeIds.updateChangeId(cid, patchsets);
		} else {
			changeIds.addChangeId(cid, patchsets);
		}
		writeChangeIdsToCache(changeIds);
	}
}

/**
 * This class contains List of all change id's and no of patch sets for that id.
 * Data stored in the cache uses this object notation
 * 
 * @author dilip
 * 
 */
class ChangeIdsMeta {
	private HashMap<Integer, Integer> changeIds;

	public ChangeIdsMeta() {
		changeIds = new HashMap<Integer, Integer>();
	}

	public void addChangeId(int change_id, int no_of_Patches) {
		this.changeIds.put(change_id, no_of_Patches);
	}

	public void updateChangeId(int change_id, int no_of_Patches) {
		this.changeIds.remove(change_id);
		this.changeIds.put(change_id, no_of_Patches);
	}

	public int getNoOfPatchsets(int cid) {
		if (this.changeIds.get(cid) == null) {
			// returning -1 to indicate cid not available
			return -1;
		}
		return this.changeIds.get(cid);
	}

	public boolean isChangeIdExists(int cid) {
		return this.changeIds.containsKey(cid);
	}
}
