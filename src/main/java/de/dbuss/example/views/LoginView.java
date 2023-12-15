package de.dbuss.example.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.dbuss.example.data.entity.User;
import de.dbuss.example.data.security.AuthenticatedUser;
import de.dbuss.example.data.service.UserService;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Optional;

@Route("simple_login")
@PageTitle("Login | Qs-Admin")
@AnonymousAllowed
public class LoginView extends LoginOverlay implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final LoginForm login = new LoginForm();

    public LoginView(AuthenticatedUser authenticatedUser, UserService userService) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;

        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("QS Grid...");
        i18n.getHeader().setDescription("Login using mapping/mapping");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
        // Add a listener to handle login events
        addLoginListener(this::onLogin);

    }
    private boolean connectToLdap(String username, String password) {
        String ldapUrl = "ldap://viaginterkom.de:389";
        String ldapUser = "cn=" + username + ",ou=people,dc=viaginterkom,dc=de"; // Adjust the DN pattern
        String ldapPassword = password;

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_PRINCIPAL, ldapUser);
        env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

        try {
            // Attempt to create an initial context with the provided credentials
            DirContext context = new InitialDirContext(env);

            // Close the context after use
            context.close();

            return true;
        } catch (NamingException e) {
            // Handle exceptions (e.g., authentication failure)
            e.printStackTrace();
            return false;
        }

    }
    private void onLogin(AbstractLogin.LoginEvent event) {

        String userName = event.getUsername();
        String password = event.getPassword();

        User user = userService.getUserByUsername(userName);
        System.out.println(user.getName());
        boolean isLoginSuccessful = false;
        if(user.getIs_ad() == 1) {
            isLoginSuccessful = connectToLdap(userName, password);
        } else {
            Optional<User> optionalUser = authenticatedUser.get();
            if(optionalUser.isPresent()) {
                isLoginSuccessful = true;
            }
        }
        // Show success or failure message
        if (isLoginSuccessful) {
            Notification.show("Login successful", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Login failed. Please check your credentials.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}