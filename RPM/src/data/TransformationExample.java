package data;

import java.util.ArrayList;
import java.util.List;

public class TransformationExample {
    String caseID;
    String source;
    String target;
    String input;
    String output;

    public TransformationExample(String caseID, String source, String target, String input, String output){
        this.caseID = caseID;
        this.source = source;
        this.target = target;
        this.input = input;
        this.output = output;
    }

    public TransformationExample(String input, String output){
        this.input = input;
        this.output = output;
    }

    public String toString() {
        return "<CaseID = " + this.caseID + "> (" + this.source + "," + this.target + "):  " + this.input + " <=> " + this.output;
    }

    public String getTarget() {return this.target; }

    public String getSource() {return this.source; }

    public String getInputExample(){ return this.input; }

    public String getOutputExample(){
        return this.output;
    }

    public String getCaseID(){ return  this.caseID; }
}
