function PB_Data() {
    this.statements = [];
    this.tags = [];
    this.rdfxml = [];
    this.turtle = [];
}

PB_Data.prototype.addStatement = function(subject, predicate, object, isLiteral) {
    this.statements.push([ subject, predicate, object, isLiteral ]);
}

PB_Data.prototype.addTag = function(subject, tagLabel) {
    this.tags.push([ subject, tagLabel ]);  
}

PB_Data.prototype.addRDFXML = function(rdfxml, baseURI) {
    this.rdfxml.push([ rdfxml, baseURI ]);  
}

PB_Data.prototype.addTurtle = function(turtle, baseURI) {
    this.turtle.push([ turtle, baseURI ]);  
}
