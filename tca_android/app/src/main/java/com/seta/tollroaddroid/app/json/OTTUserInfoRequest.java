package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-23.
 */
public class OTTUserInfoRequest implements java.io.Serializable {
/**
 *
 */
    private String plate1;
    private String state1;
    private String country1;

    private String vehicle_class;
    private String plate2;
    private String state2;
    private String country2;

    private String address1;
    private String address_first_name;
    private String address_last_name;
    private String address_city;
    private String address_state;
    private String address_zipcode;
    private String address_country;

    private String email_address;
    private Boolean email_receipt=true;

    private String rental_start_date;
    private String rental_end_date;

    private String calc_start_date;
    private String calc_end_date;

    private int calculate_toll_mode; //0: for me; 1: myself
    private int save_credit_card_option; //3: not save; 1: save for 30 days or less; 2: save forever
    private String save_start_date;
    private String save_end_date;

    private Integer payment_method;
    private String routing_number;
    private String account_number;
    private String first_name;
    private String last_name;
    private String card_number;
    private String expired_date;
    private String card_holder_name;
    private String zip_code;
    private String cvv2;

    //for register CHARGE_EXPRESS account
    private String account_username;
    private String account_password;
    private String confirm_password;
    private String primary_phone;
    private Boolean receive_promotion_material;
    private Boolean receive_road_alerts;

    private double amount =0.00;
    private double total_amount =0.00;
    private double totalAmount =0.00;
    private double cash_amount =0.00;

    public String getPlate1() {
        return plate1;
    }

    public void setPlate1(String plate1) {
        this.plate1 = plate1;
    }

    public String getState1() {
        return state1;
    }

    public void setState1(String state1) {
        this.state1 = state1;
    }

    public String getPlate2() {
        return plate2;
    }

    public void setPlate2(String plate2) {
        this.plate2 = plate2;
    }

    public String getState2() {
        return state2;
    }

    public void setState2(String state2) {
        this.state2 = state2;
    }

    public String getAddress_first_name() {
        return address_first_name;
    }

    public void setAddress_first_name(String address_first_name) {
        this.address_first_name = address_first_name;
    }

    public String getAddress_last_name() {
        return address_last_name;
    }

    public void setAddress_last_name(String address_last_name) {
        this.address_last_name = address_last_name;
    }

    public String getAddress_city() {
        return address_city;
    }

    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    public String getAddress_state() {
        return address_state;
    }

    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    public String getAddress_zipcode() {
        return address_zipcode;
    }

    public void setAddress_zipcode(String address_zipcode) {
        this.address_zipcode = address_zipcode;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public Boolean getEmail_receipt() {
        return email_receipt;
    }

    public void setEmail_receipt(Boolean email_receipt) {
        this.email_receipt = email_receipt;
    }

    public String getRental_start_date() {
        return rental_start_date;
    }

    public void setRental_start_date(String rental_start_date) {
        this.rental_start_date = rental_start_date;
    }

    public String getRental_end_date() {
        return rental_end_date;
    }

    public void setRental_end_date(String rental_end_date) {
        this.rental_end_date = rental_end_date;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress_country() {
        return address_country;
    }

    public void setAddress_country(String address_country) {
        this.address_country = address_country;
    }

    public String getExpired_date() {
        return expired_date;
    }

    public void setExpired_date(String expired_date) {
        this.expired_date = expired_date;
    }

    public Integer getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(Integer payment_method) {
        this.payment_method = payment_method;
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

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(String cvv2) {
        this.cvv2 = cvv2;
    }

    public String getCountry1() {
        return country1;
    }

    public void setCountry1(String country1) {
        this.country1 = country1;
    }

    public String getCountry2() {
        return country2;
    }

    public void setCountry2(String country2) {
        this.country2 = country2;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getVehicle_class() {
        return vehicle_class;
    }

    public void setVehicle_class(String vehicle_class) {
        this.vehicle_class = vehicle_class;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCalc_start_date() {
        return calc_start_date;
    }

    public void setCalc_start_date(String calc_start_date) {
        this.calc_start_date = calc_start_date;
    }

    public String getCalc_end_date() {
        return calc_end_date;
    }

    public void setCalc_end_date(String calc_end_date) {
        this.calc_end_date = calc_end_date;
    }

    public String getSave_start_date() {
        return save_start_date;
    }

    public void setSave_start_date(String save_start_date) {
        this.save_start_date = save_start_date;
    }

    public String getSave_end_date() {
        return save_end_date;
    }

    public void setSave_end_date(String save_end_date) {
        this.save_end_date = save_end_date;
    }

    public int getSave_credit_card_option() {
        return save_credit_card_option;
    }

    public void setSave_credit_card_option(int save_credit_card_option) {
        this.save_credit_card_option = save_credit_card_option;
    }

    public int getCalculate_toll_mode() {
        return calculate_toll_mode;
    }

    public void setCalculate_toll_mode(int calculate_toll_mode) {
        this.calculate_toll_mode = calculate_toll_mode;
    }

    public String getAccount_username() {
        return account_username;
    }

    public void setAccount_username(String account_username) {
        this.account_username = account_username;
    }

    public String getAccount_password() {
        return account_password;
    }

    public void setAccount_password(String account_password) {
        this.account_password = account_password;
    }

    public String getConfirm_password() {
        return confirm_password;
    }

    public void setConfirm_password(String confirm_password) {
        this.confirm_password = confirm_password;
    }

    public String getPrimary_phone() {
        return primary_phone;
    }

    public void setPrimary_phone(String primary_phone) {
        this.primary_phone = primary_phone;
    }

    public Boolean getReceive_promotion_material() {
        return receive_promotion_material;
    }

    public void setReceive_promotion_material(Boolean receive_promotion_material) {
        this.receive_promotion_material = receive_promotion_material;
    }

    public Boolean getReceive_road_alerts() {
        return receive_road_alerts;
    }

    public void setReceive_road_alerts(Boolean receive_road_alerts) {
        this.receive_road_alerts = receive_road_alerts;
    }

    public double getCash_amount() {
        return cash_amount;
    }

    public void setCash_amount(double cash_amount) {
        this.cash_amount = cash_amount;
    }
}
