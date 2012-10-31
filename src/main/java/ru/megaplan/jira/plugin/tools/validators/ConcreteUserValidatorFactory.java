package ru.megaplan.jira.plugin.tools.validators;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: i.loskutov
 * Date: 24.05.12
 * Time: 19:25
 * To change this template use File | Settings | File Templates.
 */
public class ConcreteUserValidatorFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginValidatorFactory {

    public static final String USER_NAME = "user";
    private static final Logger log = Logger.getLogger(ConcreteUserValidatorFactory.class);

    public ConcreteUserValidatorFactory() {
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {

    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor abstractDescriptor) {

    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor abstractDescriptor) {
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) abstractDescriptor;
        velocityParams.put(USER_NAME,functionDescriptor.getArgs().get(USER_NAME));
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> conditionParams) {
        if (conditionParams != null && conditionParams.containsKey(USER_NAME)) {
            return EasyMap.build(USER_NAME, extractSingleParam(conditionParams, USER_NAME));
        }
        return EasyMap.build();
    }


}
