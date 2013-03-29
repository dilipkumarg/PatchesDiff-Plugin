package com.pramati.gerrit.plugin.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pramati.gerrit.plugin.helpers.ClasspathHacker;
import com.pramati.gerrit.plugin.helpers.CompareResponse;
import com.pramati.gerrit.plugin.helpers.JavaDiff;

@Singleton
public class StructuralDiff extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final java.io.File homeDir;
	private final java.io.File file1;
	private final java.io.File file2;

	@Inject
	StructuralDiff(@PluginData java.io.File homeDir) {
		this.homeDir = homeDir;
		this.file1 = new File(this.homeDir.getPath() + "/test/sample1.java");
		this.file2 = new File(this.homeDir.getPath() + "/test/sample2.java");
	}

	public boolean initCheckup() throws ServletException, IOException {
		// TODO Auto-generated method stub
		// super.init();
		if (!checkClassLoaded("com.sun.source.tree.TreeVisitor")) {
			// loadJar();
			return false;
		}
		return true;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse rsp)
			throws IOException, ServletException {
		if (!this.initCheckup()) {
			// rsp.sendError(HttpServletResponse.SC_NOT_FOUND,
			// "tools.jar not found");
			loadJar();
		}
		// if (!this.initCheckup()) {
		// rsp.sendError(HttpServletResponse.SC_NOT_FOUND,
		// "tools.jar not found");
		// return;
		// }
		CompareResponse cr = null;
		PrintWriter out = rsp.getWriter();
		try {
			cr = (new JavaDiff(file1, file2)).compare();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Gson gson = new Gson();
		String json = new String();
		json = gson.toJson(cr, CompareResponse.class);
		out.print(json);
	}

	private boolean checkClassLoaded(String className) {
		try {
			@SuppressWarnings("rawtypes")
			Class cls = Class.forName(className);
			System.out.println("\n" + cls.getName() + " loaded from:"
					+ cls.getProtectionDomain().getCodeSource().getLocation());
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	// is a real dirty hack .. but classloader delegation seems to be eating
	// away tools.jar
	private void loadJar() {
		try {
			System.out.println("Dirty Hack: loading the tools.jar physically ");
			String path = this.getClass().getResource("/lib/tools-1.7.0.jar")
					.getPath();
			// ClasspathHacker.addFile("tools.jar");
			ClasspathHacker.addFile(path);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
