package voter;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.Poll;
import com.github.schwibbes.voter.data.Voter;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class PollTest {

	private static final Item I_1 = new Item("I-1");
	private static final Item I_2 = new Item("I-2");
	private static final Item I_3 = new Item("I-3");

	private static final Voter V_1 = new Voter("V-1");
	private static final Voter V_2 = new Voter("V-2");

	private final Map<Voter, List<Item>> votes = Maps.newHashMap();

	private final List<Item> items = Lists.newArrayList();

	private final List<Voter> voters = Lists.newArrayList();

	@Test
	public void testAddItem() {
		final Poll in = new Poll("", items, voters, votes);
		final Poll out = in.addItem(new Item("A"));
		assertEquals(1, out.getItems().size());
		assertEquals(0, out.getVoters().size());
		assertEquals(0, out.getVotes().size());
	}

	@Test
	public void testAddVoter() {
		final Poll in = new Poll("", items, voters, votes);
		final Poll out = in.addVoter(V_1);
		assertEquals(0, out.getItems().size());
		assertEquals(1, out.getVoters().size());
		assertEquals(1, out.getVotes().size());
		assertEquals(0, out.getVotes().get(V_1).size());
	}

	@Test
	public void testFirstVote() {
		items.add(I_1);
		voters.add(V_1);
		final Poll in = new Poll("", items, voters, votes);
		final Poll out = in.vote(V_1, I_1, 1);
		assertEquals(1, out.getItems().size());
		assertEquals(1, out.getVoters().size());
		assertEquals(1, out.getVotes().size());
		assertEquals(1, out.getVotes().get(V_1).size());
	}

	@Test
	public void testSecondVote() {
		items.add(I_1);
		items.add(I_2);
		voters.add(V_1);
		votes.put(V_1, Lists.newArrayList(I_1));
		final Poll in = new Poll("", items, voters, votes);
		final Poll out = in.vote(V_1, I_2, 1);
		assertEquals(2, out.getItems().size());
		assertEquals(1, out.getVoters().size());
		assertEquals(1, out.getVotes().size());
		assertEquals(I_2, out.getVotes().get(V_1).get(0));
		assertEquals(I_1, out.getVotes().get(V_1).get(1));
	}

	@Test
	public void testThirdVote() {
		items.add(I_1);
		items.add(I_2);
		items.add(I_3);
		voters.add(V_1);
		votes.put(V_1, Lists.newArrayList(I_2, I_1));
		final Poll in = new Poll("", items, voters, votes);
		final Poll out = in.vote(V_1, I_3, 1);
		assertEquals(3, out.getItems().size());
		assertEquals(1, out.getVoters().size());
		assertEquals(1, out.getVotes().size());
		assertEquals(I_3, out.getVotes().get(V_1).get(0));
		assertEquals(I_2, out.getVotes().get(V_1).get(1));
		assertEquals(I_1, out.getVotes().get(V_1).get(2));
	}

}
