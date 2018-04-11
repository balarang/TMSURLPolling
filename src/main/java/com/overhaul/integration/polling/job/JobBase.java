package com.overhaul.integration.polling.job;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.overhaul.integration.model.HttpResponse;
import com.overhaul.rest.RESTUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public abstract class JobBase implements Job {

    private Logger logger = LoggerFactory.getLogger(JobBase.class);
    public static final String BROKER_REFERENCE = "BROKER_REFERENCE";
    public static final String PORTAL_ID = "PORTAL_ID";
    public static final String URL = "URL";
    public static final String SHIPMENT_ID = "SHIPMENT_ID";

    public static final String X_API_KEY = "x-api-key";
    public static final String X_CREATED_TIME = "x-created-time";

    protected String brokerReference;
    protected int portalId;
    protected String url;
    protected int shipmentId;

    private AmazonSNSClient snsClient = null;

    public JobBase() {
        //create a new SNS client with credentials
        AWSCredentials awsCreds = new BasicAWSCredentials(
                System.getProperty("SNSWRITER_ACCESS_KEY"),
                System.getProperty("SNSWRITER_SECRET_KEY") );
        snsClient = new AmazonSNSClient(awsCreds);
    }

    protected void parseBasic( JobExecutionContext jobExecutionContext ) {
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        brokerReference = map.getString(BROKER_REFERENCE);
        portalId = map.getInt( PORTAL_ID );
        url = map.getString( URL );
        shipmentId = map.getInt( SHIPMENT_ID );
    }

    private void validateMandatoryFields(){

    }

    /*public String getBrokerReference() {
        return brokerReference;
    }

    public int getPortalId() {
        return portalId;
    }*/

    public String getUrl() {
        return url;
    }

    public String getURL() {
        return System.getProperty("OH_URL");
    }

    public String getAuth() {
        return System.getProperty("OH_AUTHORIZATION");
    }

    public HttpResponse<String> post(String json, Map<String, String> headers) {
        RESTUtils restUtils = new RESTUtils(this.getURL());
        Response httpResponse = restUtils.postJSON(json, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, headers);
        return this.createHttpResponse(httpResponse);
    }

    public HttpResponse<String> put(String url, String json) throws Exception {
        RESTUtils restUtils = new RESTUtils(url);
        Response httpResponse = restUtils.putJSON(json, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, createHttpHeaders());
        return createHttpResponse( httpResponse );
    }

    private HttpResponse<String> createHttpResponse(Response httpResponse) {
        HttpResponse<String> response = new HttpResponse();
        response.setStatusCode(httpResponse.getStatus());
        response.setEntity(httpResponse.readEntity(String.class));
        return response;
    }

    public abstract Map<String,String> createHttpHeaders() throws Exception ;

    protected void sendPollSuccessNotification(int httpStatus, String httpResponse, Map<String,String> mapValues ) {
        sendPollNotification( httpStatus, httpResponse, mapValues, true);
    }

    protected void sendPollFailureNotification(int httpStatus, String httpResponse, Map<String,String> mapValues ) {
        sendPollNotification( httpStatus, httpResponse, mapValues, false);
    }

    protected void sendBackendSuccessNotification(int httpStatus, String httpResponse, Map<String,String> mapValues ) {
        sendBackendPostNotification( httpStatus, httpResponse, mapValues, true);
    }

    protected void sendBackendFailureNotification(int httpStatus, String httpResponse, Map<String,String> mapValues ) {
        sendBackendPostNotification( httpStatus, httpResponse, mapValues, false);
    }

    private void sendBackendPostNotification( int httpStatus, String httpResponse, Map<String,String> mapValues, boolean notificationType ) {
        sendNotification("HTTP Post:", httpStatus, httpResponse, mapValues, notificationType );
    }

    private void sendPollNotification( int httpStatus, String httpResponse, Map<String,String> mapValues, boolean notificationType ) {
        sendNotification("URL Polling:", httpStatus, httpResponse, mapValues, notificationType );
    }

    private void sendNotification(String prefixString, int httpStatus, String httpResponse, Map<String,String> mapValues, boolean successNotification ) {
        //publish to an SNS topic
        String title = String.format(
                "Notification for Shipment ID %s. HTTP call returned with status %s and response %s\n",
                shipmentId, httpStatus, httpResponse ) ;
        StringBuffer message = new StringBuffer(prefixString).append( title );
        if( mapValues !=null ) {
            mapValues.entrySet().forEach(
                    elem -> message.append(elem.getKey()).append("=").append(elem.getValue()).append("\n"));
        }
        if( successNotification ) {
            notifySuccess( message.toString() );
        } else {
            notifyFailure(message.toString());
        }
    }

    protected void notifySuccess(String message ){
        publish( System.getProperty("SUCCESS_TOPIC_SNS_ARN"), message );
    }

    protected void notifyFailure(String message ){
        publish( System.getProperty("FAILURE_TOPIC_SNS_ARN"), message );
    }

    private PublishResult publish(String topicARN, String message) {
        PublishRequest request = new PublishRequest();
        request.setTopicArn( topicARN );
        request.setMessage( message );
        return snsClient.publish( request);
    }
}
