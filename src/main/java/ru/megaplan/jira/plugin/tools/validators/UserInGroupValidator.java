package ru.megaplan.jira.plugin.tools.validators;


import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: i.loskutov
 * Date: 24.05.12
 * Time: 19:21
 * To change this template use File | Settings | File Templates.
 */
import static ru.megaplan.jira.plugin.tools.validators.UserInGroupValidatorFactory.GROUP_NAME;
public class UserInGroupValidator implements Validator {

    private final static Logger log = Logger.getLogger(UserInGroupValidator.class);

    private final GroupManager groupManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public UserInGroupValidator(GroupManager groupManager, JiraAuthenticationContext jiraAuthenticationContext) {
          this.groupManager = groupManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }
    @Override
    public void validate(Map transientVars, Map args, PropertySet propertySet) throws WorkflowException {
        User user = jiraAuthenticationContext.getLoggedInUser();
        String group = (String) args.get(GROUP_NAME);
        Group g = groupManager.getGroupObject(group) ;

        if (!groupManager.getGroupsForUser(user).contains(g)) {
            throw new InvalidInputException("The group membership : " + group + " is required!");
        }

    }
}
