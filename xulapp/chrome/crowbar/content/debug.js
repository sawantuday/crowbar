/*
 * Debugging Utilities
 */

var PB_Debug = {};

PB_Debug.enabled = function() {
	return true;
}

PB_Debug.onCaughtException = function (e) {
	PB_Debug.print("Exception: " + e);
}

PB_Debug.print = function (msg) {
	dump("debug " + msg + "\n");
}

PB_Debug.trace = function (context,msg) {
	dump(context + " : " + msg + "\n");
}
