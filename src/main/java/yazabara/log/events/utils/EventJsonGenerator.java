package yazabara.log.events.utils;

import yazabara.log.events.client.Event;
import yazabara.log.events.client.EventsParams;

import java.util.Date;

/**
 * @author Yaroslav Zabara
 */
public class EventJsonGenerator {

    public static String generateJson() {
        Event event = new Event(java.util.UUID.randomUUID().toString(), "element2", "elType2", "elAction2", new Date().getTime(), "username2", "DEV_1");
        EventsParams eventsParams = new EventsParams(event.getEventId(), java.util.UUID.randomUUID().toString(), "param", "value");
        return "{ \"EVENT\": " + event.toString() + ", \"EVENT_PARAMETERS\": [" + eventsParams + "] }";
    }
}
