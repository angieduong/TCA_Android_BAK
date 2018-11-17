package com.seta.tollroaddroid.app.json;

import java.util.ArrayList;

/**
 * Created by thomashuang on 16-03-02.
 * "account_type": 1,
 "full_name": "STEWART DAVE",
 "balance": 148.31,
 "balance2": 148.31,
 "address1": "PO BOX 53024",
 "address2": "",
 "address3": "",
 "address_city": "IRVINE",
 "zipcode": "92619",
 "address_state": "CA",
 "address_country": "",

 primary_phone - String - 18 characters
 secondary_phone - String - 18 characters
 other_phone - String - 18 characters

 primary_receive_text_messages - boolean
 secondary_receive_text_messages - boolean
 other_receive_text_messages - boolean

 "email_address": "DAVE@FORVOICEOVER.COM",
 "statement_delivery_method": 1,
 "close_date": "",
 "receive_promotion_material": true
 */
public class AccountInfo {
    private int account_type;
    private String full_name;
    private double balance;
    private double balance2;
    private String address1;
    private String address2;
    private String address3;
    private String address_city;
    private String zipcode;
    private String address_state;
    private String address_country;
    private String address_contact;
    private String primary_phone;
    private String secondary_phone;
    private String other_phone;
    private String email_address;
    private String close_date;
    private boolean primary_receive_text_messages;
    private boolean secondary_receive_text_messages;
    private boolean other_receive_text_messages;
    private boolean receive_promotion_material;
    private int statement_delivery_method;
    private boolean receive_road_alerts;

    private ArrayList<AlertBanner> alert_list = new ArrayList<>();

    public String getPrimary_phone() {
        return primary_phone;
    }

    public void setPrimary_phone(String primary_phone) {
        this.primary_phone = primary_phone;
    }

    public int getAccount_type() {
        return account_type;
    }

    public void setAccount_type(int account_type) {
        this.account_type = account_type;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getBalance2() {
        return balance2;
    }

    public void setBalance2(double balance2) {
        this.balance2 = balance2;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getAddress_city() {
        return address_city;
    }

    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getAddress_state() {
        return address_state;
    }

    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    public String getAddress_country() {
        return address_country;
    }

    public void setAddress_country(String address_country) {
        this.address_country = address_country;
    }

    public String getSecondary_phone() {
        return secondary_phone;
    }

    public void setSecondary_phone(String secondary_phone) {
        this.secondary_phone = secondary_phone;
    }

    public String getOther_phone() {
        return other_phone;
    }

    public void setOther_phone(String other_phone) {
        this.other_phone = other_phone;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getClose_date() {
        return close_date;
    }

    public void setClose_date(String close_date) {
        this.close_date = close_date;
    }

    public boolean isPrimary_receive_text_messages() {
        return primary_receive_text_messages;
    }

    public void setPrimary_receive_text_messages(boolean primary_receive_text_messages) {
        this.primary_receive_text_messages = primary_receive_text_messages;
    }

    public boolean isSecondary_receive_text_messages() {
        return secondary_receive_text_messages;
    }

    public void setSecondary_receive_text_messages(boolean secondary_receive_text_messages) {
        this.secondary_receive_text_messages = secondary_receive_text_messages;
    }

    public boolean isOther_receive_text_messages() {
        return other_receive_text_messages;
    }

    public void setOther_receive_text_messages(boolean other_receive_text_messages) {
        this.other_receive_text_messages = other_receive_text_messages;
    }

    public boolean isReceive_promotion_material() {
        return receive_promotion_material;
    }

    public void setReceive_promotion_material(boolean receive_promotion_material) {
        this.receive_promotion_material = receive_promotion_material;
    }

    public int getStatement_delivery_method() {
        return statement_delivery_method;
    }

    public void setStatement_delivery_method(int statement_delivery_method) {
        this.statement_delivery_method = statement_delivery_method;
    }

    public ArrayList<AlertBanner> getAlert_list() {
        return alert_list;
    }

    public void setAlert_list(ArrayList<AlertBanner> alert_list) {
        this.alert_list = alert_list;
    }

    public boolean isReceive_road_alerts() {
        return receive_road_alerts;
    }

    public void setReceive_road_alerts(boolean receive_road_alerts) {
        this.receive_road_alerts = receive_road_alerts;
    }

    public String getAddress_contact() {
        return address_contact;
    }

    public void setAddress_contact(String address_contact) {
        this.address_contact = address_contact;
    }
}
