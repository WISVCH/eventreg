package ch.wisv.events.core.service.webhook;

import ch.wisv.events.core.exception.InvalidWebhookException;
import ch.wisv.events.core.exception.WebhookNotFoundException;
import ch.wisv.events.core.model.webhook.Webhook;
import ch.wisv.events.core.model.webhook.WebhookTrigger;

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
public interface WebhookService {

    /**
     * Method getAll returns the all of this WebhookService object.
     *
     * @return the all (type List<Webhook>) of this WebhookService object.
     */
    List<Webhook> getAll();

    /**
     * Method getByKey get Webhook by Key.
     *
     * @param key of type String
     * @return Webhook
     */
    Webhook getByKey(String key) throws WebhookNotFoundException;

    /**
     * Method getByTriggerAndLdapGroup ...
     *
     * @param webhookTrigger of type WebhookTrigger
     * @return List<Webhook>
     */
    List<Webhook> getByTrigger(WebhookTrigger webhookTrigger);

    /**
     * Method create a new Webhook.
     *
     * @param model of type Webhook
     */
    void create(Webhook model) throws InvalidWebhookException;

    /**
     * Method update an existing Webhook.
     *
     * @param model of type Webhook
     */
    void update(Webhook model) throws WebhookNotFoundException, InvalidWebhookException;


    /**
     * Method delete an existing Webhook.
     *
     * @param model of type Webhook
     */
    void delete(Webhook model);
}
