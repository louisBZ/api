package com.example.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.api.model.Move;
import com.example.api.service.MoveService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MoveController {

  @Autowired
  private MoveService moveService;

  /**
   * Create - Add a new move
   * 
   * @param move An object move
   * @return The move object saved
   */
  @PostMapping("/saveMove")
  public Move saveMove(@RequestBody Move move) throws ResponseStatusException {
    return moveService.saveMove(move);
  }

  /**
   * Read - Get all moves
   * 
   * @return - An List object of Move full filled
   */
  @GetMapping("/moves")
  public List<Move> getMoves() throws ResponseStatusException {
    return moveService.getMoves();
  }

  /**
   * Read - Get one move
   * 
   * @param id The id of the move
   * @return An Move object full filled
   */
  @GetMapping("/move/{id}")
  public Move getMove(@PathVariable("id") final Long id) throws ResponseStatusException {
    return moveService.getMove(id);
  }

  /**
   * Delete - Delete an move
   * 
   * @param id - The id of the move to delete
   */
  @DeleteMapping("/move/{id}")
  public void deleteMove(@PathVariable("id") final Long id) throws ResponseStatusException {
    moveService.deleteMove(id);
  }

  /**
   * Update - Update an existing move
   *
   * @param id   - The id of the move to update
   * @param move - The move object updated
   * @return
   */
  @PutMapping("/move/{id}")
  public Move updateMove(@PathVariable("id") final Long id, @RequestBody Move move) throws ResponseStatusException {
    return moveService.updateMove(id, move);
  }

}