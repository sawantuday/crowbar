var PB_HTTPUtilities = {
    doGet : function(url, onStatus, onDone, type) {
        var xmlhttp = new XMLHttpRequest();
        if (!type) type = "text/xml";
        
        xmlhttp.open('GET', url, true);
        xmlhttp.overrideMimeType(type);
        xmlhttp.onreadystatechange = function() {
            PB_HTTPUtilities_onReadyStateChange(xmlhttp, onStatus, onDone);
        };
        xmlhttp.send(null);
    },
    doPost : function(url, body, onStatus, onDone) {
        var xmlhttp = new XMLHttpRequest();
        
        xmlhttp.open('POST', url, true);
        xmlhttp.overrideMimeType("text/xml");
        xmlhttp.onreadystatechange = function() {
            PB_HTTPUtilities_onReadyStateChange(xmlhttp, onStatus, onDone);
        };
        xmlhttp.send(body);
    },
    doCustomPost : function(url, body, onStatus, onDone) {
        var xmlhttp = new XMLHttpRequest();
        
        xmlhttp.open('POST', url, true);
        xmlhttp.onreadystatechange = function() {
            PB_HTTPUtilities_onReadyStateChange(xmlhttp, onStatus, onDone);
        };
        xmlhttp.send(body);
    },
    doOptions : function(url, body, onStatus, onDone) {
        var xmlhttp = new XMLHttpRequest();
        
        xmlhttp.open('OPTIONS', url, true);
        xmlhttp.onreadystatechange = function() {
            PB_HTTPUtilities_onReadyStateChange(xmlhttp, onStatus, onDone);
        };
        xmlhttp.send(body);
    }
};

var PB_HTTPUtilities_onReadyStateChange = function(xmlhttp, onStatus, onDone) {
    switch (xmlhttp.readyState) {

        // Request not yet made
        case 1:
        break;

        // Contact established with server but nothing downloaded yet
        case 2:
            try {
                // Check for HTTP status 200
                if (xmlhttp.status != 200) {
                    if (onStatus) {
                        onStatus(
                            xmlhttp.status,
                            xmlhttp.statusText,
                            xmlhttp
                        );
                        xmlhttp.abort();
                    }
                }
            } catch (e) {
                PB_Debug.onCaughtException(e);
            }
        break;

        // Called multiple times while downloading in progress
        case 3:
        break;

        // Download complete
        case 4:
            try {
                if (onDone) {
                    onDone(xmlhttp.responseText, xmlhttp);
                }
            } catch (e) {
                PB_Debug.onCaughtException(e);
            }
        break;
    }
};
