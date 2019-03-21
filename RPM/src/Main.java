import data.Event;
import data.TransformationExample;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String path = new File("src\\logs\\runningExample.csv").getAbsolutePath();
        List<Event> events = logReader.readCSV(path);
        HashMap<String, List<Event>> cases = segmentator.segment(events);
        System.out.println("\n\n");
        var example = transformationDiscoverer.extractExamples(cases);
        for(TransformationExample te: example)
            System.out.println(te);
        transformationDiscoverer.discoverDataTransformations(example);
    }
}
