package com.github.schwibbes.voter.util;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Vote;
import com.github.schwibbes.voter.data.Voter;
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

	public List<ItemAndScore> mergeVotes(List<Vote> votes) {
		final BiFunction<List<ItemAndScore>, Vote, List<ItemAndScore>> accumulator = (before, vote) -> {

			final boolean exists = before.stream().anyMatch(x -> vote.isSameItem(x.getItem()));

			if (exists) {
				log.info("exists {}", vote.getItemAndScore().getItem());
				return sumScoresForThisItem(before, vote);
			} else {
				log.info("new {}", vote.getItemAndScore().getItem());
				return appendItemToList(before, vote);
			}
		};

		final BinaryOperator<List<ItemAndScore>> combiner = new BinaryOperator<List<ItemAndScore>>() {

			@Override
			public List<ItemAndScore> apply(List<ItemAndScore> t, List<ItemAndScore> u) {
				throw new UnsupportedOperationException("combiner not implemented");
			}
		};
		final List<ItemAndScore> result = votes.stream().reduce(Lists.newArrayList(), accumulator, combiner);
		log.trace("merge votes: {} to result {}", votes, result);
		return result;
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

}
