package fr.univ.lille1.service;

import com.sun.jersey.multipart.FormDataParam;
import fr.univ.lille1.interfaces.Authentication;
import fr.univ.lille1.interfaces.ClientSession;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;

/**
 * This implementations the main service of the server, every command are implemented here in order to be executed by the user.
 * Each time a command is executed, the user is connected and disconnected.
 * The path of this service is '/rest/ftp/'
 *
 * @author Durigneux Antoine
 * @author Scouflaire Emmanuel
 */
@Path("/ftp/")
public class FtpService {

    @Context
    HttpHeaders requestHeaders;
    @Context
    UriInfo uri;
    @Context
    private HttpServletRequest servletRequest;
    private Authentication authenticationBasic = new AuthenticationBasicImpl();

	/*
     * Create = PUT Retrieve = GET Update = POST Delete = DELETE
	 */

    /**
     * * This method connect the user before each command
     *
     * @param client the uer session
     * @throws IOException
     */
    private void connect(ClientSession client) throws IOException {

        client.getFtpClient().setAutodetectUTF8(true);
        client.getFtpClient().connect("localhost", 8000);
        client.getFtpClient().login(client.getUsername(), client.getPassword());
    }

    /**
     * this method disconnect the user after each command*
     *
     * @param client the user to disconnnect
     * @throws IOException
     */
    private void disconnect(ClientSession client) throws IOException {
        client.getFtpClient().disconnect();
    }

    /**
     * This method is executed every time the user is not connected to the server
     * It used after the Basic implementation of Authentification
     *
     * @return
     */
    private Response unauthorizedAccess() {
        return Response.status(Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Basic realm=\"localhost\"")
                .build();
    }

    /**
     * execute the command 'retr' on the ftp server
     * produce and Octe stream*
     *
     * @param filePath the path of the file to retrieve
     * @return the response in order to tell the user if there was a problem
     */
    @GET
    @Path("file/{path : .*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("path") String filePath) {
        ClientSession client = authenticationBasic
                .getClientSession(requestHeaders);
        if (client != null) {
            InputStream file = null;
            try {
                connect(client);
                file = client.getFtpClient().retrieveFileStream(
                        filePath);

            } catch (IOException e) {
                return Response.notModified().build();
            } finally {
                try {
                    disconnect(client);
                } catch (IOException ignore) {
                }
            }
            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .build();
        } else {
            return unauthorizedAccess();
        }
    }

    /**
     * execute the command 'delete' on the ftp server
     *
     * @param filePath the path of the file to delete
     * @return the response in order to tell the user if there was a problem
     */
    @DELETE
    @Path("file/{path : .*}")
    public Response removeFile(@PathParam("path") String filePath)
            throws IOException {
        ClientSession client = authenticationBasic
                .getClientSession(requestHeaders);
        if (client != null) {
            boolean allOk = false;
            try {
                connect(client);
                allOk = client.getFtpClient().deleteFile(filePath);
            } catch (IOException e) {
                return Response.notModified().build();
            } finally {
                try {
                    disconnect(client);
                } catch (IOException ignore) {
                }
            }
            if (allOk) {
                return Response.ok().build();
            }
            return Response.notModified().build();
        } else {
            return unauthorizedAccess();
        }
    }

    /**
     * execute the command 'stor' on the ftp server
     *
     * @param filePath the path of the file to send, where to save
     * @return the response in order to tell the user if there was a problem
     */
    // exec the cmd stor file
    @PUT
    @Path("file/{path : .*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addFile(@PathParam("path") String filePath,
    						@FormDataParam("fileUploaded") InputStream uploadedInputStream)
            throws IOException {
        ClientSession client = authenticationBasic
                .getClientSession(requestHeaders);
        if (client != null) {
            boolean success = false;
            try {
                connect(client);
                int endDirectoryPath = filePath.lastIndexOf("/");
                if (endDirectoryPath > 0) {
                    String remoteDirectoyPath = filePath.substring(0,
                            endDirectoryPath);
                    client.getFtpClient().makeDirectory(remoteDirectoyPath);
                }
                client.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
               
                String remoteFile = filePath;
                
                success = client.getFtpClient().storeFile(remoteFile,
                		uploadedInputStream);
            } catch (IOException e) {
                return Response.notModified().build();
            } finally {
                try {
                    disconnect(client);
                } catch (IOException ignore) {
                }
            }

            if (success)
                return Response.ok().build();
            else
                return Response.status(Response.Status.NOT_MODIFIED)
                        .entity("File not stored").build();
        } else {
            return unauthorizedAccess();
        }
    }

