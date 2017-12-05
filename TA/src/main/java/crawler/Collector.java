package crawler;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Collector implements Runnable
{
    private volatile Crawler crawler;
    private volatile HashSet<String> urls;
    private final    File output;
    private HashMap<String, CrawlData> data;

    //to prevent new operator during runtime
    //I'll create an arraylist and reuse it during
    //updates and sorting.
    private ArrayList<CrawlData> reusableArray;

    public Collector(Crawler crawler)
    {
        this.crawler = crawler;
        this.urls = new HashSet<>();
        this.output = new File(".\\results.rtf");
        this.data = new HashMap<>();
        this.reusableArray = new ArrayList<>();
    }

    public volatile boolean run;

    public synchronized void finish()
    {
        run = false;

//        int lastSize = urls.size();
//        crawler.grab(urls);
//
//        if(urls.size() > lastSize)
//        {
//            readAllData();
//            exportAllData();
//        }
    }

    @Override //<------
    public void run()
    {
        run = true;

        while (run)
        {
            if(crawler.safeToGrab())
            {
                int lastSize = urls.size();
                urls.addAll(crawler.grab());
                crawler.clear();

                if (urls.size() > lastSize)
                {
                    readAllData();
                    exportAllData();
                }

                System.out.println("new data found: " + (urls.size() - lastSize));
            }

            System.out.println("attempting to grab data.");

            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        urls.addAll(crawler.grabAll());

        readAllData();
        exportAllData();

        System.out.println("crawl finished with '" + data.size() + "' results.");
    }

    private synchronized void readAllData()
    {
        //I wont end the program if the program fails to read previous results so I will instead catch the exception and move on.
        //there could definitely be a lot of improvements with all of the error handling code, but with 3 hours, I will only work
        //on the main part of the assignment.

        //I was going to read previous data into memory then dump it back, however in a large scale application this would be inefficient due to size of memory.
//        if (output.exists())
//        {
//            try
//            {
//                BufferedReader reader = new BufferedReader(new FileReader(output));
//
//                String line = "";
//                String text = "";
//                while ((line = reader.readLine()) != null) text += line;
//
//                if (text.length() > 0)
//                {
//                    text = text.substring(text.indexOf("js-lib"));
//
//                    //System.out.println(text);
//                    System.exit(0);
//                }
//
//                reader.close();
//            } catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }

        data.clear();

        for (String url : urls)
        {
            //I clean up any urls in case the url looks like this https://google.com/sdfsdf/sdf/sd/f/sdf it will turn into this google.com
            String originalSite = url.replaceAll("(http)(s)?://", "");
            originalSite = originalSite.replaceAll("/.+", "");//.substring(0, io);

            try
            {
                Document document = Jsoup.connect(url).userAgent("Mozilla").timeout(5000).get();

                Elements jsElements = document.select("script[type=text/javascript]");

                if (jsElements != null)
                    for (Element element : jsElements)
                    {
                        String text = element.toString();

                        if (text.contains("src="))
                        {
                            String jsURL = element.attr("src");
                            if (!jsURL.isEmpty())
                            {
                                if (jsURL.endsWith("/")) jsURL = jsURL.substring(0, jsURL.length() - 1);
                                if (jsURL.startsWith("//")) jsURL = jsURL.substring(2);
                                else if (jsURL.startsWith("/")) jsURL = originalSite + jsURL;

                                CrawlData d = new CrawlData(jsURL);
                                d.updateUsages(jsURL, url);

                                if (data.containsKey(jsURL))
                                {
                                    data.get(jsURL).updateUsages(jsURL, url);
                                } else data.put(jsURL, d);

//                                System.out.println(jsURL);
                            }
                        }
                    }
            } catch (SocketTimeoutException e)
            {
            } catch (HttpStatusException e)
            {
            } catch (IOException e)
            {
                e.printStackTrace();
                run = false;
            }
        }
    }

    private synchronized void exportAllData()
    {
        try
        {
            //this code is used for testing
//            for(CrawlData data : data)
//            {
//                if(Math.random() >= 0.9)
//                {
//                    data.incrementUsages();
//                    break;
//                }
//            }



            reusableArray.addAll(data.values());
            reusableArray.sort(new CrawlDataComparator());

            if(reusableArray.isEmpty()) return;

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write("total: " + data.size() + "\n");
            writer.write("Top 5 used libraries: \n");
            writer.write("\n");
            int loop = Math.min(data.size(), 5);

            for(int i = 0; i < loop; i ++) writer.write("#" + i + ": " + reusableArray.get(i).getName() + " " + reusableArray.get(i).getURL() + "\n");

            writer.write("\n\n\n");

            for(CrawlData d : reusableArray)
            {
                d.export(writer);
                writer.write("\n\n\n");
            }

            //System.out.println(reusableArray.size());

            writer.flush();
            writer.close();
            reusableArray.clear();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}