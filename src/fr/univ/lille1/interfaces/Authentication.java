package fr.univ.lille1.interfaces;

import javax.ws.rs.core.HttpHeaders;

/**
 * This interface authentification allow us to get session object for the client on our server
 * it read the HttpHeader in order to do that
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
public interface Authentication {

    /**
     * This method read the httpHeader in order to read the information of the current user and it
     * generate an object that we use during the connexion of each command
     *
     * @param requestHttpHeaders
     * @return ClientSession An object of the client, that identify him for each connexion
     */
    public ClientSession getClientSession(HttpHeaders requestHttpHeaders);

    /**
     * This method connect to the ftpserver in order to verify if the user is real and have access to the server
     *
     * @param username
     * @param password
     * @return boolean true if could connect to the ftpserver
     */
    public boolean isValidUser(String username, String password);
}
