/*
 * SIMILE Crowbar
 */

function WebProgressListener(url) {
	this._url = url;
}

WebProgressListener.prototype = {
	_done : null,

	_stopOne : false,

	setDone: function(done) {
		this._done = done;
	}, 
  
	QueryInterface: function(iid) {
		if (iid.equals(Components.interfaces.nsIWebProgressListener) ||
			iid.equals(Components.interfaces.nsISupportsWeakReference) ||
			iid.equals(Components.interfaces.nsISupports))
		return this;
		throw Components.results.NS_ERROR_NO_INTERFACE;
	},

	onStateChange: function(webProgress, request, stateFlags, status) {
		var WPL = Components.interfaces.nsIWebProgressListener;
		dump(request.name + " " + stateFlags + "\n");

		// this should always return; but it doesn't wait for the
		// document to finish loading all subrequests, and it
		// definitely doesn't allow for the onload actions to finish
		//if (this._url == request.name) {
		//	if (stateFlags & WPL.STATE_STOP) {
		//		if (stateFlags & WPL.STATE_IS_REQUEST) {
		//			this._done();
		//		}
		//	}
		//}

		// this is technically correct according to documentation,
		// but not every page finishes these two phases?
		// http://www.xulplanet.com/references/xpcomref/ifaces/nsIWebProgressListener.html
		if (this._url == request.name) {
			if (stateFlags & WPL.STATE_STOP) {
				if (stateFlags & WPL.STATE_IS_DOCUMENT) {
					this._stopOne = true;
				} else if (this._stopOne && (stateFlags & WPL.STATE_IS_WINDOW)) {
					this._done();
				}
			}
		}
	},

	onLocationChange: function(webProgress, request, location) {
		var urlbar = document.getElementById("urlbar");
		urlbar.value = location.spec;
	},

	onProgressChange: function(webProgress, request, curSelf, maxSelf, curTotal, maxTotal) {},

	onStatusChange: function(webProgress, request, status, message) {},

	onSecurityChange: function(webProgress, request, state) {}
};

function SocketListener() {}

