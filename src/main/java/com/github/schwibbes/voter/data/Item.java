package com.github.schwibbes.voter.data;

import java.util.UUID;

public class Item extends BaseEntity {

	public Item(String name) {
		setName(name);
		setId(UUID.randomUUID());
	}
}
