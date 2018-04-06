package com.overhaul.integration.rest;

import com.overhaul.integration.model.Params;
import com.overhaul.integration.model.PollRequest;
import com.overhaul.integration.model.PollRequestType;
import com.overhaul.integration.polling.PollingService;
import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerServlet;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

// The Java class will be hosted at the URI path "/pollrequest"
@Path("/pollrequest")
public class URLPollingRESTService {
    @Context
    private Request request;

    @Context
    private ServletContext servletContext;

    @Context
    private HttpHeaders httpHeaders;

    private PollingService service = new PollingService();

    // The Java method will process requests from SQS queue
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveSQSMessage( PollRequest pollRequest ) {
        //MultivaluedMap<String,String> headers = httpHeaders.getRequestHeaders();
        /*for(String header : headers.keySet()){
            System.out.println(header+ ":" + httpHeaders.getHeaderString(header));
        }*/
        try {
            System.out.printf("HTTP Body with PollType %s is %s \n", pollRequest.getPollRequestType(), pollRequest);
            List<Params> params = pollRequest.getParams();
            /*for(Params param: params) {
                System.out.println( param.getKey() + "=" + param.getValue());
            }*/
            StdSchedulerFactory factory = (StdSchedulerFactory) servletContext.getAttribute( QuartzInitializerServlet.QUARTZ_FACTORY_KEY);
            Scheduler scheduler = factory.getScheduler();
//            System.out.println("Scheduler : " + scheduler );
            output("Scheduler started: " + scheduler.isStarted() );
            if (pollRequest.getPollRequestType() == PollRequestType.START) {
                output("START");
                service.startPolling(scheduler, pollRequest);
            } else if (pollRequest.getPollRequestType() == PollRequestType.STOP) {
                output("STOP");
                service.stopPolling(scheduler, pollRequest);
            } else {
                return Response.serverError().build();
            }
        } catch (ClassNotFoundException cnfe) {
            //This is recoverable, so return HTTP 500 so the message is put to dead letter
            //and processed back after the missing class error is fixed
            cnfe.printStackTrace();
            return Response.serverError().build();
        }
        catch (Exception ex) {
            //decide whether you want to return 200 or 500
            ex.printStackTrace();
        }
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTestMessage() {
        return "SUCCESS";
    }

    private void output( String message ) {
        servletContext.log( message );
    }
}
