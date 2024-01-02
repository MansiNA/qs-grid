package de.dbuss.example.views.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.dbuss.example.data.entity.Constants;
import de.dbuss.example.data.entity.ProjectParameter;
import de.dbuss.example.data.entity.ProjectQSEntity;
import de.dbuss.example.data.entity.Projects;
import de.dbuss.example.data.service.ProjectConnectionService;
import de.dbuss.example.views.MainLayout;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.concurrent.ListenableFuture;

import javax.sql.DataSource;

@PageTitle("Grid")
@Route(value = "grid", layout = MainLayout.class)
//@AnonymousAllowed
@RolesAllowed({"OUTLOOK", "ADMIN", "FLIP"})
//@RouteAlias(value = "", layout = MainLayout.class)
public class GridView extends VerticalLayout {

    private ProgressBar progressBar = new ProgressBar();

    private AtomicInteger threadCount = new AtomicInteger(0);
    private TextField threadCountField;
    private BackendService backendService;

    private Grid<ProjectQSEntity> grid;
    private List<ProjectQSEntity> listOfProjectQs;
    private int uploadId;
    private ProjectConnectionService projectConnectionService;
    private JdbcTemplate jdbcTemplate;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Map<Integer, List<Map<String, Object>>> rowsMap = new HashMap<>();
    @Getter
    public int projectId=9;

  /*  private GridPro<Client> grid;
    private GridListDataView<Client> gridListDataView;
    private Grid.Column<Client> clientColumn;
    private Grid.Column<Client> amountColumn;
    private Grid.Column<Client> statusColumn;
    private Grid.Column<Client> dateColumn;

   */

   public GridView(BackendService backendService, ProjectConnectionService projectConnectionService) {
       this.backendService=backendService;
       this.projectConnectionService = projectConnectionService;
       this.jdbcTemplate = projectConnectionService.getJdbcDefaultConnection();

        addClassName("grid-view");
  /*      setSizeFull();
        createGrid();
        add(grid);*/

       progressBar.setWidth("15em");
       progressBar.setIndeterminate(true);
       progressBar.setVisible(false);

       /*
        Button startButton = new Button("Start QS background task", clickEvent -> {
            UI ui = clickEvent.getSource().getUI().orElseThrow();

            increaseThreadCount();

            ListenableFuture<String> future = backendService.longRunningTask();
            future.addCallback(
                    successResult -> {
                        decreaseThreadCount();
                        updateUi(ui, "Task finished: " + successResult);

                    },


                    failureException -> {
                        decreaseThreadCount();
                        updateUi(ui, "Task failed: " + failureException.getMessage());
                    }


            );
            progressBar.setVisible(true);
        });

        */

        Button isBlockedButton = new Button("Is UI blocked?", clickEvent -> {
            Notification.show("UI isn't blocked!");
        });

       Button startQSButton = new Button("Start QS", clickEvent -> {
           executeSQL(listOfProjectQs);
           progressBar.setVisible(true);
       });

       threadCountField = new TextField("Anzahl der Threads");
       threadCountField.setReadOnly(true); // Textfeld schreibgeschützt machen, um es nur lesbar zu machen
       updateThreadCountField();

      // add(startButton, progressBar, isBlockedButton, threadCountField, startQSButton);
       add(progressBar, isBlockedButton, threadCountField, startQSButton);


       //QS-Grid aufbauen:

       getListOfProjectQsWithResult();


       grid = new Grid<>(ProjectQSEntity.class, false);
       grid.addColumn(ProjectQSEntity::getName).setHeader("QS-Name").setResizable(true);
       grid.addComponentColumn(projectQs -> {

           HorizontalLayout layout = new HorizontalLayout();
           Icon icon = new Icon();
           String status = projectQs.getResult();

           if (Constants.FAILED.equals(status)) {
               icon = VaadinIcon.CLOSE_CIRCLE.create();
               icon.getElement().getThemeList().add("badge error");
               layout.add(icon);
           } else if (Constants.OK.equals(status)) {
               icon = VaadinIcon.CHECK.create();
               icon.getElement().getThemeList().add("badge success");
               layout.add(icon);
           } else {
               icon = VaadinIcon.SPINNER.create();

               icon.getElement().getThemeList().add("badge spinner");
               if(status == null) {
                   status = "before execute...";
               }
               //layout.add(status);
               layout.add(createIcon());
             //  System.out.println(status);
           }
           icon.getStyle().set("padding", "var(--lumo-space-xs");

           return layout;

       }).setHeader("Result").setFlexGrow(0).setWidth("300px").setResizable(true);

       grid.setItems(listOfProjectQs);

       add(grid);

    }

