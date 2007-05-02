const RDF_XML_MIME_TYPE = "application/rdf+xml";
const RDF_XML_MIME_TYPE_DEPRECATED = "text/rdf";
const RDF_N3_MIME_TYPE = "text/rdf+n3";
const RDF_N3_MIME_TYPE_DEPRECATED = "application/n3";
const RDF_TURTLE_MIME_TYPE = "application/turtle";
const RDF_TURTLE_MIME_TYPE_UNOFFICIAL = "application/x-turtle";
const UNKNOWN_MIME_TYPE = "application/x-unknown-content-type";

var PiggyBank = {};

PiggyBank.isRDFXML = function(contentType) {
    return (contentType == RDF_XML_MIME_TYPE) || (contentType == RDF_XML_MIME_TYPE_DEPRECATED);
}

PiggyBank.isN3 = function(contentType) {
    return (contentType == RDF_N3_MIME_TYPE) || (contentType == RDF_N3_MIME_TYPE_DEPRECATED);
}

PiggyBank.isTurtle = function(contentType) {
    return (contentType == RDF_TURTLE_MIME_TYPE) || (contentType == RDF_TURTLE_MIME_TYPE_UNOFFICIAL);
}
