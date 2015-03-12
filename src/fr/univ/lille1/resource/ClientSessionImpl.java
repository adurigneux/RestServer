package fr.univ.lille1.resource;

import fr.univ.lille1.interfaces.ClientSession;
import org.apache.commons.net.ftp.FTPClient;

/**
 * This is the implementations of ClientSession
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
public class ClientSessionImpl implements ClientSession {

    private String username;
    private String password;
    private FTPClient ftpClient;

    public ClientSessionImpl(String username, String password) {
        this.username = username;
        this.password = password;
        ftpClient = new FTPClient();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }
}
