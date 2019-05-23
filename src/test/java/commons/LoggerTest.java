package commons;

import io.yooksi.commons.logger.CommonLogger;
import io.yooksi.commons.logger.LibraryLogger;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import java.io.IOException;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("WeakerAccess")
public class LoggerTest {

    @Test
    public void testLibraryLogging() throws IOException {

        for (Level logLvl : Level.values())
        {
            LibraryLogger.clearLogFile();

            String log = "TLL: Printing %s to logfile with CommonLogger at level %s";
            LibraryLogger.printf(logLvl, log, logLvl, logLvl);

            boolean canLogToFile = logLvl.intLevel() <= LibraryLogger.get().getLevel().intLevel();

            java.io.File logFile = LibraryLogger.getLogFile();
            TestUtils.assertTextFileLineCount(logFile, canLogToFile ? 1 : 0);
        }

    }

    @Test
    public void testLoggingToLogFileSimple() throws IOException {
        TestUtils.assertTextFileLineCount(testLoggingToLogFile(Level.TRACE, Level.INFO), 1);
    }

    @Test
    @SuppressWarnings("unused")
    public void testLoggingToLogFileMultiLoggers() throws IOException {

        CommonLogger test1 = new CommonLogger("test", Level.ALL, Level.INFO, false, true, false);
        CommonLogger test2 = new CommonLogger("test", Level.ALL, Level.INFO, false, true, false);
        CommonLogger test3 = new CommonLogger("test", Level.ALL, Level.INFO, false, true, false);

        test3.clearLogFile();
        test3.info("Printing INFO to logfile with CommonLogger at level ALL");
        TestUtils.assertTextFileLineCount(test3.getLogFile(), 1);
    }

    @Test
    public void testLoggingToLogFileAllLevels() throws IOException {

        for (Level logLvl : Level.values()) {
            for (Level logFileLvl : Level.values())
            {
                boolean isLevelOff = logFileLvl.equals(Level.OFF) || logLvl.equals(Level.OFF);
                boolean canLogToFile = logLvl.intLevel() >= logFileLvl.intLevel();

                java.io.File logFile = testLoggingToLogFile(logLvl, logFileLvl);
                TestUtils.assertTextFileLineCount(logFile, !isLevelOff && canLogToFile ? 1 : 0);
            }
        }
    }

    @Test
    public void testReloadLoggingToLogFile() throws IOException {

        for (Level logLvl : Level.values()) {
            for (Level logFileLvl : Level.values())
            {
                boolean isLevelOff = logFileLvl.equals(Level.OFF) || logLvl.equals(Level.OFF);
                boolean canLogToFile = logLvl.intLevel() >= logFileLvl.intLevel();

                java.io.File logFile = testReloadLoggingToLogFile(logLvl, logFileLvl);
                TestUtils.assertTextFileLineCount(logFile, !isLevelOff && canLogToFile ? 1 : 0);
            }
        }
    }

    private static java.io.File testLoggingToLogFile(Level level, Level logFileLevel) {

        CommonLogger logger = new CommonLogger("test", level, logFileLevel, false, true, false);
        logger.clearLogFile();

        if (level.equals(Level.OFF) || logFileLevel.equals(Level.OFF)) {
            logger.printf(Level.INFO, "TLTLF: Printing INFO to logfile with CommonLogger at level %s", level);
        }
        else {
            logger.printf(logFileLevel, "TLTLF: Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);
        }
        return logger.getLogFile();
    }

    private static java.io.File testReloadLoggingToLogFile(Level level, Level logFileLevel) {

        CommonLogger logger = new CommonLogger("test", level, logFileLevel, false, true, false);
        logger.stopLoggingToFile();
        logger.clearLogFile();

        if (level.equals(Level.OFF) || logFileLevel.equals(Level.OFF))
        {
            logger.printf(Level.INFO, "TRTLF: Printing INFO to logfile with CommonLogger at level %s", level);
            logger.setLogFileLevel(logFileLevel);
            logger.printf(Level.INFO, "TRTLF: Printing INFO to logfile with CommonLogger at level %s", level);
        }
        else {
            logger.printf(logFileLevel, "TRTLF: Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);
            logger.setLogFileLevel(logFileLevel);
            logger.printf(logFileLevel, "TRTLF: Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);
        }
        return logger.getLogFile();
    }
}
