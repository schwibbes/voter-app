package com.github.schwibbes.voter.util;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.schwibbes.voter.data.Poll;

public class JsonUtil {

	public static String stringify(Object o) {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			return mapper.writeValueAsString(o);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Poll> load(String json) {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			return mapper.readValue(json, new TypeReference<List<Poll>>() {
			});
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