SocketListener.prototype = {
	onStopListening: function(serv, status) {
		// nothing to do here
	},

	onSocketAccepted: function(serv, transport) {
		try {
			var outstream = transport.openOutputStream(1,0,0); // make sure stream reading is blocking (see http://simile.mit.edu/issues/browse/CROWBAR-1)
			var stream = transport.openInputStream(0,0,0);
			var instream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);
			instream.init(stream);
		} catch (e) {
			jsdump("Error " + e);
		}

		var dataListener = {
			data : "",
			found_get : 0,
			found_post : 0, 
			onStartRequest: function(request, context) {},
			onStopRequest: function(request, context, status) {
				instream.close();
				outstream.close();
			},

			onDataAvailable: function(request, context, inputStream, offset, count) {
				dump("data available\n");
				this.data = instream.read(count);
			
				if (this.data.match(/GET/)) {
					this.found_get++;
				} else if (this.data.match(/POST/)) {
					this.found_post++;
				}
			
				if (this.data.match(/[\n\r]{2}/)) {
					// these string fragments are horrible, we should think of using a sort of template system
					var headers = "HTTP/1.0 200 OK\nContent-type: text/html\n\n";
					var body_header = "<html>"+
						"<head><title>Crowbar</title>" +
						"<style>" +
						"html { height: 100% }" +
						"input { border: 1px solid #ccc; padding: 0.2em 0.4em; }" +
						"input#url { width: 40em; margin-right: 1em; }" +
						"textarea { border: 1px solid #ccc; background-color: #f0f0f0; }" +
									"</style>" +
						"<link rel='stylesheet' type='text/css' href='http://simile.mit.edu/styles/default.css'/>" + 
						"</head><body><div id='body'><h1>Crowbar</h1>";
					var body_footer = "</div></body></html>\n";
			
					if (this.found_get || this.found_post) {
						if (this.found_get) {
							var params = parseGETParams(this.data);
						} else {
							var params = parsePOSTParams(this.data);
						}
						dump("parsed headers\n");
			
						if (!params.url) {
							var body = body_header + getForm() + body_footer;
							var webpage = headers + body;
							outstream.write(webpage, webpage.length);
							instream.close();
							outstream.close();
						} else {
							if (params.url.match(/^https?\:\/\/.*$/)) {
								var progessListener = new WebProgressListener(params.url);
								var browser = document.getElementById("browser");
								var page;
			
								var url = params.url;
			
								if (params.delay) {
									var delay = params.delay;
								} else {
									var delay = default_delay;
								}

								dump("environment set, examining action\n");
			
								if (params.mode == "links") {
									dump("links mode\n");
									var process = function() {
										var document = browser.contentDocument;
										var code = "200";
										var mime_type = "text/plain";
										var page = "";
										var iterator = document.evaluate("//A", document, null, XPathResult.ANY_TYPE, null);
										var node = iterator.iterateNext();
										while (node) {
											var url = node.toString();
											if (url.match(/^http\:\/\/.*$/)) {
												page += url + "\n";
											}
											node = iterator.iterateNext();
										}
										return [ code, mime_type, page ];
									};
								} else if (params.mode == "scrape") {
									dump("scraping mode\n");
									var process = function() {
										var code = "200";
										var mime_type = "application/rdf+xml";
										var page = "";
										var document = browser.contentDocument;
										var scraper = params.scraper;
										if (scraper && scraper.match(/^http\:\/\/.*$/)) {
											dump("scraping: " + scraper + "\n");
											var results = Scraper.scrape(document, browser, scraper);
											code = "200";
											mime_type = "text/rdf+n3";
											page = results.store.toRDFXML();
										} else {
											code = "400";
											mime_type = "text/html";
											page = "<h2>Crowbar needs a URL in the 'scraper' parameter in 'scrape' mode</h2>";
										}
										return [ code, mime_type, page ];
									};
								} else if (params.mode == "exhibit") {
									var process = function() {
										var code = "200";
										var mime_type = "application/rdf+xml";
										// TODO(SM): here we should find a way to ask the javascript environment that is loaded in the page 
										// for the Exhibit object and call its exporters
										return [ code, mime_type, page ];
									};
								} else {
									var process = function() {
										var code = "200";
										var mime_type = "text/html";
										var serializer = new XMLSerializer();
										var page = serializer.serializeToString(browser.contentDocument);
										return [ code, mime_type, page ];
									};
								}
								var respond = function() {
									dump("starting response\n");
									var page = process();
									var response = "HTTP/1.0 " + page[0] + " OK\nContent-type: " + page[1] + "\n\n";
									if (params.view && params.view == "browser") {
										response += body_header + getForm() + "<textarea cols='100' rows='40'>" + page[2] + "</textarea>" + body_footer;
										} else {
										response += page[2];
									}
									browser.removeProgressListener(progessListener);
									outstream.write(response, response.length);
									instream.close();
									outstream.close();
								};
								var done = function() {
									// wait for the page to finish loading
									dump("waiting for page load\n");
									setTimeout(respond, delay);
									// FIXME(SM): instead of using a delay, we should find a way to securely intercept
									// when the page onload method returns before we start processing the page. This does not
									// guarantee us that it will be done (as the onload could trigger asynchronous loading 
									// as well) but it's better than nothing. We should take a look at how Greasemonkey 
									// works in this regard.
								};
								progessListener.setDone(done);
								browser.addProgressListener(progessListener, Components.interfaces.nsIWebProgress.NOTIFY_ALL);
								dump("loading\n");
								browser.loadURI(url, null, null);
							} else {
								var headers = "HTTP/1.0 Bad Request\nContent-type: text/html\n\n";
								var response = headers + body_header + getForm() + "<h2>" + url + " is not a valid HTTP URL</h2>" + body_footer;
								outstream.write(response, response.length);
								instream.close();
								outstream.close();
							}
						}
			                } else {
						var headers = "HTTP/1.0 405 Method Not Allowed\nContent-type: text/html\n\n";
						var response = headers + body_header + getForm() + "<h2>Crowbar supports only GET or POST methods</h2>" + body_footer;
						outstream.write(response, response.length);
						instream.close();
						outstream.close();
					}
				}
			}
		};

		var pump = Components.classes["@mozilla.org/network/input-stream-pump;1"].createInstance(Components.interfaces.nsIInputStreamPump);
		pump.init(stream, -1, -1, 0, 0, false);
		dump("init\n");
		pump.asyncRead(dataListener,null);
	}
};

