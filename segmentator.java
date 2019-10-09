import data.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class segmentator {
    public static HashMap<String, List<Event>> segment(List<Event> events){
        HashMap<String, List<Event>> cases = new HashMap<>();
        Integer id = 1;
        List<Event> caseEvents = new ArrayList<>();
        for(Event ev: events){
            caseEvents.add(ev);
            if(ev.eventType.equals("clickButton") && ev.payload.containsKey("target.innerText") && ev.payload.get("target.innerText").equals("Submit")) {
                cases.put(id.toString(), new ArrayList<>(caseEvents));
                caseEvents.clear();
                id++;
            }
        }
        cases.put(id.toString(), caseEvents);
        return cases;
    }
}
