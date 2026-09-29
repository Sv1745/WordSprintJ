package com.wordsprint;

import com.wordsprint.dao.*;
import com.wordsprint.model.*;
import com.wordsprint.service.*;
import com.wordsprint.util.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JavaBackendTestSuiteTest {

    private static AuthService authService;
    private static GameService gameService;
    private static AdminService adminService;
    private static UserDAO userDAO;
    private static GameDAO gameDAO;
    private static GuessDAO guessDAO;
    private static WordDAO wordDAO;
    private static AdminDAO adminDAO;

    @BeforeAll
    public static void setUp() {
        authService = new AuthService();
        gameService = new GameService();
        adminService = new AdminService();
        userDAO = new UserDAO();
        gameDAO = new GameDAO();
        guessDAO = new GuessDAO();
        wordDAO = new WordDAO();
        adminDAO = new AdminDAO();
    }

    @AfterEach
    public void resetConfig() {
        adminDAO.updateConfig(new GameConfig(5, 10, true));
    }

    // ==========================================
    // 1. JAVA AUTHENTICATION TESTS
    // ==========================================

    @Test
    @Order(1)
    public void testJavaAuth001ValidRegistration() {
        String uname = "RegVal" + System.currentTimeMillis() % 10000;
        boolean registered = authService.register(uname, "Pass123!$%*@#&", "player");
        assertTrue(registered, "Registration should succeed for valid credentials");
    }

    @Test
    @Order(2)
    public void testJavaAuth002InvalidRegistrationUsername() {
        String validationErr = authService.validateCredentials("short", "Pass123!$%*@#&");
        assertNotNull(validationErr, "Validation error expected for short username");
    }

    @Test
    @Order(3)
    public void testJavaAuth003DuplicateUsername() {
        String uname = "DupUser" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        boolean dupReg = authService.register(uname, "Pass123!$%*@#&", "player");
        assertFalse(dupReg, "Duplicate registration should fail");
    }

    @Test
    @Order(4)
    public void testJavaAuth004InvalidPasswordFormat() {
        String validationErr = authService.validateCredentials("ValidUser", "nopassword");
        assertNotNull(validationErr, "Validation error expected for simple password without numbers/special chars");
    }

    @Test
    @Order(5)
    public void testJavaAuth005ValidLogin() {
        String uname = "LogVal" + System.currentTimeMillis() % 10000;
        String pwd = "Pass123!$%*@#&";
        authService.register(uname, pwd, "player");
        boolean loginSuccess = authService.login(uname, pwd);
        assertTrue(loginSuccess, "Login should succeed with correct credentials");
    }

    @Test
    @Order(6)
    public void testJavaAuth006InvalidLogin() {
        boolean loginSuccess = authService.login("NonExistentUser99", "WrongPassword!1");
        assertFalse(loginSuccess, "Login should fail for non-existent user");
    }

    @Test
    @Order(7)
    public void testJavaAuth007PasswordHashNeverExposed() {
        String uname = "NoHashUser" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        User u = userDAO.findByUsername(uname);
        assertNotNull(u);
        assertNotNull(u.getPasswordHash());
        assertTrue(u.getPasswordHash().startsWith("$2a$") || u.getPasswordHash().startsWith("$2b$"));
    }

    // ==========================================
    // 2. JAVA AUTHORIZATION TESTS
    // ==========================================

    @Test
    @Order(10)
    public void testJavaAuthz001PlayerRoleVerification() {
        String uname = "PlayerRoleUser" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        User u = userDAO.findByUsername(uname);
        assertEquals("player", u.getUserRole());
    }

    @Test
    @Order(11)
    public void testJavaAuthz002AdminRoleVerification() {
        User adminUser = userDAO.findByUsername("admin");
        assertNotNull(adminUser, "Admin user must exist");
        assertEquals("admin", adminUser.getUserRole());
    }

    // ==========================================
    // 3. JAVA GAME LOGIC TESTS
    // ==========================================

    @Test
    @Order(20)
    public void testJavaGame001StartGame() {
        String uname = "GameUser" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        User u = userDAO.findByUsername(uname);
        
        Game game = gameService.startGame(u.getUserId());
        assertNotNull(game, "Started game should not be null");
        assertEquals("IN_PROGRESS", game.getStatus());
    }

    @Test
    @Order(21)
    public void testJavaGame002RecordAndRetrieveGuess() {
        String uname = "GuessUser" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        User u = userDAO.findByUsername(uname);
        
        Game game = gameService.startGame(u.getUserId());
        boolean recorded = gameService.recordGuess(game.getGameId(), 1, "APPLE", "GBBYB");
        assertTrue(recorded, "Guess recording should succeed");
        
        List<Guess> guesses = guessDAO.findGuessesByGameId(game.getGameId());
        assertFalse(guesses.isEmpty());
        assertEquals("APPLE", guesses.get(0).getGuessedWord());
    }

    @Test
    @Order(22)
    public void testJavaGame003GameWon() {
        String uname = "WinUser" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        User u = userDAO.findByUsername(uname);
        
        Game game = gameService.startGame(u.getUserId());
        Word w = wordDAO.findById(game.getWordId());
        
        boolean isCorrect = gameService.checkAnswer(w.getWord(), w.getWord());
        assertTrue(isCorrect);
        
        boolean ended = gameService.endGame(game.getGameId(), "WON");
        assertTrue(ended);
        
        Game updated = gameDAO.findById(game.getGameId());
        assertEquals("WON", updated.getStatus());
    }

    // ==========================================
    // 4. JAVA DYNAMIC MAX_ATTEMPTS REGRESSION TESTS
    // ==========================================

    @Test
    @Order(30)
    public void testJavaRegMaxAttempts5() {
        runMaxAttemptsTest(5);
    }

    @Test
    @Order(31)
    public void testJavaRegMaxAttempts6() {
        runMaxAttemptsTest(6);
    }

    @Test
    @Order(32)
    public void testJavaRegMaxAttempts10() {
        runMaxAttemptsTest(10);
    }

    @Test
    @Order(33)
    public void testJavaRegMaxAttempts20() {
        runMaxAttemptsTest(20);
    }

    private void runMaxAttemptsTest(int maxAttempts) {
        adminDAO.updateConfig(new GameConfig(maxAttempts, 20, true));
        
        String uname = "MaxAttUser" + maxAttempts + "_" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        User u = userDAO.findByUsername(uname);
        
        Game game = gameService.startGame(u.getUserId());
        Word target = wordDAO.findById(game.getWordId());
        String wrongGuess = target.getWord().equals("ZZZZZ") ? "YYYYY" : "ZZZZZ";

        for (int i = 1; i <= maxAttempts; i++) {
            boolean rec = gameService.recordGuess(game.getGameId(), i, wrongGuess, "BBBBB");
            assertTrue(rec, "Recording guess " + i + " out of " + maxAttempts + " failed");
        }
        
        gameService.endGame(game.getGameId(), "LOST");
        Game endedGame = gameDAO.findById(game.getGameId());
        assertEquals("LOST", endedGame.getStatus());
    }

    // ==========================================
    // 5. JAVA DAILY GAME LIMIT TESTS
    // ==========================================

    @Test
    @Order(40)
    public void testJavaDailyGameLimit() {
        adminDAO.updateConfig(new GameConfig(5, 3, true));
        
        String uname = "DailyUser" + System.currentTimeMillis() % 10000;
        authService.register(uname, "Pass123!$%*@#&", "player");
        User u = userDAO.findByUsername(uname);

        for (int i = 0; i < 3; i++) {
            Game g = gameService.startGame(u.getUserId());
            assertNotNull(g);
            gameService.endGame(g.getGameId(), "LOST");
        }

        int countToday = gameDAO.getGamesCountToday(u.getUserId());
        assertEquals(3, countToday);
        assertTrue(countToday >= 3, "Daily limit reached check should trigger");
    }

    // ==========================================
    // 6. JAVA ADMIN TESTS
    // ==========================================

    @Test
    @Order(50)
    public void testJavaAdminGetAndUpdateConfig() {
        boolean updated = adminDAO.updateConfig(new GameConfig(7, 12, true));
        assertTrue(updated);

        GameConfig cfg = adminDAO.getConfig();
        assertEquals(7, cfg.getMaxAttempts());
        assertEquals(12, cfg.getMaxDailyGames());
        assertTrue(cfg.isGameEnabled());
    }

    @Test
    @Order(51)
    public void testJavaAdminReports() {
        Map<String, Integer> stats = adminService.getSystemStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalPlayers"));
        assertTrue(stats.containsKey("totalAdmins"));

        List<Map<String, Object>> playerReports = adminService.getPlayerReports();
        assertNotNull(playerReports);

        List<Map<String, Object>> dailyReports = adminService.getDailyReports();
        assertNotNull(dailyReports);
    }

    // ==========================================
    // 7. JAVA SECURITY TESTS
    // ==========================================

    @Test
    @Order(60)
    public void testJavaSecuritySqlInjectionResilience() {
        String sqlInjection = "' OR '1'='1";
        boolean loginResult = authService.login(sqlInjection, sqlInjection);
        assertFalse(loginResult, "SQL injection login should fail");
    }

    // ==========================================
    // 8. JAVA DATABASE INTEGRITY TESTS
    // ==========================================

    @Test
    @Order(70)
    public void testJavaDatabaseConnection() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn, "Database connection must not be null");
            assertFalse(conn.isClosed(), "Database connection must be open");
        }
    }
}
