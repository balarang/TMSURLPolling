package com.overhaul.integration.polling.job.schneider;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchneiderVisibilityAPIResponse {

    @JsonProperty("OrderID")
    private String orderID;

    @JsonProperty("CustomerReferenceID")
    private String customerReferenceID;

    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("SCAC")
    private String SCAC;

    @JsonProperty("LastReportedLocation")
    private LastReportedLocation lastReportedLocation;

    @JsonProperty("EquipmentID")
    private String equipmentId;

    public SchneiderVisibilityAPIResponse(){

    }

    public String getCustomerReferenceID() {
        return customerReferenceID;
    }

    public void setCustomerReferenceID(String customerReferenceID) {
        this.customerReferenceID = customerReferenceID;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getSCAC() {
        return SCAC;
    }

    public void setSCAC(String SCAC) {
        this.SCAC = SCAC;
    }

    public LastReportedLocation getLastReportedLocation() {
        return lastReportedLocation;
    }

    public void setLastReportedLocation(LastReportedLocation lastReportedLocation) {
        this.lastReportedLocation = lastReportedLocation;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
}
