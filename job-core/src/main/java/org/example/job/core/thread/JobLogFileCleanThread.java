package org.example.job.core.thread;

import org.example.job.core.executor.JobExecutor;
import org.example.job.core.log.JobFileAppender;
import org.example.job.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JobLogFileCleanThread {

    private static Logger logger = LoggerFactory.getLogger(JobLogFileCleanThread.class);

    private static JobLogFileCleanThread instance = new JobLogFileCleanThread();

    public static JobLogFileCleanThread getInstance() {
        return instance;
    }

    private Thread localThread;
    private volatile boolean toStop = false;

    public void start(int logRetentionDays) {
        localThread = new Thread(() -> {
            while (!toStop) {
                try {
                    File[] files = new File(JobFileAppender.getLogPath()).listFiles();
                    if (files != null && files.length > 0) {
                        //获取今天日期
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        Date todayDate = calendar.getTime();
                        for (File childFile : files) {
                            // valid
                            if (!childFile.isDirectory()) {
                                continue;
                            }
                            if (childFile.getName().indexOf("-") == -1) {
                                continue;
                            }
                            //获取日志创建日期
                            Date logFileCreateDate = null;
                            try{
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                logFileCreateDate = simpleDateFormat.parse(childFile.getName());
                            }catch (Exception e){
                                logger.error(e.getMessage(), e);
                            }
                            if (logFileCreateDate == null) {
                                continue;
                            }
                            //超过日志保存天数
                            if ((todayDate.getTime()-logFileCreateDate.getTime()) >= logRetentionDays * (24 * 60 * 60 * 1000) ) {
                                FileUtil.deleteRecursively(childFile);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
                try {
                    TimeUnit.DAYS.sleep(1);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> job, executor JobLogFileCleanThread thread destory.");
        });
        localThread.setDaemon(true);
        localThread.setName("job, executor JobLogFileCleanThread");
        localThread.start();
    }

    public void stop() {
        toStop = true;

        if (localThread == null) {
            return;
        }

        // interrupt and wait
        localThread.interrupt();
        try {
            localThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
