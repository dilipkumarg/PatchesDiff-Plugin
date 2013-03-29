package com.pramati.gerrit.plugin.helpers;

import java.util.ArrayList;
import java.util.List;

public class DiffService {

	public CompareResponse validate(String added, String deleted) {

		// String result = "Restful example : " + name + " " + passwd;
		List<String> adds = new ArrayList<String>();
		adds.add(added);
		List<String> deletes = new ArrayList<String>();
		deletes.add(deleted);

		CompareResponse diff = new CompareResponse(adds, deletes);
		return diff;

	}

	public CompareResponse showDifferences(String oldFile, String newFile) {
		CompareResponse cr = null;
		try {
			cr = (new JavaDiff(oldFile, newFile)).compare();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return cr;
	}
}
