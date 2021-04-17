package com.codeoftheweb.salvo.model;


import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gameplay")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name="shiplocation")
    private List<String> locations;


    public Ship() {
    }

    public Ship(String type, List<String> locations, GamePlayer gameplayer) {
        this.type = type;
        this.locations = locations;
        this.gamePlayer = gameplayer;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getlocations() {
        return locations;
    }

    public void setlocations(List<String> locations) {
        this.locations = locations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Map<String,Object> shipPlayerDTO (){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", this.getType());
        dto.put("locations", this.getlocations());
        return dto;
    }


}
