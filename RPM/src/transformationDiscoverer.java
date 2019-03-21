import data.Event;
import data.TransformationExample;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class transformationDiscoverer {
    public static String getFoofahTransformation(String exec, List<String> from, List<String> to, String setting){
        String output = null;
        StringBuilder sb = valuesToJsonFoofah(from, to);
        try {
            String path = createFile(sb);
            output = execPython(exec, "--input "+path+ " "+setting);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return output;
    }

    private static StringBuilder valuesToJsonFoofah(List<String> from, List<String> to) {
        StringBuilder sb=new StringBuilder();
        sb.append("{\"InputTable\": [");
        listToStringBuilder(from, sb);
        sb.append("], \"OutputTable\": [");
        listToStringBuilder(to, sb);
        sb.append("]}");
        return sb;
    }

    private static void listToStringBuilder(List<String> list, StringBuilder sb) {
        int i=0;
        for(String value: list){
            sb.append("[\""+value+"\"]");
            if(i==list.size()-1){
                break;
            }
            sb.append(',');
            i++;
        }
    }

    private static String createFile(StringBuilder sb) throws IOException{
        String path = "foofahTEMP.txt";
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

    public static String execPython(String exec, String parameters) {
        String output = null;
        try {
            String s = null;
            StringBuilder sb = new StringBuilder(3000);

            Process p = Runtime.getRuntime().exec("python " + exec + " " + parameters);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null) {
                sb.append(s + "\n");
            }
            if (sb.toString().contains("*** Solution Not Found ***"))
                return null;
            else {

                output = sb.substring(sb.indexOf("#\n" +
                        "# Data Transformation\n" +
                        "#") + 26);
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

    public static List<TransformationExample> extractExamples(HashMap<String, List<Event>> cases) {
        List<TransformationExample> ts = new ArrayList<>();
        HashMap<String, List<String>> input = new HashMap<>();
        HashMap<String, List<String>> output = new HashMap<>();
        for(String caseID: cases.keySet()){
            List<Event> events = new ArrayList<>(cases.get(caseID));
            for(int i = events.size()-1; i >= 0; i--){
                if(events.get(i).eventType.equals("editField")){
                    String textField = events.get(i).payload.get("target.name");
                    for(int j = i; j >= 0; j--)
                        if(events.get(j).eventType.equals("copy")){
                            for(int k = j; k >= 0; k--)
                                if(events.get(k).eventType.equals("getCell")){
                                    if(input.containsKey(textField) ){
                                        String in = events.get(k).payload.get("target.value").replaceAll("\\P{Print}", " ");
                                        input.put(textField, Stream.concat(input.get(textField).stream(), Collections.singletonList(in).stream()).collect(Collectors.toList()));
                                        String out =  events.get(i).payload.get("target.value").replaceAll("\\P{Print}", " ");
                                        output.put(textField, Stream.concat(output.get(textField).stream(), Collections.singletonList(out).stream()).collect(Collectors.toList()));
                                        break;
                                    }
                                    else{
                                        String in = events.get(k).payload.get("target.value").replace("\\P{Print}", " ");
                                        input.put(textField, Collections.singletonList(in));
                                        String out = events.get(i).payload.get("target.value").replace("\\P{Print}", " ");
                                        output.put(textField, Collections.singletonList(out));
                                        break;
                                    }
                                }
                                break;
                        }
                }
            }
        }
        for(String key: input.keySet())
            ts.add(new TransformationExample(key, input.get(key), output.get(key)));
        return ts;
    }

    public static void discoverDataTransformations(List<TransformationExample> transformationExamples){
        for(TransformationExample te: transformationExamples){
            System.out.println(te + "\n");
            System.out.println(getFoofahTransformation("/home/vleno/Desktop/foofah-master/foofah.py", Collections.singletonList(te.getInputExamples().get(0)), Collections.singletonList(te.getOutputExamples().get(0)), ""));
            //System.out.println(getFoofahTransformation("/home/vleno/Desktop/foofah-master/foofah.py", te.getInputExamples().subList(1, 4), te.getOutputExamples().subList(1,4), ""));
        }
    }
}

