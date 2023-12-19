# Validation server
The goal of this project is to facilitate validation of HTTP queries.
It provides some end points that can be called with well known behavior and response.

# How to build and execute
You will java 11 and maven to build the project.

First clone the repository:
```shell
$ git clone git@github.com:ypiel-talend/validation-server.git
```
Then build it:
```shell
$ mvn clean install
```
And start the server:
```shell
$ java -jar target/validation-server-0.0.1-SNAPSHOT.jar
```
## Launch the server in a docker container
You have to build the porject first, then create the docker image:
```shell
$ docker build -t validation-server .

[...]
Successfully tagged validation-server:latest
```
Then execute the container:
```shell
$ docker run -p 8080:8080 validation-server


  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.9)

2023-12-11 14:27:30.779  INFO 1 --- [           main] o.t.c.t.v.ValidationServerApplication    : Starting ValidationServerApplication v0.0.1-SNAPSHOT using Java 11.0.16 on ae2f258eaccb with PID 1 (/validation-server-0.0.1-SNAPSHOT.jar started by root in /)
2023-12-11 14:27:30.780  INFO 1 --- [           main] o.t.c.t.v.ValidationServerApplication    : No active profile set, falling back to 1 default profile: "default"
2023-12-11 14:27:31.310  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2023-12-11 14:27:31.316  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2023-12-11 14:27:31.316  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.71]
2023-12-11 14:27:31.359  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2023-12-11 14:27:31.359  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 547 ms
2023-12-11 14:27:31.549  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2023-12-11 14:27:31.556  INFO 1 --- [           main] o.t.c.t.v.ValidationServerApplication    : Started ValidationServerApplication in 1.025 seconds (JVM running for 1.276)
```
The container port `8080` is mapped on the host also on `8080`, so, the server is ready to be queried from your `localhost`.

### Calling the server from another container
To enable another container to access the web server, initiate it within the same Docker network.
First, identify the network's name of the container that will call the server:
```shell
$ docker inspect my_container --format "{{range \$key, \$value := .NetworkSettings.Networks}}{{\$key}} {{end}}"

my_network
```
So, you have to start the validation server container in this network:
```shell
$ docker run --network my_network -p 8080:8080 --name my-validation-server validation-server

[...]
```
Then, you can identify the server `IP` in this network:
```shell
$ docker network inspect my_network | jq -r '.[0].Containers[] | select(.Name == "my-validation-server").IPv4Address'
www.xxx.yyy.zzz/mm
```
Where `www.xxx.yyy.zzz` is hte `IP` address of the validation server in the network and `mm` its subnet mask.
So you can query the web server using this address `http://www.xxx.yyy.zzz:8080/....` 



# Available endpoints

# Basic endpoints
## Ping
The `/ping` endpoint always responds with a `pong.` payload. It is just to check if the server is alive:
```shell
$ curl http://172.26.0.4:8080/ping
pong.
```

# OAuth2.0
## Retrieve a token using client credential flow
To retrieve a token the query has to be as `POST` with header `Content-type: x-www-form-urlencoded` and those form key/values:
- client_id = 1234567890
- client_secret = secret_1234567890_
- grant_type = client_credentials
- scope = scA scB scC
```shell
$ curl -X POST http://127.0.0.1:8080/oauth2/client-credentials/token \
     -H 'Content-Type: application/x-www-form-urlencoded' \
     -d 'client_id=1234567890' \
     -d 'client_secret=secret_1234567890_' \
     -d 'grant_type=client_credentials' \
     -d 'scope=scA scB scC'

{"access_token":"_success_token_","token_type":"Bearer","expires_in":1702306621631}
```
The returned token is always the same. On `expires_in` is computed dynamically.
If one of those value is wrong, then you should receive such answer:
```shell
$ curl -X POST http://127.0.0.1:8080/oauth2/client-credentials/token \
     -H 'Content-Type: application/x-www-form-urlencoded' \
     -d 'client_id=xxxxx' \
     -d 'client_secret=xxxxxx' \
     -d 'grant_type=client_credentials' \
     -d 'scope=scA scB scC'
     
{"message":"OAuth2 security issue.","cause":"Wrong credentials, can't provide token."}
```
## Using the retrieved token
You can ask for `user` entity in oauth section. It will expect the token:
```shell
$ curl -X GET http://127.0.0.1:8080/oauth2/get/user \
     -H 'Authorization: Bearer _success_token_'
     
{"id":1,"name":"Peter","active":true}
```
You can give `id`, `name` and/or `active` HTTP query parameter to overwrite the `User` attribute value:
```shell
$ curl -X GET 'http://127.0.0.1:8080/oauth2/get/user?id=3&name=John&active=false' \
     -H 'Authorization: Bearer _success_token_'
     
{"id":3,"name":"John","active":false}
```
If the given token is wrong, an error is returned:
```shell
$ curl -X GET http://127.0.0.1:8080/oauth2/get/user \
     -H 'Authorization: Bearer _xxxxx_'
     
{"message":"OAuth2 security issue.","cause":"Unrecognized token."}
```

# How to introducing new fonctionnalities
This project is based on spring boot. You can easily add new endpoints that will suit to your own needs.
Then build the docker image and deploy it in your test environment.  
