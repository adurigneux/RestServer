package fr.univ.lille1.service;

import fr.univ.lille1.interfaces.Authentication;
import fr.univ.lille1.interfaces.ClientSession;
import fr.univ.lille1.resource.ClientSessionImpl;

import javax.ws.rs.core.HttpHeaders;

/**
 * This implementations is used in order to connect to our server by a setted method, only for test
 * It could be used also for different type of connexion
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
public class AuthenticationOriginalImpl implements Authentication {


    public ClientSession getClientSession(HttpHeaders requestHttpHeaders) {


        ClientSessionImpl client = new ClientSessionImpl("antoine", "1234");
        return client;

    }


    public boolean isValidUser(String username, String password) {
        return true;
    }
}
