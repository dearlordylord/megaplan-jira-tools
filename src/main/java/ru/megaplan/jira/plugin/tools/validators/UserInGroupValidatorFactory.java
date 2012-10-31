package ru.megaplan.jira.plugin.tools.validators;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: i.loskutov
 * Date: 24.05.12
 * Time: 19:25
 * To change this template use File | Settings | File Templates.
 */
public class UserInGroupValidatorFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginValidatorFactory {

    public static final String GROUP_NAME = "group";
    public static final String GROUPS = "groups";
    public static final String NOT_DEFINED = "Not Defined";
    private static final Logger log = Logger.getLogger(UserInGroupValidatorFactory.class);
    private final GroupManager groupManager;

    public UserInGroupValidatorFactory(GroupManager groupManager) {
         this.groupManager = groupManager;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        velocityParams.put(GROUPS, getAllGroups());
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor abstractDescriptor) {
        velocityParams.put(GROUP_NAME, getGroupName(abstractDescriptor));
        velocityParams.put(GROUPS, getAllGroups());
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor abstractDescriptor) {
        velocityParams.put(GROUP_NAME, getGroupName(abstractDescriptor));
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> conditionParams) {
        if (conditionParams != null && conditionParams.containsKey(GROUP_NAME)) {
            return EasyMap.build(GROUP_NAME, extractSingleParam(conditionParams, GROUP_NAME));
        }
        return EasyMap.build();
    }

    private String getGroupName(AbstractDescriptor abstractDescriptor) {
        if (!(abstractDescriptor instanceof ValidatorDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor.");
        }

        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) abstractDescriptor;

        String group = (String) validatorDescriptor.getArgs().get(GROUP_NAME);
        if (group != null && group.trim().length() > 0)
            return group;
        else
            return NOT_DEFINED;
    }

    private Collection<Group> getAllGroups() {
        return groupManager.getAllGroups();
    }

}
