const PB_NS = "http://simile.mit.edu/2005/04/piggy-bank#";
const PB_GENERATED_BY = PB_NS + "generatedBy";
const PB_ORIGIN = PB_NS + "originURL";
const PB_ORIGIN_TITLE = PB_NS + "originTitle";

const TAG_NS = "http://simile.mit.edu/2005/04/ontologies/tags#";
const TAG_PRED = TAG_NS + "stringTag";

function Store() {
	this.store = new RDFFormula();
	this.rdfp = new RDFParser(this.store);
	return this;
}

Store.prototype.addRDFXML = function(response, url) {
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","> addRDFXML");
	var domp = new DOMParser();
	var doc = domp.parseFromString(response, "text/xml");
	this.rdfp.parse(doc, url);
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","< addRDFXML");
};

Store.prototype.addTurtle = function(response, url) {
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","> addTurtle");
	// @@@we need a turtle parser in javascript...
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","< addTurtle");
};

Store.prototype.addStatement = function(s, p, o, isLiteral) {
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","> addStatement: " + s + "->" + p + "->" + o);
	if (isLiteral) {
		var obj = new RDFLiteral(o);
	} else {
		var obj = this.store.sym(o);
	}
	this.store.add(this.store.sym(s), this.store.sym(p), obj);
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","< addStatement");
};

Store.prototype.addBacktracking = function(s, scraper, origin, title) {
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","> addBacktracking");
	if (scraper && scraper != "") {
		this.addStatement(s, PB_GENERATED_BY, scraper, false);
	}
	if (origin && origin != "") {
		this.addStatement(s, PB_ORIGIN, origin, false);
	}
	if (title && title != "") {
		this.addStatement(s, PB_ORIGIN_TITLE, title, true);
	}
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","< addBacktracking");
};

// tagging was user-identification dependent in PB, but there's
// no really grand way to simulate that in Crowbar; so we don't,
// this is just a straight up string tag for now.
Store.prototype.addTag = function(s, tag) {
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","> addTag");
	this.addStatement(s, TAG_PRED, tag);
	if (PB_Debug.enabled()) PB_Debug.trace("store.js","< addTag");
};

Store.prototype.dispose = function() {
	// do nothing
};
