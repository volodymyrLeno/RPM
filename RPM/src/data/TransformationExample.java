package data;

import java.util.ArrayList;
import java.util.List;

public class TransformationExample {
    String target;
    List<String> input = new ArrayList<>();
    List<String> output = new ArrayList<>();

    public TransformationExample(String target, List<String> input, List<String> output){
        this.target = target;
        this.input = input;
        this.output = output;
    }

    public String toString() {
        return this.target + ":  " + this.input + " <=> " + this.output;
    }

    public List<String> getInputExamples(){
        return this.input;
    }

    public List<String> getOutputExamples(){
        return this.output;
    }
}
