package com.arabicpoetry.dal.dao;

import com.arabicpoetry.dal.dao.impl.UserDAOImpl;
import com.arabicpoetry.model.User;
import com.arabicpoetry.testing.TestSupport;
import com.arabicpoetry.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOImplIntegrationTest {

    private UserDAOImpl dao;
    private Integer createdId;

    @BeforeEach
    void setUp() throws Exception {
        TestSupport.useTestDatabaseConfig();
        dao = new UserDAOImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (createdId != null) {
            dao.delete(createdId);
        }
    }

    @Test
    void createFindAndDeleteUser() throws SQLException {
        User user = new User();
        String username = "user-" + UUID.randomUUID();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hashPassword("secret"));
        user.setFullName("Test User");
        user.setActive(true);

        dao.create(user);
        createdId = user.getUserId();
        assertNotNull(createdId);

        User fetched = dao.findByUsername(username);
        assertNotNull(fetched);
        assertEquals(username, fetched.getUsername());
        assertTrue(PasswordUtil.verifyPassword("secret", fetched.getPasswordHash()));

        dao.delete(createdId);
        createdId = null;
        assertNull(dao.findById(user.getUserId()));
    }
}
