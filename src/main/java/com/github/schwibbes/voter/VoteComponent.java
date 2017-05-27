package com.github.schwibbes.voter;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Vote;
import com.github.schwibbes.voter.data.Voter;
import com.google.common.base.Objects;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;

public class VoteComponent implements PopupView.Content {

	private static final long serialVersionUID = 1L;
	private final HorizontalLayout layout;
	private final Voter v;
	private final Item i;
	private final Poll p;

	VoteComponent(Poll my, List<Poll> all, Voter v, Item i, UpdateHandler listener) {
		this.p = my;
		this.v = v;
		this.i = i;
		layout = new HorizontalLayout();

		my.getScores().forEach(score -> {
			layout.addComponent(new Button(score + "Punkte", e -> {

				final Poll withVote = my.addVote(v, i, score);
				System.out.println(withVote.getId());
				final List<Poll> updated = all.stream().map(x -> {
					System.out.println(x.getId());
					if (Objects.equal(withVote.getId(), x.getId())) {
						return withVote;
					} else {
						return x;
					}
				}).collect(toList());
				listener.updatePolls(updated);
			}));
		});
	}

	@Override
	public final Component getPopupComponent() {
		return layout;
	}

	@Override
	public final String getMinimizedValueAsHTML() {
		final Optional<Vote> vote = p.getVoteByVoterAndItem(v, i);
		return vote.isPresent() ? ("" + vote.get().getItemAndScore().getScore()) : "-";
	}

}
