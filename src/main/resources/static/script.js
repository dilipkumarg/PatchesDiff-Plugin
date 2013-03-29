function getResult() {
	var changeId = document.getElementById('input').value, xmlhttp;
	// checking given input is number or not
	if (isNaN(changeId)) {
		alert("Invalid change Id");
		return;
	}
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			document.getElementById("resultBox").innerHTML = xmlhttp.responseText;
		}
	}
	xmlhttp.open("GET", "patchdiff/" + changeId, true);
	xmlhttp.send();
}