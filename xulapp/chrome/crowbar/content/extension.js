var PB_Extension = {};

PB_Extension.createWorkingModel = function() {
	PB_Debug.trace("extension.js","> PB_Extension.createWorkingModel");
	var model = new Store();
	PB_Debug.trace("extension.js","< PB_Extension.createWorkingModel");
	return model;
};
