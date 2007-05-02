function PB_PiggyBankInterface() {
    this.toScrape = [];
    this.toLookup = [];
}

PB_PiggyBankInterface.prototype.scrapeURL = function(url, onSuccess, onFailure) {
    this.toScrape.push([ url, onSuccess, onFailure ]);
}

PB_PiggyBankInterface.prototype.lookupAddress = function(uri, address, onSuccess, onFailure) {
    this.toLookup.push([ uri, address, onSuccess, onFailure ]);
}