    private Component createIcon() {
        String imageUrl = "icons/spinner.gif";
        Image image = new Image(imageUrl, "GIF Icon");
        image.setWidth("20px");
        image.setHeight("20px");

        // Das Image-Objekt zurückgeben
        return image;
    }

    private void executeSQL(List<ProjectQSEntity> projectSqls) {

        UI ui = getUI().orElseThrow();

        for (ProjectQSEntity projectQS:projectSqls) {
            System.out.println("Ausführen SQL: " + projectQS.getSql() );

        increaseThreadCount();

        //ListenableFuture<String> future = backendService.longRunningTask();

            DataSource dataSource = getDataSourceUsingParameter(dbUrl, dbUser, dbPassword);
            jdbcTemplate = new JdbcTemplate(dataSource);

        ListenableFuture<ProjectQSEntity> future = backendService.getQsResult(jdbcTemplate,projectQS );
        future.addCallback(
                successResult -> {
                    decreaseThreadCount();
                    updateUi(ui, "Task finished: SQL: " + successResult.getId() + " Ergebnis: "  + successResult.getResult());

                },

                failureException -> {
                    decreaseThreadCount();
                    updateUi(ui, "Task failed: " + failureException.getMessage());
                }

        );
        }

    }


    public DataSource getDataSourceUsingParameter(String dbUrl, String dbUser, String dbPassword) {

        if(dbUser != null) {
            System.out.println(dbUrl);
            System.out.println("Username = " + dbUser + " Password = " + dbPassword);
            DataSource dataSource = DataSourceBuilder
                    .create()
                    .url(dbUrl)
                    .username(dbUser)
                    .password(dbPassword)
                    .build();
            return dataSource;
        }

        throw new RuntimeException("Database connection not found: " + dbUser);
    }

