function PB_ScrapingUtilities() {}

PB_ScrapingUtilities.prototype.trimString = function(s) {
    var i = 0;
    var spaceChars = " \n\r\t" + String.fromCharCode(160) /* &nbsp; */;
    while (i < s.length) {
        var c = s.charAt(i);
        if (spaceChars.indexOf(c) < 0) {
            break;
        }
        i++;
    }
    s = s.substring(i);
    i = s.length;
    while (i > 0) {
        var c = s.charAt(i - 1);
        if (spaceChars.indexOf(c) < 0) {
            break;
        }
        i--;
    }
    return s.substring(0, i);
}

PB_ScrapingUtilities.prototype.gatherElementsOnXPath = function(document, parentNode, xpath, nsResolver) {
    var elmts = [];
    
    var iterator = document.evaluate(xpath, parentNode, nsResolver, XPathResult.ANY_TYPE,null);
    var elmt = iterator.iterateNext();
    var i = 0;
    while (elmt) {
        elmts[i++] = elmt;
        elmt = iterator.iterateNext();
    }
    return elmts;
}

PB_ScrapingUtilities.prototype.collectURLsWithSubstring = function(document, substring) {
    var namespace = document.documentElement.namespaceURI;

    var resolver = namespace ? function(prefix) {
      return (prefix == 'x') ? namespace : null;
    } : null;

    var xpath = namespace ? "//x:a" : "//A";

    var urls = [];
    var addedURLs = [];
    
    var links = document.evaluate(xpath, doc, resolver, XPathResult.ANY_TYPE, null);
    var link = links.iterateNext();
    while (link) {
        var href = link.href;
        if (href.indexOf(substring) >= 0 && !(addedURLs[href])) {
            urls.push(href);
            addedURLs[href] = true;
        }
        link = links.iterateNext();
    }
    
    return urls;
}
