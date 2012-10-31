package ru.megaplan.jira.plugin.tools.customfields.searcher;

import com.atlassian.jira.issue.customfields.searchers.ExactTextSearcher;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;

/**
 * @since JIRA 4.0
 */
public class LastCommentSearcher extends ExactTextSearcher
{

    public LastCommentSearcher(final JqlOperandResolver jqlOperandResolver, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(jqlOperandResolver, customFieldInputHelper);
    }
}