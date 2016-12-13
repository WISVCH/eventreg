package ch.wisv.events.core.service.product;

import ch.wisv.events.core.exception.ProductInUseException;
import ch.wisv.events.core.exception.ProductNotFound;
import ch.wisv.events.core.model.event.Event;
import ch.wisv.events.core.model.product.Product;
import ch.wisv.events.core.repository.ProductRepository;
import ch.wisv.events.core.service.event.EventService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ProductServiceImpl.
 */
@Service
public class ProductServiceImpl implements ProductService {

    /**
     * ProductRepository
     */
    private final ProductRepository productRepository;

    /**
     * EventService
     */
    private final EventService eventService;

    /**
     * Default constructor
     *
     * @param productRepository ProductRepository
     * @param eventService      EventService
     */
    public ProductServiceImpl(ProductRepository productRepository, EventService eventService) {
        this.productRepository = productRepository;
        this.eventService = eventService;
    }

    /**
     * Get all products
     *
     * @return List of Products
     */
    @Override
    public List<Product> getAllProducts() {
        return this.productRepository.findAll();
    }

    /**
     * Get all available products
     *
     * @return Collection of Products
     */
    @Override
    public List<Product> getAvailableProducts() {
        return productRepository.findAllBySellStartBeforeAndSellEndAfter(LocalDateTime.now(), LocalDateTime.now())
                                .stream().filter(x -> x.getSold() < x.getMaxSold())
                                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get Product by Key
     *
     * @param key key of a Product
     * @return Product
     */
    @Override
    public Product getByKey(String key) {
        Optional<Product> product = productRepository.findByKey(key);
        if (product.isPresent()) {
            return product.get();
        }
        throw new ProductNotFound("Product with key " + key + " not found!");
    }

    /**
     * Update Product using a Product
     *
     * @param product Product containing the new product information
     */
    @Override
    public void update(Product product) {
        productRepository.save(product);
    }

    /**
     * Add a new Product using a Product
     *
     * @param product of type Product
     */
    @Override
    public void add(Product product) {
        productRepository.saveAndFlush(product);
    }

    /**
     * Remove a Product
     *
     * @param product Product to be deleted.
     */
    @Override
    public void delete(Product product) {
        List<Event> events = eventService.getEventByProductKey(product.getKey());
        if (events.size() > 0) {
            throw new ProductInUseException("Product is already added to an Event");
        }
        productRepository.delete(product);
    }

}
