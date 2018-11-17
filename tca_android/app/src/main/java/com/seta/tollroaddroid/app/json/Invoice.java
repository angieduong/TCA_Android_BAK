package com.seta.tollroaddroid.app.json;

import com.google.gson.annotations.SerializedName;

//"invoice_id":"1151222","invoice_date":"12/18/2016","invoice_amount":14.80,"balance_due":0.00
public class Invoice implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @SerializedName("invoice_id")
    private String invoiceId;

    @SerializedName("invoice_date")
    private String invoiceDate;


    @SerializedName("url")
    private String invoiceUrl;

    @SerializedName("invoice_amount")
    private double invoiceAmount;

    @SerializedName("balance_due")
    private double balanceDue;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public double getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(double invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public double getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(double balanceDue) {
        this.balanceDue = balanceDue;
    }
}
