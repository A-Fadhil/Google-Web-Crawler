package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler
{
    //to remove any duplicate URL's, I use a HashSet
    //however without proper trimming of a url duplicates can arise from such instances
    //website.com/
    //website.com
    //it's the same website but the HashSet doesn't know that.


    //because of time limit, this is the way i'm doing
    //duplicate checks, it's definitely problematic
    //however given more time, one could use regex patterns, and a simple parser to check for duplicates.
    private volatile Set<String> urls = Collections.synchronizedSet(new HashSet<String>());
    private volatile Set<String> allURLs = Collections.synchronizedSet(new HashSet<String>());

    final boolean useRegex;
    private boolean safeToGrab;

    public Crawler()
    {
        this(false);
    }

    public Crawler(boolean useRegex)
    {
        this.useRegex = useRegex;
    }

    public boolean inHouseCrawl(String blackListed, String string, String search, int amt)
    {
        //There's no point doing a while loop because even if google had enough results, Java lists use integers for indexing so adding more values than the maximum value of integer is pointless.
        if(amt == 0) amt = Integer.MAX_VALUE;

        try{
            for(int i = 0; i < amt; i ++)
            {
                /** at first I used the second method
                 * however I decided to switch to a different functionality but keep the
                 * original methods to showcase the different approaches for solving this problem.
                 */
//                if(useRegex)
                /** only use this with google **/
                    parseAndCollectElementsWithRegex(string, search + "&start=" + (i * 6));
//                else parseAndCollectElementsWithoutRegex(string, search + "&start=" + (i * 6)); // this was the initial function for crawling, I decided to use regex instead.

                /** use this with bing **/
//                simpleCrawl(blackListed, string, search, i);

                safeToGrab = true;
                Thread.sleep(1500);
                safeToGrab = false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;
    }

    //This is a simpler crawler, but less efficient, it will definitely add some unrelated links sometimes.
    //I created it because I started doing this project with bing instead of google, because google blocked
    //the crawler because it was detected as a bot, if I was allowed to use more libraries I would've used Google Custom Search API, however it's not the most lightweight API
    //therefore against the requirements of this assignment.
    @Deprecated
    private void simpleCrawl(String blackListed, String string, String search, int i) throws Exception
    {
        Document document = Jsoup.connect(string + search + "&first=" + (i * 6)).userAgent("Mozilla/5.0").timeout(5000).get();
//                String html = document.toString();

//                //System.out.println(html);

        Elements elements = document.select("a[href]");

        for(Element element : elements)
        {
            String url = (element.attr("href"));
            if(!(element.toString().contains("." + blackListed) && element.toString().contains(blackListed + ".")))
            {
                //this checks for a valid url and submits it.
                if(url.matches("(http)(s)?://.+"))
                {
                    //System.out.println(url);
                    //to make sure that there are no duplicates ex: google.com and google.com/
                    //we do this extra work after we check this is a valid url to make sure not to waste any precious CPU cycles.
                    if(url.endsWith("/")) url = url.substring(0, url.length() - 1);
                    if(!allURLs.contains(url) && !urls.contains(url)) urls.add(url);
                }
            }
        }
    }

    @Deprecated
    private void parseAndCollectElementsWithoutRegex(String string, String query) throws IOException
    {
        try
        {
            Document document = Jsoup.connect(string + query).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(5000).get();

            Elements results = document.getElementsByClass("r");

            for (Element element : results)
            {
                String url = element.select("a").toString();
                if (url.startsWith("<a href=\"/url?q="))
                {
                    String first_trim = url.substring(16);
                    String result = first_trim.substring(0, first_trim.indexOf("\""));

                    if (!allURLs.contains(result)) urls.add(result);
                }
            }
        } catch (SocketTimeoutException e) //we capture this exception and do nothing, timeouts happen! better than to stop the operation for an unresponsive page.
        {
        }
    }

    private void parseAndCollectElementsWithRegex(String string, String query) throws IOException
    {
        try
        {
            Document document = Jsoup.connect(string + query).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").timeout(5000).get();

            Elements results = document.getElementsByClass("r");

            for (Element element : results)
            {
                String url = element.select("a[href]").attr("href").replace("/url?q=", "").replaceAll("\"", "");//(element.attr("a[href]"));
                //this checks for a valid url and submits it.
                if (url.matches("(http)(s)?://.+"))
//                String url = element.select("a").toString();
                //            Matcher matcher = Pattern.compile("<a href=\"/url\\?q=.+\">").matcher(url); no need for this code.
//                Matcher matcher = Pattern.compile("(http)(s)?://.+\"").matcher(url); //this checks for a valid url and submits it.
//                if(matcher.find())
                {
                    //                matcher = Pattern.compile("(http)(s)?://.+\"").matcher(matcher.group()); // this was my previous approad with line 143
                    //                matcher.find();
//                    String result   = matcher.group();
//                    result          = result.substring(0, result.length() - 1);

                    //System.out.println(url);
                    //to make sure that there are no duplicates ex: google.com and google.com/
                    //we do this extra work after we check this is a valid url to make sure not to waste any precious CPU cycles.
                    if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
                    if (!allURLs.contains(url) && !urls.contains(url)) urls.add(url);
                }
            }
        } catch (SocketTimeoutException e) //we capture this exception and do nothing, timeouts happen! better than to stop the operation for an unresponsive page.
        {
        }
    }

    //this function grabs any new websites found.
    public synchronized Set<String> grab()
    {
        allURLs.addAll(urls);
        return urls;
    }

    public synchronized void clear()
    {
        urls.clear();
    }

    public synchronized boolean safeToGrab()
    {
        return safeToGrab;
    }

    public synchronized Set<String> grabAll()
    {
        return allURLs;
    }
}