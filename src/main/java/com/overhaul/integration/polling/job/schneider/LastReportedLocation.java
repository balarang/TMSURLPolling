package com.overhaul.integration.polling.job.schneider;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LastReportedLocation {
    @JsonProperty("LastReportedLatitude")
    private String LastReportedLatitude;

    @JsonProperty("LastReportedLongitude")
    private String LastReportedLongitude;

    @JsonProperty("LastReportedDateTime")
    private String LastReportedDateTime;

    public LastReportedLocation() {

    }

    public String getLastReportedLatitude() {
        return LastReportedLatitude;
    }

    public void setLastReportedLatitude(String lastReportedLatitude) {
        LastReportedLatitude = lastReportedLatitude;
    }

    public String getLastReportedLongitude() {
        return LastReportedLongitude;
    }

    public void setLastReportedLongitude(String lastReportedLongitude) {
        LastReportedLongitude = lastReportedLongitude;
    }

    public String getLastReportedDateTime() {
        return LastReportedDateTime;
    }

    public void setLastReportedDateTime(String lastReportedDateTime) {
        LastReportedDateTime = lastReportedDateTime;
    }
}
