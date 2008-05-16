/*
 * SIMILE Crowbar
 */

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
                            var paramsArray = parsePOSTParams(this.data);
                            var params = paramsArray[0];
                            var dataString = paramsArray[1];

                            // POST method requests must wrap the encoded text in a MIME stream
                            var Cc = Components.classes;
                            var Ci = Components.interfaces;
                            var stringStream = Cc["@mozilla.org/io/string-input-stream;1"].createInstance(Ci.nsIStringInputStream);
                            if ("data" in stringStream) { // Gecko 1.9 or newer
                                stringStream.data = dataString;
                            } else {// 1.8 or older
                                stringStream.setData(dataString, dataString.length);
                            }

                            var postData = Cc["@mozilla.org/network/mime-input-stream;1"].createInstance(Ci.nsIMIMEInputStream);
                            postData.addHeader("Content-Type", "application/x-www-form-urlencoded");
                            postData.addContentLength = true;
                            postData.setData(stringStream);
                            // postData is ready to be used as aPostData argument
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
                                var browser = document.getElementById("browser");
                                var page;
                                var url = params.url;
                                var urlbar = document.getElementById("urlbar");
                                urlbar.value = url;
                                
                                if (params.silent) {
                                    var silent = (params.silent.toLowerCase() == "true");
                                } else {
                                    var silent = false;
                                }
                                
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
                                } else if (params.mode == "shoot") {
                                    dump("shoot mode\n");
                                    var process = function() {
                                        var win = browser; 
                                        var w = win.contentDocument.width;
                                        var h = win.contentDocument.height; 
                                
                                        var canvas = document.getElementById("myCanvas");
                                        canvas.style.display = "inline";
                                        canvas.width = w;
                                        canvas.height = h; 

                                        var ctx = canvas.getContext("2d");
                                        ctx.clearRect(0, 0, canvas.width, canvas.height);
                                        ctx.save();
                                        ctx.scale(1.0, 1.0); 
                                        ctx.drawWindow(win.contentWindow, 0, 0, w, h, "rgb(255,255,255)");
                                        ctx.restore();

                                        try {
                                            var url = canvas.toDataURL("image/png"); 
                                            url = url.split(",")[1].base64decode();
                                        } catch(ex) {
                                            return alert("This feature requires XulRunner >= 1.8.1\n" + ex);
                                        }
                            
                                        canvas.style.display = "none";
                                        canvas.width = 1;
                                        canvas.height = 1;
                    
                                        var code = "200";
                                        var mime_type = "image/png";
                                        return [ code, mime_type, url ];
                                    };
                                } else if (params.mode == "scrape") {
                                    dump("scraping mode\n");
                                    var process = function() {
                                        var code = "200";
                                        var mime_type = "application/rdf+xml";
                                        var page = "";
                                        var document = browser.contentDocument;
                                        var scraper = params.scraper;
                                        if (scraper && (scraper.match(/^http\:\/\/.*$/) || scraper.match(/^file\:\/\/.*$/))) {
                                            dump("scraping: " + scraper + "\n");
                                            var results = Scraper.scrape(document, browser, scraper, silent);
                                            
                                            code = "200";
                                            if (results.params.outputFormat == "json") {
                                                mime_type = "application/json";
                                                
                                                var jsonOutputMode = ("jsonOutputMode" in results.params) ? results.params.jsonOutputMode : "relax";
                                                if (jsonOutputMode == "relax") {
                                                    page = jsonize(results.json);
                                                } else {
                                                    page = jsonize(results.json, { breakLines: false });
                                                }
                                            } else {
                                                mime_type = "text/rdf+n3";
                                                page = results.model.store.toRDFXML();
                                            }
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
                                    try {
                                        outstream.write(response, response.length);
                                        instream.close();
                                        outstream.close();
                                    } catch (e) {
                                         jsdump("Error " + e);
                                    }
                                };

                                var loaded = function(wrappedContentWin){
                                    browser.removeEventListener("DOMContentLoaded", arguments.callee, false);
                                    
                                    dump("DOM content loaded...\n");
                                    setTimeout(respond, delay);
                                    // NOTE(SM): instead of scraping right away, we introduce an optional delay
                                    // because it might well be that the content we are looking for will be
                                    // included asynchronously after the DOM has finished loading by virtue
                                    // of some javascript executed in the "onLoad" method. While this does not
                                    // guarantee that such content will have finished loading, it's better than
                                    // nothing. 
                                };
                                
                                browser.addEventListener("DOMContentLoaded", loaded, false);
                                /*if (silent) {
                                    var autoConfirm = ("autoConfirm" in params) ? (params.autoConfirm.toLowerCase() == "true") : false;
                                
                                    var listener = {
                                        onLocationChange : function(webProgress, request, location) {},
                                        onProgressChange : function(webProgress, request, curSelfProgress, maxSelfProgress, curTotalProgress, maxTotalProgress) {},
                                        onSecurityChange : function(webProgress, request, state) {},
                                        //onStateChange : function(webProgress, request, stateFlags, status) {},
                                        onStatusChange : function(webProgress, request, status, message) {},
                                        QueryInterface : function(iid) {
                                            if (!iid.equals(Components.interfaces.nsIWebProgressListener) &&
                                                !iid.equals(Components.interfaces.nsISupportsWeakReference) &&
                                                !iid.equals(Components.interfaces.nsISupports)) {
                                                throw Components.results.NS_ERROR_NO_INTERFACE;
                                            }
                                            return this;
                                        }
                                    };
                                    listener.onStateChange = function(webProgress, request, stateFlags, status) {
                                        if (stateFlags & Components.interfaces.nsIWebProgressListener.STATE_TRANSFERRING) {
                                            browser.removeProgressListener(listener);alert("here");
                                            
                                            // Can we do something to override window.alert and window.confirm here?
                                            //browser.contentWindow.top.eval("(function() { var f = window.alert; window.alert = function(s) { f('Silent alert: ' + s); }; })()");
                                            //browser.contentWindow.top.alert = function(s) { window.alert('silent: ' + s); };
                                        }
                                    };
                                    browser.addProgressListener(
                                        listener, 
                                        Components.interfaces.nsIWebProgress.NOTIFY_STATE_DOCUMENT
                                    );
                                }*/
                                
                                dump("loading\n");
                                if (this.found_post){
                                    browser.webNavigation.loadURI(url, 0, null, postData, null); 
                                } else {
                                    browser.loadURI(url, null, null);
                                }
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
    return [params, payload];
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

var port = 10000;           // this value can be modified from the command line when starting crowbar
var default_delay = 3000;   // this value can be modified when invoking the action from the web service directly

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
