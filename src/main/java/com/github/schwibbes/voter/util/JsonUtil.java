package com.github.schwibbes.voter.util;

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
}
