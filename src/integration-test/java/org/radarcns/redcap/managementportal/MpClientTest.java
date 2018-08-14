package org.radarcns.redcap.managementportal;

import org.junit.Before;
import org.junit.Test;
import org.radarcns.exception.TokenException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;
import static org.radarcns.redcap.util.IntegrationUtils.*;

public class MpClientTest {
    private Project project;

    @Before
    public void init() throws IOException, TokenException {
        updateProjectAttributes();
    }

    @Test
    public void getProjectTest() throws IOException {
        project = mpClient.getProject(new URL(REDCAP_URL), REDCAP_PROJECT_ID);
        assertEquals("radar", project.getProjectName());
    }

    @Test
    public void createSubjectTest() throws IOException, URISyntaxException {
        if(project == null){
            getProjectTest();
        }
        mpClient.createSubject(new URL(REDCAP_URL), project, REDCAP_RECORD_ID_1, WORK_PACKAGE + REDCAP_RECORD_ID_1);
        Subject subject = mpClient.getSubject(new URL(REDCAP_URL), REDCAP_PROJECT_ID, REDCAP_RECORD_ID_1);

        assertEquals(Integer.valueOf(REDCAP_RECORD_ID_1), subject.getExternalId());
        assertEquals(WORK_PACKAGE + REDCAP_RECORD_ID_1, subject.getHumanReadableIdentifier());
    }
}
