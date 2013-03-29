package com.pramati.gerrit.plugin.entities;

import java.util.ArrayList;
import java.util.List;

public class ChangeIdDetails {
	public class PatchSetDetails {
		class PatchDetails {
			String filename;
			String md5;

			public PatchDetails(String filename, String md5) {
				this.filename = filename;
				this.md5 = md5;
			}
		}

		int patchSet;
		List<PatchDetails> Patches;

		public PatchSetDetails(int patchSet) {
			this.patchSet = patchSet;
			Patches = new ArrayList<PatchDetails>();
		}

		public void addPatch(String filename, String md5) {
			PatchDetails pd = new PatchDetails(filename, md5);
			Patches.add(pd);
		}
	}

	public int changeId;
	List<PatchSetDetails> patchsets;

	public ChangeIdDetails(int changeId) {
		this.changeId = changeId;
		patchsets = new ArrayList<PatchSetDetails>();
	}
	public PatchSetDetails newPatchSet(int patchsetId) {
		PatchSetDetails psd = new PatchSetDetails(patchsetId);
		return psd;
	}
	public void addPatchSet(PatchSetDetails psd) {
		patchsets.add(psd);
	}
}
