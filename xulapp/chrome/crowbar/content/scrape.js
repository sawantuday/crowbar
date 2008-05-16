var Scraper = {};

Scraper.scrape = function(doc, browser, scraper, silent) {
	var url = doc.location.href;
	if (url.indexOf("http://127.0.0.1") < 0) {
		this._url = url;
		this._title = doc.title;
		this._contentType = doc.contentType;
		this._loaded = true;
		if (doc.contentType == "application/vnd.mozilla.xul+xml") {
			var root = doc.documentElement;
			if (root.localName == "RDF" && root.namespaceURI == "http://www.w3.org/1999/02/22-rdf-syntax-ns#") {
				this._contentType = "application/rdf+xml";
			}
		}
	}

	var canProcess = false;
    var alert2 = silent ? function() {} : function(s) { alert(s); };

	if (doc.location) {
		canProcess = (url.match(/^(https?|file|chrome)\:\/\/.*$/) && url.indexOf("http://127.0.0.1") < 0);
	} else {
		canProcess = (this._url != null);
	}

	var scraping = false;
	var doneScraping = false;

	var tofetch = 0;
	var fetched = 0;
	var successfullyFetched = 0;
	var fetching = false;

	var counter = 2000;
	var delay = 500; 

	var title = this._title;
	var contentType = this._contentType;

    var output = {
        model:  null,
        json:   {},
        params: { outputFormat: "rdf/n3" }
    }

	// the 'loader' function is 'detached' and invoked every 'delay' milliseconds
	// to check if the java-side loading has finished

	var loader = function() {
		if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","> Scraper.loader");

		if (!fetching) {
			if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","> Scraper.fetching");
			fetching = true;

			// this is the triple store that is going to be filled by the screen scraper
			output.model = PB_Extension.createWorkingModel();

			// the function that will fetch the data into the model
			var fetchData = function(url, type) {
				var onStatus = function(status, statusText, xmlhttp) {
					if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","> Scraper.fetchData.onStatus(" + url + ")");
					if (status != 0) {
                        alert2("Fetching " + url + " return status '" + statusText + "' (" + status + ")");
					}
					if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","< Scraper.fetchData.onStatus(" + url + ")");
				};

				var onDone = function(responseText, xmlhttp) {
					fetched++;
					if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","> Scraper.fetchData.onDone(" + url + ")");
					var type = xmlhttp.getResponseHeader('Content-Type').split(';')[0];
					if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","Response body: " + responseText);
					if (type == RDF_XML_MIME_TYPE) {
						var result = output.model.addRDFXML(responseText, url);
					} else if (type == RDF_N3_MIME_TYPE || type == RDF_TURTLE_MIME_TYPE || type == '') {
						// NOTE: type '' is returned for file:/// URLs but this can happen here only for N3/turtle files
						var result = output.model.addTurtle(responseText, url);
					} else {
						if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","Type '" + type + "' not recognized, trying to guess it from the URL");
						if (url.match(/^.*\.(rdf|rdfs|owl)([\?\#].*)?$/)) {
							if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","URL's extensions suggests RDF/XML");
							var result = output.model.addRDFXML(responseText, url);
						} else if (url.match(/^.*\.(turtle|n3)([\?\#].*)?$/)) {
							if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","URL's extensions suggests RDF/N3 or RDF/Turtle");
							var result = output.model.addTurtle(responseText, url);
						} else {
							if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","URL '" + url + "' doesn't end with extensions we recognize, so aborting");
						}
					}
					if (result && result != "") {
                        alert2("Loading the data from " + url + " failed because: " + result);
					} else {
						successfullyFetched++;
					}
					if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","< Scraper.fetchData.onDone(" + url + ")");
				};

				if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","  Fetching " + url);
				tofetch++;

				if (url.match(/^file:\/.*$/)) {
					try {
						var str = PB_IOUtilities.getContent(url);
						fetched++;
						if (url.match(/^file:\/.*\.(rdf|rdfs|owl)$/)) {
							if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","File's extensions suggests RDF/XML");
							var result = output.model.addRDFXML(str, url);
						} else { // default to Turtle
							if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","Trying RDF/N3 or RDF/Turtle as default");
							output.model.addTurtle(str, url);
						}
						successfullyFetched++;
					} catch (e) {
                        alert2("scrape.js","Error Loading '" + url + "': " + e);
					}
				} else {
					PB_HTTPUtilities.doGet(url, onStatus, onDone, type);
				}
			}
            
			// if the page is loaded
			if (doc) {
				// if the page is itself an RDF/XML serialization, load it
				// NOTE: this is done here for RDF/XML and not for RDF/N3 because
				// firefox intercepts the RDF mime types before we can grab it
				// so we can't run PB directly from the URL loading but we 
				// need to wait for the user to click on the data coin
				if (PiggyBank.isRDFXML(contentType)) {
					var serializer = new XMLSerializer();
					output.model.addRDFXML(serializer.serializeToString(doc), url);
				}
                
				// if the page contains an HTML DOM, look for <link rel="alternate"> tags
				if (doc.getElementsByTagName) {
					try {
						var head = doc.getElementsByTagName("head")[0];
						if (head) { // sometimes documents have no 'head'
							var links = head.getElementsByTagName("link");
							for (var i = 0; i < links.length; i++) {
								var link = links[i];
								if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","Found <link/>: " + link.rel + " -> " + link.href + "[" + link.type + "]");
								if (link.href) {
									if (link.type == RDF_XML_MIME_TYPE || link.type == RDF_N3_MIME_TYPE || link.type == RDF_TURTLE_MIME_TYPE) {
										fetchData(link.href, link.type);
									}
								}
							}
						}
					} catch (e) {
						if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","Error looking for <link/>: " + e);
					}
				}
			} else {
				fetchData(url, contentType);
			}

			if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","< Scraper.fetching");
		}
        
		if (output.model) {
			if (doc && scraper && !scraping) { // if there are scrapers and we are not already scraping
				scraping = true; // indicate that we started scraping, so that we aren't called again
                
				var scrape = function(continuation, i) {
					if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","> scrape(" + i + ")");
					if (i < 1) {
						try {
							var code = PB_IOUtilities.getContent(scraper);
							if (code != "") {
								// if we got the scraping code and we were able to persist it
								Scraper.executeScraperSandboxed(
									browser,
									code,
									output,
									PB_Debug.print,
									function() { continuation(continuation, i+1); },
									scraper,
									url,
									title
								);
							} else { // there was some error retriving the scraping code
								if (code == "") {
									alert2("Error retrieving the scraper at " + scraper);
								}
							}
						} catch (e) {
							alert2("Failed to extract data using screen scraper " + scraper + "\nError: " + e);
						}
					} else 
						doneScraping = true;
					if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","< scrape(" + i + ")");
				};
				scrape(scrape, 0);
			}
		}

//		if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","DOM available: " + this._loaded);
//		if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","Fetching: " + fetching + "," + tofetch + "," + fetched + "," + successfullyFetched);
//		if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","Scraping: " + scraping + "," + doneScraping + ((scrapers) ? "," + scrapers.length : ""));

		var finished = (fetching && fetched == tofetch) && ((scraping && doneScraping) || (!scraping && !doc));

		if (!finished && counter > 0) { // if we are still loading and we haven't expired the timeout, 
			setTimeout(loader,delay); // try the loader again with a delay
			counter--;
		} else {
			if (finished && tofetch == successfullyFetched) {

			} else {
				// we finished with errors
				if (!finished) {
					// scraping took too long and we timed out
					alert2("Collecting the data took too long, stopping it."); // tell the user about it
				}
				// if not a timeout, avoid telling the user again since we told already/
			}
		}

		if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","< Scraper.loader");
	};
	loader(); // invoke the loader
	return output;
}

Scraper.executeScraperSandboxed = function(browser, scraper, output, logger, done, scraperURI, originURI, originTitle) {
	if (PB_Debug.enabled()) PB_Debug.trace("scrape.js","> sandbox");

	// first wrap the current browser window to make it secure
	var safeWindow = new XPCNativeWrapper(browser.contentWindow); 
	// note: this is an explicit & deep wrapper
    
	// then create a sandbox with that window as the security context
	var sandbox = new Components.utils.Sandbox(safeWindow);

	// then populate the sandbox
	sandbox.window = safeWindow;
	sandbox.document = sandbox.window.document;
	sandbox.uri = sandbox.document.location.href;

	// then supply objects that will be disposed at the end of this
	// function so that we don't care if they get modified
	sandbox.log = logger;
	sandbox.data = new PB_Data(); 
	sandbox.json = output.json;
	sandbox.outputParams = output.params;
	sandbox.utilities = new PB_ScrapingUtilities();
	sandbox.piggybank = new PB_PiggyBankInterface();
    
    // this is so that the scraper can access window.x where x is added by the web page
    sandbox.getWindowObject = function(name) { return safeWindow.wrappedJSObject[name]; };
    
	// finally, we need to set the safe window as the sandbox prototype    
	sandbox.__proto__ = safeWindow;
    
	// execute the javascript scraper in the sandbox
	Components.utils.evalInSandbox("(function(){\n" + scraper + "\n})()", sandbox);

	// this is the function that performs additional scraping if the scraper instructed so
	var scrape = function(done) {
    
		// now look to see if the scraper left any URLs to scrape
		var toScrape = sandbox.piggybank.toScrape;
        	if (PB_Debug.enabled()) PB_Debug.print("scrape.js", toScrape);
		// if so, execute the activities
		if (toScrape.length > 0) {
			var dialog = window;
    
			var init = function() {
				// load the list of URLs to subscrape that the scraper loaded
				var list = dialog.document.getElementById("url-list");
				for each (var s in toScrape) {
					var url = s[0];
					list.appendItem(url, url);
				}
    
				var subscrape = function(processed) {
					if (processed < toScrape.length && !dialog.closed) {
						var s = toScrape[processed];
						var url = s[0];
						var process = s[1];
						var exception = s[2];

						var onLoad = function() { // this is called when the iframe finished loading the URL to subscrape
							var item = list.getItemAtIndex(processed);

							var dom = dialog.document.getElementById("browser-subscrape").contentDocument;

							try {
								sandbox.piggybank.toScrape = []; // the subscraper might want to subcrape too, so clean up the array
								process(dom); // subscrape the loaded DOM
								if (sandbox.piggybank.toScrape.length > 0) { // if the subscraper wants to subscrape further
									for each (var s in sandbox.piggybank.toScrape) {
										toScrape.push(s);
										var url = s[0];
										list.appendItem(url, url);
									}
								}
							} catch (e) {
								if (exception) exception(e); // handle the exception
							}

							processed++; // at this point another URL was processed
                            
							// remove ourselves as the handler or we will get invoked again
							dialog.document.getElementById("browser-subscrape").removeEventListener("load", onLoad, true);

							// call ourselves with an increased counter
							subscrape(processed);
						}

						// this sets the function above as the loading handler
						dialog.document.getElementById("browser-subscrape").addEventListener("load", onLoad, true);

						// this will trigger the hidden browser to load the url
						dialog.document.getElementById("browser-subscrape").src = url;
					} else {
						done();
					}
				};

				// start the subscraping
				subscrape(0);
			};

			init();
		} else {
			done();
        	}
	};
    
	// this is the function that performs additional address lookup if the scraper instructed so
	// @@@ will have to do this via javascript if possible
	var lookup = function(done) {

		var toLookup = sandbox.piggybank.toLookup;
        
		if (toLookup.length > 0) {
			var init = function() {
				var url = PB_Extension.getPiggyBankURL() + "?command=addresses";
               
				var body = "";  
				for each (var lookup in toLookup) {
					var address = lookup[1];
					body += address + "\n";
				}

				var onDone = function(responseText, xmlhttp) {
					if (!dialog.closed) {
						var count = 0;
						var start = 0;
						while (start < responseText.length) {
							var stop = responseText.indexOf('\n', start);
							if (stop < 0) {
								stop = responseText.length;
							}

							var lookup = toLookup[count];
							var uri = lookup[0];
							var address = lookup[1];
							var lljson = responseText.substring(start, stop);
							var p = eval('(' + lljson + ')');

							if (p && p.Status && p.Status.code == 200 && p.Placemark) {
								var ll = p.Placemark[0].Point.coordinates[1] + "," + p.Placemark[0].Point.coordinates[0];
								var onSuccess = lookup[2];
								onSuccess(uri, address, ll, p);
							} else {
								var onFailure = lookup[3];
								if (onFailure) onFailure(address);
							}

							count++;
							start = stop + 1;
						}

						if (count != toLookup.length) {
							dialog.alert("ERROR: should have processed " + toLookup.length + " instead we processed " + count);
						}

						dialog.removeEventListener("load", init, false);
						setTimeout(function() { dialog.close() }, 100);
						done();
					}
				};

				PB_HTTPUtilities.doCustomPost(url, body, onStatus, onDone);
			};

			// when the dialog is ready, initialize it
			dialog.addEventListener("load", init, false);
		} else {
			done();
	        }
	};
    
	// this is the function that loads the data the scraper added to the 
	// temporary model to the piggy bank model.
	// NOTE: we do this instead of passing the model directly to the scraper
	// for security reasons and also because RIO cannot be executed by
	// sandboxed code directly because it triggers a security violation in 
	// the java sandbox.
	var load = function(done) {
		for each (var s in sandbox.data.statements) {
			output.model.addStatement(s[0],s[1],s[2],s[3]);
			if (scraperURI || originURI || originTitle) {
				scraperURI = scraperURI ? scraperURI : "";
				originURI = originURI ? originURI : "";
				originTitle = originTitle ? originTitle : "";
				output.model.addBacktracking(s[0], scraperURI, originURI, originTitle);
			}
		}
		for each (var s in sandbox.data.tags) {
			output.model.addTag(s[0],s[1]);
		}
		for each (var s in sandbox.data.rdfxml) {
			output.model.addRDFXML(s[0],s[1]);
		}
		for each (var s in sandbox.data.turtle) {
			output.model.addTurtle(s[0],s[1]);
		}
		done();
	}

	// call the above methods, chained
	// NOTE: order is important as loading should be performed at the very end or
	// data might not get transferred over
	scrape(function() {
		lookup(function() {
			load(done);
		})
	});
}
