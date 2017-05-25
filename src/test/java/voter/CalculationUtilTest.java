package voter;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.github.schwibbes.voter.data.Vote;
import com.github.schwibbes.voter.data.Voter;
import com.github.schwibbes.voter.util.CalculationUtil;
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

}
