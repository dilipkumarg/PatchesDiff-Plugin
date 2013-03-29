package com.pramati.gerrit.plugin.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

@Singleton
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse rsp) {
		try {
			PrintWriter out = rsp.getWriter();
			rsp.setContentType("text/html");

			out.print(getHtmlHeader());

			String body = new String();
			body = String.format("<body>"
					+ "<div class='container well'> "
					+ "<div class='center'><h1>Patch Diff Plugin</h1></div>"
					+ "<hr>"
					+ "<div id='content'>"
					+ "<div id='inputBox'>"
					+ "<input type='text' id='input' class='span2'><br>"
					+ "<button class='btn brn-primary' onclick='javascript:getResult();'>Get Details</button>"
					+ "</div>" + "<div id='resultBox' class='code'></div>"
					+ "</div>" + "</div>" + "</body> </html>");
			out.print(body);
		} catch (IOException e) {
			getServletContext().log("Internal server error", e);
		}
	}

	private String getHtmlHeader() {
		String res = new String();
		res = String.format("<html> "
				+ "<head> "
				+ "<title> Welcome to Patch Diff Plugin </title> "
				+ "<link rel='stylesheet' href='static/bootstrap.css'>"
				+ "<script type='text/javascript' src='static/script.js'></script>"
				+ "</head>");
		return res;
	}

}
