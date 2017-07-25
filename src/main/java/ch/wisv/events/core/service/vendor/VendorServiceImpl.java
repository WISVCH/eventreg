package ch.wisv.events.core.service.vendor;

import ch.wisv.events.core.exception.InvalidVendorException;
import ch.wisv.events.core.exception.VendorNotFoundException;
import ch.wisv.events.core.model.sales.Vendor;
import ch.wisv.events.core.repository.VendorRepository;
import ch.wisv.events.utils.LDAPGroupEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
public class VendorServiceImpl implements VendorService {

    /**
     * VendorRepository.
     */
    @Autowired
    private VendorRepository vendorRepository;

    /**
     * Constructor VendorServiceImpl creates a new VendorServiceImpl instance.
     */
    public VendorServiceImpl() {

    }

    /**
     * Method getAll returns the all of this VendorService object.
     *
     * @return the all (type List<Vendor>) of this VendorService object.
     */
    @Override
    public List<Vendor> getAll() {
        return vendorRepository.findAll();
    }

    /**
     * Method getAllByLDAPGroup get list of vendors by ldap group.
     *
     * @param ldapEnum of type LDAPGroupEnum
     * @return List<Vendor>
     */
    @Override
    public List<Vendor> getAllByLDAPGroup(String ldapEnum) {
        try {
            LDAPGroupEnum ldapGroupEnum = LDAPGroupEnum.valueOf(ldapEnum.toUpperCase());

            return vendorRepository.findByLdapGroup(ldapGroupEnum);
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Method getByKey will return a Vendor by its key.
     *
     * @param key of type String
     * @return Vendor
     */
    @Override
    public Vendor getByKey(String key) {
        Optional<Vendor> optional = vendorRepository.findByKey(key);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new VendorNotFoundException("Vendor with key " + key + " not found");
    }

    /**
     * Method create will create a new Vendor.
     *
     * @param vendor of type Vendor
     */
    @Override
    public void create(Vendor vendor) {
        this.checkRequiredFields(vendor);

        vendorRepository.saveAndFlush(vendor);
    }

    /**
     * Method update will update an existing Vendor.
     *
     * @param vendor of type Vendor
     */
    @Override
    public void update(Vendor vendor) {
        this.checkRequiredFields(vendor);
        Vendor model = this.getByKey(vendor.getKey());

        model.setLdapGroup(vendor.getLdapGroup());
        model.setStartingTime(vendor.getStartingTime());
        model.setEndingTime(vendor.getEndingTime());
        model.setEvents(vendor.getEvents());

        vendorRepository.save(model);
    }

    /**
     * Method delete will delete an existing Vendor.
     *
     * @param vendor of type Vendor
     */
    @Override
    public void delete(Vendor vendor) {
        vendorRepository.delete(vendor);
    }

    /**
     * Will check all the required fields if they are valid.
     *
     * @param model of type Vendor
     * @throws InvalidVendorException when one of the required fields is not valid
     */
    private void checkRequiredFields(Vendor model) throws InvalidVendorException {
        if (model == null) throw new InvalidVendorException("Vendor can not be null!");

        Object[][] check = new Object[][]{
                {model.getKey(), "key"},
                {model.getLdapGroup(), "ldap group"},
        };
        this.checkFieldsEmpty(check);
    }

    /**
     * Checks if the a field in the String[][] is empty. If so it will throw an exception
     *
     * @param fields of type Object[][]
     * @throws InvalidVendorException when one of the fields in empty
     */
    private void checkFieldsEmpty(Object[][] fields) throws InvalidVendorException {
        for (Object[] row : fields) {
            if (row[0] == null || row[0].toString().equals("")) {
                throw new InvalidVendorException(StringUtils.capitalize(row[1].toString()) + " is empty, but is a "
                        + "required field, so please fill in this field!");
            }
        }
    }

}
