package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-20.
 */
public class SignUpRequest implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Integer account_type;
    private String tokenID;
    private String uniqueID;
    private String address_first_name;
    private String address_middle_name;
    private String address_last_name;
    private String address1;
    private String address2;
    private String address_city;
    private String address_state;
    private String address_zipcode;
    private String address_contact;
    private String address_country;
    private String primary_phone;
    private Boolean primary_receive_text_messages;
    private String secondary_phone;
    private Boolean secondary_receive_text_messages;
    private String other_phone;
    private String email_address;
    private Integer statement_delivery_method = 1;
    private Integer statement_delivery_day;
    private Boolean receive_promotion_material;
    private Boolean receive_road_alerts;
    private String promotion_code;
    private String account_username;
    private String account_password;
    private String confirm_password;
    private String account_pin;
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
    private Integer payment_method2;
    private String routing_number2;
    private String account_number2;
    private String first_name2;
    private String last_name2;
    private String card_number2;
    private String expired_date2;
    private String card_holder_name2;
    private String zip_code2;
    private String cvv22;
    private Integer payment_method3;
    private String routing_number3;
    private String account_number3;
    private String first_name3;
    private String last_name3;
    private String card_number3;
    private String expired_date3;
    private String card_holder_name3;
    private String zip_code3;
    private String cvv23;
    private String replenishment_amt;
    private Integer vehicle_type1;
    private String plate1;
    private String state1;
    private String year1;
    private String make1;
    private String model1;
    private String color1;
    private String start_date1;
    private String end_date1;
    private String country1;

    private Integer vehicle_type2;
    private String plate2;
    private String state2;
    private String year2;
    private String make2;
    private String model2;
    private String color2;
    private String start_date2;
    private String end_date2;
    private String country2;

    private Integer vehicle_type3;
    private String plate3;
    private String state3;
    private String year3;
    private String make3;
    private String model3;
    private String color3;
    private String start_date3;
    private String end_date3;
    private String country3;

    private Integer vehicle_type4;
    private String plate4;
    private String state4;
    private String year4;
    private String make4;
    private String model4;
    private String color4;
    private String start_date4;
    private String end_date4;
    private String country4;

    private Integer vehicle_type5;
    private String plate5;
    private String state5;
    private String year5;
    private String make5;
    private String model5;
    private String color5;
    private String start_date5;
    private String end_date5;
    private String country5;

    private String secQuestion1ID;
    private String secAnswer1;
    private String secQuestion2ID;
    private String secAnswer2;
    private String secQuestion3ID;
    private String secAnswer3;

    private Integer tag_request = 0;//request transponder num
    private Integer tag_type = 0;//request transponder type
    private String action;

    public Integer getAccount_type() {
        return account_type;
    }

    public void setAccount_type(Integer account_type) {
        this.account_type = account_type;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getAddress_first_name() {
        return address_first_name;
    }

    public void setAddress_first_name(String address_first_name) {
        this.address_first_name = address_first_name;
    }

    public String getAddress_middle_name() {
        return address_middle_name;
    }

    public void setAddress_middle_name(String address_middle_name) {
        this.address_middle_name = address_middle_name;
    }

    public String getAddress_last_name() {
        return address_last_name;
    }

    public void setAddress_last_name(String address_last_name) {
        this.address_last_name = address_last_name;
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

    public String getAddress_city() {
        return address_city;
    }

    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    public String getAddress_state() {
//        if(address_state != null && address_state.equals("US GOVT")) {
//            return "US";
//        }
//        else {
            return address_state;
        //}
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

    public String getAddress_contact() {
        return address_contact;
    }

    public void setAddress_contact(String address_contact) {
        this.address_contact = address_contact;
    }

    public String getPrimary_phone() {
        return primary_phone;
    }

    public void setPrimary_phone(String primary_phone) {
        this.primary_phone = primary_phone;
    }

    public Boolean isPrimary_receive_text_messages() {
        return primary_receive_text_messages;
    }

    public void setPrimary_receive_text_messages(Boolean primary_receive_text_messages) {
        this.primary_receive_text_messages = primary_receive_text_messages;
    }

    public String getSecondary_phone() {
        return secondary_phone;
    }

    public void setSecondary_phone(String secondary_phone) {
        this.secondary_phone = secondary_phone;
    }

    public Boolean isSecondary_receive_text_messages() {
        return secondary_receive_text_messages;
    }

    public void setSecondary_receive_text_messages(Boolean secondary_receive_text_messages) {
        this.secondary_receive_text_messages = secondary_receive_text_messages;
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

    public Integer getStatement_delivery_method() {
        return statement_delivery_method;
    }

    public void setStatement_delivery_method(Integer statement_delivery_method) {
        this.statement_delivery_method = statement_delivery_method;
    }

    public Integer getStatement_delivery_day() {
        return statement_delivery_day;
    }

    public void setStatement_delivery_day(Integer statement_delivery_day) {
        this.statement_delivery_day = statement_delivery_day;
    }

    public Boolean isReceive_promotion_material() {
        return receive_promotion_material;
    }

    public void setReceive_promotion_material(Boolean receive_promotion_material) {
        this.receive_promotion_material = receive_promotion_material;
    }

    public String getPromotion_code() {
        return promotion_code;
    }

    public void setPromotion_code(String promotion_code) {
        this.promotion_code = promotion_code;
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

    public String getAccount_pin() {
        return account_pin;
    }

    public void setAccount_pin(String account_pin) {
        this.account_pin = account_pin;
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

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(String cvv2) {
        this.cvv2 = cvv2;
    }

    public Integer getPayment_method2() {
        return payment_method2;
    }

    public void setPayment_method2(Integer payment_method2) {
        this.payment_method2 = payment_method2;
    }

    public String getRouting_number2() {
        return routing_number2;
    }

    public void setRouting_number2(String routing_number2) {
        this.routing_number2 = routing_number2;
    }

    public String getAccount_number2() {
        return account_number2;
    }

    public void setAccount_number2(String account_number2) {
        this.account_number2 = account_number2;
    }

    public String getFirst_name2() {
        return first_name2;
    }

    public void setFirst_name2(String first_name2) {
        this.first_name2 = first_name2;
    }

    public String getLast_name2() {
        return last_name2;
    }

    public void setLast_name2(String last_name2) {
        this.last_name2 = last_name2;
    }

    public String getCard_number2() {
        return card_number2;
    }

    public void setCard_number2(String card_number2) {
        this.card_number2 = card_number2;
    }

    public String getExpired_date2() {
        return expired_date2;
    }

    public void setExpired_date2(String expired_date2) {
        this.expired_date2 = expired_date2;
    }

    public String getCard_holder_name2() {
        return card_holder_name2;
    }

    public void setCard_holder_name2(String card_holder_name2) {
        this.card_holder_name2 = card_holder_name2;
    }

    public String getZip_code2() {
        return zip_code2;
    }

    public void setZip_code2(String zip_code2) {
        this.zip_code2 = zip_code2;
    }

    public String getCvv22() {
        return cvv22;
    }

    public void setCvv22(String cvv22) {
        this.cvv22 = cvv22;
    }

    public Integer getPayment_method3() {
        return payment_method3;
    }

    public void setPayment_method3(Integer payment_method3) {
        this.payment_method3 = payment_method3;
    }

    public String getRouting_number3() {
        return routing_number3;
    }

    public void setRouting_number3(String routing_number3) {
        this.routing_number3 = routing_number3;
    }

    public String getAccount_number3() {
        return account_number3;
    }

    public void setAccount_number3(String account_number3) {
        this.account_number3 = account_number3;
    }

    public String getFirst_name3() {
        return first_name3;
    }

    public void setFirst_name3(String first_name3) {
        this.first_name3 = first_name3;
    }

    public String getLast_name3() {
        return last_name3;
    }

    public void setLast_name3(String last_name3) {
        this.last_name3 = last_name3;
    }

    public String getCard_number3() {
        return card_number3;
    }

    public void setCard_number3(String card_number3) {
        this.card_number3 = card_number3;
    }

    public String getExpired_date3() {
        return expired_date3;
    }

    public void setExpired_date3(String expired_date3) {
        this.expired_date3 = expired_date3;
    }

    public String getCard_holder_name3() {
        return card_holder_name3;
    }

    public void setCard_holder_name3(String card_holder_name3) {
        this.card_holder_name3 = card_holder_name3;
    }

    public String getZip_code3() {
        return zip_code3;
    }

    public void setZip_code3(String zip_code3) {
        this.zip_code3 = zip_code3;
    }

    public String getCvv23() {
        return cvv23;
    }

    public void setCvv23(String cvv23) {
        this.cvv23 = cvv23;
    }

    public String getReplenishment_amt() {
        return replenishment_amt;
    }

    public void setReplenishment_amt(String replenishment_amt) {
        this.replenishment_amt = replenishment_amt;
    }

    public Integer getVehicle_type1() {
        return vehicle_type1;
    }

    public void setVehicle_type1(Integer vehicle_type1) {
        this.vehicle_type1 = vehicle_type1;
    }

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

    public String getYear1() {
        return year1;
    }

    public void setYear1(String year1) {
        this.year1 = year1;
    }

    public String getMake1() {
        return make1;
    }

    public void setMake1(String make1) {
        this.make1 = make1;
    }

    public String getModel1() {
        return model1;
    }

    public void setModel1(String model1) {
        this.model1 = model1;
    }

    public String getColor1() {
        return color1;
    }

    public void setColor1(String color1) {
        this.color1 = color1;
    }

    public String getStart_date1() {
        return start_date1;
    }

    public void setStart_date1(String start_date1) {
        this.start_date1 = start_date1;
    }

    public String getEnd_date1() {
        return end_date1;
    }

    public void setEnd_date1(String end_date1) {
        this.end_date1 = end_date1;
    }

    public Integer getVehicle_type2() {
        return vehicle_type2;
    }

    public void setVehicle_type2(Integer vehicle_type2) {
        this.vehicle_type2 = vehicle_type2;
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

    public String getYear2() {
        return year2;
    }

    public void setYear2(String year2) {
        this.year2 = year2;
    }

    public String getMake2() {
        return make2;
    }

    public void setMake2(String make2) {
        this.make2 = make2;
    }

    public String getModel2() {
        return model2;
    }

    public void setModel2(String model2) {
        this.model2 = model2;
    }

    public String getColor2() {
        return color2;
    }

    public void setColor2(String color2) {
        this.color2 = color2;
    }

    public String getStart_date2() {
        return start_date2;
    }

    public void setStart_date2(String start_date2) {
        this.start_date2 = start_date2;
    }

    public String getEnd_date2() {
        return end_date2;
    }

    public void setEnd_date2(String end_date2) {
        this.end_date2 = end_date2;
    }

    public String getPlate3() {
        return plate3;
    }

    public void setPlate3(String plate3) {
        this.plate3 = plate3;
    }

    public String getState3() {
        return state3;
    }

    public void setState3(String state3) {
        this.state3 = state3;
    }

    public String getYear3() {
        return year3;
    }

    public void setYear3(String year3) {
        this.year3 = year3;
    }

    public String getMake3() {
        return make3;
    }

    public void setMake3(String make3) {
        this.make3 = make3;
    }

    public String getModel3() {
        return model3;
    }

    public void setModel3(String model3) {
        this.model3 = model3;
    }

    public String getColor3() {
        return color3;
    }

    public void setColor3(String color3) {
        this.color3 = color3;
    }

    public String getStart_date3() {
        return start_date3;
    }

    public void setStart_date3(String start_date3) {
        this.start_date3 = start_date3;
    }

    public String getEnd_date3() {
        return end_date3;
    }

    public void setEnd_date3(String end_date3) {
        this.end_date3 = end_date3;
    }

    public String getPlate4() {
        return plate4;
    }

    public void setPlate4(String plate4) {
        this.plate4 = plate4;
    }

    public String getState4() {
        return state4;
    }

    public void setState4(String state4) {
        this.state4 = state4;
    }

    public String getYear4() {
        return year4;
    }

    public void setYear4(String year4) {
        this.year4 = year4;
    }

    public String getMake4() {
        return make4;
    }

    public void setMake4(String make4) {
        this.make4 = make4;
    }

    public String getModel4() {
        return model4;
    }

    public void setModel4(String model4) {
        this.model4 = model4;
    }

    public String getColor4() {
        return color4;
    }

    public void setColor4(String color4) {
        this.color4 = color4;
    }

    public String getStart_date4() {
        return start_date4;
    }

    public void setStart_date4(String start_date4) {
        this.start_date4 = start_date4;
    }

    public String getEnd_date4() {
        return end_date4;
    }

    public void setEnd_date4(String end_date4) {
        this.end_date4 = end_date4;
    }

    public String getPlate5() {
        return plate5;
    }

    public void setPlate5(String plate5) {
        this.plate5 = plate5;
    }

    public String getState5() {
        return state5;
    }

    public void setState5(String state5) {
        this.state5 = state5;
    }

    public String getYear5() {
        return year5;
    }

    public void setYear5(String year5) {
        this.year5 = year5;
    }

    public String getMake5() {
        return make5;
    }

    public void setMake5(String make5) {
        this.make5 = make5;
    }

    public String getModel5() {
        return model5;
    }

    public void setModel5(String model5) {
        this.model5 = model5;
    }

    public String getColor5() {
        return color5;
    }

    public void setColor5(String color5) {
        this.color5 = color5;
    }

    public String getStart_date5() {
        return start_date5;
    }

    public void setStart_date5(String start_date5) {
        this.start_date5 = start_date5;
    }

    public String getEnd_date5() {
        return end_date5;
    }

    public void setEnd_date5(String end_date5) {
        this.end_date5 = end_date5;
    }

    public Integer getTag_request() {
        return tag_request;
    }

    public void setTag_request(Integer tag_request) {
        this.tag_request = tag_request;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getAddress_country() {
        return address_country;
    }

    public void setAddress_country(String address_country) {
        this.address_country = address_country;
    }

    public Integer getVehicle_type3() {
        return vehicle_type3;
    }

    public void setVehicle_type3(Integer vehicle_type3) {
        this.vehicle_type3 = vehicle_type3;
    }

    public Integer getVehicle_type4() {
        return vehicle_type4;
    }

    public void setVehicle_type4(Integer vehicle_type4) {
        this.vehicle_type4 = vehicle_type4;
    }

    public Integer getVehicle_type5() {
        return vehicle_type5;
    }

    public void setVehicle_type5(Integer vehicle_type5) {
        this.vehicle_type5 = vehicle_type5;
    }

    public String getCountry4() {
        return country4;
    }

    public void setCountry4(String country4) {
        this.country4 = country4;
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

    public String getCountry3() {
        return country3;
    }

    public void setCountry3(String country3) {
        this.country3 = country3;
    }

    public String getCountry5() {
        return country5;
    }

    public void setCountry5(String country5) {
        this.country5 = country5;
    }

    public Integer getTag_type() {
        return tag_type;
    }

    public void setTag_type(Integer tag_type) {
        this.tag_type = tag_type;
    }

    public String getSecQuestion2ID() {
        return secQuestion2ID;
    }

    public void setSecQuestion2ID(String secQuestion2ID) {
        this.secQuestion2ID = secQuestion2ID;
    }

    public Boolean getPrimary_receive_text_messages() {
        return primary_receive_text_messages;
    }

    public Boolean getSecondary_receive_text_messages() {
        return secondary_receive_text_messages;
    }

    public Boolean getReceive_promotion_material() {
        return receive_promotion_material;
    }

    public String getSecQuestion1ID() {
        return secQuestion1ID;
    }

    public void setSecQuestion1ID(String secQuestion1ID) {
        this.secQuestion1ID = secQuestion1ID;
    }

    public String getSecAnswer1() {
        return secAnswer1;
    }

    public void setSecAnswer1(String secAnswer1) {
        this.secAnswer1 = secAnswer1;
    }

    public String getSecAnswer2() {
        return secAnswer2;
    }

    public void setSecAnswer2(String secAnswer2) {
        this.secAnswer2 = secAnswer2;
    }

    public String getSecQuestion3ID() {
        return secQuestion3ID;
    }

    public void setSecQuestion3ID(String secQuestion3ID) {
        this.secQuestion3ID = secQuestion3ID;
    }

    public String getSecAnswer3() {
        return secAnswer3;
    }

    public void setSecAnswer3(String secAnswer3) {
        this.secAnswer3 = secAnswer3;
    }

    public Boolean getReceive_road_alerts() {
        return receive_road_alerts;
    }

    public void setReceive_road_alerts(Boolean receive_road_alerts) {
        this.receive_road_alerts = receive_road_alerts;
    }
}
