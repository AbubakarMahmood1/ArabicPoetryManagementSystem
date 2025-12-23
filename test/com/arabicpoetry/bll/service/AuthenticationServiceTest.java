package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.dao.UserDAO;
import com.arabicpoetry.model.User;
import com.arabicpoetry.testing.TestSupport;
import com.arabicpoetry.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private AuthenticationService service;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestSupport.resetSingletons();
        service = AuthenticationService.getInstance();
        userDAO = Mockito.mock(UserDAO.class);
        service.setUserDAO(userDAO);
    }

    @Test
    void loginSucceedsWhenPasswordMatchesAndUserActive() throws SQLException {
        User user = buildUser(1, "alice", PasswordUtil.hashPassword("secret"), true);
        when(userDAO.findByUsername("alice")).thenReturn(user);

        User result = service.login("alice", "secret");

        assertNotNull(result);
        assertEquals("alice", service.getCurrentUser().getUsername());
        verify(userDAO).updateLastLogin(1);
    }

    @Test
    void loginFailsWhenPasswordDoesNotMatch() throws SQLException {
        User user = buildUser(2, "bob", PasswordUtil.hashPassword("correct"), true);
        when(userDAO.findByUsername("bob")).thenReturn(user);

        User result = service.login("bob", "wrong");

        assertNull(result);
        assertFalse(service.isLoggedIn());
        verify(userDAO, never()).updateLastLogin(anyInt());
    }

    @Test
    void loginFailsWhenUserInactive() throws SQLException {
        User user = buildUser(3, "charlie", PasswordUtil.hashPassword("secret"), false);
        when(userDAO.findByUsername("charlie")).thenReturn(user);

        User result = service.login("charlie", "secret");

        assertNull(result);
        verify(userDAO, never()).updateLastLogin(anyInt());
    }

    @Test
    void registerUserHashesPasswordAndCreatesUser() throws SQLException {
        // Using spy to capture the created user
        doAnswer(invocation -> {
            User created = invocation.getArgument(0);
            assertEquals("dana", created.getUsername());
            assertTrue(created.isActive());
            assertNotEquals("plain", created.getPasswordHash());
            assertTrue(PasswordUtil.verifyPassword("plain", created.getPasswordHash()));
            return null;
        }).when(userDAO).create(any(User.class));

        service.registerUser("dana", "plain", "Dana D");

        verify(userDAO).create(any(User.class));
    }

    private User buildUser(int id, String username, String passwordHash, boolean active) {
        User u = new User();
        u.setUserId(id);
        u.setUsername(username);
        u.setPasswordHash(passwordHash);
        u.setActive(active);
        return u;
    }
}
