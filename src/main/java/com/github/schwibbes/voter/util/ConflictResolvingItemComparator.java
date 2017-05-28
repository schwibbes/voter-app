package com.github.schwibbes.voter.util;

import java.util.Comparator;
import java.util.List;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.ItemAndScore;
import com.google.common.base.Objects;

public final class ConflictResolvingItemComparator implements Comparator<ItemAndScore> {

	private final List<List<ItemAndScore>> rankLists;
	private final CalculationUtil calculationUtil;

	public ConflictResolvingItemComparator(List<List<ItemAndScore>> rankLists) {
		this.rankLists = rankLists;
		calculationUtil = new CalculationUtil();
	}

	@Override
	public int compare(ItemAndScore o1, ItemAndScore o2) {

		final int scoreDiff = o2.getScore() - o1.getScore();

		if (scoreDiff != 0) {
			return scoreDiff;
		}

		final Item winner = calculationUtil.resolveTie(o1.getItem(), o2.getItem(), rankLists);
		if (Objects.equal(winner, o1.getItem())) {
			return -1;
		} else if (Objects.equal(winner, o2.getItem())) {
			return +1;
		} else {
			return 0;
		}
	}
}