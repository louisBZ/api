package com.example.api;

import com.example.api.service.MoveService;
import com.example.api.model.Move;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
public class MoveServiceTests {

  @Autowired
  private MoveService moveService;

  @Test
  void contextLoads() {
  }

  @Test
  public void testSaveMove() throws Exception {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    Move result = moveService.saveMove(move);
    Move expected = move;
    expected.setId((long) 1);
    expected.setMoveDate(result.getMoveDate());
    assertEquals(expected, result);
  }

  @Test
  public void testSaveMoveRefTypeException() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "AWB", "666", 12, 345, 12, 345, "ELECTRONICS");
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.saveMove(move);
    });
    String expectedMessage = "This move dont respect all buisness rules : isRefValid() : Ref type is AWB so ref must have 11 characters.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testSaveMoveQuantityException() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 344, "ELECTRONICS");
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.saveMove(move);
    });
    String expectedMessage = "This move dont respect all buisness rules : isQuantityWeightValid() : Total quantity and total weight must be superior or equal to quantity and weight.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testSaveMoveWeightException() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 11, 345, "ELECTRONICS");
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.saveMove(move);
    });
    String expectedMessage = "This move dont respect all buisness rules : isQuantityWeightValid() : Total quantity and total weight must be superior or equal to quantity and weight.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testSaveMoveQuantityAndWeightException() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 11, 344, "ELECTRONICS");
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.saveMove(move);
    });
    String expectedMessage = "This move dont respect all buisness rules : isQuantityWeightValid() : Total quantity and total weight must be superior or equal to quantity and weight.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testSaveMoveEntryAlreadyExistException() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move moveIn = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    moveService.saveMove(moveIn);
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.saveMove(moveIn);
    });
    String expectedMessage = "This move dont respect all buisness rules : isRefValid() : An entry with this reference already exists.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testSaveMoveOutputAlreadyExistException() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move moveIn = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    moveService.saveMove(moveIn);
    Move moveOut = new Move("A", "abcd", creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    moveService.saveMove(moveOut);
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.saveMove(moveOut);
    });
    String expectedMessage = "This move dont respect all buisness rules : isRefValid() : An output with this reference already exists.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testSaveMoveEntryNotExistException() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move moveOut = new Move("A", "abcd", creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.saveMove(moveOut);
    });
    String expectedMessage = "This move dont respect all buisness rules : isRefValid() : There is no entry with this reference.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testGetMovesException() {
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.getMoves();
    });
    String expectedMessage = "There is no move saved.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testGetMoves() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    moveService.saveMove(move);
    List<Move> result = moveService.getMoves();
    assertEquals(Arrays.asList(move), result);
  }

  // need to comment sendMail() in move service
  // because of free account cannot send too many mail
  @Test
  public void testGetMovesFirst50() {
    String baseref = "0771234567";
    for (int i = 0; i < 53; i++) {
      Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
      Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
      Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
          "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
          "132", baseref + String.valueOf(i), 12, 345, 12, 345, "ELECTRONICS");
      moveService.saveMove(move);
    }
    List<Move> result = moveService.getMoves();
    assertEquals(50, result.size());
  }

  @Test
  public void testGetMoveException() {
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.getMove((long) 1);
    });
    String expectedMessage = "This move doesn't exist.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testGetMove() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    moveService.saveMove(move);
    Move result = moveService.getMove((long) 1);
    move.setId((long) 1);
    move.setMoveDate(result.getMoveDate());
    assertEquals(move, result);
  }

  @Test
  public void testDeleteMoveException() {
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.deleteMove((long) 1);
    });
    String expectedMessage = "Cannot delete this move doesn't exist.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testDeleteMove() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    moveService.saveMove(move);
    moveService.deleteMove((long) 1);
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.getMove((long) 1);
    });
    String expectedMessage = "This move doesn't exist.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testUpdateMoveException() {
    Move move = new Move();
    Exception exception = assertThrows(ResponseStatusException.class, () -> {
      moveService.updateMove((long) 1, move);
    });
    String expectedMessage = "Cannot update this move doesn't exist.";
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  @Test
  public void testUpdateMove() {
    Instant creationDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant moveDate = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
    Move move = new Move(creationDate, "Pierre", moveDate, "RapidCargo CDG",
        "CDGRC1", "CDGAF1", "Air Cargo CDG 1", "X",
        "1", "666", 12, 345, 12, 345, "ELECTRONICS");
    moveService.saveMove(move);
    move.setDescription("test");
    move.setCustomsStatus("test");
    Move result = moveService.updateMove((long) 1, move);
    assertEquals(move, result);
  }
}
