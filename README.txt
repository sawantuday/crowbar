

   
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
  
  For this reason, it needs to work inside an environment that is as close
  as possible to a browser. Luckily, Mozilla provides a tool called
  XULRunner[1] that provides us with a firefox-like execution environment.
  
  [1] http://developer.mozilla.org/en/docs/XULRunner

  You need to have XULRunner (version 1.8.0.4 or higher) in order for Crowbar
  to work on your system.
  
  
  
  Design
  ------
  
  Crowbar is implemented as a XULRunner application that provides an HTTP
  RESTful web service (basically turns a web browser into a web server!)
  that you can use to 'remotely control' the browser. 
  
  
  How to use on MacOSX
  --------------------
  
  After you have installed XULRunner, do
  
   /Library/Frameworks/XUL.framework/xulrunner-bin --install-app $CROWBAR/xulapp
  
  where $CROWBAR is the location of the root of the crowbar source checkout
  (basically, where this very file you're reading is!). The above command
  will install Crowbar in your Application folder.
  
  At that point, you can execute crowbar by double clicking on the icon. The
  crowbar window will show up.
  
  Then point your browser (not Crowbar, any another one!) to
  
    http://127.0.0.1:10000
  
  this is the port where Crowbar's internal web server is listening to. 
  
  This will serve you a page where you can type the address of the location
  that you want Crowbar to fetch, parse, execute and serialize back to you.
  
  
  
  There are tons of scraping solutions, why another one?
  ------------------------------------------------------
  
  Most scraping solutions work on the so-called 'syntax space' and for that
  reason they have to cope with all the ways the same data can be serialized.
  
  We prefer to work on the 'model space', which means getting closer to the 
  'infoset' that contains the data that we want to scrape/extract/convert. 
  As HTML in the real world is very complicated and varied, we would like 
  to reuse as much as the syntax -> model software that is already out there,
  which in our case means the browser.
  
  Solvent and Piggy Bank show how much easier, natural and solid it is 
  to write scrapers against a DOM rather than against an array of characters
  (which is, the serialized DOM representation in HTML). Crowbar wants to 
  be able to automate that process, reusing the exact same scrapers that
  work for Piggy Bank and are created in Solvent.
  
  Another added benefit of working with a full browsing environment as your
  crawler's agent is that we can access the DOM *after* the onload javascript
  hooks were executed, which means that we can scrape content that was 
  not even in the HTML page served by the web server originally and that was
  client-side included via AJAX at post page load time.
                        
  

  Licensing and legal issues
  --------------------------

  Crowbar is open source software and are licensed under the BSD license
  located in the LICENSE.txt file located in the same directory as this very file
  you are reading.





  Credits
  -------

  This software was created by the SIMILE project and in particular:

   - Stefano Mazzocchi  <stefanom at mit.edu> (original author)




                                --- o ---


  Thanks for your interest.




                                                        The SIMILE Project
                                                      http://simile.mit.edu/

