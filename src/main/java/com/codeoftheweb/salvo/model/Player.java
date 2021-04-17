package com.codeoftheweb.salvo.model;

import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String userName;
    private String password;


    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamesPlayers = new HashSet<>();

    @OneToMany(mappedBy ="player", fetch = FetchType.EAGER)
    private Set<Score> scores = new HashSet<>();

    public Player() {
    }

    public Player(String userName, String password) {
        this.password = password;
        setUserName(userName);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GamePlayer> getGamesPlayers() {
        return gamesPlayers;
    }

    public long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public Score getScore (Game game){
        return scores.stream().filter(score -> score.getGame().getId()==game.getId()).findFirst().orElse(null);
    }

    //---for avoid recursion---//
    @JsonIgnore
    public List<Game> getGames () {
        return gamesPlayers.stream().map(sub-> sub.getGame()).collect(Collectors.toList());
    }

    public Map<String,Object> playerDTO (){
        Map<String,Object> dto= new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        return dto;
    }


}
