package de.dbuss.example.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import de.dbuss.example.data.entity.User;
import de.dbuss.example.data.security.AuthenticatedUser;
import de.dbuss.example.views.grid.GridView;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Optional;

public class MainLayout extends AppLayout {

    private AuthenticatedUser authenticatedUser;
    boolean isAdmin =checkAdminRole();
    Button logout;
    boolean isUser =checkUserRole();
    public MainLayout(AuthenticatedUser authenticatedUser){
        this.authenticatedUser = authenticatedUser;
        createHeader();
        createDrawer();

        //isAdmin = checkAdminRole();
    }

    private void createHeader() {
        H1 logo = new H1("QS-Grid");
        logo.addClassNames("text-l","m-m");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        logout = new Button("Log out " + currentUserName, e -> authenticatedUser.logout());

        if (currentUserName=="anonymousUser")
        {
            logout.setVisible(false);
        }
        else
        {
            logout.setVisible(true);
        }

        if (isAdmin) {

            System.out.println("Ein Admin ist angemeldet!");
            // Benutzer ist ein Administrator
            // Führen Sie hier den entsprechenden Code aus
        } else {

            System.out.println("Ein normaler User ist angemeldet!");
            // Benutzer ist kein Administrator
            // Führen Sie hier den entsprechenden Code aus
        }

      //  Image image = new Image("images/dataport.png", "Dataport Image");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("angemeldeter User: " + auth.getName());

        HorizontalLayout header= new HorizontalLayout(new DrawerToggle(),logo, logout);

        Span sp= new Span("V1.02");

      //  header.add(image,sp);
        header.add(sp);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");
        addToNavbar(header);
    }

    private boolean checkAdminRole() {

        // Überprüfen, ob der angemeldete Benutzer zur Gruppe "Admin" gehört

        // Erhalten Sie den angemeldeten Benutzer
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Überprüfen, ob der Benutzer authentifiziert ist und nicht anonym
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();

            // Überprüfen, ob der angemeldete Benutzer ein UserDetails-Objekt ist
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;

                // Überprüfen, ob der angemeldete Benutzer die Berechtigung "ROLE_ADMIN" hat
                return userDetails.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));

            }
        }

        return false;

    }


    private boolean checkPFRole() {

        // Überprüfen, ob der angemeldete Benutzer zur Gruppe "Admin" gehört

        // Erhalten Sie den angemeldeten Benutzer
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Überprüfen, ob der Benutzer authentifiziert ist und nicht anonym
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();

            // Überprüfen, ob der angemeldete Benutzer ein UserDetails-Objekt ist
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;

                // Überprüfen, ob der angemeldete Benutzer die Berechtigung "ROLE_ADMIN" hat
                return userDetails.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_PF_ADMIN"));

            }
        }

        return false;

    }

    private boolean checkUserRole() {

        // Überprüfen, ob der angemeldete Benutzer zur Gruppe "Admin" gehört

        // Erhalten Sie den angemeldeten Benutzer
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Überprüfen, ob der Benutzer authentifiziert ist und nicht anonym
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();

            // Überprüfen, ob der angemeldete Benutzer ein UserDetails-Objekt ist
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;

                // Überprüfen, ob der angemeldete Benutzer die Berechtigung "ROLE_ADMIN" hat
                return userDetails.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));

            }
        }

        return false;

    }

    private void createDrawer() {

        RouterLink gridView = new RouterLink ("grid", GridView.class);
        RouterLink link = new RouterLink("login", LoginView.class);

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            addToDrawer(new VerticalLayout(
                    gridView
            ));

            logout.setVisible(true);

        } else
        {
            addToDrawer(new VerticalLayout(link));
        }
    }
    private void navigateTo(String path) {
        UI.getCurrent().navigate(path);
    }
}
