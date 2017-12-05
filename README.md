# Google-Web-Crawler

This is a project made for an assignment, The project took 2 hours to create, however debugging it became hard after google blocked the crawler for a few hours.
Google asks for a captcha solve after around 10k requests.

The project works with bing too, when using bing, make sure to use "simplecrawl" method in "Crawler" class.
The engine name can be changed to bing in the crawl method inside the MainClass.

Libraries used: JSoup (MIT) license.
Reason: simple Json parser and more flexibility.

I wanted to use the Google Custom Search API however it does not fit with the requirements of the assignment so I refrained from using it.

There are many optimizations to be done, with the threading, the error handling, the algorithm in which search results are found.. etc.

Google crawling is harder (around 80 results per 30 cycles)
whereas with bing, there are around 112 results per cycle, however some of the results are unclean, they are not related to the search query.

All in all there's so much one can do in so little time.


///////////////////////////////////////////////////////////////////////////////////


The crawler will take two inputs, the first being the search query and the second being a number of cycles, if left at 0, the cycles will
continue on as much as the search engine can handle, it will then take the results, parse the html of the websites found and retrieve the js
libraries used in those pages, then it will list them in descending order and point out the top 5 most used libraries, which websites used them,
name of the library, number of occurences and link to the library.


///////////////////////////////////////////////////////////////////////////////////


Optimisation

One cannot optimise enough, however this is heavily un-optimised, so use at your own risk!
the Collector class needs to seperate into a JavaScript collector, and a thread that manages everything in unison.

The correct heirarchy would be something similair to the following:
                                          Main Thread
       Google Crawler  *  JavaScript Collector  *  (File Manager, Collections Manager and CrawlData exporter/importer)
       
       Where the google crawler will find websites and search results, the JavaScript collector does a similair job to the google crawler
       by finding javascript libraries inside of the html document of said search results, and the final class would manage the results file
       exporting any new found data, reading it back in small little chunks then listing them in descending order, and manage all the collections.
       
One can use a simple HttpConnection to read the page's html content, and use a Matcher to find search results, and javascript libraries,
however, with jsoup the code is a little cleaner and more structured.

I used a HashMap in Collector because I wanted a fast way to access data (by url) and not have duplicates at the same time.


///////////////////////////////////////////////////////////////////////////////////

![Screen Shot](https://github.com/A-Fadhil/Google-Web-Crawler/blob/master/crawler%20sst.PNG?raw=true "Screen Shot")

Please look at the results.rtf file to see basic output.
