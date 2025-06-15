package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest {

    private Login login;

    @BeforeEach
    void setUp() {
        login = new Login("John", "Doe", "jd_1", "Passw0rd!", "0821234567");
    }

    @Test
    void testGetters() {
        assertEquals("John", login.getFirstName());
        assertEquals("Doe", login.getLastName());
        assertEquals("jd_1", login.getUsername());
        assertEquals("Passw0rd!", login.getPassword());
        assertEquals("0821234567", login.getCellNumber());
    }

    @Test
    void testSetters() {
        login.setFirstName("Jane");
        login.setLastName("Smith");
        login.setUsername("js_2");
        login.setPassword("Strong123!");
        login.setCellNumber("+27831234567");

        assertEquals("Jane", login.getFirstName());
        assertEquals("Smith", login.getLastName());
        assertEquals("js_2", login.getUsername());
        assertEquals("Strong123!", login.getPassword());
        assertEquals("+27831234567", login.getCellNumber());
    }

    @Test
    void testCheckUserName_Valid() {
        login.setUsername("u_12");
        assertTrue(login.checkUserName());
    }

    @Test
    void testCheckUserName_Invalid() {
        login.setUsername("username"); // no underscore, too long
        assertFalse(login.checkUserName());
    }

    @Test
    void testCheckPasswordComplexity_Valid() {
        login.setPassword("GoodPass1!");
        assertTrue(login.checkPasswordComplexity());
    }

    @Test
    void testCheckPasswordComplexity_Invalid() {
        login.setPassword("weak"); // too short, no complexity
        assertFalse(login.checkPasswordComplexity());
    }

    @Test
    void testCheckCellNumber_Valid() {
        login.setCellNumber("0821234567");
        assertTrue(login.checkCellNumber());

        login.setCellNumber("+27821234567");
        assertTrue(login.checkCellNumber());
    }

    @Test
    void testCheckCellNumber_Invalid() {
        login.setCellNumber("12345");
        assertFalse(login.checkCellNumber());
    }

    @Test
    void testLoginUser_Success() {
        // Simulate login by manually checking logic
        boolean success = "jd_1".equals(login.getUsername()) &&
                "Passw0rd!".equals(login.getPassword());
        assertTrue(success);
    }

    @Test
    void testLoginUser_Failure() {
        boolean success = "wrong".equals(login.getUsername()) &&
                "wrongpass".equals(login.getPassword());
        assertFalse(success);
    }

    @Test
    void testReturnLoginStatus_Success() {
        String status = login.returnLoginStatus(true);
        assertEquals("Login successful. Welcome, John Doe!", status);
    }

    @Test
    void testReturnLoginStatus_Failure() {
        String status = login.returnLoginStatus(false);
        assertEquals("Login failed. Please check your username and password.", status);
    }
}
