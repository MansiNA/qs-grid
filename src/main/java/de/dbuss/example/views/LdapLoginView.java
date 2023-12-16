package de.dbuss.example.views;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
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

@Route("login")
@AnonymousAllowed
public class LdapLoginView extends VerticalLayout {
    private final AuthenticatedUser authenticatedUser;
    private final UserService userService;

    public LdapLoginView(AuthenticatedUser authenticatedUser, UserService userService) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;


        LoginOverlay loginOverlay = new LoginOverlay();
        loginOverlay.setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("QS Grid...");
        i18n.getHeader().setDescription("Login using mapping/mapping");
        i18n.setAdditionalInformation(null);
        loginOverlay.setI18n(i18n);

        loginOverlay.setForgotPasswordButtonVisible(false);
        loginOverlay.setOpened(true);

        // Add a listener to handle login events
        loginOverlay.addLoginListener(this::onLogin);

        // Add components to the layout
        add(loginOverlay);
    }

    private boolean connectToLdap(String username, String password) {
        String ldapUrl = "ldap://viaginterkom.de:389";
       // String ldapUser = "cn=" + username + ",ou=people,dc=viaginterkom,dc=de";
        String ldapUser= username + "@viaginterkom.de"; //Adjust the DN pattern
        String ldapPassword = password;

        System.out.println("Anmelden User: " + ldapUser);
        System.out.println("Password: " + ldapPassword);
        System.out.println("URL: " + ldapUrl);

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

     //   User user = userService.getUserByUsername(userName);
      //  System.out.println(user.getName());
        boolean isLoginSuccessful = false;


        isLoginSuccessful = connectToLdap(userName, password);
        // Show success or failure message
        if (isLoginSuccessful) {

            System.out.println("Login successful...");

            Notification.show("Login successful", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            System.out.println("Login failed!!!");
            Notification.show("Login failed. Please check your credentials.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


}