    /**
     * execute the command 'ls' on the ftp server
     * produce an html page
     *
     * @param pathname the path of the file display
     * @return the response in order to tell the user if there was a problem
     */
    @GET
    @Path("dir/{path : .*}")
    @Produces("text/html")
    public Response getDir(@PathParam("path") String pathname) {
        ClientSession client = authenticationBasic
                .getClientSession(requestHeaders);
        if (client != null) {
            String html = "";
            try {

                connect(client);
                html = "<!DOCTYPE html><html><head>";

                html += "<style>ul{list-style-type: none;} </style>";
                html += "</head><body>";

                boolean error = false;

                client.getFtpClient().changeWorkingDirectory(pathname);

                if (client.getFtpClient().getReplyCode() != 200) {
                    error = true;
                    html += "<h3 style=\"color:red\">Dossier inexistant</h3>";
                }

                FTPFile[] dirs = null;
                FTPFile[] files = null;
                dirs = client.getFtpClient().listDirectories();
                files = client.getFtpClient().listFiles();

                html += "<h2>Current dir : "
                        + client.getFtpClient().printWorkingDirectory()
                        + "</h2><br>";

                // add files

                String wd = client.getFtpClient().printWorkingDirectory();
                wd = wd.replaceFirst("/datafiles", "");
                wd = wd.replaceFirst("/", "");
                html += "<a href=\""
                        + uri.getBaseUri()
                        + "ftp/upload/"
                        + wd
                        + "\"><img alt=\"Ajouter fichier\"src=\"http://icons.iconarchive.com/icons/custom-icon-design/pretty-office-9/24/new-file-icon.png\" width=\"25\" height=\"25\"></a>";

                html += "<ul>";
                // parent directories
                if (!pathname.isEmpty() && pathname.length() > 1) {
                    html += "<img src=\"http://icons.iconseeker.com/png/32/fresh-addon/arrow-back-1.png\" width=\"25\" height=\"25\">";
                    client.getFtpClient().cdup();
                    wd = client.getFtpClient().printWorkingDirectory();
                    wd = wd.replaceFirst("/datafiles", "");
                    wd = wd.replaceFirst("/", "");
                    html += "<a href=\"" + uri.getBaseUri() + "ftp/dir/" + wd
                            + "\">Dossier parent</a>";
                }
                // directories
                html += "<ul>";
                for (FTPFile file : dirs) {
                    html += "<li>";
                    html += "<img src=\"http://icons.iconarchive.com/icons/oxygen-icons.org/oxygen/256/Mimetypes-inode-directory-icon.png\" width=\"25\" height=\"25\">";
                    html += "<a href=\"" + uri.getAbsolutePath() + file.getName()
                            + "/" + "\">" + file.getName() + "</a>";
                    html += "</li>";

                }
                html += "</ul>";

                html += "<ul>";

                for (FTPFile file : files) {
                    html += "<li>";
                    if (file.isFile()) {
                        html += "<img src=\"http://icons.iconarchive.com/icons/hopstarter/sleek-xp-basic/24/Document-Blank-icon.png\">";
                        String path = uri.getAbsolutePath().toString();
                        path = path.replaceFirst("dir", "file");
                        html += "<a href=\"" + path + file.getName() + "\">"
                                + file.getName() + "</a>";
                        html += "</li>";
                    }

                }
                html += "</ul>";

                html += "</body></html>";
            } catch (IOException e) {
                return Response.notModified().build();
            } finally {
                try {
                    disconnect(client);
                } catch (IOException ignore) {
                }
            }

            return Response.ok(html, "text/html").build();
        }
        return unauthorizedAccess();
    }

