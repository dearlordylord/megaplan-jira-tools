package ru.megaplan.jira.plugin.tools.servlet.filter;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/23/12
 * Time: 7:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemainingEstimateServletFilter implements Filter {

    private final static Set<String> enabledProjects = new HashSet<String>();

    {
        enabledProjects.add("MP");
        enabledProjects.add("PU");
        //enabledProjects.add("ONEC");
        //enabledProjects.add("NAV");
        //enabledProjects.add("UTINET");
    }

    //TODO: refactor this shit in js include with jira event "content changed" listener

    private final IssueManager issueManager;

    RemainingEstimateServletFilter(IssueManager issueManager) {
        this.issueManager = issueManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;





        filterChain.doFilter(servletRequest, servletResponse);
        if (request.getRequestURI().contains("browse/PU-") || request.getRequestURI().contains("browse/MP-") ||
                (request.getRequestURI().contains("secure/CreateWorklog") && request.getParameter("id") != null && isInEnabledProjects(request.getParameter("id")))) {
            StringBuilder inject = new StringBuilder();
            inject.append("<script>");
            inject.append("jQuery('#log-work-adjust-estimate-auto').parent().remove();");
            inject.append("jQuery('#log-work-adjust-estimate-leave').parent().remove();");
            inject.append("jQuery('#log-work-adjust-estimate-manual').parent().remove();");
            inject.append("jQuery('#log-work-adjust-estimate-new').attr('checked','checked');");
            inject.append("jQuery('#log-work-dialog label[for=\"comment\"]').append('<span class=\"aui-icon icon-required\"></span>');");
            inject.append("jQuery('#log-work-dialog legend span:contains(\"Remaining Estimate\")').append('<span class=\"aui-icon icon-required\"></span>');");
            inject.append("function toggleWorklogSubmit(){var submit=jQuery('#log-work #log-work-submit');var comment=jQuery('#log-work #comment');" +
                    "if(comment.val().length===0){submit.attr('disabled','disabled');}else{submit.removeAttr('disabled');};" +
                    "};");
            inject.append("toggleWorklogSubmit();");
            inject.append("jQuery('#log-work #comment').keyup(function(){toggleWorklogSubmit();})");
            inject.append("</script>");
            response.getWriter().write(inject.toString());
        }
    }

    private boolean isInEnabledProjects(String id) {
        Issue issue = issueManager.getIssueObject(Long.parseLong(id));
        String pk = issue.getProjectObject().getKey();
        return (enabledProjects.contains(pk));
    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
