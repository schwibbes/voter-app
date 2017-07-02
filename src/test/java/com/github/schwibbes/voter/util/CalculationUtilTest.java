package com.github.schwibbes.voter.util;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.*;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Vote;
import com.github.schwibbes.voter.data.Voter;
import com.google.common.collect.Lists;

public class CalculationUtilTest {

	private final Voter voterA = new Voter("A");
	private final Voter voterB = new Voter("B");
	private final Voter voterC = new Voter("C");
	private final Item itemA = new Item("A");
	private final Item itemB = new Item("B");
	private final Item itemC = new Item("C");
	private final CalculationUtil underTest = new CalculationUtil();

	@Test
	public void score_for_one_item_should_be_added() {
		final List<Vote> in = Lists.newArrayList(
				new Vote(voterA, new ItemAndScore(itemA, 1)),
				new Vote(voterA, new ItemAndScore(itemA, 2)),
				new Vote(voterA, new ItemAndScore(itemA, 3)));
		final List<ItemAndScore> result = underTest.mergeVotes(in);

		final List<ItemAndScore> expected = Lists.newArrayList(new ItemAndScore(itemA, 1 + 2 + 3));
		assertEquals(expected, result);
	}

	@Test
	public void score_for_multiple_item_should_be_added() {
		final List<Vote> in = Lists.newArrayList(
				new Vote(voterA, new ItemAndScore(itemA, 2)),
				new Vote(voterB, new ItemAndScore(itemB, 1)),
				new Vote(voterC, new ItemAndScore(itemB, 1)));
		final List<ItemAndScore> result = underTest.mergeVotes(in);

		final List<ItemAndScore> expected = Lists.newArrayList(
				new ItemAndScore(itemA, 2),
				new ItemAndScore(itemB, 1 + 1));
		assertEquals(expected, result);
	}

	@Test
	public void all_items_should_show_up_in_result() {
		final List<ItemAndScore> in = Lists.newArrayList(
				new ItemAndScore(itemA, 1),
				new ItemAndScore(itemB, 1),
				new ItemAndScore(itemC, 1));

		final List<ItemAndScore> result = underTest
				.mergeVotes(in.stream().map(x -> new Vote(voterA, x)).collect(toList()));

		assertThat(result, hasSize(in.size()));
		assertThat(result, containsInAnyOrder(in.toArray()));
	}

	@Test(expected = IllegalStateException.class)
	public void multiple_votes_from_one_voter_to_the_same_item_should_throw() {
		final List<Vote> votes = Lists.newArrayList(
				new Vote(voterA, new ItemAndScore(itemA, 1)),
				new Vote(voterA, new ItemAndScore(itemA, 1)));

		underTest.assertSingleVotePerVoter(votes);

	}

	@Test
	public void multiple_votes_from_one_voter_to_the_different_item_is_ok() {
		final List<Vote> votes = Lists.newArrayList(
				new Vote(voterA, new ItemAndScore(itemA, 1)),
				new Vote(voterA, new ItemAndScore(itemB, 1)),
				new Vote(voterA, new ItemAndScore(itemC, 1)));

		underTest.assertSingleVotePerVoter(votes);

	}

	@Test
	public void multiple_votes_from_different_voters_to_the_same_item_is_ok() {
		final List<Vote> votes = Lists.newArrayList(
				new Vote(voterA, new ItemAndScore(itemA, 1)),
				new Vote(voterB, new ItemAndScore(itemA, 1)),
				new Vote(voterC, new ItemAndScore(itemA, 1)));

		underTest.assertSingleVotePerVoter(votes);

	}

	@Test
	public void voter_factor() {
		final Poll p1 = new Poll("A").addVoter(voterA);
		final Poll p2 = new Poll("B").addVoter(voterA).addVoter(voterB);
		final Poll p3 = new Poll("B").addVoter(voterA).addVoter(voterB).addVoter(voterC);

		final Map<Poll, Integer> factors = underTest.findFactorByNumberOfVoters(Arrays.asList(p1, p2, p3));

		assertThat(factors.get(p1), is(2 * factors.get(p2)));
		assertThat(factors.get(p1), is(3 * factors.get(p3)));
	}

	@Test
	public void voter_factor_2() {
		final Poll twoVoters = new Poll("two")
			.addVoter(new Voter("A")).addVoter(new Voter("B"))
			.addItem(new Item("C")).addItem(new Item("D"));

		final Poll oneVoter = new Poll("one")
			.addVoter(new Voter("voter"))
			.addItem(new Item("A")).addItem(new Item("B"));

		final Poll threeVoters = new Poll("three")
			.addVoter(new Voter("voter1")).addVoter(new Voter("voter2")).addVoter(new Voter("voter3"))
			.addItem(new Item("A")).addItem(new Item("B"));


		final Map<Poll, Integer> factors = underTest.findFactorByNumberOfVoters(
			Arrays.asList(oneVoter, twoVoters, threeVoters));

		assertThat(factors.get(twoVoters), is(3));
		assertThat(factors.get(threeVoters), is(2));
		assertThat(factors.get(oneVoter), is(6));
	}

