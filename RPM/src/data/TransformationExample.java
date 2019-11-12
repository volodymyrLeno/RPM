package data;

import java.util.Collections;
import java.util.List;

public class TransformationExample {
    String caseID;
    String source;
    String target;
    List<String> input;
    List<String> output;

    public TransformationExample(String caseID, String source, String target, List<String> input, List<String> output){
        this.caseID = caseID;
        this.source = source;
        this.target = target;
        this.input = input;
        this.output = output;
    }

    public TransformationExample(String caseID, String source, String target, String input, String output){
        this.caseID = caseID;
        this.source = source;
        this.target = target;
        this.input = Collections.singletonList(input);
        this.output = Collections.singletonList(output);
    }

    public TransformationExample(List<String> input, List<String> output){
        this.input = input;
        this.output = output;
    }

    public String toString() {
        return "<CaseID = " + this.caseID + "> (" + this.source + "," + this.target + "):  " + this.input + " <=> " + this.output;
    }

    public String getTarget() {return this.target; }

    public String getSource() {return this.source; }

    public List<String> getInputExample(){ return this.input; }

    public List<String> getOutputExample(){
        return this.output;
    }

    public String getCaseID(){ return  this.caseID; }
}
