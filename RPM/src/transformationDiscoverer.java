import data.Event;
import data.TransformationExample;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;

public class transformationDiscoverer {

    static StringBuilder sb;

    /* READY FOR USAGE */

    private static String createFile(String foofahPath, StringBuilder sb) throws IOException{
        String tempFile = foofahPath + "foofahTEMP.txt";
        File file = new File(tempFile);
        if (!file.exists())
            if (!file.createNewFile())
                new IOException("Error occured when creating "+file.getAbsolutePath());
        FileWriter fw = new FileWriter(file);
        fw.write(sb.toString());
        fw.flush();
        fw.close();
        return file.getAbsolutePath();
    }

    public static String execPython(String foofahPath, String parameters, Boolean preprocessing) {

        foofahPath = foofahPath + "foofah.py";

        String output = null;
        try {
            String s = null;
            sb = new StringBuilder(100000);

            Process p = Runtime.getRuntime().exec("python " + foofahPath + " " + parameters);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null) {
                sb.append(s + "\n");
            }
            if (sb.toString().contains("*** Solution Not Found ***") || sb.toString().length() == 0)
                return null;
            else {
                output = sb.substring(sb.indexOf("#\n" +
                        "# Data Transformation\n" + "#") + 26);
                if(output.equals("\n"))
                    output = "Equality";
            }
            boolean error=false;
            while ((s = stdError.readLine()) != null) {
                error=true;
                sb.append(s+"\n");
            }
            if(error){
                new Exception("Error Foofah: "+sb.toString());
            }

            return output;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<TransformationExample> extractExamples(HashMap<String, List<Event>> cases, List<String> readActions, List<String> writeActions){
        List<TransformationExample> transformationExamples = new ArrayList<>();
        for(String caseID: cases.keySet()) {
            List<Event> events = new ArrayList<>(cases.get(caseID));
            List<String> targets = new ArrayList<>();
            for (int i = events.size() - 1; i >= 0; i--) {
                if(writeActions.contains(events.get(i).eventType) && !targets.contains(events.get(i).payload.get("target.name"))){
                    String target = events.get(i).payload.containsKey("target.id") ? events.get(i).payload.get("target.id") :
                            events.get(i).payload.get("target.name");
                    String output = events.get(i).payload.get("target.value").replaceAll("\\P{Print}", " ");;
                    String source = "";
                    List<String> input = new ArrayList<>();
                    targets.add(target);
                    for (int j = 0; j < i; j++)
                        if ((events.get(j).eventType.equals("paste") || events.get(j).eventType.equals("pasteIntoCell") ||
                                events.get(j).eventType.equals("pasteIntoRange")) &&
                                ((events.get(j).payload.containsKey("target.name") && events.get(j).payload.get("target.name").equals(target)) ||
                                (events.get(j).payload.containsKey("target.id") && events.get(j).payload.get("target.id").equals(target)))) {
                            for (int k = j; k >= 0; k--)
                                if(readActions.contains(events.get(k).eventType)){
                                    if(source.equals("")){
                                        if(events.get(k).payload.containsKey("target.id"))
                                            source = events.get(k).payload.get("target.id");
                                        else
                                            source = events.get(k).payload.get("target.name");
                                    }
                                    else{
                                        if(events.get(k).payload.containsKey("target.id"))
                                            source = source + "," + events.get(k).payload.get("target.id");
                                        else
                                            source = source + "," + events.get(k).payload.get("target.name");
                                    }
                                    input.add(events.get(k).payload.get("target.value").replaceAll("\\P{Print}", " "));
                                    break;
                                }
                        }
                    if(input.size() > 0){
                        if(input.size() > 1)
                            transformationExamples.add(new TransformationExample(caseID, source, target, input, Collections.singletonList(output)));
                        else
                            transformationExamples.add(new TransformationExample(caseID, source, target, input.get(0), output));
                    }
                }
            }
        }
        return transformationExamples;
    }

    public static void discoverDataTransformations(String foofahPath, Double frac, List<TransformationExample> transformationExamples){

        List<TransformationExample> seed = getSeed(frac, transformationExamples);
        List<TransformationExample> head = head((int) Math.ceil(frac * transformationExamples.size()), transformationExamples);

        discoverCorrelation(transformationExamples);

        Boolean preprocessing = false;

        System.out.println("\n" + getFoofahTransformation(foofahPath, head, "--timeout 3600", preprocessing) + "\n");

        /*
        if(checkForTransformation(seed.get(0).getInputExample(), seed.get(0).getOutputExample()))
            System.out.println("\n" + getFoofahTransformation("RPM/src/foofah-master/foofah.py", seed, "--timeout 600", preprocessing) + "\n");
        else
            System.out.println("\n No data transformation discovered! \n");
         */
    }

    public static void discoverTransformationsByPatterns(String foofahPath, HashMap<String, List<TransformationExample>> patterns){
        HashMap<String, List<String>> groupedPatterns = new HashMap<>();
        discoverCorrelation(patterns.get(patterns.keySet().toArray()[0]));

        for(String pattern: patterns.keySet()){
            var transformation = getFoofahTransformation(foofahPath, getSeed(1.0/patterns.get(pattern).size(), patterns.get(pattern)), "--timeout 3600", false);
            if(!groupedPatterns.containsKey(transformation))
                groupedPatterns.put(transformation, Collections.singletonList(pattern));
            else
                groupedPatterns.put(transformation, Stream.concat(groupedPatterns.get(transformation).stream(), Collections.singletonList(pattern).stream()).collect(Collectors.toList()));
        }
        groupedPatterns = groupedPatterns.entrySet().stream().sorted(comparingInt(e -> e.getValue().size())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {throw new AssertionError();}, LinkedHashMap::new));

        if(groupedPatterns.size() == 1)
            System.out.println("\n" + groupedPatterns.keySet().toArray()[0] + "\n");
        else{
            int i = 0;
            for(String transformation: groupedPatterns.keySet()){
                if(i == (groupedPatterns.size()-1) && groupedPatterns.get(transformation).size() > 1){
                    System.out.println("\nOtherwise: \n");
                    System.out.println(transformation + "\n");
                }
                else {
                    if(groupedPatterns.get(transformation).size() == 1){
                        System.out.println("\nFor pattern \"" + groupedPatterns.get(transformation).toArray()[0] + "\":\n\n" + transformation);
                    }
                    else{
                        System.out.print("\nFor patterns ");
                        for(String pattern: groupedPatterns.get(transformation))
                            System.out.print("\"" + pattern + "\", ");
                        System.out.println("\n" + transformation + "\n");
                    }
                }
                i++;
            }
        }
    }

