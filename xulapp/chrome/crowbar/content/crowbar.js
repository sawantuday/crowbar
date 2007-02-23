
// nsIWebProgressListener implementation to monitor activity in the browser.
function WebProgressListener() {
}

WebProgressListener.prototype = {
  _requestsStarted: 0,
  _requestsFinished: 0,

  // We need to advertize that we support weak references.  This is done simply
  // by saying that we QI to nsISupportsWeakReference.  XPConnect will take
  // care of actually implementing that interface on our behalf.
  QueryInterface: function(iid) {
    if (iid.equals(Components.interfaces.nsIWebProgressListener) ||
        iid.equals(Components.interfaces.nsISupportsWeakReference) ||
        iid.equals(Components.interfaces.nsISupports))
      return this;
    
    throw Components.results.NS_ERROR_NO_INTERFACE;
  },

  // This method is called to indicate state changes.
  onStateChange: function(webProgress, request, stateFlags, status) {
    const WPL = Components.interfaces.nsIWebProgressListener;

    var progress = document.getElementById("progress");

    if (stateFlags & WPL.STATE_IS_REQUEST) {
      if (stateFlags & WPL.STATE_START) {
        this._requestsStarted++;
      } else if (stateFlags & WPL.STATE_STOP) {
        this._requestsFinished++;
      }
      if (this._requestsStarted > 1) {
        var value = (100 * this._requestsFinished) / this._requestsStarted;
        progress.setAttribute("mode", "determined");
        progress.setAttribute("value", value + "%");
      }
    }

    if (stateFlags & WPL.STATE_IS_NETWORK) {
      if (stateFlags & WPL.STATE_START) {
        progress.setAttribute("style", "");
      } else if (stateFlags & WPL.STATE_STOP) {
        progress.setAttribute("style", "display: none");
        this.onStatusChange(webProgress, request, 0, "Done");
        this._requestsStarted = this._requestsFinished = 0;
      }
    }
  },

  // This method is called to indicate progress changes for the currently loading page.
  onProgressChange: function(webProgress, request, curSelf, maxSelf, curTotal, maxTotal) {
    if (this._requestsStarted == 1) {
      var progress = document.getElementById("progress");
      if (maxSelf == -1) {
        progress.setAttribute("mode", "undetermined");
      } else {
        progress.setAttribute("mode", "determined");
        progress.setAttribute("value", ((100 * curSelf) / maxSelf) + "%");
      }
    }
  },

  // This method is called to indicate a change to the current location.
  onLocationChange: function(webProgress, request, location) {
    var urlbar = document.getElementById("urlbar");
    urlbar.value = location.spec;
  },

  // This method is called to indicate a status changes for the currently
  // loading page.  The message is already formatted for display.
  onStatusChange: function(webProgress, request, status, message) {
    var statusEl = document.getElementById("status");
    statusEl.setAttribute("label", message);
  },

  // This method is called when the security state of the browser changes.
  onSecurityChange: function(webProgress, request, state) {
    const WPL = Components.interfaces.nsIWebProgressListener;

    var sec = document.getElementById("security");

    if (state & WPL.STATE_IS_INSECURE) {
      sec.setAttribute("style", "display: none");
    } else {
      var level = "unknown";
      if (state & WPL.STATE_IS_SECURE) {
        if (state & WPL.STATE_SECURE_HIGH)
          level = "high";
        else if (state & WPL.STATE_SECURE_MED)
          level = "medium";
        else if (state & WPL.STATE_SECURE_LOW)
          level = "low";
      } else if (state & WPL_STATE_IS_BROKEN) {
        level = "mixed";
      }
      sec.setAttribute("label", "Security: " + level);
      sec.setAttribute("style", "");
    }
  }
};

// nsIServerSocketListener implementation to monitor activity in the browser.
function SocketListener() {
}

SocketListener.prototype = {

  onStopListening: function(serv, status) {
    // nothing to do here
  },
    // called after connection established  onSocketAccepted: function(serv, transport) {    try {      var outstream = transport.openOutputStream(0,0,0);      var stream = transport.openInputStream(0,0,0);      var instream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);      instream.init(stream);    } catch (e) {      alert("Error " + e);    }    var dataListener = {      data : "",      return_count : 0,      found_get : 0,
      found_post : 0,       onStartRequest: function(request, context){ },      onStopRequest: function(request, context, status) {        instream.close();        outstream.close();      },      // called when there are new data      onDataAvailable: function(request, context, inputStream, offset, count) {        this.data = instream.read(count);        if (this.data.match(/GET/)) {          this.found_get++;        } else if (this.data.match(/POST/)) {          this.found_post++;
        }        var re = RegExp("[\n\r]{2}");        if (this.data.match(re)) {         this.return_count++;         if (this.return_count > 0) {
            var body_header = "<html><head><title>Crowbar</title></head><body>";
            var body_footer = "</body></html>\n";
            var form = "<form action='' method='POST'><input type='text' name='url'/><input type='submit' value='Go'/></form>";
            var headers = "HTTP/1.0 200 OK\nContent-type: text/html\n\n";
            if (this.found_get) {
                dump("GET request");
                var body = body_header + form + body_footer;
            } else if (this.found_post) {
                dump("POST request");
                var url = "http://simile.mit.edu/";
                var browser = document.getElementById("browser");
                browser.loadURI(url, null, null);
                var body = body_header + form + "<h1>done!</h1>" + body_footer;
            } else {
                var headers = "HTTP/1.0 500 OK\n\n";
                var body = "";
            }
            
            var webpage = headers + body;
            outstream.write(webpage, webpage.length);            instream.close();            outstream.close();
          }        }      }    };    // pump takes in data in chunks asynchronously    var pump = Components.classes["@mozilla.org/network/input-stream-pump;1"].createInstance(Components.interfaces.nsIInputStreamPump);    pump.init(stream, -1, -1, 0, 0, false);    pump.asyncRead(dataListener,null);  }};

var port = 10000;

var progessListener;
var serverSocket;
var socketListener;

function showConsole() {
    window.open("chrome://global/content/console.xul", "_blank", "chrome,extrachrome,menubar,resizable,scrollbars,status,toolbar");
}

function onLoad() {
    showConsole();
    
    progessListener = new WebProgressListener();
    socketListener = new SocketListener();
    
    var browser = document.getElementById("browser");
    browser.addProgressListener(progessListener, Components.interfaces.nsIWebProgress.NOTIFY_ALL);
    
    serverSocket = Components.classes["@mozilla.org/network/server-socket;1"].createInstance(Components.interfaces.nsIServerSocket);    serverSocket.init(port,false,-1);    serverSocket.asyncListen(socketListener);
}

function onUnload() {
    serverSocket.close()
}

window.addEventListener("load", onLoad, false);
window.addEventListener("unload", onUnload, false);
