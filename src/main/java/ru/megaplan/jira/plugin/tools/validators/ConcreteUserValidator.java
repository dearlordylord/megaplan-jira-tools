package ru.megaplan.jira.plugin.tools.validators;


import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;

import java.util.Map;

import static ru.megaplan.jira.plugin.tools.validators.UserInGroupValidatorFactory.GROUP_NAME;

/**
 * Created with IntelliJ IDEA.
 * User: i.loskutov
 * Date: 24.05.12
 * Time: 19:21
 * To change this template use File | Settings | File Templates.
 */

public class ConcreteUserValidator implements Validator {

    private final static Logger log = Logger.getLogger(ConcreteUserValidator.class);

    private final UserManager userManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ConcreteUserValidator(UserManager userManager, JiraAuthenticationContext jiraAuthenticationContext) {
          this.userManager = userManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }
    @Override
    public void validate(Map transientVars, Map args, PropertySet propertySet) throws WorkflowException {
        Issue issue = (Issue) transientVars.get("issue");
        String reporterName = issue.getReporter().getName();
        String name = (String) args.get(ConcreteUserValidatorFactory.USER_NAME);
        if (reporterName != null && reporterName.equals(name)) throw new InvalidInputException("Author : " + name + " is not allowed!");
    }
}
