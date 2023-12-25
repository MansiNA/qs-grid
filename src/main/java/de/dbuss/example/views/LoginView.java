package de.dbuss.example.views;

import com.vaadin.flow.component.UI;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
    UI ui = new UI();
    private static final String LOGIN_SUCCESS_URL = "/";
    private static final String LOGIN_ERROR_URL = "/simple_login";
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final LoginForm login = new LoginForm();

    @Autowired
    private AuthenticationProvider authenticationProvider;

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
            DirContext context = new InitialDirContext(env);

            // Close the context after use
            context.close();

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
    private void onLogin(AbstractLogin.LoginEvent event) {

        String userName = event.getUsername();
        String password = event.getPassword();

        System.out.println("Anmeldeversuch von User:" + userName);
        System.out.println("mit Passwort:" + password);

        User user = userService.getUserByUsername(userName);
        System.out.println(user.getName());
        boolean isLoginSuccessful = false;
        if(user.getIs_ad() == 1) {

            System.out.println(user.getName() + " ist Active Directory User...");

            isLoginSuccessful = connectToLdap(userName, password);

            if (isLoginSuccessful){
                System.out.println("successfully login...");

                //Authentication request = new UsernamePasswordAuthenticationToken("admin", "$2a$10$jpLNVNeA7Ar/ZQ2DKbKCm.MuT2ESe.Qop96jipKMq7RaUgCoQedV.");
                //Authentication request = new UsernamePasswordAuthenticationToken(userName, "$2a$10$jpLNVNeA7Ar/ZQ2DKbKCm.MuT2ESe.Qop96jipKMq7RaUgCoQedV.");
                Authentication request = new UsernamePasswordAuthenticationToken(userName, "$2a$10$jpLNVNeA7Ar/ZQ2DKbKCm.MuT2ESe.Qop96jipKMq7RaUgCoQedV.");
                Authentication result = authenticationProvider.authenticate(request);
                SecurityContextHolder.getContext().setAuthentication(result);

                ui.getCurrent().getPage().setLocation(LOGIN_SUCCESS_URL);
            }
            else {
                login.setError(true);
                ui.getCurrent().getPage().setLocation(LOGIN_ERROR_URL);

            }



        } else {

            System.out.println(user.getName() + " kein AD-User...");
        //    Optional<User> optionalUser = authenticatedUser.get();
        //    if(optionalUser.isPresent()) {
        //        System.out.println("User is successfully authenticated by spring security");
        //        isLoginSuccessful = true;
        //    }
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
