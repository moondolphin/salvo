package com.codeoftheweb.salvo.repository;

import com.codeoftheweb.salvo.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, Long> {
}
