package data;

import java.util.HashMap;
import java.util.List;

public class Event {
    public String caseID;
    public String eventType;
    private String timestamp;
    public HashMap<String, String> payload;

    public Event(List<String> attributes, String[] values){
        this.caseID = "";
        this.eventType = values[attributes.indexOf("eventType")];
        this.timestamp = values[attributes.indexOf("timeStamp")];
        payload = new HashMap<>();
        for(int i = 0; i < values.length; i++)
            if(!values[i].equals("") && i != attributes.indexOf("eventType") && i != attributes.indexOf("timeStamp"))
                payload.put(attributes.get(i), values[i]);
    }

    public Event(String activityName, String timestamp) {
        this.caseID = "";
        this.eventType = activityName;
        this.timestamp = timestamp;
        payload = new HashMap<>();
    }

    public Event(Event event){
        this.caseID = event.caseID;
        this.eventType = event.eventType;
        this.timestamp = event.timestamp;
        this.payload = new HashMap<>(event.payload);
    }

    public void setCaseID(String caseID){
        this.caseID = caseID;
    }

    public String toString() {
        return "(" + this.caseID + ", " + this.eventType + ", " + this.timestamp + ", " + payload + ")";
    }
}