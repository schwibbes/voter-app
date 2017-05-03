package voter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.util.CalculationUtil;
import com.google.common.collect.Lists;

public class CalculationUtilTest {

	private static final Item A = new Item("A");
	private static final Item B = new Item("B");
	private static final Item C = new Item("C");

	private static final Item D = new Item("D");
	private static final Item E = new Item("E");
	private static final Item F = new Item("F");

	@Test
	public void test() {
		final List<List<Item>> in = Arrays.asList( //
				Lists.newArrayList(A, B, C), //
				Lists.newArrayList(C, A, B));

		assertEquals(Lists.newArrayList(A, C, B), CalculationUtil.mergeLists(in));
	}

	@Test
	public void test2() {
		final List<List<Item>> in = Arrays.asList( //
				Lists.newArrayList(A, B, C), //
				Lists.newArrayList(A, B, C), //
				Lists.newArrayList(C, B, A));

		assertEquals(Lists.newArrayList(A, B, C), CalculationUtil.mergeLists(in));
	}

	@Test
	public void test3() {
		final List<List<Item>> in = Arrays.asList( //
				Lists.newArrayList(A, B, C, D), //
				Lists.newArrayList(A, B, C));

		assertEquals(Lists.newArrayList(A, B, C, D), CalculationUtil.mergeLists(in));
	}

	@Test
	public void test4() {
		final List<List<Item>> in = Arrays.asList( //
				Lists.newArrayList(A, B, C, D), //
				Lists.newArrayList(C, D, E, F));

		final List<Item> result = CalculationUtil.mergeLists(in);
		assertEquals(6, result.size());
		assertEquals(C, result.get(0));
		assertEquals(A, result.get(1));

	}

}
