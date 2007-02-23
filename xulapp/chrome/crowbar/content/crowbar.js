
function WebProgressListener() {}

WebProgressListener.prototype = {
  _done : null,

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
    const WPL = Components.interfaces.nsIWebProgressListener;
    
    if (stateFlags & WPL.STATE_IS_NETWORK) {
        if (stateFlags & WPL.STATE_STOP) {
            this._done();
        }
    }
  },

  onLocationChange: function(webProgress, request, location) {
    var urlbar = document.getElementById("urlbar");
    urlbar.value = location.spec;
  },

  onProgressChange: function(webProgress, request, curSelf, maxSelf, curTotal, maxTotal) {
  },

  onStatusChange: function(webProgress, request, status, message) {
  },

  onSecurityChange: function(webProgress, request, state) {
  }
};

function SocketListener() {}

SocketListener.prototype = {

  onStopListening: function(serv, status) {
    // nothing to do here
  },
    onSocketAccepted: function(serv, transport) {    try {        var outstream = transport.openOutputStream(0,0,0);        var stream = transport.openInputStream(0,0,0);        var instream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);        instream.init(stream);    } catch (e) {        jsdump("Error " + e);    }    var dataListener = {        data : "",        found_get : 0,
        found_post : 0,         onStartRequest: function(request, context){ },        onStopRequest: function(request, context, status) {            instream.close();            outstream.close();        },        onDataAvailable: function(request, context, inputStream, offset, count) {            this.data = instream.read(count);
    
            if (this.data.match(/GET/)) {                this.found_get++;            } else if (this.data.match(/POST/)) {                this.found_post++;
            }
    
            if (this.data.match(/[\n\r]{2}/)) {                // these string fragments are horrible, we should think of using a sort of template system
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
                var headers = "HTTP/1.0 200 OK\nContent-type: text/html\n\n";

                if (this.found_get) {
                    // process a GET request
                    var body = body_header + getForm() + body_footer;
                    var webpage = headers + body;
                    outstream.write(webpage, webpage.length);                    instream.close();                    outstream.close();
                } else if (this.found_post) {
                    // process a POST request
                    var params = parsePost(this.data)
                    var url = params.url;
    
                    if (url && url.match(/^http\:\/\/.*$/)) {
                        var progessListener = new WebProgressListener();
                        var browser = document.getElementById("browser");
                        var page;
                    
                        var serialize = function() {
                            var serializer = new XMLSerializer();                            page = serializer.serializeToString(browser.contentDocument);
                            browser.removeProgressListener(progessListener);
                            var body = body_header + getForm(url) + "<textarea cols='100' rows='40'>" + page + "</textarea>" + body_footer;
                            var webpage = headers + body
                            outstream.write(webpage, webpage.length);                            instream.close();                            outstream.close();
                        }
                        var done = function() {
                            // wait a few seconds for the page to finish loading
                            setTimeout(serialize, 3000);
                        }
                        progessListener.setDone(done);
                        
                        browser.addProgressListener(progessListener, Components.interfaces.nsIWebProgress.NOTIFY_ALL);
                        browser.loadURI(url, null, null);
                    } else {
                        var headers = "HTTP/1.0 400 OK\n\n";
                        var body = body_header + getForm() + "<h1>" + url + " is not a valid HTTP URL</h1>" + body_footer;
                        var webpage = headers + body
                        outstream.write(webpage, webpage.length);                        instream.close();                        outstream.close();
                    }
                } else {
                    var headers = "HTTP/1.0 405 OK\n\n";
                    var body = body_header + getForm() + "<h1>Crowbar supports only GET or POST methods</h1>" + body_footer;
                    var webpage = headers + body
                    outstream.write(webpage, webpage.length);                    instream.close();                    outstream.close();
                }
            }        }    };    var pump = Components.classes["@mozilla.org/network/input-stream-pump;1"].createInstance(Components.interfaces.nsIInputStreamPump);    pump.init(stream, -1, -1, 0, 0, false);    pump.asyncRead(dataListener,null);  }};

function parsePost(data) {
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

function getForm(url) {
    if (!url) url = "http://simile.mit.edu/crowbar/test.html";
    return "<form action='' method='POST'><input id='url' type='text' name='url' value='" + url + "'/><input id='fetch' type='submit' value='Fetch'/></form>";
}

function showConsole() {
    window.open("chrome://global/content/console.xul", "_blank", "chrome,extrachrome,menubar,resizable,scrollbars,status,toolbar");
}

// we should make the port configurable
var port = 10000;

var serverSocket;
var socketListener;

function onLoad() {
    //showConsole();
    
    socketListener = new SocketListener();
    serverSocket = Components.classes["@mozilla.org/network/server-socket;1"].createInstance(Components.interfaces.nsIServerSocket);    serverSocket.init(port,false,-1);    serverSocket.asyncListen(socketListener);
}

function onUnload() {
    serverSocket.close()
}

function jsdump(str) {    Components.classes['@mozilla.org/consoleservice;1'].getService(Components.interfaces.nsIConsoleService).logStringMessage(str);}

window.addEventListener("load", onLoad, false);
window.addEventListener("unload", onUnload, false);
