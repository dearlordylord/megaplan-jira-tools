package ru.megaplan.jira.plugin.tools.listeners;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 09.06.12
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class TestMailListener implements InitializingBean, DisposableBean {

    private final static Logger log = Logger.getLogger(TestMailListener.class);

    private final NotificationSchemeManager notificationSchemeManager;
    private final ProjectManager projectManager;
    private final EventPublisher eventPublisher;

    TestMailListener(NotificationSchemeManager notificationSchemeManager,
                     ProjectManager projectManager,
                     EventPublisher eventPublisher) {
        this.notificationSchemeManager = notificationSchemeManager;
        this.projectManager = projectManager;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void issueEvent(IssueEvent issueEvent) {
        if (!"MPS".equals(issueEvent.getProject().getKey())) return;
        if(!"TEST".equals(issueEvent.getIssue().getSummary())) return;

        List<SchemeEntity> entities = null;
        try {
            entities = notificationSchemeManager.getNotificationSchemeEntities(issueEvent.getProject(), issueEvent.getEventTypeId());
        } catch (GenericEntityException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for (SchemeEntity e : entities) {
            Set<NotificationRecipient> lr = null;
            try {
                lr = notificationSchemeManager.getRecipients(issueEvent,e);
            } catch (GenericEntityException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            for (NotificationRecipient nr : lr) {
                log.warn("found recipient format for : " + nr.getEmail() + " : " + nr.getFormat());
            }
        }

    }



    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.warn("registering : " + this);
        eventPublisher.register(this);
    }
}
