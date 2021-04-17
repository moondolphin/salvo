package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.model.*;
import com.codeoftheweb.salvo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {

        SpringApplication.run(SalvoApplication.class, args);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository,
                                      GameRepository gameRepository,
                                      GamePlayerRepository gamePlayerRepository,
                                      ShipRepository shipRepository,
                                      SalvoRepository salvoRepository,
                                      ScoreRepository scoreRepository) {
        return (args) -> {

            //--- save a couple of customers---//
            Player player1 = playerRepository.save(new Player("j.bauer@ctu.gov", passwordEncoder().encode("1234")));
            Player player2 = playerRepository.save(new Player("c.obrian@ctu.gov", passwordEncoder().encode("1234")));
            Player player3 = playerRepository.save(new Player("t.almeida@ctu.gov", passwordEncoder().encode("1234")));
            Player player4 = playerRepository.save(new Player("d.palmer@whitehouse.gov", passwordEncoder().encode("1234")));


            Game p1 = new Game(LocalDateTime.now());
            Game p2 = new Game(LocalDateTime.now().plusHours(1));
            Game p3 = new Game(LocalDateTime.now().plusHours(2));
            Game p4 = new Game(LocalDateTime.now().plusHours(3));
            Game p5 = new Game(LocalDateTime.now().plusHours(4));
            Game p6 = new Game(LocalDateTime.now().plusHours(5));

           gameRepository.save(p1);
           gameRepository.save(p2);
           gameRepository.save(p3);
           gameRepository.save(p4);
           gameRepository.save(p5);
           gameRepository.save(p6);

            GamePlayer gameplayer1 = new GamePlayer(player1,p1);
            gamePlayerRepository.save(gameplayer1);
            GamePlayer gameplayer2 = new GamePlayer(player2,p1);
            gamePlayerRepository.save(gameplayer2);
            GamePlayer gameplayer3 = new GamePlayer(player3,p2);
            gamePlayerRepository.save(gameplayer3);
            GamePlayer gameplayer4 = new GamePlayer(player4,p2);
            gamePlayerRepository.save(gameplayer4);
            GamePlayer gameplayer5 = new GamePlayer(player1,p3);
            gamePlayerRepository.save(gameplayer5);
            GamePlayer gameplayer6 = new GamePlayer(player3,p3);
            gamePlayerRepository.save(gameplayer6);

            List<String> shipLocations1 = new ArrayList<>(Arrays.asList("B5", "B6", "B7", "B8", "B9"));
            List<String> shipLocations2 = new ArrayList<>(Arrays.asList("C5","C6", "C7", "C8"));
            List<String> shipLocations3 = new ArrayList<>(Arrays.asList("D5","D6", "D7"));
            List<String> shipLocations4 = new ArrayList<>(Arrays.asList("E7","E8", "E9"));
            List<String> shipLocations5 = new ArrayList<>(Arrays.asList("G7","G8", "G9"));
            List<String> shipLocations6 = new ArrayList<>(Arrays.asList("J7","J8"));

            Ship ship1 = new Ship("Carrier", shipLocations1, gameplayer1);
            Ship ship2 = new Ship("Battleship", shipLocations2, gameplayer2);
            Ship ship3 = new Ship("Submarine", shipLocations3, gameplayer3);
            Ship ship4 = new Ship("Destroyer", shipLocations4, gameplayer4);
            Ship ship5 = new Ship ("Patrol Boat", shipLocations5, gameplayer3);
            Ship ship6 = new Ship ("Patrol Boat", shipLocations6, gameplayer4);

            shipRepository.save(ship1);
            shipRepository.save(ship2);
            shipRepository.save(ship3);
            shipRepository.save(ship4);
            shipRepository.save(ship5);
            shipRepository.save(ship6);


            Salvo salvo1 = new Salvo(gameplayer1,1, Arrays.asList("A5"));
            salvoRepository.save(salvo1);
            Salvo salvo2 = new Salvo(gameplayer2,1, Arrays.asList("B7"));
            salvoRepository.save(salvo2);
            Salvo salvo3 = new Salvo(gameplayer3,1, Arrays.asList("A1"));
            salvoRepository.save(salvo3);
            Salvo salvo4 = new Salvo(gameplayer4,2, Arrays.asList("G7"));
            salvoRepository.save(salvo4);


            Score score1 = new Score(p1, player1, LocalDateTime.now(),2.0);
            scoreRepository.save(score1);
            Score score2 = new Score(p2, player3, LocalDateTime.now(),0.5);
            scoreRepository.save(score2);
            Score score3 = new Score(p3, player4, LocalDateTime.now(),0.0);
            scoreRepository.save(score3);
            Score score4 = new Score(p4, player4, LocalDateTime.now(),1.0);
            scoreRepository.save(score4);
            Score score5= new Score(p5, player2, LocalDateTime.now(),0.0);
            scoreRepository.save(score5);
        };
    }

}


//---compare player userName request in PlayerRepository---//
//----to get a user`s passwords and roles----//
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName-> {
            Player player = playerRepository.findByUserName(inputName);
            if (player != null) {
                return new User(player.getUserName(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }
}



//---to see information---//
@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/web/**").permitAll()
                .antMatchers("/api/game_view/**").hasAuthority("USER")
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/games").permitAll()
                //.anyRequest().authenticated()
                .and().csrf().ignoringAntMatchers("/h2-console/")
                .and().headers().frameOptions().sameOrigin();

        http.formLogin()
                .usernameParameter("name")
                .passwordParameter("pwd")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        //---peticiones AJAX---//
        // turn off checking for CSRF tokens
        http.csrf().disable();

        //--- if user is not authenticated, just send an authentication failure response---//
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        //--- if login is successful, just clear the flags asking for authentication---//
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        //---if login fails, just send an authentication failure response---//
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        //---if logout is successful, just send a success response---//
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }



}