    private static List<TransformationExample> head(Integer amount, List<TransformationExample> transformationExamples){
        List<TransformationExample> head = new ArrayList<>();
        for(int i = 0; i < amount; i++)
            head.add(transformationExamples.get(i));
        return head;
    }

    private static List<TransformationExample> getSeed(Double frac, List<TransformationExample> transformationExamples){
        List<TransformationExample> seed = new ArrayList<>();

        int num = (int) Math.ceil(frac * transformationExamples.size());
        List<Integer> indexes = new ArrayList<>();
        Random random = new Random();
        while(seed.size() < num) {
            var next = random.nextInt(transformationExamples.size());
            if(!indexes.contains(next)) {
                indexes.add(next);
                seed.add(transformationExamples.get(next));
            }
        }
        return seed;
    }

    /*
    public static boolean checkForTransformation(String input, String output){
        Boolean preprocessing = input.contains(", ");
        TransformationExample transformationExample = new TransformationExample(input, output);
        if(getFoofahTransformation("RPM/src/foofah-master/foofah.py", Collections.singletonList(transformationExample),
                "--timeout 60", preprocessing) != null )
            return true;
        else
            return false;
    }
     */

    public static HashMap<String, List<TransformationExample>> groupByTarget(List<TransformationExample> transformationExamples){
        HashMap<String, List<TransformationExample>> data = new HashMap<>();
        for(TransformationExample transformationExample: transformationExamples){
            String target = transformationExample.getTarget();
            if(data.containsKey(target))
                data.put(target, Stream.concat(data.get(target).stream(), Collections.singletonList(transformationExample).stream()).collect(Collectors.toList()));
            else
                data.put(target, Collections.singletonList(transformationExample));
        }
        return data;
    }

    public static HashMap<String, List<TransformationExample>> groupBySource(List<TransformationExample> transformationExamples){
        HashMap<String, List<TransformationExample>> data = new HashMap<>();
        for(TransformationExample transformationExample: transformationExamples){
            String source = transformationExample.getSource();
            if(data.containsKey(source))
                data.put(source, Stream.concat(data.get(source).stream(), Collections.singletonList(transformationExample).stream()).collect(Collectors.toList()));
            else
                data.put(source, Collections.singletonList(transformationExample));
        }
        return data;
    }