function parseGETParams(data) {
	var params = {};
	var url = data.split(/[\n\r]/)[0].split(" ")[1];
	var query = url.split("?")[1];
	if (query) {
		var tokens = query.split("&");
		for (var i = 0; i < tokens.length; i++) {
			var token = tokens[i];
			var param = token.split("=");
			var name = param[0];
			var value = decodeURIComponent(param[1]);
			params[name] = value;
		}
	}
	return params;
}

function parsePOSTParams(data) {
	var splits = data.split(/[\n\r]{2}/);
	var payload = splits[splits.length - 1];
	var params = {};
	var tokens = payload.split("&");
	for (var i = 0; i < tokens.length; i++) {
		var token = tokens[i];
		var param = token.split("=");
		var name = param[0];
		var value = decodeURIComponent(param[1]);
		params[name] = value;
	}
	return params;
}

function getForm(url, delay) {
	if (!url) url = "http://simile.mit.edu/crowbar/test.html";
	if (!delay) delay = default_delay;
	return "<form action='' method='GET'>" +
		" <input id='url' type='text' name='url' value='" + url + "'/>" +
		" <input id='delay' type='text' name='delay' value='" + delay + "'/>" +
		" <input id='fetch' type='submit' value='Fetch'/>" + 
		" <input type='hidden' name='view' value='browser'/>" +
		"</form>";
}

function showConsole() {
	window.open("chrome://global/content/console.xul", "_blank", "chrome,extrachrome,menubar,resizable,scrollbars,status,toolbar");
}

var port = 10000;         // this value can be modified from the command line when starting crowbar
var default_delay = 3000; // this value can be modified when invoking the action from the web service directly

var serverSocket;
var socketListener;

function onLoad() {
	showConsole();
	processCommandLineArgs();
	
	socketListener = new SocketListener();
	serverSocket = Components.classes["@mozilla.org/network/server-socket;1"].createInstance(Components.interfaces.nsIServerSocket);
	serverSocket.init(port,false,-1);
	serverSocket.asyncListen(socketListener);
}

function onUnload() {
	serverSocket.close();
}

function jsdump(str) {
	Components.classes['@mozilla.org/consoleservice;1'].getService(Components.interfaces.nsIConsoleService).logStringMessage(str);
}

/*
 * Process a series of command line arguments and modify default settings
 */
function processCommandLineArgs() {
	var nsCommandLine = window.arguments[0];
	nsCommandLine = nsCommandLine.QueryInterface(Components.interfaces.nsICommandLine);
	port = getCommandLineArg(nsCommandLine, 'port', port);
}

/*
 * Attempt to retrieve a parameter from the command line in the form of:
 * -flagString param / --flagString=param / Win: /flagString:param
 */
function getCommandLineArg(iface, flagString, defaultValue, caseSensitive) {
	if (defaultValue == undefined) defaultValue = null;
	if (caseSensitive == undefined) caseSensitive = false;
	try {
		var ret = iface.handleFlagWithParam(flagString, caseSensitive);
		return (ret != null) ? ret : defaultValue;
	} catch (e) {
		jsdump("Error retrieving parameter " + e);
	}
	return defaultValue;
}

window.addEventListener("load", onLoad, false);
window.addEventListener("unload", onUnload, false);
