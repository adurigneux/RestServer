package fr.univ.lille1.interfaces;

import org.apache.commons.net.ftp.FTPClient;

/**
 * This implementations contains all the configuration that are used by the server.
 * it binds or services and offer json provider
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
public interface ClientSession {
    /**
     * Return the username of the current user session that is used
     *
     * @return String
     */
    public String getUsername();

    /**
     * Return the password of the current user session that is used
     *
     * @return String
     */
    public String getPassword();

    /**
     * Return an object of FtpClient that we use for every command that are executed by the user (defined by its username and password)
     *
     * @return FTPClient
     */
    public FTPClient getFtpClient();
}