    public static HashMap<String, List<TransformationExample>> groupByCase(List<TransformationExample> transformationExamples){
        HashMap<String, List<TransformationExample>> data = new HashMap<>();
        for(TransformationExample transformationExample: transformationExamples){
            String caseID = transformationExample.getCaseID();
            if(data.containsKey(caseID))
                data.put(caseID, Stream.concat(data.get(caseID).stream(), Collections.singletonList(transformationExample).stream()).collect(Collectors.toList()));
            else
                data.put(caseID, Collections.singletonList(transformationExample));
        }
        return data;
    }

    public static List<String> getInputs(List<Event> events){
        List<String> sources = new ArrayList<>();
        List<String> inputs = new ArrayList<>();
        for(int i = 0; i < events.size(); i++)
            if(events.get(i).eventType.equals("copyCell") && !sources.contains(events.get(i).payload.get("target.id"))){
                sources.add(events.get(i).payload.get("target.id"));
                inputs.add(events.get(i).payload.get("target.value").replaceAll("\\P{Print}", " "));
            }
        return inputs;
    }

    public static List<String> getOutputs(List<Event> events){
        List<String> targets = new ArrayList<>();
        List<String> outputs = new ArrayList<>();
        for(int i = events.size() - 1; i >= 0; i--)
            if(events.get(i).eventType.equals("editField") && !targets.contains(events.get(i).payload.get("target.name"))){
                var target = events.get(i).payload.get("target.name");
                for(int j = i-1; j >= 0; j--)
                    if(events.get(j).eventType.equals("paste") && events.get(j).payload.get("target.name").equals(target)){
                        targets.add(target);
                        outputs.add(events.get(i).payload.get("target.value").replaceAll("\\P{Print}", " "));
                        break;
                    }
            }
        Collections.reverse(outputs);
        return outputs;
    }

    public static HashMap<String, List<TransformationExample>> groupExamples(List<TransformationExample> transformationExamples){
        /*
        HashMap<String, List<TransformationExample>> groupedBySource = groupBySource(transformationExamples);
        HashMap<String, List<TransformationExample>> groupedByTarget = groupByTarget(transformationExamples);
        if(groupedBySource.size() < groupedByTarget.size())
            return groupedBySource;
        else
            return groupedByTarget;
         */

        return groupByTarget(transformationExamples);
    }

    public static void discoverCorrelation(List<TransformationExample> transformationExamples){
        List<String> sources = new ArrayList(transformationExamples.stream().map(te -> te.getSource()).collect(Collectors.toList()));
        List<String> targets = new ArrayList(transformationExamples.stream().map(te -> te.getTarget()).collect(Collectors.toList()));
        String projectedSource;
        String projectedTarget;
        if(sources.stream().allMatch(sources.get(0)::equals))
            projectedSource = sources.get(0);
        else
            projectedSource = getProjection(sources);
        if(targets.stream().allMatch(targets.get(0)::equals))
            projectedTarget = targets.get(0);
        else
            projectedTarget = getProjection(targets);
        System.out.println("\n" + projectedSource + "  =>  " + projectedTarget + "\n");
    }

    public static String getProjection(List<String> inputs){
        List<String> columns = new ArrayList<>();
        String regex = "([\\w&&\\D]+)";
        for(int i = 0; i < inputs.size(); i++)
        {
            List<String> cols = new ArrayList<>();
            Pattern pat = Pattern.compile(regex);
            Matcher m = pat.matcher(inputs.get(i));
            while (m.find()) {
                cols.add(m.group());
            }
            if(cols.size() == 1)
                columns.add("Column " + cols.get(0));
            else{
                var temp = "";
                for(int j = 0; j < cols.size(); j++)
                    temp = temp + cols.get(j) + ", ";
                columns.add("Columns " + temp.substring(0, temp.lastIndexOf(",")));
            }
        }

        List<String> rows = new ArrayList<>();
        regex = "(\\d+)";
        for(int i = 0; i < inputs.size(); i++)
        {
            List<String> rws = new ArrayList<>();
            Pattern pat = Pattern.compile(regex);
            Matcher m = pat.matcher(inputs.get(i));
            while (m.find()) {
                rws.add(m.group());
            }
            if(rws.size() == 1)
                rows.add("Row " + rws.get(0));
            else{
                var temp = "";
                for(int j = 0; j < rws.size(); j++)
                    temp = temp + rws.get(j) + ", ";
                rows.add("Rows " + temp.substring(0, temp.lastIndexOf(",")));
            }
        }
        if(columns.stream().allMatch(columns.get(0)::equals))
            return columns.get(0);
        else if(rows.stream().allMatch(rows.get(0)::equals))
            return rows.get(0);
        else return null;
    }

