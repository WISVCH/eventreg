package ch.wisv.events.dashboard.request;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sven on 16/10/2016.
 */
@Data
@NoArgsConstructor
public class EventProductRequest {

    String eventKey;

    Long eventID;

    Long productID;

}