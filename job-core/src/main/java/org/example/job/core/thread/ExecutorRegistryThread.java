package org.example.job.core.thread;

import org.example.job.core.biz.AdminBiz;
import org.example.job.core.biz.model.RegistryParam;
import org.example.job.core.biz.model.ReturnT;
import org.example.job.core.enums.RegistryConfig;
import org.example.job.core.executor.JobExecutor;
import org.example.job.core.server.EmbedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ExecutorRegistryThread {
    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();

    public static ExecutorRegistryThread getInstance() {
        return instance;
    }

    private Thread registryThread;

    private volatile boolean toStop = false;

    public void start(String appname, String address) {
        // valid
        if (appname == null || appname.trim().length() == 0) {
            logger.warn(">>>>>>>>>>> job, executor registry config fail, appname is null.");
            return;
        }
        if (JobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> job, executor registry config fail, adminAddresses is null.");
            return;
        }
        registryThread = new Thread(() -> {
            try {
                RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appname, address);
                for (AdminBiz adminBiz: JobExecutor.getAdminBizList()) {
                    try {
                        ReturnT<String> registryResult = adminBiz.registry(registryParam);
                        if (registryResult!=null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                            registryResult = ReturnT.SUCCESS;
                            logger.debug(">>>>>>>>>>> job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            break;
                        } else {
                            logger.info(">>>>>>>>>>> job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                        }
                    } catch (Exception e) {
                        logger.info(">>>>>>>>>>> job registry error, registryParam:{}", registryParam, e);
                    }

                }
            } catch (Exception e) {
                if (!toStop) {
                    logger.error(e.getMessage(), e);
                }

            }

            try {
                if (!toStop) {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                }
            } catch (InterruptedException e) {
                if (!toStop) {
                    logger.warn(">>>>>>>>>>> job, executor registry thread interrupted, error msg:{}", e.getMessage());
                }
            }
        });
    }

    public void stop() {
        toStop = true;

        // interrupt and wait
        if (registryThread != null) {
            registryThread.interrupt();
            try {
                registryThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }
}