    /**
     * execute the command 'ls' on the ftp server
     * produce an json result page
     *
     * @param pathname the path of the file display
     * @return the response in order to tell the user if there was a problem
     */
    @GET
    @Path("json/dir/{path : .*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDirJson(@PathParam("path") String pathname) {
        ClientSession client = authenticationBasic
                .getClientSession(requestHeaders);
        if (client != null) {
            FTPFile[] files = null;
            try {
                connect(client);
                client.getFtpClient().changeWorkingDirectory(pathname);
                files = client.getFtpClient().listFiles();
            } catch (IOException e) {
                return Response.notModified().build();
            } finally {
                try {
                    disconnect(client);
                } catch (IOException ignore) {
                }
            }

            return Response.ok(files, MediaType.APPLICATION_JSON).build();
        }
        return unauthorizedAccess();
    }

    /**
     * produce an html page and a form
     *
     * @param filePath the path of the file to upload
     * @return the response in order to tell the user if there was a problem
     */
    @GET
    @Path("upload/{path : .*}")
    @Produces("text/html")
    public Response getUploadForm(@PathParam("path") String filePath) {
        String html = "<!DOCTYPE html><html><head>" + "<script src=\"https://code.jquery.com/jquery-2.1.3.min.js\"></script>"
                +
                "</head><body>";
        html += "<form id=\"formid\" method=\"post\" action=\"\" enctype=\"multipart/form-data\">"
                + "Select image to upload:<br><br>"
                + "<input type=\"file\" id=\"fileUploaded\"name=\"fileUploaded\"><br><br>"
                + "<input type=\"text\" id=\"fileName\"name=\"fileName\"><br><br>"
                + "<input type=\"submit\" value=\"Upload file\" name=\"submit\">"
                + "</form>";


        String script = "        <script>\n" +
                "        window.onload = function() {\n" +
                "       \n" +
                "            $('#fileUploaded').change(function() {\n" +
                "                var filename = $('#fileUploaded').val(); var fullPath = $('#fileUploaded').val();\n" +
                "                " + "var startIndex = (fullPath.indexOf('\\\\') >= 0 ? fullPath.lastIndexOf('\\\\') : fullPath.lastIndexOf('/'));\n" +
                "\tvar filename = fullPath.substring(startIndex);\n" +
                "\tif (filename.indexOf('\\\\') === 0 || filename.indexOf('/') === 0) {\n" +
                "\t\tfilename = filename.substring(1);\n" +
                "\t}\n" +
                "\t$('#fileName').val(filename);" +
                "console.log($('#formid'));" +
                "$('#formid')[0].action = window.location + filename;" +
                "\n" +

                "            });\n" +
                "        }\n" +
                "        </script>";

        html += "</body>" +
                script +
                "</html>";

        return Response.ok(html, "text/html").build();
    }


    /**
     * execute the command 'stor' on the ftp server
     *
     * @param path                the path of the file to store
     * @param uploadedInputStream the stream that contain the file
     * @return the response in order to tell the user if there was a problem
     * @throws ServletException
     */
    @POST
    @Path("upload/{path : .*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFileFromForm(@PathParam("path") String path,
                                       @FormDataParam("fileUploaded") InputStream uploadedInputStream)
            throws ServletException {

        ClientSession client = authenticationBasic
                .getClientSession(requestHeaders);
        if (client != null) {
            boolean success = false;
            try {
                connect(client);
                client.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);

                int indexCut = path.lastIndexOf("/");
                if (indexCut > 0) {
                    String remoteDirectoyPath = path.substring(0, indexCut);
                    client.getFtpClient().makeDirectory(remoteDirectoyPath);
                }

                success = client.getFtpClient().storeFile(path,
                        uploadedInputStream);
            } catch (IOException e) {
                return Response.notModified().build();
            } finally {
                try {
                    disconnect(client);
                } catch (IOException ignore) {
                }
            }
            if (success)
                return Response.ok().build();
            else
                return Response.status(Response.Status.NOT_MODIFIED)
                        .entity("File not stored").build();
        } else {
            return unauthorizedAccess();
        }
    }

}
