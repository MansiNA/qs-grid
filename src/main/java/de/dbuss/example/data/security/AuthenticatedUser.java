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

    private boolean is_authenticated = false;
    User logged_in_user;

    public AuthenticatedUser(AuthenticationContext authenticationContext, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {

        if (is_authenticated)
        {
            return Optional.ofNullable(logged_in_user);
        }

        Optional<User> user = Optional.of(new User());
        user = authenticationContext.getAuthenticatedUser(UserDetails.class).map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));


        return user;
    }

public String getUsername(){

        if (logged_in_user == null)
        {
            return null;
        }

        return logged_in_user.getUsername();
};

    @Transactional
    public void setUser(User logged_in_user) {
        this.logged_in_user=logged_in_user;
        is_authenticated=true;
    }

    public void logout() {
        is_authenticated=false;
        logged_in_user=null;
        authenticationContext.logout();
    }

}
