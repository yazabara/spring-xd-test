package yazabara.log.events.client;

/**
 * @author Yaroslav Zabara
 */
public class EventsParams {
    private String eventId;
    private String eventParamId;
    private String eventParamName;
    private String eventParamValue;

    public EventsParams(String event_id, String event_param_id, String event_param_name, String event_param_value) {
        this.eventId = event_id;
        this.eventParamId = event_param_id;
        this.eventParamName = event_param_name;
        this.eventParamValue = event_param_value;
    }

    @Override
    public String toString() {
        return "{" +
                "\"event_id\":\"" + eventId + "\"" +
                ", \"event_param_id\":\"" + eventParamId + "\"" +
                ", \"event_param_name\":\"" + eventParamName + "\"" +
                ", \"event_param_value\":\"" + eventParamValue + "\"" +
                '}';
    }
}
