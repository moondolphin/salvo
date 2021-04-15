package com.codeoftheweb.salvo.model;


import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private LocalDateTime date;



    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;


    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Ship> ship = new HashSet<>();

    @OrderBy
    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvoes;

    public GamePlayer() {
        this.date = LocalDateTime.now();
        this.ship = new HashSet<>();
        this.salvoes = new HashSet<>();
    }


    public GamePlayer(Player player, Game game) {
        this.date = LocalDateTime.now();
        this.player = player;
        this.game = game;
    }


    public long getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setShip(Set<Ship> ship) {
        this.ship = ship;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() { return game; }

    public void setGame(Game game) {
        this.game = game;
    }

    public GamePlayer(Game game) {
        this.game = game;
    }

    public Set<Ship> getShip() {
        return ship;
    }

    public void setSalvo(Set<Salvo> salvo) {
        this.salvoes = salvo;
    }

    public Set<Salvo> getSalvo() {
        return salvoes;
    }

    public void addSalvo(Salvo salvo){
        salvoes.add(salvo);
    }

    public Score getScore (){
        return player.getScore(this.game);
    }


    public Map<String,Object> gamePlayerDTO (){
        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("player", player.playerDTO());
        return dto;
    }

    public Map<String,Object> gameView (){
        Map<String,Object> dto= new LinkedHashMap<String,Object>();
        dto.put("Id", game.getId());
        dto.put("created", game.getDateGame());
        dto.put("gamePlayers", game.getGamePlayers().stream().map(GamePlayer::gamePlayerDTO).collect(Collectors.toList())); //,playerDTO(gamePlayer.getPlayer()))
        dto.put("ships", ship.stream().map(Ship::shipPlayerDTO).collect(Collectors.toSet()));
        //dto.put("salvo", game.getGamePlayers().stream().map(GamePlayer -> GamePlayer.getSalvo().stream().map(Salvo::salvoDTO).collect(toSet())));
        dto.put("salvoes",  this.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvo()
                        .stream()
                        .map(salvo -> salvo.salvoDTO()))
                .collect(Collectors.toList()));

        return dto;
    }

    public void addShip(Set<Ship> ships){
        ships.forEach(ship->{
            ship.setGamePlayer(this);
            this.ship.add(ship);
        });
    }

//---este es el oponente---//
    public GamePlayer getOtherPlayer (){
        return this.getGame().getGamePlayers().stream().filter(gamePlayer -> gamePlayer.getId() != this.getId()).findFirst().orElse(new GamePlayer());
    }


}
