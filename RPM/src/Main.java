import data.Event;

import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Event> events = logReader.readCSV("C:\\Volodymyr\\RPAMiner\\append_http\\logs.csv");
        for(Event ev: events)
            System.out.println(ev);
        HashMap<String, List<Event>> cases = segmentator.segment(events);
        for(String id: cases.keySet())
            System.out.println(cases.get(id));
        //System.out.println(cases);
    }
}
