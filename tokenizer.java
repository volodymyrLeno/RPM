import data.TransformationExample;

import javax.xml.crypto.dsig.Transform;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class tokenizer {

    public static HashMap<String, List<TransformationExample>> clusterByPattern(List<TransformationExample> groupedExamples){
        HashMap<String, List<TransformationExample>> clusters = new HashMap<>();
        for(TransformationExample te: groupedExamples){
            var pattern = tokenize(te);
            if(!clusters.containsKey(pattern))
                clusters.put(pattern, Collections.singletonList(te));
            else{
                var collection = new ArrayList<>(clusters.get(pattern));
                collection.addAll(Collections.singletonList(te));
                clusters.put(pattern, collection);
            }
        }
        return clusters;
    }

    /*
    public static String tokenize(TransformationExample transformationExample){
        String input = transformationExample.getInputExample();
        input = input.replaceAll("[a-zA-Z]{2,}","<a>+");
        input = input.replaceAll("[a-zA-Z](?![^\\<]*\\>)", "<a>");
        input = input.replaceAll("\\d{2,}", "<d>+");
        input = input.replaceAll("\\d(?![^\\<]*\\>)", "<d>");
        return input;
    }

    public static String tokenize(String input){
        input = input.replaceAll("[a-zA-Z]","<a>+");
        input = input.replaceAll("[a-zA-Z](?![^\\<]*\\>)", "<a>");
        input = input.replaceAll("\\d{2,}", "<d>+");
        input = input.replaceAll("\\d(?![^\\<]*\\>)", "<d>");
        return input;
    }
     */

    public static String tokenize(TransformationExample transformationExample){
        String input = transformationExample.getInputExample();
        input = input.replaceAll("[a-zA-Z]+","<a>+");
        input = input.replaceAll("\\d+", "<d>+");
        return input;
    }

    public static String tokenize(String input){
        input = input.replaceAll("[a-zA-Z]+","<a>+");
        input = input.replaceAll("\\d+", "<d>+");
        return input;
    }
}
