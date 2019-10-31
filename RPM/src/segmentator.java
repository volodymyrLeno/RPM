import data.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class segmentator {
    public static HashMap<String, List<Event>> segment(List<Event> events){
        HashMap<String, List<Event>> cases = new HashMap<>();
        Integer id = 1;
        List<Event> caseEvents = new ArrayList<>();
        for(Event ev: events) {
            caseEvents.add(ev);
            if (ev.eventType.equals("clickButton") && ev.payload.containsKey("target.innerText") && ev.payload.get("target.innerText").equals("Submit")) {
                cases.put(id.toString(), new ArrayList<>(caseEvents));
                caseEvents.clear();
                id++;
            }
        }
        //cases.put(id.toString(), caseEvents);
        return cases;
    }

    public static HashMap<String, List<Event>> groupByCases(List<Event> events){
        HashMap<String, List<Event>> cases = new HashMap<>();
        for(Event event: events){
            String caseID = event.caseID;
            if(cases.containsKey(caseID))
                cases.put(caseID, Stream.concat(cases.get(caseID).stream(), Collections.singletonList(event).stream()).collect(Collectors.toList()));
            else
                cases.put(caseID, Collections.singletonList(event));
        }
        return cases;
    }
}
