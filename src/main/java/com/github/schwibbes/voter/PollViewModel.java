package com.github.schwibbes.voter;

import java.util.Set;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.util.JsonUtil;

public class PollViewModel {

	private String name;
	private Set<Item> rank;

	private String first;
	private String second;
	private String third;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Item> getRank() {
		return rank;
	}

	public void setRank(Set<Item> rank) {
		this.rank = rank;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getThird() {
		return third;
	}

	public void setThird(String third) {
		this.third = third;
	}

	@Override
	public String toString() {
		return JsonUtil.stringify(this);
	}

}
