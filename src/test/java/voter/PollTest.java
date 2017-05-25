package voter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Vote;
import com.github.schwibbes.voter.data.Voter;

@RunWith(MockitoJUnitRunner.class)
public class PollTest {

	private static final Item I_1 = new Item("I-1");
	private static final Item I_2 = new Item("I-2");
	private static final Item I_3 = new Item("I-3");

	private static final Voter V_1 = new Voter("V-1");
	private static final Voter V_2 = new Voter("V-2");

	private final List<Item> items = Lists.newArrayList();
	private final List<Voter> voters = Lists.newArrayList();
	private final List<Integer> scores = Lists.newArrayList(1, 2, 3);
	private final List<Vote> votes = Lists.newArrayList();

	@Test
	public void testAddItem() {
		final Poll in = new Poll("", items, voters, scores, votes);
		final Poll out = in.addItem(new Item("A"));
		assertThat(out.getItems(), hasSize(1));
		assertThat(out.getVoters(), hasSize(0));
		assertThat(out.getVotes(), hasSize(0));
	}

	@Test
	public void testAddVoter() {
		final Poll in = new Poll("", items, voters, scores, votes);
		final Poll out = in.addVoter(V_1);
		assertThat(out.getItems(), hasSize(0));
		assertThat(out.getVoters(), hasSize(1));
		assertThat(out.getVotes(), hasSize(0));
	}

	@Test
	public void first_vote_should_always_count() {
		items.add(I_1);
		voters.add(V_1);
		final Poll in = new Poll("", items, voters, scores, votes);
		final Poll out = in.addVote(V_1, I_1, 1);
		assertThat(out.getItems(), hasSize(1));
		assertThat(out.getVoters(), hasSize(1));
		assertThat(out.getVotes(), hasSize(1));

		assertThat(out.getVotesForItem(I_1), hasSize(1));
		assertThat(out.getVotesForItem(I_2), hasSize(0));

		assertThat(out.getVotesByVoter(V_1), hasSize(1));
		assertThat(out.getVotesByVoter(V_2), hasSize(0));
	}

	@Test
	public void vote_for_different_item_counts() {
		final List<Item> items = Lists.newArrayList(I_1, I_2);
		final List<Voter> voters = Lists.newArrayList(V_1);
		final List<Vote> votes = Lists.newArrayList(new Vote(V_1, I_1, 3));
		final Poll in = new Poll("", items, voters, scores, votes);

		final Poll out = in.addVote(V_1, I_2, 2);
		assertThat(out.getItems(), hasSize(2));
		assertThat(out.getVoters(), hasSize(1));
		assertThat(out.getVotes(), hasSize(2));

		assertThat(out.getVotesForItem(I_1), hasSize(1));
		assertThat(out.getVotesForItem(I_2), hasSize(1));

		assertThat(out.getVotesByVoter(V_1), hasSize(2));
		assertThat(out.getVotesByVoter(V_2), hasSize(0));
	}

	@Test
	public void vote_for_same_item_overwrites() {
		final List<Item> items = Lists.newArrayList(I_1);
		final List<Voter> voters = Lists.newArrayList(V_1);
		final List<Vote> votes = Lists.newArrayList(new Vote(V_1, I_1, 3));
		final Poll in = new Poll("", items, voters, scores, votes);

		final Poll out = in.addVote(V_1, I_1, 2);
		assertThat(out.getItems(), hasSize(1));
		assertThat(out.getVoters(), hasSize(1));
		assertThat(out.getVotes(), hasSize(1));

		assertThat(out.getVotesForItem(I_1), hasSize(1));
		assertThat(out.getVotesByVoter(V_1), hasSize(1));
		assertThat(in.getVoteByVoterAndItem(V_1, I_1).get().getItemAndScore().getScore(), is(3));
		assertThat(out.getVoteByVoterAndItem(V_1, I_1).get().getItemAndScore().getScore(), is(2));

	}

	@Test
	public void vote_for_different_item_with_same_score_removes_old_vote() {
		final List<Item> items = Lists.newArrayList(I_1, I_2);
		final List<Voter> voters = Lists.newArrayList(V_1);
		final List<Vote> votes = Lists.newArrayList(new Vote(V_1, I_1, 3));
		final Poll in = new Poll("", items, voters, scores, votes);

		final Poll out = in.addVote(V_1, I_2, 3);
		assertThat(out.getItems(), hasSize(2));
		assertThat(out.getVoters(), hasSize(1));
		assertThat(out.getVotes(), hasSize(1));

		assertThat(out.getVotesByVoter(V_1), hasSize(1));

		assertThat(in.getVotesForItem(I_1), hasSize(1));
		assertThat(in.getVotesForItem(I_2), hasSize(0));
		assertTrue(in.getVoteByVoterAndItem(V_1, I_1).isPresent());
		assertFalse(in.getVoteByVoterAndItem(V_1, I_2).isPresent());

		assertThat(out.getVotesForItem(I_1), hasSize(0));
		assertThat(out.getVotesForItem(I_2), hasSize(1));
		assertFalse(out.getVoteByVoterAndItem(V_1, I_1).isPresent());
		assertTrue(out.getVoteByVoterAndItem(V_1, I_2).isPresent());

		assertThat(out.getVoteByVoterAndItem(V_1, I_2).get().getItemAndScore().getScore(), is(3));

	}

}
