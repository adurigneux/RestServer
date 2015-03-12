package fr.univ.lille1.service;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import fr.univ.lille1.interfaces.Authentication;
import fr.univ.lille1.interfaces.ClientSession;
import fr.univ.lille1.resource.ClientSessionImpl;
import org.apache.commons.net.ftp.FTPClient;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This implementations is used in order to connect to our server by a Basic Http authentificate method
 * It analyse and read tje HttpHeader and try to access to the ftp in order to verify the username and password, then
 * return the session
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
public class AuthenticationBasicImpl implements Authentication {

    private Map<String, ClientSession> clients = new HashMap<String, ClientSession>();
    private FTPClient ftpClient = new FTPClient();

    public ClientSession getClientSession(HttpHeaders requestHttpHeaders) {

        if (requestHttpHeaders == null) // no header
            return new ClientSessionImpl("anonymous", "");
        List<String> authentificationHeaders = requestHttpHeaders
                .getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authentificationHeaders.isEmpty()) // no header
            return new ClientSessionImpl("anonymous", "");
        String encodedString = authentificationHeaders.get(0).split(" ")[1];
        ClientSession clientSession = clients.get(encodedString);
        if (clientSession == null) {
            String[] userInfo = null;
            try {
                userInfo = new String(Base64.decode(encodedString)).split(":");
            } catch (Base64DecodingException e) {
            }
            if (isValidUser(userInfo[0], userInfo[1])) {
                ClientSessionImpl client = new ClientSessionImpl(userInfo[0],
                        userInfo[1]);
                clients.put(encodedString, client);
                return client;
            }
        }
        return clients.get(encodedString);
    }

    public boolean isValidUser(String username, String password) {
        try {
            ftpClient.connect("localhost", 8000);
            if (ftpClient.login(username, password)) {
                ftpClient.disconnect();
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }
}
