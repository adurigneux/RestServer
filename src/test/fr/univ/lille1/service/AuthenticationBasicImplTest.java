package test.fr.univ.lille1.service;

import fr.univ.lille1.interfaces.Authentication;
import fr.univ.lille1.interfaces.ClientSession;
import fr.univ.lille1.resource.ClientSessionImpl;
import fr.univ.lille1.service.AuthenticationBasicImpl;
import org.junit.Assert;
import org.junit.Test;


/**
 * AuthenticationBasicImplTest Tester.
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 * @version 1.0
 * @since <pre>mars 11, 2015</pre>
 */
public class AuthenticationBasicImplTest {

    private String username = "antoine";
    private String password = "1234";


    @Test
    public void testGetClientSession() throws Exception {

        ClientSession client = new ClientSessionImpl(username, password);
        Authentication authentication = new AuthenticationBasicImpl();
        ClientSession receiveClient = authentication.getClientSession(null);

        Assert.assertNotEquals(client.getUsername(), receiveClient.getUsername());
        Assert.assertNotEquals(client.getPassword(), receiveClient.getPassword());

        Assert.assertEquals("anonymous", receiveClient.getUsername());
        Assert.assertEquals("", receiveClient.getPassword());
    }


    @Test
    public void testIsValidUser() throws Exception {
        Authentication authentication = new AuthenticationBasicImpl();
        Assert.assertTrue(authentication.isValidUser(username, password));
    }

} 
