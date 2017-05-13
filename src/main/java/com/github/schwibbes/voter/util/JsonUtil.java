package com.github.schwibbes.voter.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

	public static <T> T load(String json, Class<T> clazz) {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			return mapper.readValue(json, clazz);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
