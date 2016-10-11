package de.cronn.jira.sync.resolve;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import de.cronn.jira.sync.JiraSyncException;
import de.cronn.jira.sync.domain.JiraIssue;
import de.cronn.jira.sync.domain.JiraRemoteLink;
import de.cronn.jira.sync.service.JiraService;

@Service
public class JiraIssueByExternalLinkResolver implements JiraIssueResolver {

	private static final Logger log = LoggerFactory.getLogger(JiraIssueByExternalLinkResolver.class);

	@Override
	public JiraIssue resolve(JiraIssue fromIssue, JiraService fromJiraService, JiraService toJiraService) {
		List<JiraIssue> jiraIssues = resolveIssues(fromIssue, fromJiraService, toJiraService);
		if (CollectionUtils.isEmpty(jiraIssues)) {
			return null;
		} else if (jiraIssues.size() > 1) {
			throw new JiraSyncException("Illegal number of linked jira issues for " + fromIssue + ": " + jiraIssues);
		} else {
			return jiraIssues.get(0);
		}
	}

	private List<JiraIssue> resolveIssues(JiraIssue fromIssue, JiraService fromJiraService, JiraService toJiraService) {
		List<JiraRemoteLink> remoteLinks = fromJiraService.getRemoteLinks(fromIssue);
		String toBaseUrl = toJiraService.getServerInfo().getBaseUrl();
		Pattern pattern = Pattern.compile("^" + Pattern.quote(toBaseUrl) + "/+browse/([A-Z_]+-\\d+)$");
		List<JiraIssue> resolvedIssues = new ArrayList<>();
		for (JiraRemoteLink remoteLink : remoteLinks) {
			URL remoteLinkUrl = remoteLink.getObject().getUrl();
			Matcher matcher = pattern.matcher(remoteLinkUrl.toString());
			if (matcher.matches()) {
				String key = matcher.group(1);
				log.debug("{}: found remote link: {} with key {}", fromIssue, remoteLinkUrl, key);
				JiraIssue resolvedIssue = toJiraService.getIssueByKey(key);
				if (resolvedIssue == null) {
					throw new JiraSyncException("Failed to resolve " + key + " in target");
				}
				resolvedIssues.add(resolvedIssue);
			} else {
				log.debug("{}: ignoring remote link: {}", fromIssue, remoteLinkUrl);
			}
		}
		return resolvedIssues;
	}

}
