package test.fr.univ.lille1.resource;

import fr.univ.lille1.interfaces.ClientSession;
import fr.univ.lille1.resource.ClientSessionImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * ClientSessionImpl Tester.
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 * @version 1.0
 * @since <pre>mars 11, 2015</pre>
 */
public class ClientSessionImplTest {

    private String username = "antoine";
    private String password = "1234";

    @Test
    public void testGetUsername() throws Exception {
        ClientSession client = new ClientSessionImpl(username, password);
        Assert.assertEquals("antoine", client.getUsername());
    }


    @Test
    public void testGetPassword() throws Exception {
        ClientSession client = new ClientSessionImpl(username, password);
        Assert.assertEquals("1234", client.getPassword());
    }


    @Test
    public void testGetFtpClient() throws Exception {
        ClientSession client = new ClientSessionImpl(username, password);
        Assert.assertNotNull(client.getFtpClient());
    }


} 
