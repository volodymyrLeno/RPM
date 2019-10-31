import data.Event;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        //List<String> files = new ArrayList<>();

        List<String> files = new ArrayList<>(Arrays.asList(args[0].split(",")));
        //files.add("/home/vleno/Desktop/RPM/RPM/src/logs/experiments/d61.csv");
        //files.add("/home/vleno/Desktop/RPM/RPM/src/logs/experiments/d5.csv");
        //files.add("/home/vleno/Desktop/RPM/RPM/src/logs/experiments/d4.csv");
        //files.add("/home/vleno/Desktop/RPM/RPM/src/logs/experiments/d3.csv");
        //files.add("/home/vleno/Desktop/RPM/RPM/src/logs/experiments/d2.csv");
        //files.add("/home/vleno/Desktop/RPM/RPM/src/logs/experiments/d1.csv");

        //String files = args[0];

        List<String> readEvents = new ArrayList<>(Arrays.asList(args[1].split(",")));
        List<String> writeEvents = new ArrayList<>(Arrays.asList(args[2].split(",")));
        Boolean preprocess = Boolean.parseBoolean(args[3]);

        List<Event> events;
        HashMap<String, List<Event>> cases;

        /*
        if(preprocess){
            preprocess(path);
            events = logReader.readCSV(path.substring(0,path.lastIndexOf(".")) + "_filtered.csv");
        }
        else
            events = logReader.readCSV(path);

         */

        for(String file: files) {
            events = logReader.readCSV(file);
            cases = segmentator.groupByCases(events);

            // System.out.println(transformationDiscoverer.getFoofahTransformation("RPM/src/foofah-master/foofah.py", examples, "--timeout 60000", true));

            // Approach 1: Map all inputs to all outputs

            /*
            var totalStartTime = System.currentTimeMillis();
            var result = transformationDiscoverer.getFoofahTransformation2("RPM/src/foofah-master/foofah.py", cases, "--timeout 3600", true);
            System.out.println(result);
            var totalStopTime = System.currentTimeMillis();
            System.out.println("\nTotal execution time: " + (totalStopTime - totalStartTime) / 1000.0 + " sec\n");

             */

            //Approach 2: Correlate inputs and outputs, and find a mapping

            var totalStartTime = System.currentTimeMillis();
            var examples = transformationDiscoverer.extractExamples(cases, readEvents, writeEvents);
            var groupedExamples = transformationDiscoverer.groupExamples(examples);
            for (String key : groupedExamples.keySet()) {
                var startTime = System.currentTimeMillis();
                transformationDiscoverer.discoverDataTransformations(1.0, groupedExamples.get(key));
                var stopTime = System.currentTimeMillis();
                System.out.println("\nExecution time: " + (stopTime - startTime) / 1000.0 + " sec\n");
            }
            var totalStopTime = System.currentTimeMillis();
            System.out.println("\nTotal execution time: " + (totalStopTime - totalStartTime) / 1000.0 + " sec\n");


            // Approach 3: Correlate inputs and outputs, group inputs by patterns and find a mapping

            /*
            var examples = transformationDiscoverer.extractExamples(cases, readEvents, writeEvents);
            var totalStartTime = System.currentTimeMillis();

            var groupedExamples = transformationDiscoverer.groupExamples(examples);

            for (String key : groupedExamples.keySet()) {
                var startTime = System.currentTimeMillis();
                var patterns = tokenizer.clusterByPattern(groupedExamples.get(key));
                transformationDiscoverer.discoverTransformationsByPatterns(patterns);
                var stopTime = System.currentTimeMillis();
                System.out.println("\nExecution time: " + (stopTime - startTime) / 1000.0 + " sec\n");
            }
            var totalStopTime = System.currentTimeMillis();
            System.out.println("\nTotal execution time: " + (totalStopTime - totalStartTime) / 1000.0 + " sec\n");

             */
        }
    }

    public static void preprocess(String filePath){
        List<Event> events = logReader.readCSV(filePath);

        Collections.sort(events, Comparator.comparing(ev -> ev.getTimestamp()));

        System.out.println("Applying segmentation...");
        HashMap<String, List<Event>> cases = segmentator.segment(events);

        for(String caseID: cases.keySet())
            for(Event event: cases.get(caseID)){
                event.setCaseID(caseID);
            }

        writeDataLineByLine(filePath, cases);
        System.out.println("Log is segmented");

        runSimplifier(filePath);
        System.out.println("Log is simplified");
    }

    public static void writeDataLineByLine(String filePath, HashMap<String, List<Event>> cases) {
        String[] header = {"caseID", "timeStamp", "userID", "targetApp", "eventType", "url", "content", "target.workbookName",
                "target.sheetName", "target.id", "target.class", "target.tagName", "target.type", "target.name",
                "target.value", "target.innerText", "target.checked", "target.href", "target.option", "target.title", "target.innerHTML"
        };

        try{
            FileWriter csvWriter = new FileWriter(filePath);
            String headerLine = "";
            for(int i = 0; i < header.length; i++){
                headerLine += "\"" + header[i] + "\",";
            }
            csvWriter.append(headerLine.substring(0, headerLine.lastIndexOf(",")) + "\n");
            for(String caseID: cases.keySet()){
                for(Event event: cases.get(caseID)){
                    var line = "\"" + caseID + "\",\"" + event.getTimestamp() + "\",";
                    line += event.payload.containsKey("userID") ? "\"" + event.payload.get("userID") + "\"," : "\"\",";
                    line += event.payload.containsKey("targetApp") ? "\"" + event.payload.get("targetApp") + "\"," : "\"\",";
                    line += "\"" + event.eventType + "\",";
                    for(int i = 5; i < header.length; i++)
                        if(event.payload.containsKey(header[i]) && !event.payload.get(header[i]).equals("\"\""))
                            line += "\"" + event.payload.get(header[i]) + "\",";
                        else
                            line += "\"\",";

                    csvWriter.append(line.substring(0, line.lastIndexOf(",")) + "\n");
                }
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runSimplifier(String path){
        try{
            System.out.println("Running simplifier ...");
            Process process = Runtime.getRuntime().exec("java -jar /home/vleno/Desktop/RPM/RPM/src/RPA_SemFilter.jar " + path);
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((reader.readLine()) != null) {}
            process.waitFor();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
