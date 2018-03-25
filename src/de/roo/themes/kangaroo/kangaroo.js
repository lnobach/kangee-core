
function bringToFront() {
	rotateScale("kangaroo", 0, 1, 0, 0, false);
	rotateScale("filecontainer", 0, 1, 0, 0, false);
	setOpacity("greeter", 0, false);

}

function undoBringToFront(write) {
	rotateScale("kangaroo", -15, 0.4, -20, -20, write);
	rotateScale("filecontainer", -9, 0, -320, -50, write);
	setOpacity("greeter", 70, write);
}

function rotateScale(elementId, degree, scale, transX, transY, write) {
	var param = "translate(" + transX + "px, " + transY + "px) rotate(" + degree + "deg) scale(" + scale + ")";
	if (!write) {
		var element = document.getElementById(elementId);
		element.style.webkitTransform = param;
		element.style.MozTransform = param;
		element.style.OTransform = param;
		element.style.msTransform = param;
		element.style.transform = param;
	} else {
		document.write("#" + elementId + "{");
		document.write("-webkit-transform: " + param + ";");
		document.write("-moz-transform: " + param + ";");
		document.write("-o-transform: " + param + ";");
		document.write("-ms-transform: " + param + ";");
		document.write("transform: " + param + ";");
		document.write("}");
	}
}

function cssSmooth() {
	var param = "all 1s ease-in-out";
	document.write("-webkit-transition: " + param + ";");
	document.write("-moz-transition: " + param + ";");
	document.write("-o-transition: " + param + ";");
	document.write("-ms-transition: " + param + ";");
	document.write("transition: " + param + ";");

}

function setOpacity(elementId, value, write) {
	if (!write) {
		var element = document.getElementById(elementId);
		element.style.opacity = value/100;
		element.style.filter = 'alpha(opacity=' + value + ')';
	} else {
		document.write("#" + elementId + "{");
		document.write("opacity: " + value/100 + ";");
		document.write("-moz-opacity: " + value/100 + ";");
		document.write("filter: Alpha(opacity=" + value + ");");
		document.write("}");
	}
}

function writeCSSJSOnly() {
	var kangaroo = document.getElementById("kangaroo");
	var filecontainer = document.getElementById("filecontainer");

	document.writeln("<style type=\"text\/css\">");
	document.writeln(".smooth {");
	cssSmooth();
	document.writeln("}");
	undoBringToFront(true);
	document.writeln("<\/style>");
}

