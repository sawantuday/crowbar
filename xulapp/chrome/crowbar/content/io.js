var PB_IOUtilities = {
	getContent : function(url) {
		var ioService = Components.classes["@mozilla.org/network/io-service;1"].getService(Components.interfaces.nsIIOService);
		var scriptableStream = Components.classes["@mozilla.org/scriptableinputstream;1"].getService(Components.interfaces.nsIScriptableInputStream);
		var channel = ioService.newChannel(url,null,null);
		channel.QueryInterface(Components.interfaces.nsIRequest);
		channel.loadFlags |= Components.interfaces.nsIRequest.LOAD_BYPASS_CACHE;
		channel.QueryInterface(Components.interfaces.nsIChannel);
		var input = channel.open();
		scriptableStream.init(input);
		var str = "";
		var avail;
		while ((avail = input.available()) > 0)
			str += scriptableStream.read(avail);
		scriptableStream.close();
		input.close();
		return str;
	}
};
