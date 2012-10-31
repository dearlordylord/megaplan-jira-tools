package ru.megaplan.jira.plugin.tools.actions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 7/1/12
 * Time: 10:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class CFEditStatusAction extends JiraWebActionSupport {

    private final PluginSettingsFactory pluginSettingsFactory;
    private final StatusManager statusManager;

    public static final String CONFIG_KEY = "ru.megaplan.jira.plugins.tools.customfields.timeinstatus.status";
    public static final String CUSTOMFIELDPREFIX = "customfield_";

    private String statusId;
    private Long fieldConfigId;
    private Long customFieldId;
    private FieldConfig fieldConfig;

    CFEditStatusAction(PluginSettingsFactory pluginSettingsFactory, StatusManager statusManager) {

        this.pluginSettingsFactory = pluginSettingsFactory;
        this.statusManager = statusManager;
    }

    public String doDefault() {
        PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(CONFIG_KEY);
        statusId = (String) pluginSettings.get(CUSTOMFIELDPREFIX + customFieldId);
        return INPUT;
    }

    @Override
    public String doExecute() {
        PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(CONFIG_KEY);
        Status status = statusManager.getStatus(statusId);
        if (status == null) {
            addErrorMessage("status with id " + statusId + " is wrong");
            statusId = null;
            return INPUT;
        }
        if (customFieldId == null || customFieldId == 0) {
            addErrorMessage("customFieldId do not set!");
            return INPUT;
        }
        pluginSettings.put(CUSTOMFIELDPREFIX + customFieldId, status.getId());
        return getRedirect("CFEditStatusAction!default.jspa?customFieldId="+customFieldId);
    }



    public void setFieldConfigId(Long fieldConfigId)
    {
        this.fieldConfigId = fieldConfigId;
    }

    public Long getFieldConfigId()
    {
        return fieldConfigId;
    }

    public FieldConfig getFieldConfig()
    {
        if (fieldConfig == null && fieldConfigId != null)
        {
            final FieldConfigManager fieldConfigManager = ComponentAccessor.getComponent(FieldConfigManager.class);
            fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);
        }

        return fieldConfig;
    }

    public CustomField getCustomField()
    {
        return getFieldConfig().getCustomField();
    }

    public Long getCustomFieldId() {
        return customFieldId;
    }

    public void setCustomFieldId(Long customFieldId) {
        this.customFieldId = customFieldId;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }
}
