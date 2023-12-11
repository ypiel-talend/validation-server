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

# Available endpoints

# OAuth2.0
## Retrieve a token using client credential flow
To retrieve a token the query has to be as `POST` with header `Content-type: x-www-form-urlencoded` and those form key/values:
- client_id = 1234567890
- client_secret = secret_1234567890_
- grant_type = client_credentials
- scope = scA
```shell
$ curl -X POST http://127.0.0.1:8080/oauth2/client-credentials/token \
     -H 'Content-Type: application/x-www-form-urlencoded' \
     -d 'client_id=1234567890' \
     -d 'client_secret=secret_1234567890_' \
     -d 'grant_type=client_credentials' \
     -d 'scope=scA'

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
     -d 'scope=scA'
     
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
