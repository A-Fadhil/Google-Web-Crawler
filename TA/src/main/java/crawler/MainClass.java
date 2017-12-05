package crawler;

import java.util.Scanner;

public class MainClass
{
    static final boolean ConsoleInput = true;

    public static void main(String...args) throws InterruptedException
    {
        //set to 0 for infinite crawl.
        int    crawlAmount= 20;
        //I learned something cool during this assignment, theres a website called potatoworld.eu!
        String searchTerm = "Potato";

        if(ConsoleInput && args != null && args.length > 1)
        {
            searchTerm = args[0];
            if(args[1].matches("\\d+"))
                crawlAmount = Integer.parseInt(args[1]);
            else
                throw new IllegalArgumentException("second parameter must be a number.");
        } else
        {
            Scanner scanner = new Scanner(System.in);

            System.out.println("please insert a search term: ");
            searchTerm = scanner.nextLine();
            System.out.println("please insert amount of cycles (0 for infinite cycles): ");

            String in = scanner.nextLine();

            if(in.matches("\\d+"))
                crawlAmount = Integer.parseInt(in);
            else
                throw new IllegalArgumentException("second parameter must be a number.");

            System.out.println("will attempt to crawl for '" + searchTerm + "' " + (crawlAmount == 0 ? "in 'infinity'." : (crawlAmount == 1 ? "in '" + crawlAmount + "' page." : crawlAmount + "' pages.")));
        }

        Crawler crawler = new Crawler(true);
        Collector collector = new Collector(crawler);
        new Thread(collector).start();

        crawl(collector, crawler, searchTerm, crawlAmount);
    }

    private static void crawl(Collector collector, Crawler crawler, String searchTerm, int crawlAmount)
    {
        String engine       = "google";
        String searchEngine = "https://www." + engine + ".com/";

        try {
            if(crawler.inHouseCrawl(engine, searchEngine + (engine.equalsIgnoreCase("yahoo") ? "search;?p=" : "/search?q="), searchTerm.replaceAll("\\s", "%20"), crawlAmount))
                collector.finish();
        } catch (Exception e)
        {
            collector.finish();
            e.printStackTrace();
        }
    }
}