/**
 *
 */
package ru.megaplan.jira.plugin.tools.customfields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comparator.UserComparator;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Get the author of either the last update or the last comment, whatever came latest.
 *
 * @author Paul Curren
 */
public class AuthorOfLastCommentCFType extends CalculatedCFType implements SortableCustomField, UserField
{

    private static final Logger log = Logger.getLogger(AuthorOfLastCommentCFType.class);
    private static final UserComparator USER_COMPARATOR = new UserComparator();

    private final CommentManager commentManager;
    private final UserConverter userConverter;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public AuthorOfLastCommentCFType(CommentManager commentManager, UserConverter userConverter,
                                      JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.commentManager = commentManager;
        this.userConverter = userConverter;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public Object getSingularObjectFromString(String s) throws FieldValidationException
    {
        return userConverter.getUser(s);
    }

    public String getStringFromSingularObject(Object obj)
    {
        return userConverter.getString((User) obj);
    }

    /**
     * @return a String representing the user name of the last commenter.
     */
    public Object getValueFromIssue(CustomField customfield, Issue issue)
    {
        User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        final List<Comment> comments = commentManager.getCommentsForUser(issue,loggedInUser);
        if (comments == null || comments.isEmpty()) return null;
        User commenter = comments.get(comments.size()-1).getAuthorUser();
        if (commenter == null) {
            log.warn("Username '" + comments.get(comments.size()-1).getAuthor() +
                    "' could not be found. The user has probably been deleted and was the last user to update issue '"
                    + issue.getKey() + "'.");
            return null;
        }
        return commenter;
    }

    public int compare(Object o1, Object o2, FieldConfig fieldConfig)
    {
        if (o1 instanceof User && o2 instanceof User)
        {
            return USER_COMPARATOR.compare((User) o1, (User) o2);
        }
        else
        {
            throw new IllegalArgumentException("Object passed must be null or of type User " + o1 + " " + o2);
        }
    }
}