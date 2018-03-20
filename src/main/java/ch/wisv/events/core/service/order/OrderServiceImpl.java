package ch.wisv.events.core.service.order;

import ch.wisv.events.core.exception.normal.EventsException;
import ch.wisv.events.core.exception.normal.OrderInvalidException;
import ch.wisv.events.core.exception.normal.OrderNotFoundException;
import ch.wisv.events.core.exception.normal.ProductInvalidException;
import ch.wisv.events.core.exception.normal.ProductNotFoundException;
import ch.wisv.events.core.model.customer.Customer;
import ch.wisv.events.core.model.order.Order;
import ch.wisv.events.core.model.order.OrderProduct;
import ch.wisv.events.core.model.order.OrderProductDto;
import ch.wisv.events.core.model.order.OrderStatus;
import static ch.wisv.events.core.model.order.OrderStatus.ANONYMOUS;
import static ch.wisv.events.core.model.order.OrderStatus.ASSIGNED;
import static ch.wisv.events.core.model.order.OrderStatus.CANCELLED;
import static ch.wisv.events.core.model.order.OrderStatus.ERROR;
import static ch.wisv.events.core.model.order.OrderStatus.EXPIRED;
import static ch.wisv.events.core.model.order.OrderStatus.PAID;
import static ch.wisv.events.core.model.order.OrderStatus.PENDING;
import static ch.wisv.events.core.model.order.OrderStatus.REJECTED;
import static ch.wisv.events.core.model.order.OrderStatus.RESERVATION;
import ch.wisv.events.core.model.product.Product;
import ch.wisv.events.core.model.ticket.Ticket;
import ch.wisv.events.core.repository.OrderProductRepository;
import ch.wisv.events.core.repository.OrderRepository;
import ch.wisv.events.core.service.mail.MailService;
import ch.wisv.events.core.service.product.ProductService;
import ch.wisv.events.core.service.ticket.TicketService;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * OrderServiceImpl class.
 */
@Service
public class OrderServiceImpl implements OrderService {

    /** OrderRepository. */
    private final OrderRepository orderRepository;

    /** OrderProductRepository. */
    private final OrderProductRepository orderProductRepository;

    /** ProductService. */
    private final ProductService productService;

    /** MailService. */
    private final MailService mailService;

    /** TicketService. */
    private final TicketService ticketService;

