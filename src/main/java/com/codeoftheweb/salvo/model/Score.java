package com.codeoftheweb.salvo.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game")
    private Game game;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player")
    private Player player;

    public LocalDateTime finishDate;

    public double scores;

    public Score() {
    }

    public Score(Game game, Player player, LocalDateTime finishDate, double scores) {
        this.game = game;
        this.player = player;
        this.finishDate = LocalDateTime.now();
        this.scores = scores;
    }

    public long getId() {
        return id;
    }


    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public double getScores() {
        return scores;
    }

    public void setScores(double scores) {
        this.scores = scores;
    }


    public Map<String,Object> scoreDTO (){
        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("finishDate", this.finishDate);
        dto.put("player", this.player.getId());
        dto.put ("score", this.scores);
        return dto;
    }

}