    public String handleDatabaseError(Exception e) {

        if (e instanceof DataAccessException) {
            Throwable rootCause = getRootCause(e);
            if (rootCause instanceof org.springframework.jdbc.CannotGetJdbcConnectionException) {
                return "Error: Cannot connect to the database. Check database configuration.";
            } else if (rootCause instanceof org.springframework.jdbc.BadSqlGrammarException) {
                return "Error: Table does not exist or SQL syntax error.";
            } else {
                e.printStackTrace();
                if(e.getMessage().contains(";")) {
                    String [] errorMessage = e.getMessage().split(";");
                    return errorMessage[errorMessage.length - 1];
                }
                return "Database error: " + e.getMessage();
            }
        } else {
            e.printStackTrace();
            return "Unknown error: " + e.getMessage();
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return getRootCause(cause);
    }

    private void getListOfProjectQsWithResult() {
        String tableName = "project_qs";
        listOfProjectQs = getProjectQSList(tableName);

        String sql = "select pp.name, pp.value from pit.dbo.project_parameter pp, [PIT].[dbo].[projects] p\n" +
                "  where pp.namespace=p.page_url\n" +
                "  and pp.name in ('DB_Server','DB_Name', 'DB_User','DB_Password')\n" +
                "  and p.id="+projectId ;

        List<ProjectParameter> resultList = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProjectParameter projectParameter = new ProjectParameter();
            projectParameter.setName(rs.getString("name"));
            projectParameter.setValue(rs.getString("value"));
            return projectParameter;
        });
        String dbName = null;
        String dbServer = null;
        for (ProjectParameter projectParameter : resultList) {
            if (Constants.DB_NAME.equals(projectParameter.getName())) {
                dbName = projectParameter.getValue();
            } else if (Constants.DB_USER.equals(projectParameter.getName())) {
                dbUser = projectParameter.getValue();
            } else if (Constants.DB_PASSWORD.equals(projectParameter.getName())) {
                dbPassword = projectParameter.getValue();
            } else if (Constants.DB_SERVER.equals(projectParameter.getName())) {
                dbServer = projectParameter.getValue();
            }
        }
        dbUrl = "jdbc:sqlserver://" + dbServer + ";databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true";

    }

    public List<ProjectQSEntity> getProjectQSList(String tableName) {
        try {
            jdbcTemplate = projectConnectionService.getJdbcDefaultConnection();
            String sqlQuery = "SELECT * FROM " + tableName + " WHERE [project_id] =" + projectId;

            // Create a RowMapper to map the query result to a ProjectQSEntity object
            RowMapper<ProjectQSEntity> rowMapper = (rs, rowNum) -> {
                ProjectQSEntity projectQSEntity = new ProjectQSEntity();
                projectQSEntity.setId(rs.getInt("id"));
                projectQSEntity.setName(rs.getString("name"));
                projectQSEntity.setSql(rs.getString("sql"));
                projectQSEntity.setDescription(rs.getString("description"));
                Projects projects = new Projects();
                projects.setId(rs.getLong("project_id"));
                projectQSEntity.setProject(projects);
                projectQSEntity.setCreate_date(rs.getDate("create_date"));
                return projectQSEntity;
            };

            List<ProjectQSEntity> fetchedData = jdbcTemplate.query(sqlQuery, rowMapper);

            return fetchedData;
        } catch (Exception ex) {
            ex.printStackTrace();
            String errorMessage = handleDatabaseError(ex);
            return Collections.emptyList();
        }
    }
    private void updateThreadCountField() {
        int count = threadCount.get();
        // Setze die Thread-Anzahl im Textfeld
        threadCountField.setValue(String.valueOf(count));
    }

    private void updateUi(UI ui, String result) {
        ui.access(() -> {
            Notification.show(result);

            updateThreadCountField();
            int count = threadCount.get();
            System.out.println("Anzahl Threads jetzt: " + count);

            grid.getDataProvider().refreshAll();

            if (count == 0)
            {
                progressBar.setVisible(false);
            }

        });

    }

    private void increaseThreadCount() {
        int count = threadCount.incrementAndGet();
    //    Notification.show("Anzahl der Threads: " + count);
        updateThreadCountField();
    }

    private void decreaseThreadCount() {
        int count = threadCount.decrementAndGet();
       // Notification.show("Anzahl der Threads: " + count);
        //System.out.println("in decreaseThreadCount count jetzt: " + count);
       }

   /*
    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
    }


    private void createGridComponent() {
        grid = new GridPro<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("100%");

        List<Client> clients = getClients();
        gridListDataView = grid.setItems(clients);
    }

    private void addColumnsToGrid() {
        createClientColumn();
        createAmountColumn();
        createStatusColumn();
        createDateColumn();
    }

    private void createClientColumn() {
        clientColumn = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setAlignItems(Alignment.CENTER);
            Image img = new Image(client.getImg(), "");
            Span span = new Span();
            span.setClassName("name");
            span.setText(client.getClient());
            hl.add(img, span);
            return hl;
        })).setComparator(client -> client.getClient()).setHeader("Client");
    }

    private void createAmountColumn() {
        amountColumn = grid
                .addEditColumn(Client::getAmount,
                        new NumberRenderer<>(client -> client.getAmount(), NumberFormat.getCurrencyInstance(Locale.US)))
                .text((item, newValue) -> item.setAmount(Double.parseDouble(newValue)))
                .setComparator(client -> client.getAmount()).setHeader("Amount");
    }

    private void createStatusColumn() {
        statusColumn = grid.addEditColumn(Client::getClient, new ComponentRenderer<>(client -> {
            Span span = new Span();
            span.setText(client.getStatus());
            span.getElement().setAttribute("theme", "badge " + client.getStatus().toLowerCase());
            return span;
        })).select((item, newValue) -> item.setStatus(newValue), Arrays.asList("Pending", "Success", "Error"))
                .setComparator(client -> client.getStatus()).setHeader("Status");
    }

    private void createDateColumn() {
        dateColumn = grid
                .addColumn(new LocalDateRenderer<>(client -> LocalDate.parse(client.getDate()),
                        () -> DateTimeFormatter.ofPattern("M/d/yyyy")))
                .setComparator(client -> client.getDate()).setHeader("Date").setWidth("180px").setFlexGrow(0);
    }

    private void addFiltersToGrid() {
        HeaderRow filterRow = grid.appendHeaderRow();

        TextField clientFilter = new TextField();
        clientFilter.setPlaceholder("Filter");
        clientFilter.setClearButtonVisible(true);
        clientFilter.setWidth("100%");
        clientFilter.setValueChangeMode(ValueChangeMode.EAGER);
        clientFilter.addValueChangeListener(event -> gridListDataView
                .addFilter(client -> StringUtils.containsIgnoreCase(client.getClient(), clientFilter.getValue())));
        filterRow.getCell(clientColumn).setComponent(clientFilter);

        TextField amountFilter = new TextField();
        amountFilter.setPlaceholder("Filter");
        amountFilter.setClearButtonVisible(true);
        amountFilter.setWidth("100%");
        amountFilter.setValueChangeMode(ValueChangeMode.EAGER);
        amountFilter.addValueChangeListener(event -> gridListDataView.addFilter(client -> StringUtils
                .containsIgnoreCase(Double.toString(client.getAmount()), amountFilter.getValue())));
        filterRow.getCell(amountColumn).setComponent(amountFilter);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.setItems(Arrays.asList("Pending", "Success", "Error"));
        statusFilter.setPlaceholder("Filter");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("100%");
        statusFilter.addValueChangeListener(
                event -> gridListDataView.addFilter(client -> areStatusesEqual(client, statusFilter)));
        filterRow.getCell(statusColumn).setComponent(statusFilter);

        DatePicker dateFilter = new DatePicker();
        dateFilter.setPlaceholder("Filter");
        dateFilter.setClearButtonVisible(true);
        dateFilter.setWidth("100%");
        dateFilter.addValueChangeListener(
                event -> gridListDataView.addFilter(client -> areDatesEqual(client, dateFilter)));
        filterRow.getCell(dateColumn).setComponent(dateFilter);
    }

    private boolean areStatusesEqual(Client client, ComboBox<String> statusFilter) {
        String statusFilterValue = statusFilter.getValue();
        if (statusFilterValue != null) {
            return StringUtils.equals(client.getStatus(), statusFilterValue);
        }
        return true;
    }

    private boolean areDatesEqual(Client client, DatePicker dateFilter) {
        LocalDate dateFilterValue = dateFilter.getValue();
        if (dateFilterValue != null) {
            LocalDate clientDate = LocalDate.parse(client.getDate());
            return dateFilterValue.equals(clientDate);
        }
        return true;
    }

    private List<Client> getClients() {
        return Arrays.asList(
                createClient(4957, "https://randomuser.me/api/portraits/women/42.jpg", "Amarachi Nkechi", 47427.0,
                        "Success", "2019-05-09"),
                createClient(675, "https://randomuser.me/api/portraits/women/24.jpg", "Bonelwa Ngqawana", 70503.0,
                        "Success", "2019-05-09"),
                createClient(6816, "https://randomuser.me/api/portraits/men/42.jpg", "Debashis Bhuiyan", 58931.0,
                        "Success", "2019-05-07"),
                createClient(5144, "https://randomuser.me/api/portraits/women/76.jpg", "Jacqueline Asong", 25053.0,
                        "Pending", "2019-04-25"),
                createClient(9800, "https://randomuser.me/api/portraits/men/24.jpg", "Kobus van de Vegte", 7319.0,
                        "Pending", "2019-04-22"),
                createClient(3599, "https://randomuser.me/api/portraits/women/94.jpg", "Mattie Blooman", 18441.0,
                        "Error", "2019-04-17"),
                createClient(3989, "https://randomuser.me/api/portraits/men/76.jpg", "Oea Romana", 33376.0, "Pending",
                        "2019-04-17"),
                createClient(1077, "https://randomuser.me/api/portraits/men/94.jpg", "Stephanus Huggins", 75774.0,
                        "Success", "2019-02-26"),
                createClient(8942, "https://randomuser.me/api/portraits/men/16.jpg", "Torsten Paulsson", 82531.0,
                        "Pending", "2019-02-21"));
    }

    private Client createClient(int id, String img, String client, double amount, String status, String date) {
        Client c = new Client();
        c.setId(id);
        c.setImg(img);
        c.setClient(client);
        c.setAmount(amount);
        c.setStatus(status);
        c.setDate(date);

        return c;
    }

     */



};
