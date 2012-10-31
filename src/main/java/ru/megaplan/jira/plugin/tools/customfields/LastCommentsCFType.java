package ru.megaplan.jira.plugin.tools.customfields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LastCommentsCFType extends CalculatedCFType  {

    private static final Logger log = Logger.getLogger(LastCommentsCFType.class);

    private JiraAuthenticationContext jiraAuthenticationContext;
    private CommentManager commentManager;
    private OptionsManager optionsManager;
    private RendererManager rendererManager;
    private IssueRenderContext issueRenderContext;

    private final static int MAX_COMMENTS = 1;
    private final static int MAX_CHARS = 200;
    public static final String END_COMMENT = "...";
    public static final String DATE_FORMAT = "dd/MMM/yy hh:mm aa";
    public static final SimpleDateFormat simpleDateForm = new SimpleDateFormat(DATE_FORMAT);


    public LastCommentsCFType(JiraAuthenticationContext jiraAuthenticationContext, CommentManager commentManager, OptionsManager optionsManager, RendererManager rendererManager) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.commentManager = commentManager;
        this.optionsManager = optionsManager;
        this.rendererManager = rendererManager;


    }

    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        try {
            map.put("value", StringEscapeUtils.escapeHtml(getValueFromIssue(field, issue).toString()));
        } catch (RuntimeException e) {
            map.put("errorMessage", e.getMessage());
        }

        map.put("configValues", MAX_COMMENTS + "," + MAX_CHARS);
        return map;
    }

    @Override
    public Object getValueFromIssue(CustomField field, Issue issue) {
        String commentTextToDisplay = "";

        int maxChars = MAX_CHARS - END_COMMENT.length();
        int maxComments = MAX_COMMENTS;


        try {

            StringBuffer commentDisplayText = new StringBuffer();
            int commentCharacterCount = 0;
            User remoteUser = jiraAuthenticationContext.getUser();

            if (issue != null && issue.isCreated()) {

                List<Comment> comments = commentManager.getCommentsForUser(issue, remoteUser);

                //    Collections.reverse(comments);

                for (int commentCount = comments.size(); commentCount-- > 0; ) {

                    String commentPrefix = "";

                    if (comments.size() - commentCount > maxComments) {
                        // Exceeded max number of comments - break out
                        break;
                    }

                    if (comments.size() != commentCount + 1) {
                        // Add HTML break for all but first comment
                        commentPrefix += "";
                    }

                    Comment comment = comments.get(commentCount);
                    if (maxComments != 1) {
                        commentPrefix += comment.getAuthorFullName() + " - " + simpleDateForm.format(comment.getCreated());
                    }


                    // append a space to the prefix
                    if( commentPrefix.length() > 0 )
                        commentPrefix += " ";

                    // number of characters over the maxChar limit. If positive
                    // then we have reached out limit.
                    String completeComment = commentPrefix + comment.getBody();
                    int newCommentBodyLength = commentCharacterCount + completeComment.length();
                    int excessCharacterLength = newCommentBodyLength - maxChars;
                    if (excessCharacterLength > 0) {
                        if (excessCharacterLength > comment.getBody().length()) {
                            break; // can't even fix the comment prefix in
                        } else {
                            int validCharacterLength = completeComment.length() - excessCharacterLength;
                            commentDisplayText.append(completeComment.substring(0, validCharacterLength) + END_COMMENT);
                            break;
                        }
                    }

                    commentDisplayText.append(completeComment);
                    commentCharacterCount += completeComment.length();
                }
            }

            commentTextToDisplay = commentDisplayText.toString();
//            issueRenderContext = issue.getIssueRenderContext();
//            RenderedCommentTextToDisplay = rendererManager.getRenderedContent("atlassian-wiki-renderer", commentTextToDisplay, issueRenderContext);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return commentTextToDisplay;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Long> remove(CustomField field) {
        return super.remove(field);
    }

    @Override
    public Object getSingularObjectFromString(String value) throws FieldValidationException {
        log.debug("getSingularObjectFromString() value=" + value);
        return value;
    }

    @Override
    public String getStringFromSingularObject(Object value) {
        log.debug("getStringFromSingularObject() value=" + value);
        return value.toString();
    }



}