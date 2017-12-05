package crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashSet;

public class CrawlData implements Comparable<CrawlData>
{
    private String url;
    private int    occurrences;
    private HashSet<String> usedBy;
    private String name;

    public CrawlData(String url)
    {
        this.url = url;
        this.usedBy = new HashSet<>();
        this.name = url.substring(url.lastIndexOf("/") + 1).replaceAll("(-?\\d)*\\..+$", "");
    }

    public CrawlData(BufferedReader reader)
    {
        try{
            String line = "";
            while((line = reader.readLine()) != null)
            {
                if(line.startsWith("js-lib:"))
                    url = line.substring(8, line.lastIndexOf(":") - 2);
                else if(line.startsWith("\toccurences: ")) occurrences = Integer.parseInt(line.substring(line.indexOf(": ")));
                else if(line.startsWith("\tuser(s): "))
                {

                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updateUsages(String url, String user)
    {
        //I don't use equals ignore case because La will have a different hashCode than lA or la
        if(this.url.equals(url) && !this.usedBy.contains(user))
        {
            occurrences ++;
            usedBy.add(user);
        }
    }

    @Override
    public int compareTo(CrawlData o)
    {
        if(occurrences == o.occurrences) return 0;
        return occurrences >= o.occurrences ? -1 : 1;
    }

    public void export(BufferedWriter writer) throws Exception
    {
        writer.write("js-lib: " + name + " :\n{\t");
        writer.write("location: " + url + "\n\t");
        writer.write("occurrences: " + occurrences + "\n");
        for(String string : usedBy)
            writer.write("\tuser: " + string + "\n");
        writer.write("};");
    }

    public int getOccurrences() { return occurrences; }

    public String getURL()
    {
        return url;
    }

    @Override
    public int hashCode()
    {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return url.equals(obj.toString());
    }

    @Override
    public String toString()
    {
        return url;
    }

    //this is used for testing.
    public void incrementUsages()
    {
        occurrences ++;
    }

    public String getName()
    {
        return name;
    }
}