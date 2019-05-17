package commons;

import io.yooksi.commons.logger.CommonLogger;
import org.apache.logging.log4j.Level;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoggerTest {

    @Test
    public void loggingToLogFileMultiLoggers() throws IOException {

        CommonLogger test1 = new CommonLogger("test", Level.ALL, Level.INFO);
        CommonLogger test2 = new CommonLogger("test", Level.ALL, Level.INFO);
        CommonLogger test3 = new CommonLogger("test", Level.ALL, Level.INFO);
        test3.info("Printing INFO to logfile with CommonLogger at level ALL");
        TestUtils.assertTextFileLineCount(test3.getLogFile(), 1);
    }

//    @Test
    public void test1LoggingToLogFileAllLevels() throws IOException {

        for (Level logLvl : Level.values()) {
            for (Level logFileLvl : Level.values())
            {
                boolean isLevelOff = logFileLvl.equals(Level.OFF) || logLvl.equals(Level.OFF);
                boolean canLogToFile = logLvl.intLevel() >= logFileLvl.intLevel();
                TestUtils.assertTextFileLineCount(testLoggingToLogFile(logLvl, logFileLvl), !isLevelOff && canLogToFile ? 1 : 0);
            }
        }
    }

//    @Test
    public void test2ReloadLoggingToLogFile() throws IOException {

        for (Level logLvl : Level.values()) {
            for (Level logFileLvl : Level.values())
            {
                boolean isLevelOff = logFileLvl.equals(Level.OFF) || logLvl.equals(Level.OFF);
                boolean canLogToFile = logLvl.intLevel() >= logFileLvl.intLevel();
                TestUtils.assertTextFileLineCount(testReloadLoggingToLogFile(logLvl, logFileLvl), !isLevelOff && canLogToFile ? 1 : 0);
            }
        }
    }

//    @Test
    public void test3ReloadLoggingToLogFileFail() throws IOException {

        //TestUtils.assertTextFileLineCount(testReloadLoggingToLogFile(
    }

    private static java.io.File testLoggingToLogFile(Level level, Level logFileLevel) {

        CommonLogger logger = new CommonLogger("test", level, logFileLevel);
        if (level.equals(Level.OFF) || logFileLevel.equals(Level.OFF)) {
            logger.printf(Level.INFO, "Printing INFO to logfile with CommonLogger at level %s", level);
        }
        else {
            logger.printf(logFileLevel, "Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);
        }
        return logger.getLogFile();
    }

    private static java.io.File testReloadLoggingToLogFile(Level level, Level logFileLevel) {

        CommonLogger logger = new CommonLogger("test", level, logFileLevel);
        logger.stopLoggingToFile();

        if (level.equals(Level.OFF) || logFileLevel.equals(Level.OFF))
        {
            logger.printf(Level.INFO, "Printing INFO to logfile with CommonLogger at level %s", level);
            logger.startLoggingToFile(logFileLevel);
            logger.printf(Level.INFO, "Printing INFO to logfile with CommonLogger at level %s", level);
        }
        else {
            logger.printf(logFileLevel, "Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);
            logger.startLoggingToFile(logFileLevel);
            logger.printf(logFileLevel, "Printing %s to logfile with CommonLogger at level %s", logFileLevel.name(), level);
        }
        return logger.getLogFile();
    }
}
