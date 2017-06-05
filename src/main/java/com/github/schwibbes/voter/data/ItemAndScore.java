package com.github.schwibbes.voter.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemAndScore implements Comparable<ItemAndScore> {
	private final Item item;
	private final int score;

	@JsonCreator
	public ItemAndScore(@JsonProperty("item") Item item, @JsonProperty("score") int score) {
		this.item = item;
		this.score = score;
	}

	public Item getItem() {
		return item;
	}

	public int getScore() {
		return score;
	}

	public ItemAndScore update(int score) {
		return new ItemAndScore(item, this.score + score);
	}

	public ItemAndScore updateByFactor(int factor) {
		return new ItemAndScore(item, this.score * factor);
	}

	@Override
	public String toString() {
		return "ItemAndScore [item=" + item + ", score=" + score + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + score;
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
		final ItemAndScore other = (ItemAndScore) obj;
		if (item == null) {
			if (other.item != null) {
				return false;
			}
		} else if (!item.equals(other.item)) {
			return false;
		}
		if (score != other.score) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(ItemAndScore other) {
		return other.score - this.score;
	}

}
