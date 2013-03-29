package com.pramati.gerrit.plugin.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.junit.Test;

import com.pramati.gerrit.plugin.helpers.MD5CheckSum;

public class Md5CheckSumTest {
	@Test
	public void testComputeMd5Hash() throws NoSuchAlgorithmException,
			IOException {
		String path = this.getClass().getResource("/testdoc.txt").getPath();
		File f = new File(path);
		// File f = new File("../target/test-classes/testdoc.txt");
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
		String md5Hash = MD5CheckSum.ComputeMd5(br);
		Assert.assertEquals(md5Hash, "9da948f96abb2efff867d8da9f84b602");
	}
}
