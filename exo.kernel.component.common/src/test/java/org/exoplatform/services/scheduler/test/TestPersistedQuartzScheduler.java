package org.exoplatform.services.scheduler.test;

import junit.framework.TestCase;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.exoplatform.services.scheduler.*;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.exoplatform.services.scheduler.impl.QuartzSheduler;
import org.quartz.JobDetail;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * Test persisted quartz
 */
public class TestPersistedQuartzScheduler extends TestCase {
    private PortalContainer manager;
    private Connection connection;
    private JobSchedulerService jobSchedulerService;
    private Statement st;

    public void setUp() throws Exception {
        manager = PortalContainer.getInstance();
        manager.getComponentInstanceOfType(InitialContextInitializer.class);
        DataSource dataSource = (DataSource) new InitialContext().lookup("jdbcjcr");
        connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        st = connection.createStatement();

        /***init quartz tables***/
        String scripts = IOUtil.getStreamContentAsString(PrivilegedFileHelper.getResourceAsStream("/quartz_hsqldb.sql"));

        for (String query : splitWithSQLDelimiter(scripts)) {
            st.executeUpdate(query);
        }

        /***init QuartzSheduler Service***/
        InitParams initParams = new InitParams();
        ValueParam valueParam = new ValueParam();
        valueParam.setName("org.quartz.dataSource.quartzDS.jndiURL");
        valueParam.setValue("jdbcjcr");
        initParams.addParameter(valueParam);

        valueParam = new ValueParam();
        valueParam.setName("org.quartz.jobStore.driverDelegateClass");
        valueParam.setValue("org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        initParams.addParameter(valueParam);

        valueParam = new ValueParam();
        valueParam.setName("org.quartz.jobStore.class");
        valueParam.setValue("org.quartz.impl.jdbcjobstore.JobStoreTX");
        initParams.addParameter(valueParam);

        valueParam = new ValueParam();
        valueParam.setName("org.quartz.threadPool.threadCount");
        valueParam.setValue("5");
        initParams.addParameter(valueParam);

        valueParam = new ValueParam();
        valueParam.setName("org.quartz.jobStore.dataSource");
        valueParam.setValue("quartzDS");
        initParams.addParameter(valueParam);

        valueParam = new ValueParam();
        valueParam.setName("org.quartz.jobStore.tablePrefix");
        valueParam.setValue("QRTZ_");
        initParams.addParameter(valueParam);

        valueParam = new ValueParam();
        valueParam.setName("org.quartz.jobStore.useProperties");
        valueParam.setValue("true");
        initParams.addParameter(valueParam);

        QueueTasks queueTasks = new QueueTasks();
        QuartzSheduler quartzSheduler = new QuartzSheduler(PortalContainer.getInstance().getContext(), initParams);
        jobSchedulerService = new JobSchedulerServiceImpl(quartzSheduler, queueTasks);
    }

    public void testUpdateJobDetailClassNotFoundException() throws Exception {
        jobSchedulerService.addGlobalTriggerListener(new SchedulerServiceTestBase.GlobalTriggerListener());
        Date firedTime = new Date(System.currentTimeMillis() + 1000000);
        jobSchedulerService.addJob(new JobInfo("queuejob", null, AJob.class), firedTime);

        st.executeUpdate("update QRTZ_JOB_DETAILS  set JOB_CLASS_NAME='org.exoplatform.services.scheduler.test.NotFoundJob'  where JOB_NAME='queuejob'");

        try {
            jobSchedulerService.addJob(new JobInfo("queuejob", null, BJob.class), firedTime);
            List<JobDetail> list = jobSchedulerService.getAllJobs();
            assertEquals(list.size(), 1);
            assertEquals(list.get(0).getJobClass(), BJob.class);
        } catch (Exception e) {
            fail();
        }
    }

    private String[] splitWithSQLDelimiter(String resource) {
        if (resource.startsWith("/*$DELIMITER:")) {
            try {
                String e = resource.substring("/*$DELIMITER:".length());
                int endOfDelimIndex = e.indexOf("*/");
                String delim = e.substring(0, endOfDelimIndex).trim();
                e = e.substring(endOfDelimIndex + 2).trim();
                return e.split(delim);
            } catch (IndexOutOfBoundsException var4) {
                return resource.split(";");
            }
        } else {
            return resource.split(";");
        }
    }

    private class BJob extends BaseJob {
        public void execute(JobContext context) throws Exception {
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (st != null) {
            st.close();
        }
        if (connection != null) {
            connection.close();
        }
        super.tearDown();
    }
}
