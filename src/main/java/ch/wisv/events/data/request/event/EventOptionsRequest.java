package ch.wisv.events.data.request.event;

import ch.wisv.events.data.request.Request;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sven on 19/10/2016.
 */
@Data
@NoArgsConstructor
public class EventOptionsRequest implements Request {

    public String key;

    public int status;

    public EventOptionsRequest(String key, int status) {
        this.key = key;
        this.status = status;
    }
}