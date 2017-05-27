package com.github.schwibbes.voter;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.github.schwibbes.voter.data.Item;
import com.github.schwibbes.voter.data.Poll;
import com.google.common.collect.Lists;

@Component
@ConfigurationProperties()
public class Config {

	private final List<Poll> polls = Lists.newArrayList();
	private final List<Item> items = Lists.newArrayList();

	public List<Poll> getPolls() {
		return polls;
	}

	public List<Item> getItems() {
		return items;
	}
}
