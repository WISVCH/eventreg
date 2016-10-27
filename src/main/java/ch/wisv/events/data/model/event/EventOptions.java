package ch.wisv.events.data.model.event;

import ch.wisv.events.data.model.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * EventOption
 */
public class EventOptions implements Serializable, Model {

    /**
     * Status of the Event
     */
    @Getter
    @Setter
    public EventStatus published;

    /**
     * Default constructor, with status not published.
     */
    public EventOptions() {
        this.published = EventStatus.NOT_PUBLISHED;
    }

}