import data.Event;
import data.TransformationExample;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String path = args[0];
        List<String> readEvents = new ArrayList<>(Arrays.asList(args[1].split(",")));
        List<String> writeEvents = new ArrayList<>(Arrays.asList(args[2].split(",")));

        List<Event> events = logReader.readCSV(path);
        HashMap<String, List<Event>> cases = segmentator.segment(events);

        for(String caseID: cases.keySet())
            for(Event event: cases.get(caseID)){
                event.setCaseID(caseID);
            }

        var examples = transformationDiscoverer.extractExamples(cases, readEvents, writeEvents);

       // System.out.println(transformationDiscoverer.getFoofahTransformation("RPM/src/foofah-master/foofah.py", examples, "--timeout 60000", true));

        // Approach 1: Map all inputs to all outputs

        /*
        var groupedExamples = transformationDiscoverer.groupByCase(examples);
        HashMap<String, List<TransformationExample>> data = new HashMap<>();
        data.put("1", groupedExamples.get("1"));
        long startTime = System.currentTimeMillis();
        var result = transformationDiscoverer.getFoofahTransformation2("RPM/src/foofah-master/foofah.py", data, "--timeout 60000", true);
        long stopTime = System.currentTimeMillis();
        System.out.println(result);
        System.out.println("\nExecution time: " + (stopTime - startTime) / 1000.0 + " sec\n");
         */

        //System.out.println(transformationDiscoverer.getFoofahTransformation("RPM/src/foofah-master/foofah.py", examples, "--timeout 60000", true));

         //Approach 2: Correlate inputs and outputs, and find a mapping

        /*
        var groupedExamples = transformationDiscoverer.groupExamples(examples);
        var startTime = System.currentTimeMillis();
        for(String key: groupedExamples.keySet())
            transformationDiscoverer.discoverDataTransformations(0.125, groupedExamples.get(key));
        var stopTime = System.currentTimeMillis();
        System.out.println("\nExecution time: " + (stopTime - startTime) / 1000.0 + " sec\n");
         */

        // Approach 3: Correlate inputs and outputs, group inputs by patterns and find a mapping

        var groupedExamples = transformationDiscoverer.groupExamples(examples);
        var startTime = System.currentTimeMillis();
        for(String key: groupedExamples.keySet()){
            var patterns = tokenizer.clusterByPattern(groupedExamples.get(key));
            transformationDiscoverer.discoverTransformationsByPatterns(patterns);
        }
        var stopTime = System.currentTimeMillis();
        System.out.println("\nExecution time: " + (stopTime - startTime) / 1000.0 + " sec\n");
    }
}
