package pt.ist.applications.admissions.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationsAdmissionsContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
