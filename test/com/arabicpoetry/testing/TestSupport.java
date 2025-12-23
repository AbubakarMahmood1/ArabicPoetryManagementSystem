package com.arabicpoetry.testing;

import com.arabicpoetry.bll.service.AuthenticationService;
import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.bll.service.PoemService;
import com.arabicpoetry.bll.service.PoetService;
import com.arabicpoetry.bll.service.VerseService;
import com.arabicpoetry.dal.DAOFactory;
import com.arabicpoetry.util.DatabaseConnection;
import com.arabicpoetry.bll.service.LinguisticAnalysisService;
import com.arabicpoetry.bll.service.FrequencyService;

/**
 * Utilities for resetting singletons and pointing to the test configuration.
 */
public final class TestSupport {
    private TestSupport() {}

    /**
     * Force DatabaseConfig to use the test properties file.
     */
    public static void useTestDatabaseConfig() {
        DatabaseConnection.useConfigFile("config-test.properties");
        DAOFactory.getInstance().reset();
    }

    /**
     * Reset singleton instances so tests start from a clean state.
     */
    public static void resetSingletons() {
        AuthenticationService.resetInstance();
        BookService.resetInstance();
        PoetService.resetInstance();
        PoemService.resetInstance();
        VerseService.resetInstance();
        LinguisticAnalysisService.resetInstance();
        FrequencyService.resetInstance();
        DAOFactory.getInstance().reset();
        DatabaseConnection.reset();
    }
}
