package ch.wisv.events.core.service.webhook;

import ch.wisv.events.core.exception.InvalidWebhookException;
import ch.wisv.events.core.model.webhook.Webhook;
import ch.wisv.events.core.repository.WebhookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Copyright (c) 2016  W.I.S.V. 'Christiaan Huygens'
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@Service
public class WebhookServiceImpl implements WebhookService {

    /**
     * Field repository
     */
    private final WebhookRepository repository;

    /**
     * Constructor WebhookServiceImpl creates a new WebhookServiceImpl instance.
     *
     * @param repository of type WebhookRepository
     */
    @Autowired
    public WebhookServiceImpl(WebhookRepository repository) {
        this.repository = repository;
    }

    /**
     * Method getAll returns the all of this WebhookService object.
     *
     * @return the all (type List<Webhook>) of this WebhookService object.
     */
    @Override
    public List<Webhook> getAll() {
        return repository.findAll();
    }

    /**
     * Method create ...
     *
     * @param model of type Webhook
     */
    @Override
    public void create(Webhook model) throws InvalidWebhookException {
        if (model.getPayloadUrl() == null || model.getPayloadUrl().equals("")) {
            throw new InvalidWebhookException();
        } else {
            repository.saveAndFlush(model);
        }
    }
}