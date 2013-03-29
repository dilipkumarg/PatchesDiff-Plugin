package com.pramati.gerrit.plugin.helpers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper Class to compute MD5 Hash
 * 
 * @author dilip
 * 
 */
public class MD5CheckSum {
	/**
	 * returns MD5 hash for given Buffered stream(file stream)
	 * 
	 * @param bInput
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String ComputeMd5(BufferedInputStream bInput)
			throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] dataBytes = new byte[1024];
		int nread = 0;
		while ((nread = bInput.read(dataBytes)) != -1) {
			md.update(dataBytes, 0, nread);
		}
		;
		byte[] mdbytes = md.digest();

		// convert the byte to hex format method
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}

		// System.out.println("Digest(in hex format):: " + sb.toString());

		bInput.close();
		return sb.toString();
	}
}
