package com.pramati.gerrit.plugin.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "results")
public class CompareResponse {
	private List<String> methodsAdded;
	private List<String> methodsDeleted;

	public CompareResponse() {
		super();
	}

	public CompareResponse(List<String> methodsAdded, List<String> methodsDeleted) {
		this.methodsAdded = methodsAdded;
		this.methodsDeleted = methodsDeleted;
	}

	public List<String> getMethodsAdded() {
		return methodsAdded;
	}

	public void setMethodsAdded(List<String> methodsAdded) {
		this.methodsAdded = methodsAdded;
	}

	public List<String> getMethodsDeleted() {
		return methodsDeleted;
	}

	public void setMethodsDeleted(List<String> methodsDeleted) {
		this.methodsDeleted = methodsDeleted;
	}

}
