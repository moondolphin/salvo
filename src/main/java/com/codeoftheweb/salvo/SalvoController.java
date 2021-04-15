package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.model.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.codeoftheweb.salvo.controller.Util.makeMap;

@RestController
@RequestMapping("/api")
public class SalvoController {


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


    @GetMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        if (!isGuest(authentication)) {
            dto.put("player", playerRepository.findByUserName(authentication.getName()).playerDTO());
        } else {
            dto.put("player", "Guest");
        }
        dto.put("games", gameRepository.findAll().stream().map(game -> game.gameDTO()).collect(Collectors.toList()));
        return dto;
    }


    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        ResponseEntity<Map<String, Object>> response;
        if (isGuest(authentication)) {
            response = new ResponseEntity<>(makeMap("error", "player is UNAUTHORIZED"), HttpStatus.UNAUTHORIZED);
        } else {

            Game newGame = gameRepository.save(new Game(LocalDateTime.now()));
            Player thisPlayer = playerRepository.findByUserName(authentication.getName());
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(playerRepository.save(thisPlayer), newGame));

            response = new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
        return response;
    }

    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {
        ResponseEntity<Map<String, Object>> resp;
        Optional<Game> boxGame = gameRepository.findById(gameId);

        if (isGuest(authentication)) {
            resp = new ResponseEntity<>(makeMap("error", "Player is UNAUTHORIZED"), HttpStatus.UNAUTHORIZED);
        } else if (!boxGame.isPresent()) {
            resp = new ResponseEntity<>(makeMap("error", "Game Id doesnt exist"), HttpStatus.FORBIDDEN);
        } else if (boxGame.get().getGamePlayers().size() > 1) {
            resp = new ResponseEntity<>(makeMap("error", "Sorry, Game is full..."), HttpStatus.FORBIDDEN);
        } else {
            Player player = playerRepository.findByUserName(authentication.getName());
            GamePlayer newGamePlayer = gamePlayerRepository.save(new GamePlayer(player, boxGame.get()));
            resp = new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
        return resp;
    }


    @GetMapping("/score")
    private Map<String, Object> getScore() {
        Map<String, Object> map = new HashMap<>();
        map.put("score", scoreRepository.findAll().stream().map(Score::scoreDTO).collect(Collectors.toList()));
        return map;
    }

    @GetMapping("/players")
    private List<Map<String, Object>> getPlayer() {
        return playerRepository.findAll().stream().map(player -> player.playerDTO()).collect(Collectors.toList());
    }

    @PostMapping("/players")
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByUserName(email) != null) {
            return new ResponseEntity<>(makeMap("error", "Name already in use"), HttpStatus.FORBIDDEN);
        }
        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("message", "Success, player created"), HttpStatus.CREATED);
    }




    @RequestMapping(value = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> shipsCreated(@PathVariable long gamePlayerId, @RequestBody Set<Ship> ships, Authentication authentication) {

        ResponseEntity<Map<String, Object>> resp;

        Optional<GamePlayer> gamePlayerPos = gamePlayerRepository.findById(gamePlayerId);
        Player playerNow = playerRepository.findByUserName(authentication.getName());

        if (isGuest(authentication)) {
            resp = new ResponseEntity<>(makeMap("error", "there is no current user logged in"), HttpStatus.UNAUTHORIZED);
        } else if (!gamePlayerPos.isPresent()) {
            resp = new ResponseEntity<>(makeMap("error", "there is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayerPos.get().getPlayer().getId() != playerNow.getId()) {
            resp = new ResponseEntity<>(makeMap("error", "the current user is not the game player the ID references"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayerPos.get().getShip().size() > 0) {
            resp = new ResponseEntity<>(makeMap("error", "the user already has ships placed"), HttpStatus.FORBIDDEN);
        } else if (ships.size() > 0) {
            //---ships added to the game player and saved---//
            gamePlayerPos.get().addShip(ships);
            gamePlayerRepository.save(gamePlayerPos.get());

            resp = new ResponseEntity<>(makeMap("OK", "Success"), HttpStatus.CREATED);
        } else {
            resp = new ResponseEntity<>(makeMap("error", "you dont send any ship"), HttpStatus.FORBIDDEN);
        }

        return resp;
    }


    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addSalvo(@PathVariable long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication) {

        ResponseEntity<Map<String, Object>> respon = null;
        Player playerN = playerRepository.findByUserName(authentication.getName());
        Optional<GamePlayer> gamePlayerP = gamePlayerRepository.findById(gamePlayerId);
        GamePlayer otherPlayer = gamePlayerP.get().getOtherPlayer();

        if (isGuest(authentication)) {
            respon = new ResponseEntity<>(makeMap("error", "there is no current user logged in"), HttpStatus.UNAUTHORIZED);
        } else if (!gamePlayerP.isPresent()) {
            respon = new ResponseEntity<>(makeMap("error", "there is no game player with the given ID"), HttpStatus.UNAUTHORIZED);
        } else if (gamePlayerP.get().getPlayer().getId() != playerN.getId()) {
            respon = new ResponseEntity<>(makeMap("error", "the current user is not the game player the ID references"), HttpStatus.UNAUTHORIZED);

        } else if (otherPlayer.getId() == 0 ){
            respon = new ResponseEntity<>(makeMap("error", "no opponent"), HttpStatus.UNAUTHORIZED);
        }else if (gamePlayerP.get().getSalvo().size() <= otherPlayer.getSalvo().size()){
                int turn = (gamePlayerP.get().getSalvo().size()+1);
                if (repeatedTurn(gamePlayerP.get(),turn)){
                    respon = new ResponseEntity<>(makeMap("error", "repited turn"), HttpStatus.FORBIDDEN);
                }else if (salvo.getSalvoLocations().size() != 5){
                    respon = new ResponseEntity<>(makeMap("error", "only 5"), HttpStatus.FORBIDDEN);
                }else {
                    salvo.setTurn(turn);
                    salvo.setGamePlayer(gamePlayerP.get());
                    gamePlayerP.get().addSalvo(salvo);
                    salvoRepository.save(salvo);
                    gamePlayerRepository.save(gamePlayerP.get());
                    respon = new ResponseEntity<>(makeMap("OK", "Salvo created"), HttpStatus.CREATED);
                }
            }else {
                respon = new ResponseEntity<>(makeMap("error", "you have already a turn"), HttpStatus.FORBIDDEN);
            }
        return respon;
    }


    public static boolean repeatedTurn(GamePlayer gp, int salvoTurn) {
        boolean isRepeatedTurn = false;
        for (Salvo salvo : gp.getSalvo()) {
            if (salvo.getTurn() == salvoTurn) {
                isRepeatedTurn = true;
                break;
            }
        }
        return isRepeatedTurn;
    }


}