package com.overhaul.integration.polling;

import com.overhaul.integration.model.PollRequest;
import com.overhaul.integration.polling.job.JobBase;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingService {
    Logger logger = LoggerFactory.getLogger( PollingService.class );

    public PollingService(){

    }

    public void startPolling(Scheduler scheduler, PollRequest pollRequest) throws Exception {
        validatePollRequest( pollRequest );
        System.out.println("Job Class name :"+ pollRequest.getJob_class_name() );
        Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(pollRequest.getJob_class_name());
        System.out.println("Job Class loaded "+ clazz );
        JobKey jobKey = new JobKey(pollRequest.getJob_name());

        //Set the scheduler timings.
        JobDataMap jobMap = createJobDataMap(pollRequest);

        JobDetail jobDetail = scheduler.getJobDetail( jobKey );
        if(  jobDetail == null ) {
            jobDetail = JobBuilder
                    .newJob( clazz )
                    .storeDurably()
                    .requestRecovery()
                    .withIdentity(jobKey).build();
            System.out.println("JobDetail created and added");
            scheduler.addJob(jobDetail, true);
        }

        TriggerKey triggerKey = createTriggerKey ( pollRequest );
        Trigger trigger1 = scheduler.getTrigger( triggerKey );
        if( trigger1 == null ) {
            trigger1 = createTrigger(triggerKey, pollRequest, jobDetail, jobMap);
            scheduler.scheduleJob(trigger1);
            System.out.println("Job scheduled for poll");
        } else {
            scheduler.rescheduleJob( triggerKey, trigger1 );
        }
    }

    public void stopPolling( Scheduler scheduler, PollRequest pollRequest ) throws Exception {
        TriggerKey triggerKey = createTriggerKey ( pollRequest );
        scheduler.unscheduleJob( triggerKey );
        System.out.println("trigger stopped for "+ pollRequest.getShipment_id() );

        /*JobKey jobKey = new JobKey(pollRequest.getShipmentId());
        scheduler.deleteJob( jobKey );
        System.out.println("job deleted for "+ pollRequest.getShipmentId() );*/
    }

    public void updatePollTime( Scheduler scheduler, PollRequest pollRequest ) throws Exception {
        TriggerKey triggerKey = createTriggerKey( pollRequest );
        JobKey jobKey = new JobKey(pollRequest.getJob_name());
        //Set the scheduler timings.
        JobDataMap jobMap = createJobDataMap(pollRequest);
        JobDetail jobDetail = scheduler.getJobDetail( jobKey );
        Trigger trigger = createTrigger( triggerKey, pollRequest, jobDetail, jobMap );
        scheduler.rescheduleJob( triggerKey, trigger );
    }

    private JobDataMap createJobDataMap(PollRequest pollRequest) {
        JobDataMap jobMap = new JobDataMap();
        pollRequest.getParams().forEach(
                elem -> jobMap.put(elem.getKey(), elem.getValue() ));
        jobMap.put(JobBase.BROKER_REFERENCE, pollRequest.getReference_numbers().getBroker());
        jobMap.put(JobBase.PORTAL_ID, pollRequest.getPortal_id());
        jobMap.put(JobBase.URL, pollRequest.getJob_url());
        //jobMap.put(JobBase.ACCESS_TOKEN, pollRequest.getAccess().getToken());
        jobMap.put(JobBase.SHIPMENT_ID, pollRequest.getShipment_id());
        return jobMap;
    }

    private TriggerKey createTriggerKey( PollRequest pollRequest ) {
        return new TriggerKey( String.valueOf( pollRequest.getShipment_id()));
    }

    private Trigger createTrigger(TriggerKey triggerKey, PollRequest pollRequest, JobDetail jobDetail, JobDataMap jobMap ) {
        return TriggerBuilder.newTrigger()
                .withIdentity( triggerKey )
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(pollRequest.getPoll_time_in_minutes())
                .repeatForever())
                .forJob(jobDetail)
                .usingJobData( jobMap )
                .build();
    }

    private void validatePollRequest( PollRequest request ) {

    }
}
