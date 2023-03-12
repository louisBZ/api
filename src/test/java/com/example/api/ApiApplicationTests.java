package com.example.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.when;

import com.example.api.service.MoveService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.api.controller.MoveController;
import com.example.api.model.Move;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MoveController.class)
class ApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MoveService moveService;

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	@Test
	public void testSaveMove() throws Exception {
		Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
		Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
				"CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
				"AWB", "666", 12, 345, 12, 345, "ELECTRONICS");
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		String requestJson = mapper.writeValueAsString(move);
		when(moveService.saveMove(move)).thenReturn(move);
		this.mockMvc.perform(post("/saveMove").contentType(APPLICATION_JSON_UTF8).content(requestJson)).andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(requestJson));
	}

	@Test
	public void testGetMoves() throws Exception {
		this.mockMvc.perform(get("/moves")).andDo(print())
				.andExpect(status().isOk());
	}

}
