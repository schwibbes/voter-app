package com.github.schwibbes.voter.data;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.schwibbes.voter.util.CalculationUtil;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Poll extends BaseEntity {

	private final List<Item> items;
	private final List<Voter> voters;
	private final Map<Voter, List<Item>> votes;
	private final Map<UUID, List<UUID>> jsonVotes;

	public Poll(String name) {
		this(name, Lists.newArrayList(), Lists.newArrayList(), Maps.newHashMap());
	}

	public Poll(String name,
			List<Item> items,
			List<Voter> voters,
			Map<Voter, List<Item>> votes) {
		setId(UUID.randomUUID());
		setName(name);
		this.items = Lists.newArrayList(items);
		this.voters = Lists.newArrayList(voters);
		this.votes = cloneVotes(votes);
		this.jsonVotes = Maps.newHashMap();
		votes.forEach((k, v) -> {
			jsonVotes.put(k.getId(), v.stream().map(Item::getId).collect(toList()));
		});
	}

	@JsonCreator()
	public static Poll createFromJson(
			@JsonProperty("id") String id,
			@JsonProperty("name") String name,
			@JsonProperty("items") List<Item> items,
			@JsonProperty("voters") List<Voter> voters,
			@JsonProperty("votes") Map<UUID, List<UUID>> jsonVotes) {

		final Poll result = new Poll(name, Lists.newArrayList(items), Lists.newArrayList(voters),
				fromData(items, voters, jsonVotes));
		result.setId(UUID.randomUUID());
		return result;
	}

	private static Map<Voter, List<Item>> fromData(List<Item> items,
			List<Voter> voters,
			Map<UUID, List<UUID>> jsonVotes2) {
		final Map<Voter, List<Item>> result = Maps.newHashMap();
		jsonVotes2.forEach((k, v) -> {
			result.put(findInList(Voter.class, k, voters),
					v.stream().map(i -> findInList(Item.class, i, items)).collect(toList()));
		});
		return result;
	}

	private static <T extends BaseEntity> T findInList(Class<T> clazz, UUID id, List<T> list) {
		return list.stream()
				.filter(x -> Objects.equal(id, x.getId()))
				.map(clazz::cast)
				.findFirst()
				.orElseThrow(
						() -> new IllegalArgumentException("not found with id: " + id));
	}

	private Map<Voter, List<Item>> cloneVotes(Map<Voter, List<Item>> votes) {
		final Map<Voter, List<Item>> result = Maps.newHashMap();
		votes.keySet().forEach(voter -> {
			result.put(voter, Lists.newArrayList(votes.get(voter)));
		});
		voters.stream().forEach(voter -> result.putIfAbsent(voter, Lists.newArrayList()));
		return result;
	}

	public Poll addVoter(Voter v) {
		final List<Voter> updated = Lists.newArrayList(voters);
		updated.add(v);
		return new Poll(name, items, updated, votes);
	}

	public Poll addItem(Item i) {
		final List<Item> updated = Lists.newArrayList(items);
		updated.add(i);
		return new Poll(name, updated, voters, votes);
	}

	public Poll vote(Voter v, Item i, int rank) {

		Preconditions.checkArgument(rank <= 3 && rank >= 1, "rank must be one of 1,2,3, but was: " + rank);

		final Map<Voter, List<Item>> updated = cloneVotes(votes);
		final List<Item> favourites = updated.get(v);
		favourites.add(Math.min(rank - 1, favourites.size()), i);

		return new Poll(name, items, voters, updated);
	}

	public List<Item> getItems() {
		return items;
	}

	public List<Voter> getVoters() {
		return voters;
	}

	@JsonIgnore
	public List<Item> getInOrder() {
		return CalculationUtil.mergeLists(votes.values());
	}

	@JsonIgnore
	public String get1st() {
		final List<Item> inOrder = getInOrder();
		return inOrder.isEmpty() ? "" : inOrder.iterator().next().getName();
	}

	@JsonIgnore
	public Map<Voter, List<Item>> getVotes() {
		return votes;
	}

	@JsonProperty("votes")
	public Map<UUID, List<UUID>> getJsonVotes() {
		return jsonVotes;
	}

	public int queryVote(Voter v, Item i) {
		if (!votes.containsKey(v)) {
			return -1;
		}
		return votes.get(v).indexOf(i);
	}

}
