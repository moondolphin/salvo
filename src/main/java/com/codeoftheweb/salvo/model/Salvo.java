package com.codeoftheweb.salvo.model;

import org.hibernate.annotations.GenericGenerator;


import javax.persistence.*;
import java.util.*;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gameplayerId")
    private GamePlayer gamePlayer;


    private Integer turn;

    @ElementCollection
    @Column (name="salvolocation")
    private List<String> salvoLocations;

    public Salvo() {
        this.salvoLocations = new ArrayList<>();

    }


    public Salvo(GamePlayer gamePlayers, Integer turn, List<String> salvoLocations) {
        this.gamePlayer = gamePlayers;
        this.turn = turn;
        this.salvoLocations = salvoLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public long getId() {
        return id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public Integer getTurn() {
        return turn;
    }

    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }


    public Map<String,Object> salvoDTO (){
        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("player", getGamePlayer().getId());
        dto.put("turn", this.turn);
        dto.put("locations", this.salvoLocations);
        return dto;
    }



}
