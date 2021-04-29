package com.codeoftheweb.salvo.controller;

import com.codeoftheweb.salvo.model.*;
import com.codeoftheweb.salvo.repository.*;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.codeoftheweb.salvo.controller.Util.makeMap;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    ShipRepository shipRepository;

    @Autowired
    GamePlayerRepository gamePlayerRepository;

    @Autowired
    ScoreRepository scoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SalvoRepository salvoRepository;


    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }


    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String, Object>> getGameViewByGamePlayerID(@PathVariable Long nn, Authentication authentication) {

        Player player;

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "no authorized"), HttpStatus.UNAUTHORIZED);
        }
        if (playerRepository.findByUserName(authentication.getName()) instanceof Player) {
            player = playerRepository.findByUserName(authentication.getName());
        } else {
            player = null;
        }

        GamePlayer gamePlayer = gamePlayerRepository.findById(nn).orElse(null);

        if (player == null) {
            return new ResponseEntity<>(makeMap("error", "no authorized, no hay player"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "no authorized, no hay juego"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.getPlayer().getId() != player.getId()) {
            return new ResponseEntity<>(makeMap("error", "no authorized, no estas en la misma direccion"), HttpStatus.CONFLICT);
        }

        Map<String, Object> dto = new LinkedHashMap<>();
        Map<String, Object> hits = new LinkedHashMap<>();
        GamePlayer opponent = gamePlayer.getOtherPlayer();
        if(opponent.getId() != 0){
            hits.put("self", historyofSalvos(gamePlayer, opponent));
            hits.put("opponent", historyofSalvos(opponent, gamePlayer));
        }else {
            hits.put("self", new ArrayList<>());
            hits.put("opponent", new ArrayList<>());
        }


        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getDateGame());
        dto.put("gameState", gameState(gamePlayer));
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers()
                .stream()
                .map(gamePlayer1 -> gamePlayer1.gamePlayerDTO())
                .collect(Collectors.toList()));
        dto.put("ships", gamePlayer.getShip()
                .stream()
                .map(ship -> ship.shipPlayerDTO())
                .collect(Collectors.toList()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvo()
                        .stream()
                        .map(salvo -> salvo.salvoDTO()))
                .collect(Collectors.toList()));
        dto.put("hits", hits);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //---sink and hits of the game---//
    private List<Map> historyofSalvos (GamePlayer gamePlayer, GamePlayer oponente) {

        List<Map> totalMap = new ArrayList<>();

        int carrierDamage = 0;
        int battleshipDamage = 0;
        int submarineDamage = 0;
        int destroyerDamage = 0;
        int patrolboatDamage = 0;

        List<String> carrierLocation = findShipLocations(gamePlayer, "carrier");
        List<String> battleshipLocation = findShipLocations(gamePlayer, "battleship");
        List<String> submarineLocation = findShipLocations(gamePlayer, "submarine");
        List<String> destroyerLocation = findShipLocations(gamePlayer, "destroyer");
        List<String> patrolboatLocation = findShipLocations(gamePlayer, "patrolboat");


        for (Salvo salvo : oponente.getSalvo()) {

            Map<String, Object> map = new LinkedHashMap<>();
            Map<String, Object> map2 = new LinkedHashMap<>();

            int carrierHitsInTurn = 0;
            int battleshipHitsInTurn = 0;
            int submarineHitsInTurn = 0;
            int destroyerHitsInTurn = 0;
            int patrolboatHitsInTurn = 0;

            ArrayList<String> hitCellsList = new ArrayList<>();
            int missedShots = salvo.getSalvoLocations().size();

            for (String salvoLocation : salvo.getSalvoLocations()) {

                if (carrierLocation.contains(salvoLocation)) {
                    carrierDamage++;
                    carrierHitsInTurn++;
                    hitCellsList.add(salvoLocation);
                    missedShots--;
                }
                if (battleshipLocation.contains(salvoLocation)) {
                    battleshipDamage++;
                    battleshipHitsInTurn++;
                    hitCellsList.add(salvoLocation);
                    missedShots--;
                }
                if (submarineLocation.contains(salvoLocation)) {
                    submarineDamage++;
                    submarineHitsInTurn++;
                    hitCellsList.add(salvoLocation);
                    missedShots--;
                }
                if (destroyerLocation.contains(salvoLocation)) {
                    destroyerDamage++;
                    destroyerHitsInTurn++;
                    hitCellsList.add(salvoLocation);
                    missedShots--;
                }
                if (patrolboatLocation.contains(salvoLocation)) {
                    patrolboatDamage++;
                    patrolboatHitsInTurn++;
                    hitCellsList.add(salvoLocation);
                    missedShots--;
                }
            }
                map2.put("carrierHits", carrierHitsInTurn);
                map2.put("battleshipHits", battleshipHitsInTurn);
                map2.put("submarineHits", submarineHitsInTurn);
                map2.put("destroyerHits", destroyerHitsInTurn);
                map2.put("patrolboatHits", patrolboatHitsInTurn);

                map2.put("carrier", carrierDamage);
                map2.put("battleship", battleshipDamage);
                map2.put("submarine", submarineDamage);
                map2.put("destroyer", destroyerDamage);
                map2.put("patrolboat", patrolboatDamage);

                map.put("turn", salvo.getTurn());
                map.put("hitLocations", hitCellsList);
                map.put("damages", map2);
                map.put("missed", missedShots);

                totalMap.add(map);



        }
        return totalMap;
    }

        public List<String> findShipLocations(GamePlayer gpl, String type) {
            Optional<Ship> response;
            response = gpl.getShip().stream().filter(ship -> ship.getType().equals(type)).findFirst();
            if (response.isEmpty()) {
                return new ArrayList<String>();
            }
            return response.get().getlocations();
        }


    private String gameState(GamePlayer gamePlayer) {

        GamePlayer opp = gamePlayer.getOtherPlayer();

        if (gamePlayer.getShip().size() == 0) {
            return "PLACESHIPS";
        }
        if (gamePlayer.getGame().getGamePlayers().size() == 1) {
            return "WAITINGFOROPP";
        }

        if (gamePlayer.getGame().getGamePlayers().size() == 2) {

            if (gamePlayer.getSalvo().size() == opp.getSalvo().size() ) {

                if ((getIfAllSunk(opp, gamePlayer) && !getIfAllSunk(gamePlayer, opp))) {
                    scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), LocalDateTime.now(), 1.0));


                    return "WON";
                }
                if (getIfAllSunk(opp, gamePlayer) && getIfAllSunk(gamePlayer, opp)) {
                    scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), LocalDateTime.now(), 0.5));


                    return "TIE";
                }
                if (!getIfAllSunk(opp, gamePlayer) && getIfAllSunk(gamePlayer, opp)) {
                    scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(), LocalDateTime.now(), 0.0));


                    return "LOST";
                }
            }

            if (gamePlayer.getSalvo().size() < opp.getSalvo().size()) {
                return "PLAY";
            }

            if (gamePlayer.getSalvo().size() == opp.getSalvo().size() && (gamePlayer.getId() < opp.getId())) {
                return "PLAY";
            }

            if (gamePlayer.getSalvo().size() > opp.getSalvo().size()) {
                return "WAIT";
            }

            if (gamePlayer.getSalvo().size() == opp.getSalvo().size() && (gamePlayer.getId() > opp.getId())) {
                return "WAIT";
            }

        }
        return "UNDEFINED";

    }


        private Boolean getIfAllSunk (GamePlayer self, GamePlayer opponent) {
        if(!opponent.getShip().isEmpty() && !self.getSalvo().isEmpty()){
            return opponent.getSalvo().stream().flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList()).containsAll(self.getShip().stream()
                    .flatMap(ship -> ship.getlocations().stream()).collect(Collectors.toList()));
        }
        return false;
    }


}
