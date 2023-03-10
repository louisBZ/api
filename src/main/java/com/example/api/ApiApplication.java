package com.example.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.api.model.Move;
import com.example.api.repository.MoveRepository;
import com.example.api.service.MoveService;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Bean
	CommandLineRunner init(MoveRepository moveRepository, MoveService moveService) {
		return args -> {
			String baseref = "0771234567";
			for (int i = 0; i < 53; i++) {
				Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
				Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
				Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
						"CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
						"AWB", baseref + String.valueOf(i), 12, 345, 12, 345, "ELECTRONICS");
				moveRepository.save(move);
			}
			// GENERATE XML FILE
			long id = 1;
			Move move = moveRepository.findById(id).get();
			long newId = 666;
			move.setId(newId);
			move.setRefType("AWB");
			move.setRef("11111111111");
			// move.setInOut(true);//Impossible car la référence n'éxiste pas
			moveService.saveMove(move);
		};
	}
}
