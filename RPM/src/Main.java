import data.Event;
import data.TransformationExample;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String path = new File("RPM/src/logs/runningExample.csv").getAbsolutePath();
        List<Event> events = logReader.readCSV(path);
        HashMap<String, List<Event>> cases = segmentator.segment(events);
        System.out.println("\n\n");
        var example = transformationDiscoverer.extractExamples(cases);

        //TransformationExample te = example.get(3);

        //for(int i = 0; i < te.getInputExamples().size(); i++)
        //    System.out.println(te.getInputExamples().get(i) + " => " + te.getOutputExamples().get(i));

        //for(String address: example.get(11).getOutputExamples())
        //    System.out.println("("+address+")");
        for(TransformationExample te: example)
            System.out.println(te);
        transformationDiscoverer.discoverDataTransformations(example);
    }
}
