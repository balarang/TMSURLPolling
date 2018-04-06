package com.overhaul.integration.model;

import java.util.ArrayList;
import java.util.List;

public class PollRequest {
    private PollRequestType type;
    private References reference_numbers;
    private int shipment_id;
    private int portal_id;
    private String job_name;
    private String job_class_name;
    private String job_url;
    private JobAccess access;
    private int poll_time_in_minutes;
    private List<Params> params = new ArrayList<Params>();
    private String targetQueue;

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getJob_class_name() {
        return job_class_name;
    }

    public void setJob_class_name(String job_class_name) {
        this.job_class_name = job_class_name;
    }

    public String getJob_url() {
        return job_url;
    }

    public void setJob_url(String job_url) {
        this.job_url = job_url;
    }

    public int getPoll_time_in_minutes() {
        return poll_time_in_minutes;
    }

    public void setPoll_time_in_minutes(int poll_time_in_minutes) {
        this.poll_time_in_minutes = poll_time_in_minutes;
    }

    public JobAccess getAccess() {
        return access;
    }

    public void setAccess(JobAccess access) {
        this.access = access;
    }

    public List<Params> getParams() {
        return params;
    }

    public void setParams( List<Params> params ) {
        this.params = params;
    }

    public void addParams(Params params) {
        this.params.add(params);
    }

    public String getTargetQueue() {
        return targetQueue;
    }

    public void setTargetQueue(String targetQueue) {
        this.targetQueue = targetQueue;
    }

    public PollRequestType getType() {
        return type;
    }

    public void setType(PollRequestType type) {
        this.type = type;
    }

    public References getReference_numbers() {
        return reference_numbers;
    }

    public void setReference_numbers(References reference_numbers) {
        this.reference_numbers = reference_numbers;
    }

    public int getShipment_id() {
        return shipment_id;
    }

    public void setShipment_id(int shipment_id) {
        this.shipment_id = shipment_id;
    }

    public int getPortal_id() {
        return portal_id;
    }

    public void setPortal_id(int portal_id) {
        this.portal_id = portal_id;
    }

    public PollRequestType getPollRequestType() {
        return type;
    }

    public void setPollRequestType(PollRequestType type) {
        this.type = type;
    }
}
