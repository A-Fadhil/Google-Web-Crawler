package crawler;

import java.util.Comparator;

public class CrawlDataComparator implements Comparator<CrawlData>
{
    @Override
    public int compare(CrawlData o1, CrawlData o2)
    {
        return o1.compareTo(o2);
    }
}
