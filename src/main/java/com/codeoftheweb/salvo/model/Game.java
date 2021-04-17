package com.codeoftheweb.salvo.model;


import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime dateGame;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    private Set<Score> score = new HashSet<>();

    public Game() {
    }

    public Game(LocalDateTime dateGame) {
        this.dateGame = dateGame;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getDateGame() {
        return dateGame;
    }

    public void setDateGame(LocalDateTime dateGame) {
        this.dateGame = dateGame;
    }

    public Game(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }


    public Map<String,Object> gameDTO (){
        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("created", this.getDateGame());
        dto.put("gamePlayers", this.getGamePlayers().stream().map(gamePlayer -> gamePlayer.gamePlayerDTO()).collect(Collectors.toList()));
        dto.put ("scores", this.score.stream().map(Score::scoreDTO).collect(Collectors.toList()));
        return dto;
    }


}
