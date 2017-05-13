package com.github.schwibbes.voter.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Item extends BaseEntity {

	@JsonCreator
	public Item(@JsonProperty("id") UUID id, @JsonProperty("name") String name) {
		this.id = id;
		this.name = name;
	}

	public Item(String name) {
		this(UUID.randomUUID(), name);
	}
}
