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

    private static String createFile(StringBuilder sb) throws IOException{
        String path = "RPM/src/foofah-master/foofahTEMP.txt";
        File file = new File(path);
        if (!file.exists())
            if (!file.createNewFile())
                new IOException("Error occured when creating "+file.getAbsolutePath());
        FileWriter fw = new FileWriter(file);
        fw.write(sb.toString());
        fw.flush();
        fw.close();
        return file.getAbsolutePath();
    }

    public static String execPython(String exec, String parameters, Boolean preprocessing) {
        //String initialCommand = "t = f_split_char(t, 0, ', ')\n";

        String output = null;
        try {
            String s = null;
            sb = new StringBuilder(100000);

            Process p = Runtime.getRuntime().exec("python " + exec + " " + parameters);
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
                //else
                //   output = preprocessing ? initialCommand + output : output;
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

    /*
    public static List<TransformationExample> extractExamples(HashMap<String, List<Event>> cases, List<String> readActions, List<String> writeActions){
        List<TransformationExample> transformationExamples = new ArrayList<>();
        String target;
        String source;
        for(String caseID: cases.keySet()){
            List<Event> events = new ArrayList<>(cases.get(caseID));
            for(int i = events.size()-1; i >= 0; i--){
                if(writeActions.contains(events.get(i).eventType)){
                    if(events.get(i).payload.containsKey("target.id"))
                        target = events.get(i).payload.get("target.id");
                    else
                        target = events.get(i).payload.get("target.name");
                    for(int j = i; j >= 0; j--)
                        if(readActions.contains(events.get(j).eventType)){
                            String input = events.get(j).payload.get("target.value").replaceAll("\\P{Print}", " ");
                            String output =  events.get(i).payload.get("target.value").replaceAll("\\P{Print}", " ");
                            if(events.get(j).payload.containsKey("target.id"))
                                source = events.get(j).payload.get("target.id");
                            else
                                source = events.get(j).payload.get("target.name");
                            transformationExamples.add(new TransformationExample(caseID, source, target, input, output));
                            break;
                        }
                }
            }
        }
        return transformationExamples;
    }
     */

    /*
    public static List<TransformationExample> extractExamples(HashMap<String, List<Event>> cases, List<String> readActions, List<String> writeActions){
        List<TransformationExample> transformationExamples = new ArrayList<>();
        for(String caseID: cases.keySet()){
            List<Event> events = new ArrayList<>(cases.get(caseID));
            for(int i = events.size()-1; i >= 0; i--){
                if(events.get(i).eventType.equals("editField") && events.get(i-1).eventType.equals("paste")){
                    String target = events.get(i).payload.get("target.name");
                    for(int j = i; j >= 0; j--)
                        if(events.get(j).eventType.equals("copy")){
                            for(int k = j; k >= 0; k--)
                                if(events.get(k).eventType.equals("getCell") || events.get(k).eventType.equals("editCell")){
                                    String input = events.get(k).payload.get("target.value").replaceAll("\\P{Print}", " ");
                                    String output =  events.get(i).payload.get("target.value").replaceAll("\\P{Print}", " ");
                                    String source = events.get(k).payload.get("target.id");
                                    transformationExamples.add(new TransformationExample(caseID, source, target, input, output));
                                    break;
                                }
                            break;
                        }
                }
            }
        }
        return transformationExamples;
    }
     */

    public static List<TransformationExample> extractExamples(HashMap<String, List<Event>> cases, List<String> readActions, List<String> writeActions){
        List<TransformationExample> transformationExamples = new ArrayList<>();
        for(String caseID: cases.keySet()) {
            List<Event> events = new ArrayList<>(cases.get(caseID));
            List<String> targets = new ArrayList<>();
            for (int i = events.size() - 1; i >= 0; i--) {
                if (events.get(i).eventType.equals("editField") && !targets.contains(events.get(i).payload.get("target.name"))) {
                    String target = events.get(i).payload.get("target.name");
                    String output = events.get(i).payload.get("target.value").replaceAll("\\P{Print}", " ");;
                    String source = "";
                    List<String> input = new ArrayList<>();
                    targets.add(target);
                    for (int j = 0; j < i; j++)
                        if (events.get(j).eventType.equals("paste") &&
                                events.get(j).payload.get("target.name").equals(target)) {
                            for (int k = j; k >= 0; k--)
                                if (events.get(k).eventType.equals("copyCell")) {
                                    source = source == "" ? events.get(k).payload.get("target.id") : source + "," + events.get(k).payload.get("target.id");
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

    public static void discoverDataTransformations(Double frac, List<TransformationExample> transformationExamples){

        List<TransformationExample> seed = getSeed(frac, transformationExamples);
        List<TransformationExample> head = head((int) Math.ceil(frac * transformationExamples.size()), transformationExamples);

        discoverCorrelation(transformationExamples);

        Boolean preprocessing = false;

        /* Small tweak
        var temp = seed.get(0).getInputExample();
        for(var el: temp)
            if(el.contains(", ")){
                preprocessing = true;
                break;
            }
         */

        System.out.println("\n" + getFoofahTransformation("RPM/src/foofah-master/foofah.py", head, "--timeout 3600", preprocessing) + "\n");

        /*
        if(checkForTransformation(seed.get(0).getInputExample(), seed.get(0).getOutputExample()))
            System.out.println("\n" + getFoofahTransformation("RPM/src/foofah-master/foofah.py", seed, "--timeout 600", preprocessing) + "\n");
        else
            System.out.println("\n No data transformation discovered! \n");
         */
    }

    public static void discoverTransformationsByPatterns(HashMap<String, List<TransformationExample>> patterns){
        HashMap<String, List<String>> groupedPatterns = new HashMap<>();
        discoverCorrelation(patterns.get(patterns.keySet().toArray()[0]));

        for(String pattern: patterns.keySet()){
            var transformation = getFoofahTransformation("RPM/src/foofah-master/foofah.py", getSeed(1.0/patterns.get(pattern).size(), patterns.get(pattern)), "--timeout 600", false);
            //System.out.println(transformation);
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
        //System.out.println(num);
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
                /*
                for(int j = i; j >= 0; j--)
                    if(events.get(j).eventType.equals("paste") && events.get(j).payload.get("target.name").equals(target)){
                        targets.add(target);
                        outputs.add(events.get(i).payload.get("target.value").replaceAll("\\P{Print}", ""));
                    }

                 */
            }
        Collections.reverse(outputs);
        return outputs;
    }

    /*
    public static List<List<String>> getInputs(List<TransformationExample> transformationExamples){
        Collections.reverse(transformationExamples);
        List<List<String>> inputs = transformationExamples.stream().map(TransformationExample::getInputExample).collect(Collectors.toList());
        Collections.reverse(transformationExamples);
        return inputs.stream().distinct().collect(Collectors.toList());
    }

    public static List<List<String>> getOutputs(List<TransformationExample> transformationExamples){
        Collections.reverse(transformationExamples);
        List<List<String>> outputs = transformationExamples.stream().map(TransformationExample::getOutputExample).collect(Collectors.toList());
        Collections.reverse(transformationExamples);
        return outputs.stream().distinct().collect(Collectors.toList());
    }
     */

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

    /* UNDER DEVELOPMENT */

    public static String getFoofahTransformation(String exec, List<TransformationExample> transformationExamples, String setting, Boolean preprocessing){
        String output;
        for(var te: transformationExamples) {
            var inputs = te.getInputExample().stream().map(el -> "\"" + el + "\"").collect(Collectors.toList());
            var outputs = te.getOutputExample().stream().map(el -> "\"" + el + "\"").collect(Collectors.toList());
            System.out.println(inputs.toString() + " => " + outputs.toString());
        }
        sb = valuesToJsonFoofah(transformationExamples);
        try {
            String path = createFile(sb);
            output = execPython(exec,"--input "+path+ " "+setting, preprocessing);
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
        System.out.println(sb);
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

    /* Small tweak
    public static void listToStringBuilder(List<List<String>> list, StringBuilder sb, String exampleType) {
        int i=0;
        for(var example: list){
            if(example.size() == 1){
                if(!example.get(0).contains(", ") || exampleType.equals("to"))
                    sb.append("[\""+example.get(0).replaceAll(",\\s",",")+"\"]");
                else{
                    sb.append("[");
                    String[] stringComp = example.get(0).split(",\\s");
                    for (int j = 0; j < stringComp.length; j++) {
                        sb.append("\"" + stringComp[j] + "\"");
                        if (j < stringComp.length - 1)
                            sb.append(',');
                    }
                    sb.append("]");
                }
            }
            else{
                int k = 0;
                sb.append("[");
                for(var value: example){
                    sb.append("\"" + value + "\"");
                    if(k < example.size() - 1)
                        sb.append(",");
                    k++;
                }
                sb.append("]");
            }
            if(i == list.size() - 1){
                break;
            }
            sb.append(',');
            i++;
        }
    }

     */

    /* For handling Excel operations
    private static StringBuilder valuesToJsonFoofah(List<TransformationExample> transformationExamples) {
        sb = new StringBuilder();
        if(transformationExamples.get(0).getInputExample().matches("\\[\\[.*\\]\\]")){
            sb.append("{\"InputTable\": ");
            sb.append(prepareArray(transformationExamples.get(0).getInputExample()));
            if(transformationExamples.get(0).getOutputExample().matches("\\[\\[.*\\]\\]")){
                sb.append(", \"OutputTable\": ");
                sb.append(prepareArray(transformationExamples.get(0).getOutputExample()));
                sb.append("}");
            }
            else{
                sb.append(", \"OutputTable\": [");
                listToStringBuilder(Collections.singletonList(transformationExamples.get(0).getOutputExample()), sb, "to");
                sb.append("]}");
            }
        }
        else{
            sb.append("{\"InputTable\": [");
            listToStringBuilder(transformationExamples.stream().map(TransformationExample::getInputExample).collect(Collectors.toList()), sb, "from");
            sb.append("], \"OutputTable\": [");
            listToStringBuilder(transformationExamples.stream().map(TransformationExample::getOutputExample).collect(Collectors.toList()), sb, "to");
            sb.append("]}");
        }
        System.out.println(sb);
        return sb;
    }

    private static void listToStringBuilder(List<String> list, StringBuilder sb, String exampleType) {
        int i = 0;
        for(String value: list){
            if(value.equals("\"\""))
                sb.append("[" + value + "]");
            else
                sb.append("[\""+value+"\"]");
            if(i == list.size() - 1){
                break;
            }
            sb.append(',');
            i++;
        }
    }

     */

    /* COPYING FOR NOW, REWORK IN THE FUTURE */

    public static String getFoofahTransformation2(String exec, HashMap<String, List<Event>> cases, String setting, Boolean preprocessing){
        String output = null;
        sb = valuesToJsonFoofah2(cases);
        try {
            String path = createFile(sb);
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
        System.out.println(sb);
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