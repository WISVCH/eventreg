package ch.wisv.events.app.controller.dashboard;

import ch.wisv.events.api.request.EventProductRequest;
import ch.wisv.events.core.exception.EventNotFound;
import ch.wisv.events.core.model.event.Event;
import ch.wisv.events.core.model.order.SoldProduct;
import ch.wisv.events.core.service.event.EventService;
import ch.wisv.events.core.service.product.SoldProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardEventController.
 */
@Controller
@RequestMapping(value = "/dashboard/events")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardEventController {

    /**
     * EventService.
     */
    private final EventService eventService;

    /**
     * Field productService
     */
    private final SoldProductService soldProductService;

    /**
     * Default constructor
     *
     * @param eventService       EventService
     * @param soldProductService SoldProductService
     */
    @Autowired
    public DashboardEventController(EventService eventService, SoldProductService soldProductService) {
        this.eventService = eventService;
        this.soldProductService = soldProductService;
    }

    /**
     * Get request on "/dashboard/events/" will show overview of all Events
     *
     * @param model SpringUI model
     * @return path to Thymeleaf template
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("events", eventService.getAllEvents());

        return "dashboard/events/index";
    }

    /**
     * Get request on "/dashboard/events/create/" will show page to create Event
     *
     * @param model SpringUI model
     * @return path to Thymeleaf template
     */
    @GetMapping("/create/")
    public String create(Model model) {
        if (!model.containsAttribute("event")) {
            model.addAttribute("event", new Event());
        }

        return "dashboard/events/create";
    }

    /**
     * Get request on "/dashboard/events/edit/{key}" will show the edit page to edit Event with requested key
     *
     * @param model SpringUI model
     * @return path to Thymeleaf template
     */
    @GetMapping("/edit/{key}/")
    public String edit(Model model, @PathVariable String key) {
        try {
            if (!model.containsAttribute("event")) {
                model.addAttribute("event", eventService.getByKey(key));
            }

//            model.addAttribute("eventProduct", EventProductRequestFactory.create(event));

            return "dashboard/events/edit";
        } catch (EventNotFound e) {
            return "redirect:/dashboard/events/";
        }
    }

    @GetMapping("/overview/{key}/")
    public String overview(Model model, @PathVariable String key) {
        try {
            Event event = eventService.getByKey(key);

            List<SoldProduct> soldProduct = new ArrayList<>();
            event.getProducts().forEach(x -> soldProduct.addAll(soldProductService.getByProduct(x)));

            model.addAttribute("event", event);
            model.addAttribute("soldProducts", soldProduct);

            return "dashboard/events/overview";
        } catch (EventNotFound e) {
            return "redirect:/dashboard/events/";
        }
    }

    /**
     * Get request to delete event by Key
     *
     * @param redirect Spring RedirectAttributes
     * @param key                PathVariable key of the Event
     * @return redirect
     */
    @GetMapping("/delete/{key}")
    public String deleteEvent(RedirectAttributes redirect, @PathVariable String key) {
        try {
            Event event = eventService.getByKey(key);
            eventService.delete(event);

            redirect.addFlashAttribute("message", "Event " + event.getTitle() + " has been deleted!");

            return "redirect:/dashboard/events/";
        } catch (EventNotFound e) {
            redirect.addFlashAttribute("message", "Event has not been deleted, because it does not exists!");

            return "redirect:/dashboard/events/";
        }
    }

    /**
     * Post request to create a new Event
     *
     * @param event    EventRequest model attr.
     * @param redirect Spring RedirectAttributes
     * @return redirect
     */
    @PostMapping("/create")
    public String create(RedirectAttributes redirect, @ModelAttribute Event event) {
        try {
            eventService.create(event);
            redirect.addFlashAttribute("message", event.getTitle() + " successfully created!");

            return "redirect:/dashboard/events/";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("event", event);

            return "redirect:/dashboard/events/create/";
        }
    }

    /**
     * Post request to delete a Product from an Event
     *
     * @param eventProductRequest EventProductRequest model attr.
     * @param redirectAttributes  Spring RedirectAttributes
     * @return redirect
     */
    @PostMapping("/product/delete")
    public String deleteProductFromEvent(@ModelAttribute @Validated EventProductRequest eventProductRequest,
                                         RedirectAttributes redirectAttributes) {
        eventService.deleteProductFromEvent(eventProductRequest.getEventID(), eventProductRequest.getProductID());
        redirectAttributes.addFlashAttribute("message", "Product removed from Event!");

        return "redirect:/dashboard/events/edit/" + eventProductRequest.getEventKey();
    }

    /**
     * Post request to update an Event
     *
     * @param event    EventRequest model attr.
     * @param redirect Spring RedirectAttributes
     * @return redirect
     */
    @PostMapping("/update")
    public String update(RedirectAttributes redirect, @ModelAttribute Event event) {
        try {
            eventService.update(event);
            redirect.addFlashAttribute("message", "Event changes saved!");

            return "redirect:/dashboard/events/edit/" + event.getKey() + "/";
        } catch (EventNotFound e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("event", event);

            return "redirect:/dashboard/events/edit/" + event.getKey() + "/";
        }
    }

}