    public static String prepareArray(String array){
        return array.replaceAll("\"([^;\"\\[\\]]+)\"","$1").replaceAll("([^\\[\\];]+)", "\"$1\"").
                replaceAll("\\[;","\\[\"\";").replaceAll(";;",";\"\";").replaceAll(";\\]",";\"\"\\]").
                replaceAll("\"\"\"\"","\"\"").replaceAll(";", ", ");
    }

    public static String getFoofahTransformation(String foofahPath, List<TransformationExample> transformationExamples, String setting, Boolean preprocessing){
        String output;
        for(var te: transformationExamples) {
            var inputs = te.getInputExample().stream().map(el -> "\"" + el + "\"").collect(Collectors.toList());
            var outputs = te.getOutputExample().stream().map(el -> "\"" + el + "\"").collect(Collectors.toList());
            System.out.println(inputs.toString() + " => " + outputs.toString());
        }
        sb = valuesToJsonFoofah(transformationExamples);
        try {
            String filePath = createFile(foofahPath, sb);
            output = execPython(foofahPath,"--input " + filePath + " " + setting, preprocessing);
            if(output != null){
                output = output.replace("\n\n", "");
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return output;
    }

    private static StringBuilder valuesToJsonFoofah(List<TransformationExample> transformationExamples) {
        sb = new StringBuilder();
        sb.append("{\"InputTable\": [");
        listToStringBuilder(transformationExamples.stream().map(te -> te.getInputExample()).collect(Collectors.toList()), sb, "from");
        sb.append("], \"OutputTable\": [");
        listToStringBuilder(transformationExamples.stream().map(te -> te.getOutputExample()).collect(Collectors.toList()), sb, "to");
        sb.append("]}");
        //System.out.println(sb);
        return sb;
    }

    private static void listToStringBuilder(List<List<String>> list, StringBuilder sb, String exampleType) {
        int i = 0;
        for(var example: list){
            if(example.size() == 1)
                sb.append("[\"" + example.get(0) + "\"]");
            else{
                int j = 0;
                sb.append("[");
                for(var value: example){
                    sb.append("\"" + value + "\"");
                    if(j < example.size() - 1)
                        sb.append(", ");
                    j++;
                }
                sb.append("]");
            }
            if(i == list.size() - 1){
                break;
            }
            sb.append(", ");
            i++;
        }
    }

    public static String getFoofahTransformation2(String exec, HashMap<String, List<Event>> cases, String setting, Boolean preprocessing){
        String output = null;
        for(var caseID: cases.keySet())
            System.out.println(getInputs(cases.get(caseID)).stream().map(el -> "\"" + el + "\"").collect(Collectors.toList()) + " => " +
                    getOutputs(cases.get(caseID)).stream().map(el -> "\"" + el + "\"").collect(Collectors.toList()));
        System.out.println("\n\n");
        sb = valuesToJsonFoofah2(cases);
        try {
            String path = createFile(exec, sb);
            output = execPython(exec,"--input "+path+ " "+setting, preprocessing);
            if(output!= null && output.contains("','"))
                output = output.replace("','","', '");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return output;
    }

    private static StringBuilder valuesToJsonFoofah2(HashMap<String, List<Event>> cases){
        int i = 0;
        sb = new StringBuilder();
        sb.append("{\"InputTable\": [");
        for(String caseID: cases.keySet()){
            listToStringBuilder2(getInputs(cases.get(caseID)), sb, "from");
            sb.append(", ");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        sb.append("], \"OutputTable\": [");
        for(String caseID: cases.keySet()){
            listToStringBuilder2(getOutputs(cases.get(caseID)), sb, "to");
            sb.append(", ");
        }
        sb.delete(sb.lastIndexOf(","), sb.length());
        sb.append("]}");
        //System.out.println(sb);
        return sb;
    }

    private static void listToStringBuilder2(List<String> list, StringBuilder sb, String exampleType) {
        int i=0;
        sb.append("[");
        for(var element: list){
            sb.append("\"" + element + "\"");
            if(i < list.size() - 1)
                sb.append(", ");
            i++;
        }
        sb.append("]");
    }
}