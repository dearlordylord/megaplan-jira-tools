package ru.megaplan.jira.plugin.tools.actions;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.action.JiraActionSupport;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.ofbiz.core.entity.GenericEntityException;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: i.loskutov
 * Date: 01.06.12
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
public class FireEventAction extends JiraWebActionSupport {

    private final IssueManager issueManager;
    private final ApplicationProperties applicationProperties;
    private final EventTypeManager eventTypeManager;
    private final IssueEventManager issueEventManager;
    private final UserManager userManager;
    private final SearchService searchService;
    private final CustomFieldManager customFieldManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final ProjectManager projectManager;
    private final UserPreferencesManager userPreferencesManager;
    private final EventPublisher eventPublisher;
    private final IssueService issueService;

    private String[] issues;

    public FireEventAction(IssueManager issueManager,
                           ApplicationProperties applicationProperties,
                           EventTypeManager eventTypeManager,
                           IssueEventManager issueEventManager,
                           UserManager userManager, SearchService searchService,
                           CustomFieldManager customFieldManager,
                           NotificationSchemeManager notificationSchemeManager,
                           ProjectManager projectManager,
                           UserPreferencesManager userPreferencesManager,
                           EventPublisher eventPublisher,
                           IssueService issueService) {
        this.issueManager = issueManager;
        this.applicationProperties = applicationProperties;
        this.eventTypeManager = eventTypeManager;
        this.issueEventManager = issueEventManager;
        this.userManager = userManager;
        this.searchService = searchService;
        this.customFieldManager = customFieldManager;
        this.notificationSchemeManager = notificationSchemeManager;
        this.projectManager = projectManager;
        this.userPreferencesManager = userPreferencesManager;
        this.eventPublisher = eventPublisher;
        this.issueService = issueService;
    }

    public String doSlacreation() {
        User remoteUser = userManager.getUser("megaplan");
        String jqlQuery = "project=MPS and status=1";
        List<Issue> lissues = new ArrayList<Issue>();
        SearchService.ParseResult parseResult = searchService.
                parseQuery(remoteUser, jqlQuery);
        log.warn(parseResult.isValid());
        if (parseResult.isValid()) {
            Query query = parseResult.getQuery();
            try {
                SearchResults results = searchService.search(remoteUser, query,
                        PagerFilter.getUnlimitedFilter());
                lissues = results.getIssues();
            } catch (SearchException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        CustomField cf=  customFieldManager.getCustomFieldObject(13560L);
        CustomField slaCf = customFieldManager.getCustomFieldObject(12266L);
        List<String> issuesStrings = new ArrayList<String>();
        Long newIssueEvent = 10000L;
        for (Issue i : lissues) {
            Object val = cf.getValue(i);
            Object slaVal = slaCf.getValue(i);

            if (val != null) continue;
            if (slaVal != null) {
                log.warn(i + " SLA NOT NULL");
                issueEventManager.dispatchEvent(newIssueEvent,i,remoteUser,false);
            }
            String stringIssue = i.getKey() + " : " + val;
            issuesStrings.add(stringIssue);

            //issueEventManager.dispatchEvent(newIssueEvent,i,params,remoteUser,false);
        }
        issues = issuesStrings.toArray(new String[issuesStrings.size()]);
        return SUCCESS;
    }

    public String doTestmail() throws GenericEntityException, AtlassianCoreException {
       // MutableIssue i = issueManager.getIssueObject("TEST-1");
      //  log.warn("issue : " + i);
        return SUCCESS;
    }

    public String doMigrateField() throws GenericEntityException {
        CustomField oldExpireTimeCf = customFieldManager.getCustomFieldObjectByName("Дата окончания поддержки2");
        CustomField newExpireTimeCf = customFieldManager.getCustomFieldObjectByName("Дата окончания поддержки2");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat inputDateFormat = new SimpleDateFormat("d/MMM/yy");
        User megabot = userManager.getUser("megaplan");
        List<String> issuesStrings = new ArrayList<String>();
        int cap = 1000;
        int iter = 0;
        for (Long id : issueManager.getIssueIdsForProject(projectManager.getProjectObjByKey("MPS").getId())) {
            Issue i = issueManager.getIssueObject(id);
            Object oldTimeObject = i.getCustomFieldValue(oldExpireTimeCf);
            if (oldTimeObject != null) {
                Date sqlFormatDate = null;
                try {
                    sqlFormatDate = new Date(dateFormat.parse(oldTimeObject.toString()).getTime());
                } catch (ParseException e) {
                    continue;
                }
                IssueInputParameters iip = issueService.newIssueInputParameters();
                iip.addCustomFieldValue(newExpireTimeCf.getIdAsLong(), inputDateFormat.format(sqlFormatDate));
                iip.addCustomFieldValue(oldExpireTimeCf.getIdAsLong(), "");
                IssueService.UpdateValidationResult issueValidationResult = issueService.validateUpdate(megabot, i.getId(), iip);
                issuesStrings.add(i.getKey() + " : " + sqlFormatDate.toString());
                if (issueValidationResult.isValid()) {
                    issueService.update(megabot,issueValidationResult);
                } else {
                    issuesStrings.add(issueValidationResult.getErrorCollection().getErrors().toString());
                }
                if (iter == cap) break;
                iter++;
            }

        }
        issues = issuesStrings.toArray(new String[issuesStrings.size()]);
        return SUCCESS;
    }

    public String[] getIssues() {
        return issues;
    }

    public void setIssues(String[] issues) {
        this.issues = issues;
    }
}
