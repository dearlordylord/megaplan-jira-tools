package ru.megaplan.jira.plugin.tools.customfields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import org.apache.log4j.Logger;

import java.util.List;

public class MPSLastCommentedByClientCFType extends CalculatedCFType implements SortableCustomField
{
    /** The group whose members are regarded as non-Users. */
    private static final String EMAIL_ACCOUNTS = "email-accounts";
    private static final Logger log = Logger.getLogger(MPSLastCommentedByClientCFType.class);

    private final JiraAuthenticationContext authenticationContext;

    private final CommentManager commentManager;
    private final UserUtil userUtil;
    private final GroupManager groupManager;

    public MPSLastCommentedByClientCFType(CommentManager commentManager, JiraAuthenticationContext authenticationContext, UserUtil userUtil, GroupManager groupManager)
    {
        this.commentManager = commentManager;
        this.authenticationContext = authenticationContext;
        this.userUtil = userUtil;
        this.groupManager = groupManager;
    }

    public String getStringFromSingularObject(Object value)
    {
        return value != null ? value.toString() : Boolean.FALSE.toString();
    }

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        if (string != null)
        {
            return (string);
        }
        else
        {
            return Boolean.FALSE.toString();
        }
    }

    public Object getValueFromIssue(CustomField field, Issue issue)
    {
        Boolean lastCommentedByClient = Boolean.FALSE;
        try
        {
            User lastCommentUser = null;
            User currentUser = authenticationContext.getLoggedInUser();
            List comments = commentManager.getCommentsForUser(issue, currentUser);
            if (comments != null && !comments.isEmpty())
            {
                Comment lastComment = (Comment) comments.get(comments.size() - 1);
                lastCommentUser = userUtil.getUser(lastComment.getAuthor());
            }

            if (lastCommentUser != null && groupManager.isUserInGroup(lastCommentUser.getName(), EMAIL_ACCOUNTS))
            {
                lastCommentedByClient = Boolean.TRUE;
            }
        }
        catch (Exception e)
        {
            log.debug(e.getMessage() + " - user most probably used to exist but doesn't now");
        }

        return lastCommentedByClient.toString();
    }
}