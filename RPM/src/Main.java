import data.Event;
import data.TransformationExample;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String path = new File("src\\logs\\test1.csv").getAbsolutePath();
        List<Event> events = logReader.readCSV(path);
        HashMap<String, List<Event>> cases = segmentator.segment(events);
        System.out.println("\n\n");
        var example = transformationDiscoverer.extractExamples(cases);
        transformationDiscoverer.discoverDataTransformations(example);
    }
}
