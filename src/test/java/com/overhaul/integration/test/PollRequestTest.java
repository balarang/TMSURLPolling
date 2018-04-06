package com.overhaul.integration.test;

import com.overhaul.common.FileUtils;
import com.overhaul.integration.model.PollRequest;
import com.overhaul.lambda.ContentTypeHandler;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import javax.ws.rs.core.MediaType;

public class PollRequestTest{

    @Test
    public void testPollRequest() throws Exception {
        ContentTypeHandler<PollRequest> jsonhandler = new ContentTypeHandler<>();
        PollRequest request = jsonhandler.unmarshal(MediaType.APPLICATION_JSON, FileUtils.getData("./src/test/resources/start_request.json"), PollRequest.class);

        assertEquals( 2, request.getParams().size());
    }

}
