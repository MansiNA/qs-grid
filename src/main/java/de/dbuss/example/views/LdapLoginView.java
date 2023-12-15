package de.dbuss.example.views;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

@Route("login")
@AnonymousAllowed
public class LdapLoginView extends VerticalLayout {

    private TextField usernameField;
    private PasswordField passwordField;

    public LdapLoginView() {
        // Initialize UI components
        usernameField = new TextField("Username");
        passwordField = new PasswordField("Password");
        Button loginButton = new Button("Login", this::login);

        // Add components to the layout
        add(usernameField, passwordField, loginButton);
    }

    private void login(ClickEvent<Button> event) {
        // Get username and password from the fields
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        // Attempt to connect to LDAP
        boolean ldapConnectionSuccessful = connectToLdap(username, password);

        // Show success or failure message
        if (ldapConnectionSuccessful) {
            Notification.show("Login successful", 3000, Notification.Position.MIDDLE);
            // Redirect to the main page or perform other actions
        } else {
            Notification.show("Login failed. Please check your credentials.", 5000, Notification.Position.MIDDLE);
        }
    }

    private boolean connectToLdap(String username, String password) {
        String ldapUrl = "ldap://viaginterkom.de:389";
       // String ldapUser = "cn=" + username + ",ou=people,dc=viaginterkom,dc=de"; // Adjust the DN pattern

        String ldapUser= username + "@viaginterkom.de";

        String ldapPassword = password;

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_PRINCIPAL, ldapUser);
        env.put(Context.SECURITY_CREDENTIALS, ldapPassword);

        try {
            // Attempt to create an initial context with the provided credentials
            DirContext context = new InitialDirContext(env);

            // If the context is created successfully, the LDAP connection is successful

            // Close the context after use
            context.close();
            System.out.println("User " + ldapUser + " connected");
            return true;
        } catch (NamingException e) {
            // Handle exceptions (e.g., authentication failure)

            System.out.println("User " + ldapUser + " failed to connect!");
            System.out.println("PWD: " + ldapPassword);

            e.printStackTrace();
            return false;
        }

    }
}
