package ru.megaplan.jira.plugin.tools.customfields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.NumberTool;

import java.util.Collection;
import java.util.Map;

public class CommentCountCFType extends CalculatedCFType implements SortableCustomField
{

    private final JiraAuthenticationContext authenticationContext;

    private static final Double NO_COMMENTS = 0d;
    private CommentManager commentManager;

    public CommentCountCFType(CommentManager commentManager, JiraAuthenticationContext authenticationContext)
    {
        this.commentManager = commentManager;
        this.authenticationContext = authenticationContext;
    }

    public String getStringFromSingularObject(Object value)
    {
        return value != null ? value.toString() : "0";
    }

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        if (string != null)
        {
            return new Double(string);
        }
        else
        {
            return NO_COMMENTS;
        }
    }

    public Object getValueFromIssue(CustomField field, Issue issue)
    {
        if (issue != null)
        {
            Collection comments = commentManager.getCommentsForUser(issue, authenticationContext.getLoggedInUser());
            if (comments != null)
            {
                return new Double(comments.size());
            }
        }

        return NO_COMMENTS;
    }

    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        map.put("numberTool", new NumberTool());
        return map;
    }
}