public class DiscardedCode {
    /*
    public static void getFoofahExamples(HashMap<String, List<TransformationExample>> groupedExamples){
        List<String> inputExamples = new ArrayList<>();
        List<String> outputExamples = new ArrayList<>();

        for(int i = 0; i < groupedExamples.get(groupedExamples.keySet().toArray()[0]).size(); i++){
            String input = "";
            String output = "";
            for(String key: groupedExamples.keySet()){
                input += "\"" + groupedExamples.get(key).get(i).getInputExamples() + "\", ";
                output += "\"" + groupedExamples.get(key).get(i).getOutputExamples() + "\", ";
            }
            input = input.substring(1, input.lastIndexOf('\"'));
            output = output.substring(1, output.lastIndexOf('\"'));
            inputExamples.add(input);
            outputExamples.add(output);
        }
        System.out.println("\n" + getFoofahTransformation("RPM/src/foofah-master/foofah.py", inputExamples, outputExamples, "--timeout 60000", true) + "\n");
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

    /*
    public static void listToStringBuilder(List<String> list, StringBuilder sb, String exampleType) {
        int i=0;
        for(String value: list){
            if(!value.contains(", ") || exampleType.equals("to")){
                sb.append("[\""+value.replaceAll(",\\s",",")+"\"]");
            }
            else{
                sb.append("[");
                String[] stringComp = value.split(",\\s");
                for (int j = 0; j < stringComp.length; j++) {
                    sb.append("\"" + stringComp[j] + "\"");
                    if (j < stringComp.length - 1)
                        sb.append(',');
                }
                sb.append("]");
            }
            if(i==list.size()-1){
                break;
            }
            sb.append(',');
            i++;
        }
    }
     */

    /*
    private static void listToStringBuilder(List<String> list, StringBuilder sb, String exampleType) {
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
    */

    /*
    private static StringBuilder valuesToJsonFoofah2(HashMap<String, List<TransformationExample>> transformationExamples){
        sb = new StringBuilder();
        sb.append("{\"InputTable\": [");
        for(String key: transformationExamples.keySet())
            listToStringBuilder2(getInputs(transformationExamples.get(key)), sb, "from");
        sb.delete(sb.lastIndexOf(","), sb.length());
        sb.append("], \"OutputTable\": [");
        for(String key: transformationExamples.keySet())
            listToStringBuilder2(getOutputs(transformationExamples.get(key)), sb, "to");
        sb.delete(sb.lastIndexOf(","), sb.length());
        sb.append("]}");
        System.out.println(sb);
        return sb;
    }

    private static void listToStringBuilder2(List<String> list, StringBuilder sb, String exampleType) {
        int i=0;
        int k;
        if(exampleType.equals("from"))
            k = 0;
        else
            k = 0;
        sb.append("[");
        for(int j = 0; j < list.size()-k; j++){
            sb.append("\""+list.get(j)+"\"");
            if(i==list.size()-(k+1)){
                break;
            }
            sb.append(", ");
            i++;
        }
        sb.append("], ");
    }
    */

    /*
    public static String getFoofahTransformation2(String exec, HashMap<String, List<TransformationExample>> transformationExamples, String setting, Boolean preprocessing){
        String output = null;
        sb = valuesToJsonFoofah2(transformationExamples);
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
    */

     /*
    private static StringBuilder valuesToJsonFoofah(List<TransformationExample> transformationExamples) {
        sb = new StringBuilder();
        if(transformationExamples.get(0).getSource().contains(":"))
            sb.append("{\"InputTable\": ");
        else
            sb.append("{\"InputTable\": [");
        listToStringBuilder(transformationExamples.stream().map(te -> te.getInputExample()).collect(Collectors.toList()), sb, "from");
        if(transformationExamples.get(0).getTarget().contains(":"))
            sb.append("], \"OutputTable\": ");
        else
            sb.append("], \"OutputTable\": [");
        listToStringBuilder(transformationExamples.stream().map(te -> te.getOutputExample()).collect(Collectors.toList()), sb, "to");
        if(transformationExamples.get(0).getTarget().contains(":"))
            sb.append("}");
        else
            sb.append("]}");
        //System.out.println(sb);
        return sb;
    }

    private static void listToStringBuilder(List<String> list, StringBuilder sb, String exampleType) {
        int i = 0;
        for(String value: list){
            sb.append("[\""+value+"\"]");
            if(i == list.size() - 1){
                break;
            }
            sb.append(',');
            i++;
        }
    }
     */
}
