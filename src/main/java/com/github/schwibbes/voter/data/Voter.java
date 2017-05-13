package com.github.schwibbes.voter.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Voter extends BaseEntity {

	@JsonCreator
	public Voter(@JsonProperty("id") UUID id, @JsonProperty("name") String name) {
		this.id = id;
		this.name = name;
	}

	public Voter(String name) {
		this(UUID.randomUUID(), name);
	}

}
