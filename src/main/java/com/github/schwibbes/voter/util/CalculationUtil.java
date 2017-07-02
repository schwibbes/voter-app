package com.github.schwibbes.voter.util;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Vote;
import com.github.schwibbes.voter.data.Voter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class CalculationUtil {

	private static final Logger log = LoggerFactory.getLogger(CalculationUtil.class);

	public void assertSingleVotePerVoter(List<Vote> votes) {
		final Multimap<Voter, Item> seen = HashMultimap.create();
		for (final Vote vote : votes) {
			final Item item = vote.getItemAndScore().getItem();
			final boolean putOk = seen.put(vote.getVoter(), item);
			Preconditions.checkState(putOk,
					"voter %s votes twice for item %s in votes %s",
					vote.getVoter(),
					item,
					votes);
		}

	}

	public List<ItemAndScore> mergePolls(List<Poll> polls, List<Integer> factors) {
		Preconditions.checkArgument(polls.size() == factors.size());

		final Map<Poll, Integer> voterFactor = findFactorByNumberOfVoters(polls);

		final List<ItemAndScore> result = polls.stream().reduce(
				(List<ItemAndScore>) new ArrayList<ItemAndScore>(),
				(acc, x) -> {
					final int factor = voterFactor.get(x) * factors.get(polls.indexOf(x));
					final List<ItemAndScore> newAcc = Lists.newArrayList(acc);
					for (final ItemAndScore itemInNewPoll : x.getInOrder()) {

						final Map<Boolean, List<ItemAndScore>> alreadyContainedItem = filterWithSameItem(
								itemInNewPoll.getItem(), newAcc);

						final List<ItemAndScore> itemsMatchingCurrent = alreadyContainedItem.get(true);
						if (itemsMatchingCurrent.isEmpty()) {
							log.info("merge-polls: add {}", itemInNewPoll.getItem().getName());
							newAcc.add(itemInNewPoll.updateByFactor(factor));
						} else {
							log.info("merge-polls: update {}", itemInNewPoll.getItem().getName());
							Preconditions.checkState(itemsMatchingCurrent.size() == 1);
							final boolean removeOk = newAcc.remove(itemsMatchingCurrent.get(0));
							Preconditions.checkState(removeOk);
							newAcc.add(new ItemAndScore(itemInNewPoll.getItem(),
									itemInNewPoll.getScore() * factor +
											itemsMatchingCurrent.get(0).getScore()));
						}
					}
					return newAcc;
				},
				(acc1, acc2) -> {
					throw new UnsupportedOperationException("combiner not implemented");
				});
		log.trace("merge polls: {} to result {}", polls, result);

		return result.stream().sorted().collect(toList());
	}

	@VisibleForTesting
	Map<Poll, Integer> findFactorByNumberOfVoters(List<Poll> polls) {

		final Map<Poll, Integer> result = polls.stream()
				.peek(p -> log.warn("factor for " + p.getName()))
				.collect(toMap(x -> x, currentPoll -> {
					final int multiplied = polls.stream()
							.filter(p -> !p.getId().equals(currentPoll.getId()))
							.peek(p -> log.warn("  {}: {}", p.getName(), p.getDistinctFactor()))
							.map(p -> p.getDistinctFactor())
							.reduce((a, b) -> a * b)
							.orElseThrow(() -> new IllegalArgumentException(""));
					return multiplied;
				}));
		log.info("poll factors {}", prettyPrintPollFactors(result));

		return result;
	}

	private List<String> prettyPrintPollFactors(final Map<Poll, Integer> result) {
		return result.entrySet()
				.stream()
				.map(x -> "" + x.getKey().getVoters().stream().map(Voter::getName).collect(toList())
						+ ": "
						+ x.getValue())
				.collect(toList());
	}

	private Map<Boolean, List<ItemAndScore>> filterWithSameItem(Item item, List<ItemAndScore> newAcc) {
		return newAcc.stream()
				.collect(partitioningBy(x -> Objects.equal(item, x.getItem())));
	}

	public List<ItemAndScore> mergeVotes(List<Vote> votes) {
		final List<ItemAndScore> result = votes.stream().reduce(
				(List<ItemAndScore>) new ArrayList<ItemAndScore>(),
				(acc, x) -> {
					if (exists(acc, x)) {
						log.debug("merge-votes: update {}", x.getItemAndScore().getItem().getName());
						return sumScoresForThisItem(acc, x);
					} else {
						log.debug("merge-votes: new {}", x.getItemAndScore().getItem().getName());
						return appendItemToList(acc, x);
					}
				},
				(acc1, acc2) -> {
					throw new UnsupportedOperationException("combiner not implemented");
				});
		log.trace("merge votes: {} to result {}", votes, result);
		return result;
	}

	private boolean exists(List<ItemAndScore> acc, Vote vote) {
		return acc.stream().anyMatch(x -> vote.isSameItem(x.getItem()));
	}

	private List<ItemAndScore> appendItemToList(List<ItemAndScore> before, Vote vote) {
		final List<ItemAndScore> result = Lists.newArrayList(before);
		result.add(vote.getItemAndScore());
		return result;
	}

	private List<ItemAndScore> sumScoresForThisItem(List<ItemAndScore> before,
			Vote vote) {
		return before.stream()
				.map(x -> {
					if (vote.isSameItem(x.getItem())) {
						return x.update(vote.getItemAndScore().getScore());
					} else {
						return x;
					}
				})
				.collect(toList());
	}

	public Item resolveTie(Item itemA, Item itemB, List<List<ItemAndScore>> allItemRankLists) {

		Item result = null;
		for (final List<ItemAndScore> list : allItemRankLists) {

			final Optional<ItemAndScore> aScore = list.stream()
					.filter(x -> Objects.equal(x.getItem(), itemA))
					.findFirst();
			final Optional<ItemAndScore> bScore = list.stream()
					.filter(x -> Objects.equal(x.getItem(), itemB))
					.findFirst();

			if (aScore.isPresent() && bScore.isPresent()) {
				final int _a = aScore.get().getScore();
				final int _b = bScore.get().getScore();

				if (_a == _b) {
					log.warn("items no not differ in this list: {} <-> {}", itemA, itemB);
				} else if (_a > _b) {
					result = aScore.get().getItem();
				} else if (_a < _b) {
					result = bScore.get().getItem();
				}
			} else if (!aScore.isPresent() && !bScore.isPresent()) {
				log.warn("nothing to compare", itemA, itemB);
			} else if (aScore.isPresent()) {
				result = aScore.get().getItem();
			} else if (bScore.isPresent()) {
				result = bScore.get().getItem();
			}
		}

		return result;
	}

}
