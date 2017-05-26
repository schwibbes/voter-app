package com.github.schwibbes.voter.data;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.schwibbes.voter.util.CalculationUtil;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Poll extends BaseEntity {

	// prepare poll
	private final List<Item> items;
	private final List<Voter> voters;
	private final List<Integer> scores;

	// state
	private final List<Vote> votes;

	// beans
	private final CalculationUtil calcUtil = new CalculationUtil();

	/**
	 * Default Values
	 */
	public Poll(String name) {
		this(name, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList(1, 2, 3), Lists.newArrayList());
	}

	public Poll(
			String name,
			List<Item> items,
			List<Voter> voters,
			List<Integer> scores,
			List<Vote> votes) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.items = Lists.newArrayList(items);
		this.voters = Lists.newArrayList(voters);
		this.scores = Lists.newArrayList(scores);
		this.votes = Lists.newArrayList(votes);
	}

	public Poll addVoter(Voter v) {
		final List<Voter> updated = Lists.newArrayList(voters);
		updated.add(v);
		return new Poll(name, items, updated, scores, votes);
	}

	public Poll addItem(Item i) {
		final List<Item> updated = Lists.newArrayList(items);
		updated.add(i);
		return new Poll(name, updated, voters, scores, votes);
	}

	public Poll addVote(Voter v, Item i, int score) {

		Preconditions.checkArgument(voters.contains(v));
		Preconditions.checkArgument(items.contains(i));
		Preconditions.checkArgument(scores.contains(score),
				"score {} not allowed, choose one of {}",
				score,
				scores);

		final List<Vote> updated = cleanupObsoleteVotes(v, i, score);
		// allways add current vote
		updated.add(new Vote(v, i, score));

		return new Poll(name, items, voters, scores, updated);
	}

	/**
	 * One voters can not vote for the same item twice OR give the same score
	 * twice
	 */
	private List<Vote> cleanupObsoleteVotes(Voter v, Item i, int score) {
		return votes.stream()
				.filter(vote -> !vote.isSameVoterAndScore(v, score))
				.filter(vote -> !vote.isSameVoterAndItem(v, i))
				.collect(toList());
	}

	public List<Item> getItems() {
		return items;
	}

	public List<Voter> getVoters() {
		return voters;
	}

	@JsonIgnore
	public List<ItemAndScore> getInOrder() {
		return calcUtil.mergeVotes(votes)
				.stream()
				.sorted()
				// .map(ItemAndScore::getItem)
				.collect(toList());
	}

	@JsonIgnore
	public List<Vote> getVotesForItem(Item i) {
		return votes.stream()
				.filter(v -> Objects.equal(i, v.getItemAndScore().getItem()))
				.collect(toList());
	}

	@JsonIgnore
	public List<Vote> getVotesByVoter(Voter voter) {
		return votes.stream()
				.filter(v -> Objects.equal(voter, v.getVoter()))
				.collect(toList());
	}

	@JsonIgnore
	public Optional<Vote> getVoteByVoterAndItem(Voter voter, Item item) {
		return votes.stream()
				.filter(v -> Objects.equal(voter, v.getVoter()))
				.filter(v -> Objects.equal(item, v.getItemAndScore().getItem()))
				.findFirst();
	}

	public List<Vote> getVotes() {
		return votes;
	}

	public List<Integer> getScores() {
		return scores;
	}

}
