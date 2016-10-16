package ch.wisv.events.event.service;

import ch.wisv.events.event.model.Ticket;
import ch.wisv.events.event.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sven on 14/10/2016.
 */
@Service
public class TicketServiceImpl implements TicketService{

    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<Ticket> getAllTickets() {
        return this.ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketByKey(String ticketKey) {
        return this.ticketRepository.findByKey(ticketKey);
    }
}