package com.example.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;

import com.example.api.service.MoveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.api.controller.MoveController;
import com.example.api.model.Move;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

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
		this.mockMvc.perform(post("/saveMove")
				.contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk())
				.andExpect(content().string(requestJson));
	}

	@Test
	public void testGetMoves() throws Exception {
		Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
		Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
				"CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
				"AWB", "666", 12, 345, 12, 345, "ELECTRONICS");
		List<Move> allMoves = Arrays.asList(move);
		when(moveService.getMoves()).thenReturn(allMoves);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		String requestJson = mapper.writeValueAsString(allMoves);
		mockMvc.perform(get("/moves")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(content().string(requestJson));
	}

	@Test
	public void testGetMove() throws Exception {
		Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
		Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
				"CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
				"AWB", "666", 12, 345, 12, 345, "ELECTRONICS");
		long id = 1;
		move.setId(id);
		when(moveService.getMove(id)).thenReturn(move);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		String requestJson = mapper.writeValueAsString(move);
		mockMvc.perform(get("/move/" + id)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(requestJson));
	}

	@Test
	public void testDeleteMove() throws Exception {
		mockMvc.perform(delete("/move/1")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void testUpdateMoves() throws Exception {
		Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
		Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
				"CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
				"AWB", "666", 12, 345, 12, 345, "ELECTRONICS");
		long id = 1;
		move.setId(id);
		when(moveService.updateMove(id, move)).thenReturn(move);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		String requestJson = mapper.writeValueAsString(move);
		mockMvc.perform(put("/move/" + id)
				.contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk())
				.andExpect(content().string(requestJson));
	}
}

// String baseref = "0771234567";
// for (int i = 0; i < 53; i++) {
// Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
// Instant moveDate = Instant.now().plus(1,
// ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
// Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
// "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
// "AWB", baseref + String.valueOf(i), 12, 345, 12, 345, "ELECTRONICS");
// moveRepository.save(move);
// }
// // GENERATE XML FILE
// long id = 1;
// Move move = moveRepository.findById(id).get();
// long newId = 666;
// move.setId(newId);
// move.setRefType("AWB");
// move.setRef("666");
// // move.setCustomsDocType("T1");
// // move.setCustomsDocRef("666");
// // move.setInOut(true);// Impossible car la référence n'éxiste pas
// moveService.saveMove(move);
