package com.digitalforensics.controller;

import com.digitalforensics.util.BackgroundExecutor;
import com.digitalforensics.util.FileUtil;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Called by Tomcat when the web app starts and stops. */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().log("Evidence storage folder: " + FileUtil.storageDir());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        BackgroundExecutor.shutdown();               // stop worker threads
        AbandonedConnectionCleanupThread.checkedShutdown(); // stop the MySQL driver's cleanup thread
    }
}
