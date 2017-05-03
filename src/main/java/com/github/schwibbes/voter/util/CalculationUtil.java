package com.github.schwibbes.voter.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.schwibbes.voter.data.Item;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class CalculationUtil {

	public static List<Item> mergeLists(Collection<List<Item>> in) {

		final Set<ScoredItem> result = in.stream()
				.flatMap(x -> x.stream())
				.map(x -> new ScoredItem(x, 0))
				.collect(toSet());

		in.stream().map(list -> scoreByItem(list)).forEach(list -> {
			list.stream().forEach(item -> {
				find(item, result).ifPresent(x -> x.score += item.score);
			});
		});

		return result.stream().sorted().map(si -> si.item).collect(toList());
	}

	private static <T> Optional<T> find(T object, Set<T> inList) {
		for (final T obj : inList) {
			if (Objects.equal(obj, object)) {
				return Optional.of(obj);
			}
		}
		return Optional.empty();

	}

	private static final class ScoredItem implements Comparable<ScoredItem> {
		private final Item item;
		private int score;

		public ScoredItem(Item item, int score) {
			this.item = item;
			this.score = score;
		}

		@Override
		public int compareTo(ScoredItem o) {
			return o.score - score;
		}

		@Override
		public String toString() {
			return "ScoredItem [item=" + item + ", score=" + score + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((item == null) ? 0 : item.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ScoredItem other = (ScoredItem) obj;
			if (item == null) {
				if (other.item != null) {
					return false;
				}
			} else if (!item.equals(other.item)) {
				return false;
			}
			return true;
		}
	}

	private static List<ScoredItem> scoreByItem(List<Item> l) {
		final List<ScoredItem> result = Lists.newArrayList();
		for (int i = 0; i < l.size(); i++) {
			result.add(new ScoredItem(l.get(i), mapScore(i)));
		}
		return result;
	}

	private static int mapScore(int i) {
		switch (i) {
		case 0:
			return 3;
		case 1:
			return 2;
		case 2:
			return 1;
		default:
			return 0;
		}
	}
}
