package com.github.schwibbes.voter.data;

import static java.util.stream.Collectors.*;

import java.util.*;

import com.google.common.base.Preconditions;

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

	public Poll() {
		this("");
	}

	public Poll(String name) {
		this(UUID.randomUUID(),
				name,
				Lists.newArrayList(),
				Lists.newArrayList(),
				Lists.newArrayList(1, 2, 3),
				Lists.newArrayList());
	}

	public Poll(
			UUID id,
			String name,
			List<Item> items,
			List<Voter> voters,
			List<Integer> scores,
			List<Vote> votes) {
		Preconditions.checkArgument(voters.stream().map(Voter::getName).collect(toSet()).size() == voters.size());
		Preconditions.checkArgument(items.stream().map(Item::getName).collect(toSet()).size() == items.size());

		this.id = id;
		this.name = name;
		this.items = Lists.newArrayList(items);
		this.voters = Lists.newArrayList(voters);
		this.scores = Lists.newArrayList(scores);
		this.votes = Lists.newArrayList(votes);
	}

	public Poll addVoter(Voter v) {
		final List<Voter> updated = Lists.newArrayList(voters);
		updated.add(v);
		return new Poll(id, name, items, updated, scores, votes);
	}

	public Poll addItem(Item i) {
		final List<Item> updated = Lists.newArrayList(items);
		updated.add(i);
		return new Poll(id, name, updated, voters, scores, votes);
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

		return new Poll(id, name, items, voters, scores, updated);
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

	@JsonIgnore
	public int getDistinctFactor() {
		// # A B C D
		// A - + + +
		// B + - + +
		// C + + - +
		// D + + + -
		Set<String> itemNames = items.stream().map(Item::getName).collect(toSet());
		Set<String> voterAndItem = voters.stream().map(Voter::getName)
			.filter(x -> itemNames.contains(x))
			.collect(toSet());

		if (voterAndItem.size() == voters.size()) {
			return voters.size() - 1;
		} else if (voterAndItem.size() == 0) {
			return voters.size();
		} else {
			throw new RuntimeException("invalid poll: " + voterAndItem);
		}
	}

	public List<Vote> getVotes() {
		return votes;
	}

	public List<Integer> getScores() {
		return scores;
	}

}
