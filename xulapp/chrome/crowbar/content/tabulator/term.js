// These are the classes corresponding to the RDF and N3 data models
//
// Designed to look like rdflib and cwm designs.
//
// Issues: Should the names start with RDF to make them
//      unique as program-wide symbols?
//
// W3C open source licence 2005.
//

var RDFTracking = 0  // Are we requiring reasons for statements?

//takes in an object and makes it an object if it's a literal
function makeTerm(val) {
    //  fyi("Making term from " + val)
    if (typeof val == 'object') return val;
    if (typeof val == 'string') return new RDFLiteral(val);
    if (typeof val == 'undefined') return undefined;
    alert("Can't make term from " + val + " of type " + typeof val) // @@ add numbers
    return null;
}

//	Symbol

function RDFEmpty() {
	return this;
}
RDFEmpty.prototype.termType = 'empty'
RDFSymbol.prototype.toString = function () { return "" }
RDFSymbol.prototype.toNT = function () { return "" }

function RDFSymbol_toNT(x) {
    return ("<" + x.uri + ">")
}

function toNT() {
    return RDFSymbol_toNT(this)
}

function RDFSymbol(uri) {
    this.uri = uri
    return this
}
	
RDFSymbol.prototype.termType = 'symbol'
RDFSymbol.prototype.toString = toNT
RDFSymbol.prototype.toNT = toNT


//	Blank Node

var RDFNextId = 0;  // Gobal genid
var RDFGenidPrefix = "genid:"
var NTAnonymousNodePrefix = "_:n"

function RDFBlankNode(id) {
    if (id)
    	this.id = val;
    else
    	this.id = RDFNextId++
    return this
}

RDFBlankNode.prototype.termType = 'bnode'

RDFBlankNode.prototype.toNT = function() {
    return NTAnonymousNodePrefix + this.id
}
RDFBlankNode.prototype.toString = RDFBlankNode.prototype.toNT  

//	Literal

//LiteralSmush = []

function RDFLiteral(value, lang, datatype) {
    this.value = value
    this.lang=lang;	  // string
    this.datatype=datatype;  // term
    this.toString = RDFLiteralToString
    this.toNT = RDFLiteral_toNT
    //if (LiteralSmush[this.toNT()]) return LiteralSmush[this.toNT()];
    //else LiteralSmush[this.toNT()]=this;
    return this
}

RDFLiteral.prototype.termType = 'literal'

