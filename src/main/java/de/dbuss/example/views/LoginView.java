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

@Route("login")
@PageTitle("Login | Qs-Admin")
@AnonymousAllowed
//public class LoginView extends LoginOverlay implements BeforeEnterObserver {
public class LoginView extends VerticalLayout  implements BeforeEnterObserver {
    UI ui = new UI();
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;
    private final LoginForm login = new LoginForm();;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    public LoginView(AuthenticatedUser authenticatedUser, UserService userService) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
       // setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));


        login.setForgotPasswordButtonVisible(false);

        login.setAction("login");

      /*  login.addLoginListener(e -> {
            //System.out.println("Im Login-Listener...");
            if (authenticate(e.getUsername(), e.getPassword())) {
                login.setError(false);
                login.getUI().ifPresent(ui -> ui.navigate("/"));


            } else {
                login.setError(true);
            }
        });*/


        add(login);

    }

  /*  private boolean authenticate(String username, String password) {

        System.out.println("Authentifiziere User: " + username + " / " + password);

        User user = userService.getUserByUsername(username);

        if (user == null)
        {
            System.out.println("User " + username + " not found in table application_user!!!");
            return false;
        }


        Authentication result;

        if(user.getIs_ad() == 1) {

            System.out.println(user.getName() + " ist Active Directory User...");

            boolean isLoginSuccessful = false;
            isLoginSuccessful = connectToLdap(username, password);

            if (isLoginSuccessful) {
                System.out.println("AD says successfully login...");

                Authentication request = new UsernamePasswordAuthenticationToken(username, "admin");
                result = authenticationProvider.authenticate(request);
                SecurityContextHolder.getContext().setAuthentication(result);
                return true;
            }
            return false;
        }
        else
        {
            Authentication request = new UsernamePasswordAuthenticationToken(username, password);
            result = authenticationProvider.authenticate(request);
        }


        if (result != null)
        {
            SecurityContextHolder.getContext().setAuthentication(result);
            return true;
        }
        else
        {
            return false;
        }

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
*/
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
