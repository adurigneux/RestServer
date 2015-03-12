Passerelles Rest en java
===================
Auteurs
: Antoine Durigneux
: Emmanuel Scouflaire

11/03/2015



Introduction
-------------
Ce logiciel permet de créer une passerelle et client Rest qui communique avec un serveur FTP.
cf. TP Précédent.


Utilisation
----------

"java -jar tp2-durigneux-scouflaire.jar"


Liste des url
-------------

Le projet fonctionne via les url suivantes : 
```
http://{IP_serveur}:{port}/rest/ftp/
```
@_GET Récupère un dossier sous forme de page html contenant d'autres options
```
http://localhost:8080/rest/ftp/dir/{path}
```
@_GET Récupère un dossier sous forme de contenu json
```
http://localhost:8080/rest/ftp/json/dir/{path}
```
@_GET Récupère un fichier sour forme d'un octet-stream
@_PUT Permet de mettre un fichier sur le serveur ftp
@_DELETE permet de supprimer un fichier/dossier sur le serveur ftp
```
http://localhost:8080/rest/ftp/file/{path}
```

@_GET Récupère un formulaire html permettant l'ajout d'un fichier dans le path définit
@_POST Envoie le fichier uploader dans le formulaire sur le serveur ftp
```
http://localhost:8080/rest/ftp/upload/{path}
```

Architecture
-------------
L'architecture de ce projet se divise sous plusieurs compposantes : 
-configuration
-main
-ressources
-services

###configuration
AppConfig ainsi que JaxRsApiApplication servent à la configuration des servlet, des apis ainsi que 
l'associations entre les services et les urls.

###ressources
Les ressources sont representes des entités existantes sur le serveur. ici, un client qui se connecte.

###services
Il y a 2 types de services sur le serveur : FtpService et Authentication
Le premier sert uniquement à tous les appeles et accès url fournissant les commandes executées sur le serveurs FTP distant
alors que le second n'est une implémentation de connexion differentes.

###Polymorphisme
La ressource du client est implementee à l'aide d'un interface ainsi que les services d'authetifications.
Cela permet essentiellement de définir different moyen d'implémenter un algorithme donnant un résultat identique.
Il est possible d'utiliser plusieurs moyen de connexion, en dur ou basique, par exemple.
De ce fait il est simple de pouvoir introduire une connexion via OAuth ou Digest.


###Erreurs
Les erreurs sont à la fois gérées par le serveur et pour des réponses HTTP.

Le serveur renvoie et catch les erreurs internes, ce qui concerne ses propres problemes. 
Url inexistantes, problème de parsing des headers, de leurs lectures etc
```
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
```

Par contre, pour l'utilisateur qui aura besoin d'un retour visuel, le serveur utilise des codes d'erreurs HTTP.

Par exemple, lorsqu'un utilisateur n'est pas connecté, une demmande de connexion est envoyée : 

```
 Response.status(Status.UNAUTHORIZED)
                 .header("WWW-Authenticate", "Basic realm=\"localhost\"")
                 .build();
 Response.notModified().build();
```



Codes samples
-------------

###1.Application

Le serveur est developpé sans fichier de configuration xml pouvant etre lourd a comprendre.
Cette classe permet de tres facilement ajouter des services à l'application en ajoutant simplement le nom de celle ci dans le set
contenant les classes que le serveur doit faire correspondre
```
public class JaxRsApiApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        // Add all services
        // For non-JAX-RS aware web container environments
        // Application sub-class needs to be created which returns sets of
        // JAX-RS root resources and providers
        classes.add(FtpService.class);
        return classes;
    }
}
```

###2.Connexion
La possibilitées d'intégrer different moyen de connexions est assez simple, en effet, il suffit d'implémenter l'interface 
d'authentification ainsi que de fournir le comportement de ces methodes qui correspond à la manière dont la connexion doit être faites.
Il peut etre possibl de rajouter la connexion par OAuth ou Digest, ...

```
public interface Authentication {

    public ClientSession getClientSession(HttpHeaders requestHttpHeaders);

    public boolean isValidUser(String username, String password);
}
```

###3.Instanciation

La classe Main du serveur est très simple. En effet, le serveur se démarre assez simplement.
Il faut définir le numero de port sur lequel il sera accessible, rajouter la configurations et les controllers (voir point 1)
et d'executer la methode 'start'

```
        Server server = new Server(8080);
      
        // Register and map the dispatcher servlet
        final ServletHolder servletHolder = new ServletHolder(new CXFServlet());
        final ServletContextHandler context = new ServletContextHandler();
        
        ...
        
        context.setContextPath("/");
        context.addServlet(servletHolder, "/rest/*");
        context.setInitParameter("contextClass",
                AnnotationConfigWebApplicationContext.class.getName());
        context.setInitParameter("contextConfigLocation",
                AppConfig.class.getName());

        server.setHandler(context);

        server.start();
        server.join();
```


###4.Connexion/deconnexion
Afin de fournir à l'utilisateur un moyen de naviger et utiliser le serveur Rest sans problemes ou contraintes, 
chaque commandes ou actions executees sur le serveur commence par une connexion et termine par une deconnexion.
En effet, grâce a cela, l'utilisateur ne risque pas d'etre deconnecter a cause d'une trop longue utilisation,
En cas de coupure par exemple.

```
    private void connect(ClientSession client) throws IOException {

        client.getFtpClient().setAutodetectUTF8(true);
        client.getFtpClient().connect("localhost", 8000);
        client.getFtpClient().login(client.getUsername(), client.getPassword());
    }

    private void disconnect(ClientSession client) throws IOException {
        client.getFtpClient().disconnect();
    }
```


###5.Renommage du fichier a l'upload
Lors de l'ajout d'un fichier sur le serveur ftp, nous affichons un formulaire assez simple.
Dans ce formulaire contenant un champs pour le fichier nous avons également ajouté un champs qui
reprends le nom du fichier pour que l'utilisateur puisse facilement le renommé avant de l'envoyer sur le serveur.
Un petit moyen pratique et simple de renommage avant l'envoie du fichier.


Pour cela nous avons ajouté un petit code javascript dans la page html.

```

window.onload = function() {

    $('#fileUploaded').change(function() {
        var filename = $('#fileUploaded').val(); var fullPath = $('#fileUploaded').val();
        var startIndex = (fullPath.indexOf('\\') >= 0 ? fullPath.lastIndexOf('\\') : fullPath.lastIndexOf('/'));
        var filename = fullPath.substring(startIndex);
        
        if (filename.indexOf('\\') === 0 || filename.indexOf('/') === 0) {
            filename = filename.substring(1);
        }
        
        $('#fileName').val(filename);
    });
}
        
```

