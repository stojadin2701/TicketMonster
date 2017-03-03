function displayForm(formID) {
	document.getElementById("manual_btn").style.display = "none";
	document.getElementById("file_btn").style.display = "none";	
	document.getElementById(formID).style.display = "inline";
}

function reset() {
	document.getElementById("manual_btn").style.display = "inline";
	document.getElementById("file_btn").style.display = "inline";
}