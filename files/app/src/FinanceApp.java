import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;

public class FinanceApp extends Application {

    private Connection connection;
    private int currentUserId;
    private Stage primaryStage;
    private TabPane mainTabPane; // Ссылка на TabPane для отслеживания переключений
    
    private ComboBox<String> accountComboBox;
    private ComboBox<String> categoryComboBox;
    private TextField amountField;
    private DatePicker datePicker;
    private TextField descriptionField;
    private Label balanceLabel;
    private TextArea statsArea;
    private TextArea goalsArea;
    private ListView<String> accountsListView;
    private ComboBox<String> accountForGoalBox;
    private ComboBox<String> goalSelectionBox;
    private TextField amountToGoalField;

    private static Properties props;
    private static String activeDbType;

    static {
        try {
            props = new Properties();
            FileInputStream fis = new FileInputStream("database.config");
            props.load(fis);
            activeDbType = props.getProperty("ACTIVE_DB", "POSTGRESQL").toUpperCase();
            fis.close();
        } catch (IOException e) {
            System.err.println("Ошибка: Файл database.config не найден в корне проекта.");
            System.exit(1);
        }
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showDatabaseSelection();
    }

    private void showDatabaseSelection() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label title = new Label("Система учета финансов");
        title.setFont(Font.font("Arial", 24));

        Label dbInfo = new Label("Активная БД в config: " + activeDbType);
        dbInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        Button connectBtn = new Button("Подключиться к БД");
        connectBtn.setStyle("-fx-font-size: 16; -fx-padding: 10 20;");
        connectBtn.setOnAction(e -> connectToDb());

