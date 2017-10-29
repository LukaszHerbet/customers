package com.herbet.ffm.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

// extracted from Customer during basic normalization

/**
 * Entity representing address.
 */
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String street;

    private String postcode;

    public Address() {
        super();
    }

    public Address(String street, String postcode) {
        super();
        this.street = street;
        this.postcode = postcode;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(final String postcode) {
        this.postcode = postcode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("street", street).append("postcode", postcode)
                .toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param addressToCompare the reference address with which to compare.
     * @return true if this object has equal street and postcode as the addressToCompare argument; false otherwise.
     */
    public boolean isSameLocation(Address addressToCompare) {
        boolean isSameLocation = false;

        if (addressToCompare != null) {
            isSameLocation = StringUtils.equals(this.getStreet(), addressToCompare.getStreet()) && StringUtils.equals(
                    this.getPostcode(), addressToCompare.getPostcode());
        }

        return isSameLocation;
    }
}
