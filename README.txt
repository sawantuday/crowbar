

                      +-------------------------------------+
                      |            W A R N I N G            |
                      |                                     |
                      | This project is in very alpha stage |
                      | and might not work at all.          |
                      |                                     |
                      | You have been warned.               |
                      +-------------------------------------+




   
                                   C R O W B A R 




  What is this?
  -------------

  Crowbar aims to be the command-line companion of Solvent, meaning that 
  you write a scraper for your HTML pages in your browser and then
  you can apply the scraper to a particular URL from the command line with
  crowbar. Basically, you should be able to do something like
  
    crowbar -in URL -scraper scraper.js -out results.rdf

  and have the page at URL be scraped into RDF content in the results.rdf
  file.
  
  One can imagine to then plug crowbar into a web crawler such as 
  Apache Nutch with a regexp->scraper.js map and turn it into 
  an RDF harvester.
  
  
  
  Requirements
  ------------
  
  Crowbar wants to be as similar as possible to Solvent, to avoid having to
  tweak and tune the scrapers due to implementation differences between
  parsers and DOM/XPath implementations.
  
  For this reason, it needs JavaXPCOM to function. The best way to have this
  is to install XULRunner from
  
    http://developer.mozilla.org/en/docs/XULRunner
    
  NOTE: on macosx, you need version 1.8.0.4 or later, which hasn't been 
  released yet so you need to get it from a nightly build at
  
    http://ftp.mozilla.org/pub/mozilla.org/xulrunner/nightly/latest-mozilla1.8.0/

  
  
  Why such a complicated design?
  ------------------------------
  
  Writing scrapers in javascript is natural and easy, also given the power of xpath
  queries against a DOM. The hardest part of scraping is getting out of the syntax
  space (which is what you do if you use regexp and perl, for example) and into
  the model space (which is what Solvent and Piggy Bank do, leaving the browser
  the problem of parsing the HTML, creating the DOM and then executing the 
  scripting code embedded in the page, which is increasingly important for 
  content that is AJAX-injected as client-side include and it's not even
  contained in the page that the browser loads!
  
  Crowbar tries to emulate the full-blown mozilla-based browsing experience but
  windowless, embedding and connecting directly to the Gecko and XPCOM subsystems
  thru the JavaXPCOM interface.
  
                      
  

  Licensing and legal issues
  --------------------------

  Longwell is open source software and are licensed under the BSD license
  located in the LICENSE.txt file located in the same directory as this very file
  you are reading.





  Credits
  -------

  This software was created by the SIMILE project and originally written
  by the SIMILE development team (in alphabetical order):

   - Stefano Mazzocchi    <stefanom at mit.edu>




                                --- o ---


  Thanks for your interest.




                                                        The SIMILE Project
                                                      http://simile.mit.edu/