        root.getChildren().addAll(title, dbInfo, connectBtn);
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Выбор БД");
        primaryStage.show();
    }

    private void connectToDb() {
        try {
            String url, user, pass, driver;

            if (activeDbType.equals("MSSQL")) {
                url = props.getProperty("MSSQL_URL");
                user = props.getProperty("MSSQL_USER");
                pass = props.getProperty("MSSQL_PASSWORD");
                driver = props.getProperty("MSSQL_DRIVER");
            } else {
                url = props.getProperty("POSTGRESQL_URL");
                user = props.getProperty("POSTGRESQL_USER");
                pass = props.getProperty("POSTGRESQL_PASSWORD");
                driver = props.getProperty("POSTGRESQL_DRIVER");
            }

            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pass);
            
            showAlert("Успех", "Подключено к " + activeDbType, Alert.AlertType.INFORMATION);
            showLoginOrRegister();
            
        } catch (Exception e) {
            showAlert("Ошибка подключения", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showLoginOrRegister() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Добро пожаловать!");
        titleLabel.setFont(Font.font("Arial", 18));

        Button loginBtn = new Button("Войти в систему");
        loginBtn.setPrefWidth(200);
        loginBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14;");
        loginBtn.setOnAction(e -> showLogin());

        Button registerBtn = new Button("Регистрация");
        registerBtn.setPrefWidth(200);
        registerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14;");
        registerBtn.setOnAction(e -> showRegister());

        root.getChildren().addAll(titleLabel, loginBtn, registerBtn);
        primaryStage.setScene(new Scene(root, 300, 250));
    }

    private void showLogin() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Вход в систему");
        titleLabel.setFont(Font.font("Arial", 16));

        TextField userField = new TextField();
        userField.setPromptText("Логин");
        userField.setPrefWidth(200);
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Пароль");
        passField.setPrefWidth(200);

        Button loginBtn = new Button("Войти");
        loginBtn.setPrefWidth(200);
        loginBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        loginBtn.setOnAction(e -> login(userField.getText(), passField.getText()));

        Button backBtn = new Button("Назад");
        backBtn.setPrefWidth(200);
        backBtn.setOnAction(e -> showLoginOrRegister());

        Label hint = new Label("Тест: ivanov_ii / password123");
        hint.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        root.getChildren().addAll(titleLabel, userField, passField, loginBtn, backBtn, hint);
        primaryStage.setScene(new Scene(root, 300, 320));
    }

    private void showRegister() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Регистрация нового пользователя");
        titleLabel.setFont(Font.font("Arial", 16));

        TextField userField = new TextField();
        userField.setPromptText("Придумайте логин");
        userField.setPrefWidth(250);
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Придумайте пароль");
        passField.setPrefWidth(250);

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Подтвердите пароль");
        confirmPassField.setPrefWidth(250);

        Button registerBtn = new Button("Зарегистрироваться");
        registerBtn.setPrefWidth(250);
        registerBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> register(userField.getText(), passField.getText(), confirmPassField.getText()));

        Button backBtn = new Button("Назад");
        backBtn.setPrefWidth(250);
        backBtn.setOnAction(e -> showLoginOrRegister());

        root.getChildren().addAll(titleLabel, userField, passField, confirmPassField, registerBtn, backBtn);
        primaryStage.setScene(new Scene(root, 350, 350));
    }

    private void register(String username, String password, String confirmPassword) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert("Ошибка", "Логин и пароль не могут быть пустыми!", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Ошибка", "Пароли не совпадают!", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 4) {
            showAlert("Ошибка", "Пароль должен содержать минимум 4 символа!", Alert.AlertType.ERROR);
            return;
        }

        try {
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement psCheck = connection.prepareStatement(checkSql);
            psCheck.setString(1, username.trim());
            ResultSet rs = psCheck.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Ошибка", "Пользователь с таким логином уже существует!\nВыберите другой логин.", Alert.AlertType.ERROR);
                return;
            }

            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            PreparedStatement psInsert = connection.prepareStatement(insertSql);
            psInsert.setString(1, username.trim());
            psInsert.setString(2, password); 
            psInsert.executeUpdate();

            showAlert("Успех", "Регистрация прошла успешно!\nТеперь войдите в систему.", Alert.AlertType.INFORMATION);
            showLogin();

        } catch (SQLException e) {
            showAlert("Ошибка регистрации", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void login(String username, String password) {
        try {
            String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUserId = rs.getInt("id");
                showMainWindow();
            } else {
                showAlert("Ошибка", "Неверный логин или пароль", Alert.AlertType.ERROR);
            }
        } catch (SQLException ex) {
            showAlert("Ошибка", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showMainWindow() {
        mainTabPane = new TabPane();
        
        Tab accountsTab = createTab("Счета", createAccountsTab());
        Tab transactionsTab = createTab("Транзакции", createTransactionsTab());
        Tab goalsTab = createTab("Цели", createGoalsTab());
        Tab statsTab = createTab("Аналитика", createStatsTab());
        
        mainTabPane.getTabs().addAll(accountsTab, transactionsTab, goalsTab, statsTab);
        
        // 🔥 АВТОМАТИЧЕСКАЯ ЗАГРУЗКА ДАННЫХ ПРИ ПЕРЕКЛЮЧЕНИИ ВКЛАДОК
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                if ("Счета".equals(newTab.getText())) {
                    refreshAccounts();
                } else if ("Транзакции".equals(newTab.getText())) {
                    loadDropdowns();
                } else if ("Цели".equals(newTab.getText())) {
                    refreshGoals();
                    loadGoalDropdowns();
                } else if ("Аналитика".equals(newTab.getText())) {
                    loadStats();
                }
            }
        });
        
        primaryStage.setScene(new Scene(mainTabPane, 850, 650));
        primaryStage.setTitle("Finance Manager (" + activeDbType + ") - ID: " + currentUserId);
        
        refreshAccounts();
    }

    private Tab createTab(String name, Node content) {
        Tab t = new Tab(name);
        t.setContent(content);
        t.setClosable(false);
        return t;
    }

    private VBox createAccountsTab() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Управление счетами");
        title.setFont(Font.font("Arial", 18));

        HBox inputBox = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Название счета");
        nameField.setPrefWidth(250);
        
        Button addBtn = new Button("Добавить счет");
        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            try {
                String sql = "INSERT INTO accounts (user_id, name, balance) VALUES (?, ?, 0)";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, currentUserId);
                ps.setString(2, nameField.getText());
                ps.executeUpdate();
                refreshAccounts();
                nameField.clear();
                showAlert("Успех", "Счет добавлен", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Ошибка", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        inputBox.getChildren().addAll(nameField, addBtn);

        balanceLabel = new Label("Общий баланс: загрузка...");
        balanceLabel.setFont(Font.font("Arial", 16));
        balanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        accountsListView = new ListView<>();
        accountsListView.setPrefHeight(300);

        root.getChildren().addAll(title, inputBox, balanceLabel, accountsListView);
        return root;
    }

    private void refreshAccounts() {
        try {
            String sql = "SELECT SUM(balance) as total FROM accounts WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                balanceLabel.setText(String.format("Общий баланс: %.2f ₽", rs.getDouble("total")));
            }

            ObservableList<String> list = FXCollections.observableArrayList();
            String sqlList = "SELECT name, balance FROM accounts WHERE user_id = ?";
            ps = connection.prepareStatement(sqlList);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("name") + " - " + rs.getDouble("balance") + " ₽");
            }
            accountsListView.setItems(list);
        } catch (SQLException e) {
            if (balanceLabel != null) balanceLabel.setText("Ошибка загрузки");
        }
    }

    private VBox createTransactionsTab() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Добавление транзакции");
        title.setFont(Font.font("Arial", 18));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Счет:"), 0, 0);
        accountComboBox = new ComboBox<>();
        accountComboBox.setPrefWidth(250);
        grid.add(accountComboBox, 1, 0);

        grid.add(new Label("Категория:"), 0, 1);
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPrefWidth(250);
        grid.add(categoryComboBox, 1, 1);

        grid.add(new Label("Сумма:"), 0, 2);
        amountField = new TextField();
        amountField.setPromptText("0.00");
        grid.add(amountField, 1, 2);

        grid.add(new Label("Дата:"), 0, 3);
        datePicker = new DatePicker(LocalDate.now());
        grid.add(datePicker, 1, 3);

        grid.add(new Label("Описание:"), 0, 4);
        descriptionField = new TextField();
        descriptionField.setPromptText("Комментарий");
        grid.add(descriptionField, 1, 4);

        Button addTransBtn = new Button("Добавить транзакцию");
        addTransBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14;");
        addTransBtn.setPrefWidth(250);
        addTransBtn.setOnAction(e -> addTransaction());
        grid.add(addTransBtn, 1, 5);

        // Кнопка "Загрузить справочники" УДАЛЕНА - теперь загрузка автоматическая

        root.getChildren().addAll(title, grid);
        return root;
    }

    private void loadDropdowns() {
        try {
            ObservableList<String> accList = FXCollections.observableArrayList();
            PreparedStatement ps = connection.prepareStatement("SELECT id, name FROM accounts WHERE user_id = ?");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                accList.add(rs.getInt("id") + " - " + rs.getString("name"));
            }
            accountComboBox.setItems(accList);

            ObservableList<String> catList = FXCollections.observableArrayList();
            ps = connection.prepareStatement("SELECT id, name, type FROM categories");
            rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type").equals("income") ? "(Доход)" : "(Расход)";
                catList.add(rs.getInt("id") + " - " + rs.getString("name") + " " + type);
            }
            categoryComboBox.setItems(catList);
        } catch (SQLException e) {
            showAlert("Ошибка", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addTransaction() {
        try {
            if (accountComboBox.getValue() == null || categoryComboBox.getValue() == null) {
                showAlert("Ошибка", "Выберите счет и категорию", Alert.AlertType.WARNING);
                return;
            }

            int accId = Integer.parseInt(accountComboBox.getValue().split(" - ")[0]);
            int catId = Integer.parseInt(categoryComboBox.getValue().split(" - ")[0]);
            double amount = Double.parseDouble(amountField.getText());
            
            if (amount <= 0) {
                showAlert("Ошибка ввода", "Сумма должна быть больше нуля", Alert.AlertType.WARNING);
                return;
            }

            // Определяем тип категории
            PreparedStatement psType = connection.prepareStatement("SELECT type FROM categories WHERE id = ?");
            psType.setInt(1, catId);
            ResultSet rsType = psType.executeQuery();
            if (!rsType.next()) {
                showAlert("Ошибка", "Категория не найдена", Alert.AlertType.ERROR);
                return;
            }
            String type = rsType.getString("type");

            // Проверяем баланс ДО начала транзакции
            if (type.equals("expense")) {
                PreparedStatement psCheck = connection.prepareStatement(
                    "SELECT balance FROM accounts WHERE id = ? AND user_id = ?");
                psCheck.setInt(1, accId);
                psCheck.setInt(2, currentUserId);
                ResultSet rsCheck = psCheck.executeQuery();
                
                if (rsCheck.next()) {
                    double currentBalance = rsCheck.getDouble("balance");
                    if (currentBalance < amount) {
                        showAlert("Недостаточно средств", 
                            String.format("На счете недостаточно средств.\n" +
                                        "Текущий баланс: %.2f ₽\n" +
                                        "Требуется: %.2f ₽\n" +
                                        "Не хватает: %.2f ₽", 
                                        currentBalance, amount, amount - currentBalance), 
                            Alert.AlertType.ERROR);
                        return;
                    }
                }
            }

            // 🔥 ОБЕРТЫВАЕМ ВСЕ ОПЕРАЦИИ В ТРАНЗАКЦИЮ
            connection.setAutoCommit(false);
            
            try {
                // 1. Вставляем транзакцию
                String sqlInsert = "INSERT INTO transactions (account_id, category_id, amount, transaction_date, description) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psIns = connection.prepareStatement(sqlInsert);
                psIns.setInt(1, accId);
                psIns.setInt(2, catId);
                psIns.setDouble(3, amount);
                psIns.setDate(4, Date.valueOf(datePicker.getValue()));
                psIns.setString(5, descriptionField.getText());
                psIns.executeUpdate();

                // 2. Обновляем баланс
                String sqlUpdate = type.equals("income")
                    ? "UPDATE accounts SET balance = balance + ? WHERE id = ?"
                    : "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                
                PreparedStatement psUpd = connection.prepareStatement(sqlUpdate);
                psUpd.setDouble(1, amount);
                psUpd.setInt(2, accId);
                int rowsUpdated = psUpd.executeUpdate();
                
                if (rowsUpdated == 0) {
                    throw new SQLException("Не удалось обновить баланс счета");
                }

                // ✅ Если всё успешно - коммитим
                connection.commit();
                
                showAlert("Успех", "Транзакция добавлена", Alert.AlertType.INFORMATION);
                amountField.clear();
                descriptionField.clear();
                loadDropdowns();
                
            } catch (SQLException e) {
                // ❌ Если ошибка - откатываем ВСЁ
                connection.rollback();
                
                // Понятное сообщение об ошибке
                if (e.getMessage().contains("chk_balance_non_negative")) {
                    showAlert("Ошибка операции", 
                        "Не удалось выполнить операцию: недостаточно средств на счете.\n" +
                        "Проверьте баланс и попробуйте снова.", 
                        Alert.AlertType.ERROR);
                } else if (e.getMessage().contains("FOREIGN KEY")) {
                    showAlert("Ошибка данных", 
                        "Выбранный счет или категория больше не существуют.\n" +
                        "Обновите списки и попробуйте снова.", 
                        Alert.AlertType.ERROR);
                } else {
                    showAlert("Ошибка базы данных", 
                        "Произошла ошибка при сохранении транзакции:\n" + e.getMessage(), 
                        Alert.AlertType.ERROR);
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            
        } catch (NumberFormatException e) {
            showAlert("Ошибка ввода", "Некорректная сумма. Введите числовое значение.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            // Уже обработано выше, просто логируем
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Неожиданная ошибка", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    
    private VBox createGoalsTab() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label createTitle = new Label("1. Создание новой цели");
        createTitle.setFont(Font.font("Arial", 16));
        
        GridPane createGrid = new GridPane();
        createGrid.setHgap(10); createGrid.setVgap(10);

        TextField newGoalName = new TextField(); newGoalName.setPromptText("Название");
        TextField newGoalAmount = new TextField(); newGoalAmount.setPromptText("Целевая сумма");
        
        Button createBtn = new Button("Создать");
        createBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
        createBtn.setOnAction(e -> {
            try {
                String sql = "INSERT INTO savings_goals (user_id, name, target_amount, current_amount) VALUES (?, ?, ?, 0)";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, currentUserId);
                ps.setString(2, newGoalName.getText());
                ps.setDouble(3, Double.parseDouble(newGoalAmount.getText()));
                ps.executeUpdate();
                newGoalName.clear(); newGoalAmount.clear();
                refreshGoals(); loadGoalDropdowns();
                showAlert("Успех", "Цель создана", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Ошибка", "Проверьте данные", Alert.AlertType.ERROR);
            }
        });

        createGrid.add(new Label("Название:"), 0, 0); createGrid.add(newGoalName, 1, 0);
        createGrid.add(new Label("Сумма:"), 0, 1); createGrid.add(newGoalAmount, 1, 1);
        createGrid.add(createBtn, 1, 2);

        Separator separator = new Separator();
        Label refillTitle = new Label("2. Пополнение цели со счёта");
        refillTitle.setFont(Font.font("Arial", 16));

        VBox refillContainer = new VBox(10);
        
        accountForGoalBox = new ComboBox<>();
        accountForGoalBox.setPromptText("Выберите счёт для списания");
        
        goalSelectionBox = new ComboBox<>();
        goalSelectionBox.setPromptText("Выберите цель");
        
        amountToGoalField = new TextField();
        amountToGoalField.setPromptText("Сумма перевода");

        HBox actionBox = new HBox(10);
        Button refillBtn = new Button("Перевести средства");
        refillBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        refillBtn.setOnAction(e -> replenishGoal());

        actionBox.getChildren().addAll(refillBtn);
        refillContainer.getChildren().addAll(accountForGoalBox, goalSelectionBox, amountToGoalField, actionBox);

        goalsArea = new TextArea();
        goalsArea.setPrefHeight(250);
        goalsArea.setEditable(false);

        root.getChildren().addAll(createTitle, createGrid, separator, refillTitle, refillContainer, goalsArea);
        
        return root;
    }

    private void loadGoalDropdowns() {
        try {
            ObservableList<String> accList = FXCollections.observableArrayList();
            PreparedStatement psAcc = connection.prepareStatement(
                "SELECT id, name, balance FROM accounts WHERE user_id = ? ORDER BY name");
            psAcc.setInt(1, currentUserId);
            ResultSet rsAcc = psAcc.executeQuery();
            while (rsAcc.next()) {
                accList.add(rsAcc.getInt("id") + " - " + rsAcc.getString("name") + 
                           " (" + rsAcc.getDouble("balance") + " ₽)");
            }
            accountForGoalBox.setItems(accList);

            ObservableList<String> goalList = FXCollections.observableArrayList();
            PreparedStatement psGoal = connection.prepareStatement(
                "SELECT id, name, target_amount, current_amount FROM savings_goals WHERE user_id = ?");
            psGoal.setInt(1, currentUserId);
            ResultSet rsGoal = psGoal.executeQuery();
            while (rsGoal.next()) {
                double remaining = rsGoal.getDouble("target_amount") - rsGoal.getDouble("current_amount");
                goalList.add(rsGoal.getInt("id") + " - " + rsGoal.getString("name") + 
                           " (осталось: " + remaining + " ₽)");
            }
            goalSelectionBox.setItems(goalList);

        } catch (SQLException e) {
            showAlert("Ошибка загрузки списков", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void replenishGoal() {
        try {
            if (accountForGoalBox.getValue() == null || goalSelectionBox.getValue() == null || 
                amountToGoalField.getText().trim().isEmpty()) {
                showAlert("Ошибка ввода", "Пожалуйста, выберите счёт, цель и введите сумму.", Alert.AlertType.WARNING);
                return;
            }

            int accountId = Integer.parseInt(accountForGoalBox.getValue().split(" - ")[0]);
            int goalId = Integer.parseInt(goalSelectionBox.getValue().split(" - ")[0]);
            double amount;
            
            try {
                amount = Double.parseDouble(amountToGoalField.getText().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                showAlert("Ошибка формата", "Сумма должна быть корректным числом.", Alert.AlertType.ERROR);
                return;
            }

            if (amount <= 0) {
                showAlert("Ошибка значения", "Сумма перевода должна быть больше нуля.", Alert.AlertType.WARNING);
                return;
            }

            // Проверяем баланс
            String checkBalanceSql = "SELECT balance FROM accounts WHERE id = ? AND user_id = ?";
            PreparedStatement psCheckBalance = connection.prepareStatement(checkBalanceSql);
            psCheckBalance.setInt(1, accountId);
            psCheckBalance.setInt(2, currentUserId);
            ResultSet rsBalance = psCheckBalance.executeQuery();

            if (!rsBalance.next()) {
                showAlert("Ошибка", "Выбранный счёт не найден.", Alert.AlertType.ERROR);
                return;
            }

            double currentBalance = rsBalance.getDouble("balance");
            if (currentBalance < amount) {
                showAlert("Недостаточно средств", 
                    String.format("На счёте недостаточно средств.\n" +
                                "Текущий баланс: %.2f ₽\n" +
                                "Требуется: %.2f ₽\n" +
                                "Не хватает: %.2f ₽", 
                                currentBalance, amount, amount - currentBalance), 
                    Alert.AlertType.ERROR);
                return;
            }

            // Проверяем цель
            String checkGoalSql = "SELECT target_amount, current_amount FROM savings_goals WHERE id = ? AND user_id = ?";
            PreparedStatement psCheckGoal = connection.prepareStatement(checkGoalSql);
            psCheckGoal.setInt(1, goalId);
            psCheckGoal.setInt(2, currentUserId);
            ResultSet rsGoal = psCheckGoal.executeQuery();

            if (!rsGoal.next()) {
                showAlert("Ошибка", "Выбранная цель не найдена.", Alert.AlertType.ERROR);
                return;
            }

            double targetAmount = rsGoal.getDouble("target_amount");
            double currentAmount = rsGoal.getDouble("current_amount");
            double remainingAmount = targetAmount - currentAmount;

            if (amount > remainingAmount) {
                showAlert("Превышение цели", 
                    String.format("До цели осталось %.2f ₽.\n" +
                                 "Вы пытаетесь перевести %.2f ₽.\n" +
                                 "Введите сумму не больше %.2f ₽", 
                                 remainingAmount, amount, remainingAmount), 
                    Alert.AlertType.ERROR);
                return;
            }

            // 🔥 ТРАНЗАКЦИЯ для пополнения цели
            connection.setAutoCommit(false);
            try {
                String deductSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                PreparedStatement psDeduct = connection.prepareStatement(deductSql);
                psDeduct.setDouble(1, amount);
                psDeduct.setInt(2, accountId);
                psDeduct.executeUpdate();

                String addGoalSql = "UPDATE savings_goals SET current_amount = current_amount + ? WHERE id = ?";
                PreparedStatement psAdd = connection.prepareStatement(addGoalSql);
                psAdd.setDouble(1, amount);
                psAdd.setInt(2, goalId);
                psAdd.executeUpdate();

                connection.commit();
                
                showAlert("Успешно!", String.format("Переведено %.2f ₽ со счёта на цель.", amount), Alert.AlertType.INFORMATION);
                
                amountToGoalField.clear();
                refreshAccounts();
                refreshGoals();
                loadGoalDropdowns();
                
            } catch (SQLException e) {
                connection.rollback();
                if (e.getMessage().contains("chk_balance_non_negative")) {
                    showAlert("Ошибка операции", 
                        "Недостаточно средств на счете для выполнения операции.", 
                        Alert.AlertType.ERROR);
                } else {
                    showAlert("Ошибка базы данных", e.getMessage(), Alert.AlertType.ERROR);
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            if (!e.getMessage().contains("chk_balance_non_negative")) {
                e.printStackTrace();
            }
        }
    }

    private void refreshGoals() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT name, target_amount, current_amount FROM savings_goals WHERE user_id = ?");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder("=== ВАШИ ЦЕЛИ ===\n\n");
            while (rs.next()) {
                double percent = (rs.getDouble("current_amount") / rs.getDouble("target_amount")) * 100;
                double remaining = rs.getDouble("target_amount") - rs.getDouble("current_amount");
                sb.append(String.format(" %s\n", rs.getString("name")));
                sb.append(String.format("   Цель: %.2f ₽\n", rs.getDouble("target_amount")));
                sb.append(String.format("   Накоплено: %.2f ₽ (%.1f%%)\n", 
                    rs.getDouble("current_amount"), percent));
                sb.append(String.format("   Осталось: %.2f ₽\n\n", remaining));
            }
            goalsArea.setText(sb.toString());
        } catch (SQLException e) {
            goalsArea.setText("Ошибка: " + e.getMessage());
        }
    }

    private VBox createStatsTab() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Аналитика и статистика");
        title.setFont(Font.font("Arial", 18));

        statsArea = new TextArea();
        statsArea.setPrefHeight(450);
        statsArea.setEditable(false);
        statsArea.setFont(new javafx.scene.text.Font("Consolas", 12));

        root.getChildren().addAll(title, statsArea);
        return root;
    }

    private void loadStats() {
        try {
            StringBuilder sb = new StringBuilder("=== ФИНАНСОВАЯ АНАЛИТИКА ===\n\n");

            // 1. ВСЕГО ДОХОДОВ
            String incomeSql = "SELECT SUM(t.amount) FROM transactions t " +
                             "JOIN accounts a ON t.account_id = a.id " +
                             "JOIN categories c ON t.category_id = c.id " +
                             "WHERE a.user_id = ? AND c.type = 'income'";
            PreparedStatement ps = connection.prepareStatement(incomeSql);
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Double income = rs.getObject(1) != null ? rs.getDouble(1) : 0.0;
                sb.append(String.format("📈 ВСЕГО ДОХОДОВ: %.2f ₽\n\n", income));
            }

            // 2. ДОХОДЫ ПО КАТЕГОРИЯМ
            String incomeByCategorySql = "SELECT c.name, SUM(t.amount) as total " +
                                       "FROM transactions t " +
                                       "JOIN accounts a ON t.account_id = a.id " +
                                       "JOIN categories c ON t.category_id = c.id " +
                                       "WHERE a.user_id = ? AND c.type = 'income' " +
                                       "GROUP BY c.name ORDER BY total DESC";
            ps = connection.prepareStatement(incomeByCategorySql);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();
            
            sb.append("📊 ДОХОДЫ ПО КАТЕГОРИЯМ:\n");
            double totalIncome = 0;
            boolean hasIncome = false;
            while (rs.next()) {
                hasIncome = true;
                double amount = rs.getDouble("total");
                sb.append(String.format("   • %s: %.2f ₽\n", rs.getString("name"), amount));
                totalIncome += amount;
            }
            if (hasIncome) {
                sb.append(String.format("\n   Итого доходов: %.2f ₽\n\n", totalIncome));
            } else {
                sb.append("   Нет доходов\n\n");
            }

            // 3. РАСХОДЫ ПО КАТЕГОРИЯМ
            String expSql = "SELECT c.name, SUM(t.amount) as total " +
                          "FROM transactions t " +
                          "JOIN accounts a ON t.account_id = a.id " +
                          "JOIN categories c ON t.category_id = c.id " +
                          "WHERE a.user_id = ? AND c.type = 'expense' " +
                          "GROUP BY c.name ORDER BY total DESC";
            ps = connection.prepareStatement(expSql);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();
            
            sb.append("📊 РАСХОДЫ ПО КАТЕГОРИЯМ:\n");
            double totalExpenses = 0;
            boolean hasExpenses = false;
            while (rs.next()) {
                hasExpenses = true;
                double amount = rs.getDouble("total");
                sb.append(String.format("   • %s: %.2f ₽\n", rs.getString("name"), amount));
                totalExpenses += amount;
            }
            if (hasExpenses) {
                sb.append(String.format("\n   Итого расходов: %.2f ₽\n\n", totalExpenses));
            } else {
                sb.append("   Нет расходов\n\n");
            }

            // 4. 🔥 ВСЕ ОПЕРАЦИИ (с комментариями)
            sb.append("📋 ВСЕ ОПЕРАЦИИ:\n");
            
            String allOpsSql = getAllTransactionsQuery();
            ps = connection.prepareStatement(allOpsSql);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();
            
            boolean hasOps = false;
            while (rs.next()) {
                hasOps = true;
                String typeLabel = rs.getString("type").equals("income") ? "Доход" : "Расход";
                String description = rs.getString("description");
                
                // Форматируем вывод с комментарием (если есть)
                if (description != null && !description.trim().isEmpty()) {
                    sb.append(String.format("   %s | %s | %.2f ₽ | [%s]\n   └─ 💬 %s\n",
                        rs.getDate("transaction_date"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        typeLabel,
                        description));
                } else {
                    sb.append(String.format("   %s | %s | %.2f ₽ | [%s]\n",
                        rs.getDate("transaction_date"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        typeLabel));
                }
            }
            if (!hasOps) {
                sb.append("   Нет операций\n");
            }

            statsArea.setText(sb.toString());
        } catch (SQLException e) {
            statsArea.setText("Ошибка загрузки статистики: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getAllTransactionsQuery() {
        // Добавлено поле t.description
        if (activeDbType.equals("MSSQL")) {
            return "SELECT t.amount, t.transaction_date, c.name as category, c.type, t.description " +
                   "FROM transactions t " +
                   "JOIN accounts a ON t.account_id = a.id " +
                   "JOIN categories c ON t.category_id = c.id " +
                   "WHERE a.user_id = ? " +
                   "ORDER BY t.transaction_date DESC";
        } else {
            return "SELECT t.amount, t.transaction_date, c.name as category, c.type, t.description " +
                   "FROM transactions t " +
                   "JOIN accounts a ON t.account_id = a.id " +
                   "JOIN categories c ON t.category_id = c.id " +
                   "WHERE a.user_id = ? " +
                   "ORDER BY t.transaction_date DESC";
        }
    }




    private String getLastTransactionsQuery() {
        if (activeDbType.equals("MSSQL")) {
            return "SELECT TOP 5 t.amount, t.transaction_date, t.description, c.name as category " +
                   "FROM transactions t " +
                   "JOIN accounts a ON t.account_id = a.id " +
                   "JOIN categories c ON t.category_id = c.id " +
                   "WHERE a.user_id = ? " +
                   "ORDER BY t.transaction_date DESC";
        } else {
            return "SELECT t.amount, t.transaction_date, t.description, c.name as category " +
                   "FROM transactions t " +
                   "JOIN accounts a ON t.account_id = a.id " +
                   "JOIN categories c ON t.category_id = c.id " +
                   "WHERE a.user_id = ? " +
                   "ORDER BY t.transaction_date DESC LIMIT 5";
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
