package de.cronn.jira.sync.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JiraChangeLog implements Serializable {

	private static final long serialVersionUID = 1L;

	private int total;
	
	private List<JiraIssueHistoryEntry> history;
	
	public JiraChangeLog() {
		this.history = new ArrayList<>();
	}
	
	public JiraChangeLog(List<JiraIssueHistoryEntry> history) {
		this.history = history;
	}	

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<JiraIssueHistoryEntry> getHistory() {
		return history;
	}
	
	public void setHistory(List<JiraIssueHistoryEntry> history) {
		this.history = history;
	}
	
	public JiraChangeLog addHistoryEntry(JiraIssueHistoryEntry historyEntry) {
		this.history.add(historyEntry);
		return this;
	}
	
	public JiraIssueHistoryEntry getLatestStatusTransition() {
		return history.stream()
			.filter(historyEntry -> historyEntry.hasItemWithField("status"))
			.sorted(Comparator.comparing(JiraIssueHistoryEntry::getCreated).reversed())
			.findFirst().orElse(null);
	}


	
}
