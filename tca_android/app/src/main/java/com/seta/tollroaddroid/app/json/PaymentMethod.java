package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-14.
 * "paymethod_id": 888187,
 "payment_type": 4,
 "payment_order": 1,
 "card_number": "xxxxxxxxxxxx9018",
 "expired_date": "04/17",
 "card_holder_name": "DAVID T STEWART",
 "zip_code": "92619",
 "routing_number": "",
 "account_number": "",
 "first_name": "",
 "last_name": ""
 */
public class PaymentMethod implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String paymethod_id;
    private int payment_type;
    private int payment_order;
    private String card_number;
    private String expired_date;
    private String card_holder_name;
    private String zip_code;
    private String routing_number;
    private String account_number;
    private String first_name;
    private String last_name;

    public String getPaymethod_id() {
        return paymethod_id;
    }

    public void setPaymethod_id(String paymethod_id) {
        this.paymethod_id = paymethod_id;
    }

    public int getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(int payment_type) {
        this.payment_type = payment_type;
    }

    public int getPayment_order() {
        return payment_order;
    }

    public void setPayment_order(int payment_order) {
        this.payment_order = payment_order;
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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

}
