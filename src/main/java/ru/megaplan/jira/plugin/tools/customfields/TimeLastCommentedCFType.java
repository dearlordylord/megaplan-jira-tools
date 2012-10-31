package ru.megaplan.jira.plugin.tools.customfields;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.NumberTool;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class TimeLastCommentedCFType extends CalculatedCFType implements SortableCustomField, DateField
{
    private static final Logger log = Logger.getLogger(TimeLastCommentedCFType.class);

    private final JiraAuthenticationContext authenticationContext;
    private CommentManager commentManager;

    public TimeLastCommentedCFType(CommentManager commentManager, JiraAuthenticationContext authenticationContext)
    {
        this.commentManager = commentManager;
        this.authenticationContext = authenticationContext;
    }

    public String getStringFromSingularObject(Object customFieldObject)
    {
        assertObjectImplementsType(Long.class, customFieldObject);
        return customFieldObject.toString();
    }

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        if (string != null)
        {
            return new Long(string);
        }
        else
        {
            return null;
        }
    }

    public Object getValueFromIssue(CustomField field, Issue issue)
    {
        User currentUser = authenticationContext.getLoggedInUser();
        Number lastCommentedByUser = null;
        try
        {
            List comments = commentManager.getCommentsForUser(issue, currentUser);
            Date lastCommentedDate;
            if (comments != null && !comments.isEmpty())
            {
                Comment lastComment = (Comment) comments.get(comments.size() - 1);
                lastCommentedDate = lastComment.getCreated();
            }
            else
            {
                return null;
            }

            Date today = new Date();
            long mills = (today.getTime() - lastCommentedDate.getTime());

            // do some rounding
            if (mills > DateUtils.HOUR_MILLIS)
            {
                mills = mills - (mills % DateUtils.HOUR_MILLIS);
            }

            lastCommentedByUser = new Long(mills/1000);
        }
        catch (Exception e)
        {
            log.warn(e, e);
        }

        return lastCommentedByUser;
    }

    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        map.put("numberTool", new NumberTool());
        return map;
    }
}