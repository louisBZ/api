package com.example.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.api.model.Move;

@Repository
public interface MoveRepository extends JpaRepository<Move, Long> {
  List<Move> findFirst50ByOrderByCreationDateDesc();

  List<Move> findByRef(String ref);

}