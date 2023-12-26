package de.dbuss.example.data.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import de.dbuss.example.data.entity.User;
import de.dbuss.example.data.service.UserService;
import de.dbuss.example.views.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

//@EnableWebSecurity(debug = true)
@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public SecurityConfig(UserDetailsService userDetailsService,  UserService userService) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Component
    public class CustomAuthenticationProvider implements AuthenticationProvider {


      private boolean connectToLdap(String username, String password) {
            //String ldapUrl = "ldap://viaginterkom.de:389";
            //String ldapUrl = "ldap://fhhnet.stadt.hamburg.de:389";
            String ldapUrl = "ldap://91.107.232.133:10389";

            //String ldapUser= username + "@viaginterkom.de";
            //String ldapUser= username + "@fhhnet.stadt.hamburg.de";
//        String ldapUser= username + "@wimpi.net";

            String ldapUser = "uid=" + username + ",ou=users,dc=wimpi,dc=net"; // Adjust the DN pattern


            String ldapPassword = password;

            System.out.println("Anmelden User: " + ldapUser);
            System.out.println("Password: " + ldapPassword);
            System.out.println("URL: " + ldapUrl);


            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            //env.put(Context.SECURITY_PRINCIPAL, ldapUser);
            env.put(Context.SECURITY_PRINCIPAL, ldapUser);

            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

            try {
                // Attempt to create an initial context with the provided credentials

                System.out.println("Aufruf InitialDirContext Start");
                DirContext context = new InitialDirContext(env);

                // Close the context after use
                context.close();
                System.out.println("Aufruf InitialDirContext Ende");

                System.out.println("Check User against AD is successfully...");

                return true;
            } catch (Exception e) {
                // Handle exceptions (e.g., authentication failure)
                System.out.println("Check User against AD failed!!!");
                //System.out.println("Still act like it was successful");
                //return true;

                //e.printStackTrace();
                return false;
            }

        }






        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {

            System.out.println("Angemeldet bei Spring Security wird: " + authentication.getName());

            String username = authentication.getName();
            String password = authentication.getCredentials().toString();

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);


            System.out.println("Authentifiziere User: " + username + " / " + password);


            User user = userService.getUserByUsername(username);

            if (user == null)
            {
                System.out.println("User " + username + " not found in table application_user!!!");
                return null;
            }

            Authentication result;

            if( user.getIs_ad() == 1) {

                System.out.println(user.getName() + " ist Active Directory User...");

                boolean isLoginSuccessful = false;
                isLoginSuccessful = connectToLdap(username, password);

                if (isLoginSuccessful) {
                    System.out.println("AD says successfully login...");


                    return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
                }

            }
            else {

                if(!passwordEncoder().matches(password,userDetails.getPassword()))
                {
                    System.out.println("Falsches Passwort!");


                    return null;
                }
                return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
            }

            return null;


        }

        @Override
        public boolean supports(Class<?> authentication) {
            return authentication.equals(UsernamePasswordAuthenticationToken.class);
        }
        // Implementierung der Authentifizierungsmethoden
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
          .authorizeHttpRequests()
          .requestMatchers(new AntPathRequestMatcher("/images/*.png"))
          .permitAll();

        // Icons from the line-awesome addon
        http.authorizeHttpRequests().requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll();
        super.configure(http);
        setLoginView(http, LoginView.class);
    }






 /*   @Bean
    JdbcUserDetailsManager user(){
       // JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();

        return null;
    };*/

}