    /**
     * Constructor OrderServiceImpl creates a new OrderServiceImpl instance.
     *
     * @param orderRepository        of type OrderRepository
     * @param orderProductRepository of type OrderProductRepository
     * @param productService         of type ProductService
     * @param mailService            of type MailService
     * @param ticketService          of type TicketService
     */
    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderProductRepository orderProductRepository,
            ProductService productService,
            MailService mailService,
            TicketService ticketService
    ) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productService = productService;
        this.mailService = mailService;
        this.ticketService = ticketService;
    }

    /**
     * Method getAllOrders returns the allOrders of this OrderService object.
     *
     * @return List of Orders.
     */
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Method getByReference returns Order with the given Reference.
     *
     * @param reference of type String
     *
     * @return Order
     */
    @Override
    public Order getByReference(String reference) throws OrderNotFoundException {
        return orderRepository.findOneByPublicReference(reference).orElseThrow(() -> new OrderNotFoundException("reference " + reference));
    }

    /**
     * Create an Order form a OrderProductDto.
     *
     * @param orderProductDto of type OrderProductDto
     *
     * @return Order
     */
    @Override
    public Order createOrderByOrderProductDto(OrderProductDto orderProductDto) throws ProductNotFoundException {
        Order order = new Order();

        for (Map.Entry<String, Long> values : orderProductDto.getProducts().entrySet()) {
            if (values.getValue() > 0) {
                Product product = productService.getByKey(values.getKey());
                order.addOrderProduct(new OrderProduct(product, product.getCost(), values.getValue()));
            }
        }

        return order;
    }

    /**
     * Create and save and Order.
     *
     * @param order of type Order
     */
    @Override
    public void create(Order order) throws OrderInvalidException {
        if (order.getOrderProducts() == null) {
            throw new OrderInvalidException("Order should contain a list of OrderProducts");
        }

        order.getOrderProducts().forEach(orderProductRepository::saveAndFlush);
        order.updateOrderAmount();

        orderRepository.saveAndFlush(order);
    }

    /**
     * Update an Order.
     *
     * @param order of type Order
     *
     * @throws OrderNotFoundException when Order is not found.
     * @throws OrderInvalidException  when Order is invalid to update.
     */
    @Override
    public void update(Order order) throws OrderNotFoundException, OrderInvalidException {
        if (order.getPublicReference() == null) {
            throw new OrderInvalidException("Order should contain a public reference before updating.");
        }

        Order old = this.getByReference(order.getPublicReference());
        old.setOwner(order.getOwner());
        old.setPaymentMethod(order.getPaymentMethod());
        old.updateOrderAmount();

        orderRepository.saveAndFlush(order);
    }

    /**
     * Update OrderStatus of an Order.
     *
     * @param order  of type Order
     * @param status of type OrderStatus
     */
    @Override
    public void updateOrderStatus(Order order, OrderStatus status) throws EventsException {
        HashMap<OrderStatus, List<OrderStatus>> allowedChanges = new HashMap<OrderStatus, List<OrderStatus>>() {{
            put(ANONYMOUS, ImmutableList.of(ASSIGNED, CANCELLED));
            put(ASSIGNED, ImmutableList.of(PENDING, CANCELLED, RESERVATION));
            put(CANCELLED, ImmutableList.of(ASSIGNED, CANCELLED));
            put(PENDING, ImmutableList.of(PAID, PENDING, ASSIGNED, ERROR, CANCELLED, EXPIRED));
            put(RESERVATION, ImmutableList.of(PAID, EXPIRED, REJECTED));

            put(PAID, ImmutableList.of(REJECTED));
            put(ERROR, ImmutableList.of());
            put(REJECTED, ImmutableList.of());
            put(EXPIRED, ImmutableList.of());
        }};

        if (!allowedChanges.get(order.getStatus()).contains(status)) {
            throw new OrderInvalidException("Not allowed to update status from " + order.getStatus() + " to " + status);
        }

        if (status == PAID) {
            this.updateOrderStatusPaid(order);
        } else if (status == RESERVATION) {
            this.updateOrderStatusReservation(order);
        }

        order.setStatus(status);
        orderRepository.saveAndFlush(order);
    }

    /**
     * Get all reservation Order by a Customer.
     *
     * @param customer of type Customer.
     *
     * @return List of Orders
     */
    @Override
    public List<Order> getAllReservationOrderByCustomer(Customer customer) {
        return orderRepository.findAllByOwnerAndStatus(customer, RESERVATION);
    }

    /**
     * Create the ticket if an order has the status paid.
     *
     * @param order of type Order.
     *
     * @return List of Ticket
     */
    private List<Ticket> createTicketForOrder(Order order) {
        return order.getOrderProducts()
                .stream()
                .map(orderProduct -> ticketService.createByOrderProduct(order, orderProduct))
                .collect(Collectors.toList());
    }

    /**
     * Update order status to PAID.
     *
     * @param order of type Order
     */
    private void updateOrderStatusPaid(Order order) {
        List<Ticket> tickets = this.createTicketForOrder(order);
        mailService.sendOrderConfirmation(order, tickets);

        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        this.updateProductSoldCount(order);
    }

    /**
     * Update order status to reservation.
     *
     * @param order of type Order
     */
    private void updateOrderStatusReservation(Order order) {
        mailService.sendOrderReservation(order);

        this.updateProductReservedCount(order);
    }

    /**
     * Update product reservation count.
     *
     * @param order of type Order
     */
    private void updateProductReservedCount(Order order) {
        order.getOrderProducts().forEach(orderProduct -> {
            orderProduct.getProduct().increaseReserved(orderProduct.getAmount().intValue());
            try {
                productService.update(orderProduct.getProduct());
            } catch (ProductNotFoundException | ProductInvalidException ignored) {
            }
        });
    }

    /**
     * Update product sold count.
     *
     * @param order of type Order
     */
    private void updateProductSoldCount(Order order) {
        order.getOrderProducts().forEach(orderProduct -> {
            orderProduct.getProduct().increaseSold(orderProduct.getAmount().intValue());
            try {
                productService.update(orderProduct.getProduct());
            } catch (ProductNotFoundException | ProductInvalidException ignored) {
            }
        });
    }
}
