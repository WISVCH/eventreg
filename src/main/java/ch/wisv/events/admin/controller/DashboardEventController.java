package ch.wisv.events.admin.controller;

import ch.wisv.events.core.exception.normal.EventInvalidException;
import ch.wisv.events.core.exception.normal.EventNotFoundException;
import ch.wisv.events.core.model.document.Document;
import ch.wisv.events.core.model.event.Event;
import ch.wisv.events.core.model.event.EventStatus;
import ch.wisv.events.core.model.webhook.WebhookTrigger;
import ch.wisv.events.core.service.document.DocumentService;
import ch.wisv.events.core.service.event.EventService;
import ch.wisv.events.core.service.ticket.TicketService;
import ch.wisv.events.core.webhook.WebhookPublisher;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * DashboardEventController class.
 */
@Controller
@RequestMapping(value = "/administrator/events")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardEventController {

    /** EventService. */
    private final EventService eventService;

    /** TicketService. */
    private final TicketService ticketService;

    /** WebhookPublisher. */
    private final WebhookPublisher webhookPublisher;

    /** DocumentService. */
    private final DocumentService documentService;

    /**
     * DashboardEventController constructor.
     *
     * @param eventService     of type EventService
     * @param ticketService    of type TicketService
     * @param webhookPublisher of type WebhookPublisher
     * @param documentService  of type DocumentService
     */
    public DashboardEventController(
            EventService eventService,
            TicketService ticketService,
            WebhookPublisher webhookPublisher,
            DocumentService documentService
    ) {
        this.eventService = eventService;
        this.ticketService = ticketService;
        this.webhookPublisher = webhookPublisher;
        this.documentService = documentService;
    }

    /**
     * Get request on "/admin/events/" will show overview of all Events.
     *
     * @param model of type Model
     *
     * @return path to Thymeleaf template
     */
    @GetMapping()
    public String index(Model model) {
        model.addAttribute("events", eventService.getAll());

        return "admin/events/index";
    }

    /**
     * Get request on "/admin/events/view/{key}" will show page to view an Event.
     *
     * @param model    of type Model
     * @param redirect of type RedirectAttributes
     * @param key      of type String
     *
     * @return String
     */
    @GetMapping("/view/{key}")
    public String view(Model model, RedirectAttributes redirect, @PathVariable String key) {
        try {
            model.addAttribute("event", eventService.getByKey(key));

            return "admin/events/view";
        } catch (EventNotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());

            return "redirect:/administrator/events/";
        }
    }

    /**
     * Get request on "/admin/events/create/" will show page to create Event.
     *
     * @param model of type Model
     *
     * @return path to Thymeleaf template
     */
    @GetMapping("/create")
    public String create(Model model) {
        if (!model.containsAttribute("event")) {
            model.addAttribute("event", new Event());
        }

        return "admin/events/event";
    }

    /**
     * Post request to create a new Event.
     *
     * @param redirect of type RedirectAttributes
     * @param event    of type Event.
     *
     * @return redirect
     */
    @PostMapping("/create")
    public String create(RedirectAttributes redirect, @ModelAttribute Event event, @RequestParam("file") MultipartFile file) {
        try {
            if (file != null) {
                eventService.addDocumentImage(event, documentService.storeDocument(file));
            }
            eventService.create(event);
            redirect.addFlashAttribute("success", event.getTitle() + " successfully created!");

            if (event.getPublished() == EventStatus.PUBLISHED) {
                this.webhookPublisher.createWebhookTask(WebhookTrigger.EVENT_CREATE_UPDATE, event);
            }

            return "redirect:/administrator/events/";
        } catch (EventInvalidException | IOException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("event", event);

            return "redirect:/administrator/events/create/";
        }
    }

    /**
     * Get request on "/admin/events/edit/{key}" will show the edit page to edit Event with requested key.
     *
     * @param model of type Model
     * @param key   of type String
     *
     * @return path to Thymeleaf template
     */
    @GetMapping("/edit/{key}")
    public String edit(Model model, @PathVariable String key) {
        try {
            if (!model.containsAttribute("event")) {
                model.addAttribute("event", eventService.getByKey(key));
            }

            return "admin/events/event";
        } catch (EventNotFoundException e) {
            return "redirect:/administrator/events/";
        }
    }

    /**
     * Post request to update an Event.
     *
     * @param redirect of type RedirectAttributes
     * @param event    of type Event
     * @param key      of type String
     * @param file     of type MultipartFile
     *
     * @return redirect
     */
    @PostMapping("/edit/{key}")
    public String update(
            RedirectAttributes redirect,
            @ModelAttribute Event event,
            @PathVariable String key,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file != null) {
                eventService.addDocumentImage(event, documentService.storeDocument(file));
            }
            event.setKey(key);
            eventService.update(event);
            redirect.addFlashAttribute("success", "Event changes saved!");

            if (event.getPublished() == EventStatus.PUBLISHED) {
                this.webhookPublisher.createWebhookTask(WebhookTrigger.EVENT_CREATE_UPDATE, event);
            } else {
                this.webhookPublisher.createWebhookTask(WebhookTrigger.EVENT_DELETE, event);
            }

            return "redirect:/administrator/events/view/" + event.getKey() + "/";
        } catch (EventNotFoundException | EventInvalidException | IOException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("event", event);

            return "redirect:/administrator/events/edit/" + event.getKey() + "/";
        }
    }

    /**
     * Method overview ...
     *
     * @param model of type Model
     * @param key   of type String
     *
     * @return String
     */
    @GetMapping("/overview/{key}")
    public String overview(Model model, @PathVariable String key) {
        try {
            Event event = eventService.getByKey(key);

            model.addAttribute("event", event);
            model.addAttribute(
                    "tickets",
                    event.getProducts().stream().flatMap(product -> ticketService.getAllByProduct(product).stream()).collect(Collectors.toList())
            );

            return "admin/events/overview";
        } catch (EventNotFoundException e) {
            return "redirect:/administrator/events/";
        }
    }

    /**
     * Get request to delete event by Key.
     *
     * @param redirect of type RedirectAttributes
     * @param key      PathVariable key of the Event
     *
     * @return redirect
     */
    @GetMapping("/delete/{key}")
    public String deleteEvent(RedirectAttributes redirect, @PathVariable String key) {
        try {
            Event event = eventService.getByKey(key);
            eventService.delete(event);
            webhookPublisher.createWebhookTask(WebhookTrigger.EVENT_DELETE, event);
            redirect.addFlashAttribute("message", "Event " + event.getTitle() + " has been deleted!");

            return "redirect:/administrator/events/";
        } catch (EventNotFoundException e) {
            redirect.addFlashAttribute("message", "Event has not been deleted, because it does not exists!");

            return "redirect:/administrator/events/";
        }
    }
}
