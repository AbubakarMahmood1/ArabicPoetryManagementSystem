package com.arabicpoetry.dal.dao;

import com.arabicpoetry.dal.dao.impl.PoetDAOImpl;
import com.arabicpoetry.model.Poet;
import com.arabicpoetry.testing.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PoetDAOImplIntegrationTest {

    private PoetDAOImpl dao;
    private Integer createdId;

    @BeforeEach
    void setUp() throws Exception {
        TestSupport.useTestDatabaseConfig();
        dao = new PoetDAOImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (createdId != null) {
            dao.delete(createdId);
        }
    }

    @Test
    void createFindAndDeletePoet() throws SQLException {
        Poet poet = new Poet();
        String name = "Poet-" + UUID.randomUUID();
        poet.setName(name);
        poet.setBiography("Bio");
        poet.setBirthYear("1900");
        poet.setDeathYear("2000");

        dao.create(poet);
        createdId = poet.getPoetId();
        assertNotNull(createdId);

        Poet byId = dao.findById(createdId);
        assertNotNull(byId);
        assertEquals(name, byId.getName());

        Poet byName = dao.findByName(name);
        assertNotNull(byName);

        dao.delete(createdId);
        createdId = null;
        assertNull(dao.findById(poet.getPoetId()));
    }
}
