package com.seta.tollroaddroid.app.json;

import java.io.Serializable;

/**
 * Created by thomashuang on 2018-04-03.
 */

public class PaySelectedViolationsRequest implements Serializable{
//    cvv2=132&first_name=&address_contact=mary smith
//    &expired_date=11/21&payment_type=6&primary_phone=1113332222&email_address=sallen@thetollroads.com
//    &email_address2=sallen@thetollroads.com&card_number=4111111111111111&zip_code=12121
//            &card_holder_name=mary smith&action=paySelVios&account_number=&last_name=&routing_number=
//            &vioIDs=420453655,420528730,422229139&primary_phone_ext=7692

    private int cvv2;
    private String address_contact;

    private Integer payment_type;
    private String routing_number;
    private String account_number;
    private String first_name;
    private String last_name;
    private String card_number;
    private String expired_date;
    private String card_holder_name;
    private String zip_code;

    private String primary_phone;
    private String primary_phone_ext;
    private String email_address;
    private String email_address2;

    private String vioIDs;

    private String payment_token;

    public int getCvv2() {
        return cvv2;
    }

    public void setCvv2(int cvv2) {
        this.cvv2 = cvv2;
    }

    public String getAddress_contact() {
        return address_contact;
    }

    public void setAddress_contact(String address_contact) {
        this.address_contact = address_contact;
    }

    public Integer getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(Integer payment_type) {
        this.payment_type = payment_type;
    }

    public String getRouting_number() {
        return routing_number;
    }

    public void setRouting_number(String routing_number) {
        this.routing_number = routing_number;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getExpired_date() {
        return expired_date;
    }

    public void setExpired_date(String expired_date) {
        this.expired_date = expired_date;
    }

    public String getCard_holder_name() {
        return card_holder_name;
    }

    public void setCard_holder_name(String card_holder_name) {
        this.card_holder_name = card_holder_name;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }

    public String getPrimary_phone() {
        return primary_phone;
    }

    public void setPrimary_phone(String primary_phone) {
        this.primary_phone = primary_phone;
    }

    public String getPrimary_phone_ext() {
        return primary_phone_ext;
    }

    public void setPrimary_phone_ext(String primary_phone_ext) {
        this.primary_phone_ext = primary_phone_ext;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getEmail_address2() {
        return email_address2;
    }

    public void setEmail_address2(String email_address2) {
        this.email_address2 = email_address2;
    }

    public String getVioIDs() {
        return vioIDs;
    }

    public void setVioIDs(String vioIDs) {
        this.vioIDs = vioIDs;
    }

    public String getPayment_token() {
        return payment_token;
    }

    public void setPayment_token(String payment_token) {
        this.payment_token = payment_token;
    }
}
