package yazabara.log.events.client;

/**
 * @author Yaroslav Zabara
 */
public class Event {

    private String eventId;
    private String eventElement;
    private String eventElementType;
    private String eventAction;
    private Long eventDate;
    private String eventUsername;
    private String eventEnvironment;

    public Event(String eventId, String eventElement, String eventElementType, String eventAction, Long eventDate, String eventUsername, String eventEnvironment) {
        this.eventId = eventId;
        this.eventElement = eventElement;
        this.eventElementType = eventElementType;
        this.eventAction = eventAction;
        this.eventDate = eventDate;
        this.eventUsername = eventUsername;
        this.eventEnvironment = eventEnvironment;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventElement() {
        return eventElement;
    }

    public void setEventElement(String eventElement) {
        this.eventElement = eventElement;
    }

    public String getEventElementType() {
        return eventElementType;
    }

    public void setEventElementType(String eventElementType) {
        this.eventElementType = eventElementType;
    }

    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(String eventAction) {
        this.eventAction = eventAction;
    }

    public Long getEventDate() {
        return eventDate;
    }

    public void setEventDate(Long eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventUsername() {
        return eventUsername;
    }

    public void setEventUsername(String eventUsername) {
        this.eventUsername = eventUsername;
    }

    public String getEventEnvironment() {
        return eventEnvironment;
    }

    public void setEventEnvironment(String eventEnvironment) {
        this.eventEnvironment = eventEnvironment;
    }

    @Override
    public String toString() {
        return "{" +
                "\"event_id\":\"" + eventId + "\"" +
                ", \"event_element\":\"" + eventElement + "\"" +
                ", \"event_element_type\":\"" + eventElementType + "\"" +
                ", \"event_action\":\"" + eventAction + "\"" +
                ", \"event_date\":\"" + eventDate + "\"" +
                ", \"event_username\":\"" + eventUsername + "\"" +
                ", \"event_environment\":\"" + eventEnvironment + "\"" +
                '}';
    }
}
