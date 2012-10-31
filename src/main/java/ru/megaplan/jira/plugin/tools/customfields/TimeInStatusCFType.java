package ru.megaplan.jira.plugin.tools.customfields;

import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItem;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugin.tools.actions.CFEditStatusAction;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 7/1/12
 * Time: 8:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeInStatusCFType extends CalculatedCFType<Double,Double> {

    private final static Logger log = Logger.getLogger(TimeInStatusCFType.class);

    private final StatusManager statusManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final WorkflowManager workflowManager;
    private final OptionsManager optionsManager;
    private final PluginSettingsFactory pluginSettingsFactory;

    private final static String FIRST_STATUS_NUMBER = "1";

    TimeInStatusCFType(StatusManager statusManager,
                       ChangeHistoryManager changeHistoryManager,
                       WorkflowManager workflowManager,
                       OptionsManager optionsManager,
                       PluginSettingsFactory pluginSettingsFactory) {
        this.statusManager = statusManager;
        this.changeHistoryManager = changeHistoryManager;
        this.workflowManager = workflowManager;
        this.optionsManager = optionsManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    @NotNull
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        return Lists.<FieldConfigItemType>newArrayList(new FieldConfigItemType() {
            @Override
            public String getDisplayName() {
                return "Status id";
            }

            @Override
            public String getDisplayNameKey() {
                return null;
            }

            @Override
            public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem) {
                Object confObject = TimeInStatusCFType.getConfigurationObject(fieldConfig.getCustomField(), pluginSettingsFactory);
                if (confObject == null) confObject = "null";
                return "status : " + confObject.toString();
            }

            @Override
            public String getObjectKey() {
                return "status";
            }

            @Override
            public Object getConfigurationObject(Issue issue, FieldConfig fieldConfig) {
                return TimeInStatusCFType.getConfigurationObject(fieldConfig.getCustomField(), pluginSettingsFactory);
            }

            @Override
            public String getBaseEditUrl() {
                return "CFEditStatusAction!default.jspa";
            }
        });
    }

    @Override
    public String getStringFromSingularObject(Double o) {
        return o.toString();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Double getSingularObjectFromString(String s) throws FieldValidationException {
        return Double.parseDouble(s);
    }

    private static Object getConfigurationObject(CustomField cf, PluginSettingsFactory pluginSettingsFactory) {
        Object confObject = pluginSettingsFactory.createSettingsForKey(CFEditStatusAction.CONFIG_KEY)
                .get(CFEditStatusAction.CUSTOMFIELDPREFIX+cf.getIdAsLong());
        return confObject;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
        Map<String,Object> result = new HashMap<String, Object>();
        result.put("decimalFormat",new DecimalFormat("#"));
        return result;
    }

    @Override
    public Double getValueFromIssue(CustomField customField, Issue issue) {
        log.warn("getting value from issue : " + issue.getKey());
        Object confObject = getConfigurationObject(customField,pluginSettingsFactory);
        String statusId = confObject.toString();
        Status status = statusManager.getStatus(statusId);
        if (status == null) {
            log.error("can't see right status here");
            return null;
        }
        double time = getTimeInStatus(issue, status)/1000/60;
        if (time == 0) return null;
        return time;
    }

    private double getTimeInStatus(Issue issue, Status status) {
        LinkedHashMap<Timestamp, Status> changeMap = getStatusChangeMap(issue);
        double time = 0;
        Timestamp previous = null;
        for (Map.Entry<Timestamp,Status> entry : changeMap.entrySet()) {
            if (!status.equals(entry.getValue())) {
                if (previous != null) {
                    Timestamp next = entry.getKey();
                    time += next.getTime() - previous.getTime();
                    previous = null;
                }
                continue;
            }
            if (previous == null) {
                previous = entry.getKey();
            }
        }
        if (previous != null) { //we get to end with THIS status
            time += (new Date()).getTime() - previous.getTime();
        }
        return time;  //To change body of created methods use File | Settings | File Templates.
    }

    private LinkedHashMap<Timestamp, Status> getStatusChangeMap(Issue issue) {
        LinkedHashMap<Timestamp, Status> changeMap = new LinkedHashMap<Timestamp, Status>();
        java.util.List<ChangeItemBean> changes = changeHistoryManager.getChangeItemsForField(issue,"status");
        log.warn("changes size : " + changes.size());
        Timestamp firstChange = issue.getCreated();
        Status firstStatus = statusManager.getStatus(FIRST_STATUS_NUMBER);
        changeMap.put(firstChange,firstStatus);
        for (ChangeItemBean change : changes) {
            Timestamp changed = change.getCreated();
            String to = change.getTo();
            Status toStatus = statusManager.getStatus(to);
            changeMap.put(changed,toStatus);
        }
        log.warn("changeMap size : " + changeMap.size());
        return changeMap;
    }

}
