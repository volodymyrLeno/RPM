import data.Event;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String log = args[0];
        String FoofahPath = args[1];
        List<String> readEvents = new ArrayList<>(Arrays.asList(args[2].split(",")));
        List<String> writeEvents = new ArrayList<>(Arrays.asList(args[3].split(",")));
        Boolean preprocess = Boolean.parseBoolean(args[4]);
        String approach = args[5];

        List<Event> events;
        HashMap<String, List<Event>> cases;

        if(preprocess){
            preprocess(log);
            events = logReader.readCSV(log.substring(0, log.lastIndexOf(".")) + "_filtered.csv");
            cases = segmentator.segment(events, "submitButton");
        }
        else{
            events = logReader.readCSV(log);
            cases = segmentator.groupByCases(events);
        }

        if(approach.equals("-1")){
            var totalStartTime = System.currentTimeMillis();
            var result = transformationDiscoverer.getFoofahTransformation2(FoofahPath, cases, "--timeout 3600", true);
            System.out.println(result);
            var totalStopTime = System.currentTimeMillis();
            System.out.println("\nTotal execution time: " + (totalStopTime - totalStartTime) / 1000.0 + " sec\n");
        }
        else if(approach.equals("-2")){
            var totalStartTime = System.currentTimeMillis();
            var examples = transformationDiscoverer.extractExamples(cases, readEvents, writeEvents);

            if(examples.size() == 0){
                System.out.println("No transformation examples found");
                return;
            }

            var groupedExamples = transformationDiscoverer.groupExamples(examples);
            for (String key : groupedExamples.keySet()) {
                var startTime = System.currentTimeMillis();
                transformationDiscoverer.discoverDataTransformations(FoofahPath, 1.0, groupedExamples.get(key));
                var stopTime = System.currentTimeMillis();
                System.out.println("\nExecution time: " + (stopTime - startTime) / 1000.0 + " sec\n");
            }
            var totalStopTime = System.currentTimeMillis();
            System.out.println("\nTotal execution time: " + (totalStopTime - totalStartTime) / 1000.0 + " sec\n");
        }
        else if(approach.equals("-3")){
            var examples = transformationDiscoverer.extractExamples(cases, readEvents, writeEvents);

            if(examples.size() == 0){
                System.out.println("No transformation examples found");
                return;
            }

            var totalStartTime = System.currentTimeMillis();

            var groupedExamples = transformationDiscoverer.groupExamples(examples);

            for (String key : groupedExamples.keySet()) {
                var startTime = System.currentTimeMillis();
                var patterns = tokenizer.clusterByPattern(groupedExamples.get(key));
                transformationDiscoverer.discoverTransformationsByPatterns(FoofahPath, patterns);
                var stopTime = System.currentTimeMillis();
                System.out.println("\nExecution time: " + (stopTime - startTime) / 1000.0 + " sec\n");
            }
            var totalStopTime = System.currentTimeMillis();
            System.out.println("\nTotal execution time: " + (totalStopTime - totalStartTime) / 1000.0 + " sec\n");
        }
        else{
            System.out.println("Wrong input parameter! Select one from the given options:\n\n\"-1\" - baseline approach\n\"-2\" - grouping by target" +
                    "\n\"-3\" - grouping by target and input structure");
        }
    }

    public static void preprocess(String filePath){
        List<Event> events = logReader.readCSV(filePath);

        Collections.sort(events, Comparator.comparing(ev -> ev.getTimestamp()));

        System.out.println("Applying segmentation...");
        HashMap<String, List<Event>> cases = segmentator.segment(events, "submitButton");

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
            Process process = Runtime.getRuntime().exec("java -jar RPM/src/RPA_SemFilter.jar " + path);
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
