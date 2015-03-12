package test.fr.univ.lille1.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.*;

/**
 * FtpServiceTest Tester.
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 * @version 1.0
 * @since <pre>mars 11, 2015</pre>
 */
public class FtpServiceTest {

    private static Client client;

    @BeforeClass
    public static void BeforeClass() throws Exception {
        client = Client.create();
    }

    @AfterClass
    public static void AfterClass() throws Exception {
        client.destroy();
    }

    @Test
    public void notAuthenticatedTest() throws Exception {
        WebResource webResource = client.resource("http://localhost:8080/rest/ftp/dir/");
        ClientResponse response = webResource.accept("text/html").get(ClientResponse.class);
        Assert.assertEquals(401, response.getStatus());
        Assert.assertTrue(response.getHeaders().containsKey("WWW-Authenticate"));
        Assert.assertEquals(response.getHeaders().getFirst("WWW-Authenticate"), "Basic realm=\"localhost\"");
    }

    @Test
    public void HTTPAuthenticationOkTest() throws Exception {
        WebResource webResource = client.resource("http://localhost:8080/rest/ftp/dir/");
        ClientResponse response = webResource.accept("text/html").header("Authorization: Basic", new String(Base64.encode("manu:1234"))).get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void HTTPAuthenticationFailTest() throws Exception {
        WebResource webResource = client.resource("http://localhost:8080/rest/ftp/dir/");
        ClientResponse response = webResource.accept("text/html").header("Authorization: Basic", new String(Base64.encode("manu:1235"))).get(ClientResponse.class);
        Assert.assertEquals(401, response.getStatus());
    }

    @Test
    public void addFileTest() throws Exception {
        File file = new File("pika.jpg");
        System.out.println(file.getName());
        InputStream fileInputStream = new FileInputStream(file);
        String sContentDisposition = "attachment; filename=\"" + file.getName() + "\"";
        WebResource fileResource = client.resource("http://localhost:8080/rest/ftp/file/" + file.getName());
        ClientResponse response = fileResource.type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", sContentDisposition)
                .put(ClientResponse.class, fileInputStream);
        Assert.assertEquals(200, response.getStatus());

    }

    @Test
    public void getFileTest() throws Exception {

        WebResource webResource = client.resource("http://localhost:8080/rest/ftp/file/lena_original.jpg");
        InputStream response = webResource.accept(MediaType.APPLICATION_OCTET_STREAM).header("Authorization: Basic", new String(Base64.encode("manu:1234"))).get(InputStream.class);

        BufferedInputStream in = new BufferedInputStream(response);
        File test = new File("lena_original.jpg");

        Assert.assertTrue(!test.exists());

        FileOutputStream fos = new FileOutputStream(test);
        byte[] buffer = new byte[1024];
        int nbBytes = 0;
        while ((nbBytes = in.read(buffer)) != -1) {
            fos.write(buffer, 0, nbBytes);
        }
        fos.close();
        in.close();

        Assert.assertTrue(test.exists());
        test.delete();

    }

    @Test
	public void getDirectoryContentHTMLTest() throws Exception {
		WebResource webResource = client
				.resource("http://localhost:8080/rest/ftp/dir/");
		ClientResponse response = webResource
				.accept("text/html")
				.header("Authorization: Basic",
						new String(Base64.encode("manu:1234")))
				.get(ClientResponse.class);
		Assert.assertEquals(MediaType.TEXT_HTML_TYPE, response.getType());
	}
	
	@Test
	public void getDirectoryContentJSONTest() throws Exception {
		WebResource webResource = client
				.resource("http://localhost:8080/rest/ftp/json/dir/");
		ClientResponse response = webResource
				.accept("application/json")
				.header("Authorization: Basic",
						new String(Base64.encode("manu:1234")))
				.get(ClientResponse.class);
		Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
	}
}