function RDFLiteral_toNT() {
    var str = this.value
    if (typeof str != 'string') {
	alert("Value of RDF literal is not string: "+str)
	throw Error("Value of RDF literal is not string: "+str)
    }
    str = str.replace(/\\/g, '\\\\');  // escape
    str = str.replace(/\"/g, '\\"');
    str = '"' + str + '"'

    if (this.datatype){
	str = str + '^^' + this.datatype.toNT
    }
    if (this.lang) {
	str = str + "@" + this.lang
    }
    return str
}

function RDFLiteralToString() {
    fyi("literal '"+this.value+"'") //@@@
    return this.value
}
    
RDFLiteral.prototype.toString = RDFLiteralToString   
RDFLiteral.prototype.toNT = RDFLiteral_toNT

function RDFCollection() {
    this.id = RDFNextId++
    this.elements = []
    this.closed = false
}

RDFCollection.prototype.termType = 'collection'

RDFCollection.prototype.toNT = function() {
    return NTAnonymousNodePrefix + this.id
}
RDFCollection.prototype.toString = RDFCollection.prototype.toNT 

RDFCollection.prototype.append = function (el) {
    this.elements[this.elements.length] = el
}

RDFCollection.prototype.close = function () {
    this.closed = true
}

//	Statement
//
//  This is a triple with an optional reason.
//
//   The reason can point to provenece or inference
//
function RDFStatement_toNT() {
    return (this.subject.toNT() + " "
	    + this.predicate.toNT() + " "
	    +  this.object.toNT() +" .")
}

function RDFStatement_toRDFXML(nsIndex, local) {
    var ns = "ns" + nsIndex;
    var qname = ns + ":" + local;
    var pred = (this.object.uri) ?
	"<" + qname + " rdf:resource='" + this.object.uri
		+ "'/>\n" :
	"<" + qname + ">" + this.object.value + "</"
		+ qname + ">";

    return "<rdf:Description rdf:about='" + this.subject.uri + "'>\n" +
	pred + "</rdf:Description>\n";
}

function RDFStatement(subject, predicate, object, why) {
    this.subject = makeTerm(subject)
    this.predicate = makeTerm(predicate)
    this.object = makeTerm(object)
    if (typeof why !='undefined') {
	this.why = why
    } else if (RDFTracking) {
	fyi("WARNING: No reason on "+subject+" "+predicate+" "+object)
    }
    return this
}

RDFStatement.prototype.toNT = RDFStatement_toNT
RDFStatement.prototype.toString = RDFStatement_toNT
RDFStatement.prototype.toRDFXML = RDFStatement_toRDFXML


//	Formula
//
//	Set of statements.

function RDFFormula() {
    this.statements = []
    this.constraints = []
    this.initBindings = []
    this.optional = []
    this.prefixes = []
    this.prefixLookup = [];
    return this
}

/*function RDFQueryFormula() {
	this.statements = []
	this.constraints = []
	this.initBindings = []
	this.optional = []
	return this
}*/

function RDFFormula_toNT() {
    return this.statements.join('\n')
}

function RDFFormula_toRDFXML() {
    var rdf = "";
    for each (var statement in this.statements) {
	var delim = 0;
	var pred = statement.predicate.uri;
	if (pred.lastIndexOf('#') >= 0) {
		delim = pred.lastIndexOf('#');
	} else if (pred.lastIndexOf('/') >= 0) {
		delim = pred.lastIndexOf('/');
	}
	var ns = pred.substring(0, delim+1);
	var local = pred.substr(delim+1);
	if (this.prefixLookup[ns]) {
	        rdf += statement.toRDFXML(this.prefixLookup[ns], local);
	} else {
		this.prefixes.push(ns);
        	this.prefixLookup[ns] = this.prefixes.length-1;
	        rdf += statement.toRDFXML(this.prefixes.length-1, local);
	}
    }
    rdf = "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" + nsKey(this.prefixes) + ">\n" + rdf + "</rdf:RDF>\n";
    return rdf;
}

function nsKey(namespaces) {
    var keys = "";
    for (var i = 0; i < namespaces.length; i++) {
        keys += " xmlns:ns" + i + "='" + namespaces[i] + "'";
    }
    return keys;
}

//RDFQueryFormula.prototype = new RDFFormula()
//RDFQueryFormula.termType = 'queryFormula'
RDFFormula.prototype.termType = 'formula'
RDFFormula.prototype.toNT = RDFFormula_toNT
RDFFormula.prototype.toString = RDFFormula_toNT   
RDFFormula.prototype.toRDFXML = RDFFormula_toRDFXML

RDFFormula.prototype.add = function(subj, pred, obj, why) {
    this.statements.push(new RDFStatement(subj, pred, obj, why))
}

// Convenience methods on a formula allow the creation of new RDF terms:

RDFFormula.prototype.sym = function(uri) {
    // dfhuynh: only wrap strings
    return (typeof uri == "string") ? new RDFSymbol(uri) : uri;
}

RDFFormula.prototype.literal = function(val, lang, dt) {
    return new RDFLiteral(val, lang, dt)
}

RDFFormula.prototype.bnode = function(id) {
    return new RDFBlankNode(id)
}

RDFFormula.prototype.formula = function() {
    return new RDFFormula()
}

RDFFormula.prototype.collection = function () {
    return new RDFCollection()
}


/*RDFFormula.prototype.queryFormula = function() {
	return new RDFQueryFormula()
}*/

/*RDFFormula.prototype.variable = function(val) {
	alert("YOOOO!!!!");
	return new RDFLiteral(val)
}*/


// The namespace function generator 
// dfhuynh: changed Namespace to RDFNamespace or otherwise we'll get
//   Error: redeclaration of const Namespace

function RDFNamespace(nsuri) {
    return function(ln) { return new RDFSymbol(nsuri+ln) }
}

// Parse a single token
//
// The bnode bit should not be used on program-external values; designed
// for internal work such as storing a bnode id in an HTML attribute.
// Not coded for literals.

RDFFormula.prototype.fromNT = function(str) {
    var len = str.length
    var ch = str.slice(0,1)
    if (ch == '<') return this.sym(str.slice(1,len-1))
    if (ch == '_') {
		var x = new RDFBlankNode()
		x.id = parseInt(str.slice(3))
		RDFNextId--
		return x
    }
    alert("Can't yet convert from NT: '"+str+"', "+str[0])
    return null;
}

// ends
