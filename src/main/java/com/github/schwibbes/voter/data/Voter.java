package com.github.schwibbes.voter.data;

import java.util.UUID;

public class Voter extends BaseEntity {

	public Voter(String name) {
		setName(name);
		setId(UUID.randomUUID());
	}

}
