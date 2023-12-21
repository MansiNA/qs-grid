package de.dbuss.example.data.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.dbuss.example.data.entity.User;
import de.dbuss.example.data.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    User logged_in_user;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {

        Optional<User> user = Optional.of(new User());
        user = authenticationContext.getAuthenticatedUser(UserDetails.class).map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));


        return user;
    }



    public void logout() {
        authenticationContext.logout();
    }

}