	@Test
	public void voter_factor_3() {
		final Poll twoWithRepeats = new Poll("two-with-repeats")
			.addVoter(new Voter("A")).addVoter(new Voter("B"))
			.addItem(new Item("A")).addItem(new Item("B"));

		final Poll oneVoter = new Poll("one")
			.addVoter(new Voter("voter"))
			.addItem(new Item("A")).addItem(new Item("B"));

		final Poll threeVoters = new Poll("three")
			.addVoter(new Voter("voter1")).addVoter(new Voter("voter2")).addVoter(new Voter("voter3"))
			.addItem(new Item("A")).addItem(new Item("B"));


		final Map<Poll, Integer> factors = underTest.findFactorByNumberOfVoters(
			Arrays.asList(oneVoter, twoWithRepeats, threeVoters));

		assertThat(factors.get(twoWithRepeats), is(3));
		assertThat(factors.get(threeVoters), is(1));
		assertThat(factors.get(oneVoter), is(3));
	}
	@Ignore
	@Test
	public void result_should_not_be_sensitive_to_number_of_voters_per_poll_() {

		final Poll p1 = new Poll("A")
				.addItem(itemA)
				.addItem(itemB)
				.addVoter(voterA)
				.addVoter(voterB)
				.addVote(voterA, itemA, 3)
				.addVote(voterA, itemB, 2)
				.addVote(voterB, itemA, 3)
				.addVote(voterB, itemB, 2);

		final Poll p2 = new Poll("B")
				.addItem(itemA)
				.addItem(itemB)
				.addVoter(voterA)
				.addVote(voterA, itemA, 2)
				.addVote(voterA, itemB, 3);

		final List<ItemAndScore> result = underTest.mergePolls(
				Arrays.asList(p1, p2),
				Arrays.asList(1, 1));

		assertThat(result, hasSize(2));

		assertEquals(result.get(0).getScore(), result.get(1).getScore());

		assertEquals(itemB, result.get(0).getItem());
		assertEquals(itemA, result.get(1).getItem());
		System.out.println(result);
	}

	@Ignore
	@Test
	public void complex_poll_merge_1() {

		final Poll p1 = new Poll("A")
				.addItem(itemA)
				.addItem(itemB)
				.addVoter(voterA)
				.addVoter(voterB)
				.addVoter(voterC)
				.addVote(voterA, itemA, 3)
				.addVote(voterA, itemB, 2)
				.addVote(voterB, itemA, 3)
				.addVote(voterB, itemB, 2)
				.addVote(voterC, itemA, 3)
				.addVote(voterC, itemB, 2);

		final Poll p2 = new Poll("B")
				.addItem(itemA)
				.addItem(itemB)
				.addVoter(voterA)
				.addVote(voterA, itemA, 2)
				.addVote(voterA, itemB, 3);

		final Poll p3 = new Poll("C")
				.addItem(itemA)
				.addItem(itemB)
				.addVoter(voterB)
				.addVote(voterB, itemA, 2)
				.addVote(voterB, itemB, 3);

		final List<ItemAndScore> result = underTest.mergePolls(
				Arrays.asList(p1, p2, p3),
				Arrays.asList(1, 1, 1));

		assertThat(result, hasSize(2));
		assertThat(result.get(0).getItem(), is(itemB));
		assertThat(result.get(1).getItem(), is(itemA));
		assertThat(result.get(0).getScore(), is(
				1 * (2 + 2 + 2) +
						3 * (3) +
						3 * (3)));
		assertThat(result.get(1).getScore(), is(
				1 * (3 + 3 + 3) +
						3 * (2) +
						3 * (2)));
	}

	@Test
	public void solve_tie_game_strategy() {

		final Poll p1 = new Poll("A")
				.addVoter(voterA)
				.addItem(itemA)
				.addItem(itemB)
				.addVote(voterA, itemA, 3)
				.addVote(voterA, itemB, 1);

		final Poll p2 = new Poll("B")
				.addVoter(voterA)
				.addItem(itemA)
				.addItem(itemB)
				.addVote(voterA, itemA, 1)
				.addVote(voterA, itemB, 3);

		final List<ItemAndScore> result = underTest.mergePolls(
				Arrays.asList(p1, p2),
				Arrays.asList(1, 1));

		assertThat(result, hasSize(2));
		assertEquals(result.get(0).getScore(), result.get(1).getScore());

		assertEquals(itemB, result.get(0).getItem());
		assertEquals(itemA, result.get(1).getItem());

		Item winner = underTest.resolveTie(itemA, itemB, Arrays.asList(p1.getInOrder(), p2.getInOrder()));
		assertThat(winner, is(itemB));

		winner = underTest.resolveTie(itemA, itemB, Arrays.asList(p2.getInOrder(), p1.getInOrder()));
		assertThat(winner, is(itemA));

	}

	@Test
	public void solve_tie_game_strategy_2() {

		final Poll p1 = new Poll("A")
				.addVoter(voterA)
				.addItem(itemA)
				.addItem(itemB)
				.addVote(voterA, itemA, 3)
				.addVote(voterA, itemB, 1);

		final Poll p2 = new Poll("B")
				.addVoter(voterA)
				.addItem(itemA)
				.addItem(itemB)
				.addVote(voterA, itemA, 1)
				.addVote(voterA, itemB, 3);

		final List<ItemAndScore> result = underTest.mergePolls(
				Arrays.asList(p1, p2),
				Arrays.asList(1, 1));

		assertThat(result, hasSize(2));
		assertEquals(result.get(0).getScore(), result.get(1).getScore());

		assertEquals(itemB, result.get(0).getItem());
		assertEquals(itemA, result.get(1).getItem());

		Collections.sort(result,
				new ConflictResolvingItemComparator(Arrays.asList(p1.getInOrder(), p2.getInOrder())));
		assertThat(result.get(0).getItem(), is(itemB));
		assertThat(result.get(1).getItem(), is(itemA));

		Collections.sort(result,
				new ConflictResolvingItemComparator(Arrays.asList(p2.getInOrder(), p1.getInOrder())));
		assertThat(result.get(0).getItem(), is(itemA));
		assertThat(result.get(1).getItem(), is(itemB));

	}
}
