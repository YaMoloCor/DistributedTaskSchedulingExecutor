package org.example.job.executor.config;

import org.example.job.core.executor.impl.JobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfig {

    private Logger logger = LoggerFactory.getLogger(JobConfig.class);

    @Value("${job.admin.addresses}")
    private String adminAddresses;

    @Value("${job.accessToken}")
    private String accessToken;

    @Value("${job.executor.appname}")
    private String appName;

    @Value("${job.executor.address}")
    private String address;

    @Value("${job.executor.ip}")
    private String ip;

    @Value("${job.executor.port}")
    private int port;

    @Value("${job.executor.logpath}")
    private String logPath;

    @Value("${job.executor.logretentiondays}")
    private int logRetentionDays;

    @Bean
    public JobSpringExecutor jobSpringExecutor() {
        logger.info(">>>>>>>>>>> job config init.");
        JobSpringExecutor jobSpringExecutor = new JobSpringExecutor();
        jobSpringExecutor.setAdminAddresses(adminAddresses);
        jobSpringExecutor.setAccessToken(accessToken);
        jobSpringExecutor.setAppname(appName);
        jobSpringExecutor.setAddress(address);
        jobSpringExecutor.setIp(ip);
        jobSpringExecutor.setPort(port);
        jobSpringExecutor.setLogPath(logPath);
        jobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return jobSpringExecutor;
    }
}
