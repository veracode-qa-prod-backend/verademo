package com.veracode.verademo.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserFactory {
	private static final String COOKIE_NAME = "user";

	private static final Logger logger = LogManager.getLogger("VeraDemo:UserFactory");

	public static User createFromRequest(HttpServletRequest req) {
		Cookie cookie = getCookieFromRequestByName(req, COOKIE_NAME);

		if (cookie == null) {
			logger.info("No user cookie");
			return null;
		}

		if (cookie.getValue().isEmpty()) {
			logger.info("User cookie is empty");
			return null;
		}

		try {
			// Decode Base64 cookie value
			byte[] decodedBytes = Base64.getDecoder().decode(cookie.getValue());
			String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);

			// Use JSON deserialization instead of Java object deserialization
			ObjectMapper objectMapper = new ObjectMapper();
			User user = objectMapper.readValue(jsonString, User.class);

			return user;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static Cookie getCookieFromRequestByName(HttpServletRequest req, String username) {
		if (req == null) {
			logger.info("No request");
			return null;
		}

		Cookie[] cookies = req.getCookies();

		if (cookies == null) {
			logger.info("No cookies");
			return null;
		}

		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals(username)) {
				return cookies[i];
			}
		}
		return null;
	}

	public static HttpServletResponse updateInResponse(User user, HttpServletResponse response) {
		try {
			// Use JSON serialization instead of Java object serialization
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(user);

			// Encode to Base64
			byte[] encodedBytes = Base64.getEncoder().encode(jsonString.getBytes(StandardCharsets.UTF_8));
			String encodedValue = new String(encodedBytes, StandardCharsets.UTF_8);

			Utils.setCookie(response, COOKIE_NAME, encodedValue);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response;
	}
}
