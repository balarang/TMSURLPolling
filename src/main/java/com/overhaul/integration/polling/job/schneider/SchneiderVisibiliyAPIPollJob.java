package com.overhaul.integration.polling.job.schneider;

import com.overhaul.integration.model.HttpResponse;
import com.overhaul.integration.platform.model.v1.Mastershipment;
import com.overhaul.integration.platform.model.v1.PlatformDevicesType;
import com.overhaul.integration.polling.job.JobBase;
import com.overhaul.lambda.ContentTypeHandler;
import com.overhaul.lambda.Utils;
import com.overhaul.smarttruck.model.SmartTruckEvent;
import com.overhaul.tms.helper.SchneiderAccessTokenHandler;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SchneiderVisibiliyAPIPollJob extends JobBase {
    private Logger logger = LoggerFactory.getLogger(SchneiderVisibiliyAPIPollJob.class);
    private static final String BEARER = "Bearer ";
    private static final String ORDER_IDS = "Orderids";
    private String accessToken;
    private JobDataMap map;
    private static Map<String, String> statusMap = new HashMap();

    static {
        statusMap.put("In-Transit", "ENROUTE_TO_DELIVERY");
        statusMap.put("Delivered", "DELIVERED");
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        super.parseBasic(jobExecutionContext);
        //output("BrokerRef: "+ getBrokerReference());
        map = jobExecutionContext.getMergedJobDataMap();
        //output("BrokerRef: "+ map.getString(BROKER_REFERENCE));
        accessToken = getAccessToken();
        //output( String.format("Token %s, Ref %s", accessToken, getBrokerReference()));
        Response response = null;
        try {
            response = get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String json = response.readEntity( String.class );
                output("Response from URL :" + json);
                if( json!= null ) {
                    ContentTypeHandler<SchneiderVisibilityAPIResponse[]> handler =
                            new ContentTypeHandler<SchneiderVisibilityAPIResponse[]>();
                    SchneiderVisibilityAPIResponse[] responseObj =
                            handler.unmarshal(MediaType.APPLICATION_JSON, json, SchneiderVisibilityAPIResponse[].class);

                    //check if its empty response. Ignore any empty response
                    //else convert JSON String to
                    // OH backend format and HTTP Post it OH backend
                    if( responseObj.length > 0 ) {
                        output("Adding devices");
                        
                        //addDevice(responseObj[0]);
                        SmartTruckEvent evt = SchneiderVisibilityMapper.INSTANCE.convertToSmartTruckEvent( responseObj[0] );
                        ContentTypeHandler<SmartTruckEvent> evtHandler = new ContentTypeHandler<>();

                        //Map Scehndier status to OH status
                        evt.getShipment().setStatus( statusMap.get( evt.getShipment().getStatus() ));

                        String jsonToBackend = evtHandler.marshal( MediaType.APPLICATION_JSON, evt, SmartTruckEvent.class);
                        output( "Final JSON sent to OH :"+ jsonToBackend );
                        HttpResponse<String> httpResponse = post(jsonToBackend, createHttpHeaders());
                        int httpStatus = httpResponse.getStatusCode();
                        output(String.format("HTTP status: %d, Entity: %s", httpStatus, httpResponse.getEntity()));
                    }
                }
            } else {
                String responseMsg = response.readEntity(String.class);
                output(String.format("Response status %d, Reason %s :", response.getStatus(), responseMsg));
                //notifyFailure( response.getStatus(), responseMsg, null );
                //notifyFailure( responseMsg );
            }
        } catch(JobExecutionException jex ){
            jex.printStackTrace();
            logger.error("Cannot complete poll job. Reason: ", jex.toString() );
            //notifyFailure( jex.toString() );
        }
        catch(Exception ex ){
            ex.printStackTrace();
            logger.error("Cannot process JSON response", ex.toString() );
            //notifyFailure( ex.toString() );
        }
        finally {
            if (response!=null) {
                response.close();
            }
        }
    }

    //Make this method generic
    private Response get() {
        /*ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration.property(ClientProperties.READ_TIMEOUT, 10000);
        Client client = ClientBuilder.newClient( configuration );*/

        Client client = ClientBuilder.newClient();

        Invocation.Builder invocationBuilder = client
                .target(getUrl())
                .queryParam(ORDER_IDS,getBrokerReference())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER+ accessToken );

        return invocationBuilder.get();
    }

    private String getAccessToken() throws JobExecutionException {
        SchneiderAccessTokenHandler tokenHandler = new SchneiderAccessTokenHandler();
        String token = tokenHandler.getAccessToken(map.getString("loginURL"), map.getString("tenantId"),
                map.getString("authURL"), map.getString("resource"),map.getString("clientId"),
                map.getString("username"), map.getString("password"));
        if( token == null ) {
            throw new JobExecutionException("Unable to get access token");
        }

        return token;
    }

    private void addDevice( SchneiderVisibilityAPIResponse response ) throws Exception {
        String name = response.getEquipmentId();
        String imei = name;
        String type = "trailer";

        Mastershipment mastershipment=new Mastershipment();
        PlatformDevicesType device = new PlatformDevicesType();
        device.setNumber( name );
        device.setImei( imei );
        device.setPlacement( type );
        mastershipment.getDevices().add( device);

        ContentTypeHandler<Mastershipment> handler = new ContentTypeHandler<>();
        String putJSON = handler.marshal(MediaType.APPLICATION_JSON, mastershipment, Mastershipment.class);
        output("Add device JSON :" + putJSON);

        String url = String.format(System.getenv("OH_URL_BY_BROKER_REF"), getBrokerReference());
        output( "PUT URL: " + url );
        HttpResponse<String> httpResponse = put(url, putJSON);
        int httpStatus = httpResponse.getStatusCode();
        output(String.format("HTTP status: %d, Entity: %s", httpStatus, httpResponse.getEntity()));
    }

    public Map<String, String> createHttpHeaders() throws Exception {
        Map<String, String> headers = new HashMap<>();
        Date currDate = new Date();
        SimpleDateFormat tgtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String x_created_at = tgtFormat.format(currDate);
        String hmacSHA256 = Utils.createHMACSHA256(getSecretKey(), x_created_at);
        headers.put(X_API_KEY, getAPIKey());
        headers.put(HttpHeaders.AUTHORIZATION, hmacSHA256);
        headers.put(X_CREATED_TIME, x_created_at);
        return headers;
    }

    public String getAPIKey() {
        if( map!= null ) {
            return map.getString("API_KEY");
        } else {
            return null;
        }
    }

    public String getSecretKey() {
        if( map!=null ) {
            return map.getString("SECRET_KEY");
        } else {
            return null;
        }
    }

    private void output( String s ) {
        System.out.println( s );
    }

}
