package com.github.schwibbes.voter.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class Vote {
	private final Voter voter;
	private final ItemAndScore itemAndScore;

	public Vote(Voter voter, Item item, int score) {
		this(voter, new ItemAndScore(item, score));
	}

	@JsonCreator
	public Vote(@JsonProperty("voter") Voter voter, @JsonProperty("itemAndScore") ItemAndScore itemAndScore) {
		super();
		this.voter = voter;
		this.itemAndScore = itemAndScore;
	}

	public boolean isSameVoterAndScore(Voter v, int s) {
		return Objects.equal(v, voter) && Objects.equal(itemAndScore.getScore(), s);
	}

	public boolean isSameVoterAndItem(Voter v, Item i) {
		return Objects.equal(v, voter) && Objects.equal(itemAndScore.getItem(), i);
	}

	public boolean isSameItem(Item i) {
		return Objects.equal(itemAndScore.getItem(), i);
	}

	@Override
	public String toString() {
		return "Vote [voter=" + voter + ", itemAndScore=" + itemAndScore + "]";
	}

	public Voter getVoter() {
		return voter;
	}

	public ItemAndScore getItemAndScore() {
		return itemAndScore;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemAndScore == null) ? 0 : itemAndScore.hashCode());
		result = prime * result + ((voter == null) ? 0 : voter.hashCode());
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
		final Vote other = (Vote) obj;
		if (itemAndScore == null) {
			if (other.itemAndScore != null) {
				return false;
			}
		} else if (!itemAndScore.equals(other.itemAndScore)) {
			return false;
		}
		if (voter == null) {
			if (other.voter != null) {
				return false;
			}
		} else if (!voter.equals(other.voter)) {
			return false;
		}
		return true;
	}

}
