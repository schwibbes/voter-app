package com.github.schwibbes.voter;

import java.util.List;

import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Poll;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PopupView;

public class PollViewModel {

	private final Poll poll;
	private ListSelect<ItemAndScore> rank;
	private List<PopupView> popups;

	public PollViewModel(Poll poll) {
		this.poll = poll;
	}

	public void updateDisplay() {
		rank.clear();
		rank.setItems(poll.getInOrder());
		popups.forEach(PopupView::markAsDirty);
	}

	public Poll getPoll() {
		return poll;
	}

	public ListSelect<ItemAndScore> getRank() {
		return rank;
	}

	public void setRank(ListSelect<ItemAndScore> rank) {
		this.rank = rank;
	}

	public List<PopupView> getPopups() {
		return popups;
	}

	public void setPopups(List<PopupView> popups) {
		this.popups = popups;
	}
}
