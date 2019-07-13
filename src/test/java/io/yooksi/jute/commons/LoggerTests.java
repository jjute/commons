package io.yooksi.jute.commons;

import io.yooksi.jute.commons.logger.CommonLogger;
import io.yooksi.jute.commons.logger.LibraryLogger;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.*;

import java.io.IOException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("WeakerAccess")
public class LoggerTests {

    @Test @Order(1)
    public void testLoggingToLogFileSimple() throws IOException {
        assertTextFileLineCount(testLoggingToLogFile(Level.TRACE, Level.INFO), 1);
    }

    @Test @Order(2)
    public void testLibraryLogging() throws IOException {

        for (Level logLvl : Level.values())
        {
            LibraryLogger.clearLogFile();

            String log = "TLL: Printing %s to logfile with CommonLogger at level %s";
            LibraryLogger.printf(logLvl, log, logLvl, logLvl);

            boolean canLogToFile = logLvl.intLevel() <= LibraryLogger.get().getLevel().intLevel();

            java.io.File logFile = LibraryLogger.getLogFile();
            assertTextFileLineCount(logFile, canLogToFile ? 1 : 0);
        }

    }

    @Test @Order(3)
    @SuppressWarnings("unused")
    public void testLoggingToLogFileMultiLoggers() throws IOException {

        CommonLogger test1 = new CommonLogger("test", Level.ALL, Level.INFO, false, true, false);
        CommonLogger test2 = new CommonLogger("test", Level.ALL, Level.INFO, false, true, false);
        CommonLogger test3 = new CommonLogger("test", Level.ALL, Level.INFO, false, true, false);

        test3.clearLogFile();
        test3.info("Printing INFO to logfile with CommonLogger at level ALL");
        assertTextFileLineCount(test3.getLogFile(), 1);
    }

    @Test @Order(4)
    public void testLoggingToLogFileAllLevels() throws IOException {

        for (Level logLvl : Level.values()) {
            for (Level logFileLvl : Level.values())
            {
                java.io.File logFile = testLoggingToLogFile(logLvl, logFileLvl);
                assertTextFileLineCount(logFile, 1);
            }
        }
    }

    @Test @Order(5)
    public void testReloadLoggingToLogFile() throws IOException {

        for (Level logLvl : Level.values()) {
            for (Level logFileLvl : Level.values())
            {
                java.io.File logFile = testReloadLoggingToLogFile(logLvl, logFileLvl);
                assertTextFileLineCount(logFile,1);
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static java.io.File testLoggingToLogFile(Level level, Level logFileLevel) {

        CommonLogger logger = new CommonLogger("test", level, logFileLevel, true, true, false);
        logger.clearLogFile();

        logger.printf(logFileLevel, "TLTLF: Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);

        return logger.getLogFile();
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static java.io.File testReloadLoggingToLogFile(Level level, Level logFileLevel) {

        CommonLogger logger = new CommonLogger("test", level, logFileLevel, true, true, false);
        logger.stopLoggingToFile();
        logger.clearLogFile();

        logger.printf(logFileLevel, "TRTLF: Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);

        logger.startLoggingToFile();

        logger.printf(logFileLevel, "TRTLF: Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);

        return logger.getLogFile();
    }

    private void assertTextFileLineCount(java.io.File file, int count) throws IOException {

        long lineCount = getTextFileLineCount(file);
        Assertions.assertEquals(count, lineCount);
    }

    private long getTextFileLineCount(java.io.File file) throws IOException {
        return java.nio.file.Files.lines(file.toPath()).count();
    }
}