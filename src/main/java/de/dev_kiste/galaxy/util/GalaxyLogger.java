package de.dev_kiste.galaxy.util;

import java.util.logging.Level;

/**
 * Logging interface to delegate logging functionality
 *
 * @author Benny Lach
 */
public interface GalaxyLogger {
    /**
     * Triggers a new request to log the given content
     * 
     * @param lvl used log level
     * @param message message to log
     */
    void log(Level lvl, String message);
}
