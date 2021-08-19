package org.example.job.core.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class JobFileAppender {
    private static Logger logger = LoggerFactory.getLogger(JobFileAppender.class);

    private static String logBasePath = "/data/applogs/job/jobhandler";
    private static String glueSrcPath = logBasePath.concat("/gluesource");
    public static String getLogPath() {
        return logBasePath;
    }
    public static String getGlueSrcPath() {
        return glueSrcPath;
    }

    public static void initLogPath(String logPath) {
        //初始化
        if (logPath != null && logPath.trim().length() > 9) {
            logBasePath = logPath;
        }
        //创建文件夹
        File logPathDir = new File(logBasePath);
        if (!logPathDir.exists()) {
            logPathDir.mkdir();
        }
        logBasePath = logPathDir.getPath();
        // mk glue dir
        File glueBaseDir = new File(logPathDir, "gluesource");
        if (!glueBaseDir.exists()) {
            glueBaseDir.mkdirs();
        }
        glueSrcPath = glueBaseDir.getPath();
    }
}
