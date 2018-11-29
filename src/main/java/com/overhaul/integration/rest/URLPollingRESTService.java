package com.overhaul.integration.rest;

import com.overhaul.common.SNSNotificationService;
import com.overhaul.integration.model.Params;
import com.overhaul.integration.model.PollRequest;
import com.overhaul.integration.model.PollRequestType;
import com.overhaul.integration.polling.PollingService;
import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerServlet;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

    private SNSNotificationService snsService;

    // The Java method will process requests from SQS queue
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveSQSMessage(PollRequest pollRequest) {
        try {
            snsService = new SNSNotificationService(System.getProperty("SNSWRITER_SECRET_KEY"),
                    System.getProperty("SNSWRITER_ACCESS_KEY"),
                    System.getProperty("SUCCESS_TOPIC_SNS_ARN"),
                    System.getProperty("FAILURE_TOPIC_SNS_ARN")
            );
            System.out.printf("HTTP Body with PollType %s is %s \n", pollRequest.getPollRequestType(), pollRequest);
            List<Params> params = pollRequest.getParams();
            StdSchedulerFactory factory = (StdSchedulerFactory) servletContext.getAttribute(QuartzInitializerServlet.QUARTZ_FACTORY_KEY);
            Scheduler scheduler = factory.getScheduler();
            output("Scheduler started: " + scheduler.isStarted());
            if (pollRequest.getPollRequestType() == PollRequestType.START) {
                output("START");
                service.startPolling(scheduler, pollRequest);
            } else if (pollRequest.getPollRequestType() == PollRequestType.STOP) {
                output("STOP");
                service.stopPolling(scheduler, pollRequest);
            } else if (pollRequest.getPollRequestType() == PollRequestType.UPDATE) {
                output("UPDATE");
                service.updatePolling(scheduler, pollRequest);
            }else {
                return Response.serverError().build();
            }
        } catch (ClassNotFoundException cnfe) {
            //This is recoverable, so return HTTP 500 so the message is put to dead letter
            //and processed back after the missing class error is fixed
            cnfe.printStackTrace();
            snsService.notifyFailure(String.format("Job Class %s not found", cnfe.getMessage()));
            return Response.serverError().build();
        } catch (Exception ex) {
            //decide whether you want to return 200 or 500
            ex.printStackTrace();
            snsService.notifyFailure(String.format("Unknown exception while executing scheduled jobs", ex.toString()));
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    /*@GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTestMessage() {
        return "SUCCESS";
    }*/

    private void output(String message) {
        servletContext.log(message);
    }
}
