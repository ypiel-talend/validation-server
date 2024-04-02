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
$ java -jar target/validation-server-0.0.2-SNAPSHOT.jar --server.port=9098
```
The `--server.port` option let you define the port you want to listen to, `8080` is the default.

Once the server is running, you can access its API swagger documentation: http://127.0.0.1:9098/swagger-ui/index.html
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

2023-12-11 14:27:30.779  INFO 1 --- [           main] o.t.c.t.v.ValidationServerApplication    : Starting ValidationServerApplication v0.0.2-SNAPSHOT using Java 11.0.16 on ae2f258eaccb with PID 1 (/validation-server-0.0.2-SNAPSHOT.jar started by root in /)
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

# No authentication endpoints
## Ping
The `/ping` endpoint always responds with a `pong.` payload. It is just to check if the server is alive.

This endpoint support `Accept` header. If `Accept: text/plain` then it responds with a plain text response payload:
```shell
$ curl -H "Accept: text/plain" http://172.26.0.4:8080/ping
pong.
```
This endpoint support `Accept` header. If `Accept: application/json` then it responds with a simple json document:
```shell
$ curl -H "Accept: application/json" http://172.26.0.4:8080/ping
{"message":"pong."}
```
It only supports `GET` verb. With any other verb, an error message generated by `Sprint` will be returned:
```shell
$ curl -X POST -H "Accept: application/json" http://127.0.0.1:8080/ping
{"timestamp":"2024-03-06T20:51:39.177+00:00","status":405,"error":"Method Not Allowed","path":"/ping"}
```

## Load a local file
The `/loadfile` endpoint load the file designed by the `file` query param and return its content as its payload:
```shell
$ echo "Hello" > /tmp/hello.txt
$ echo "the" >> /tmp/hello.txt
$ echo "world!" >> /tmp/hello.txt
$ curl http://127.0.0.1:8080/loadfile?file=/tmp/hello.txt
Hello
the
world!
```

## Post a payload
The `/post` endpoint accepts `POST` verb and a body. It will return exactly the body it received as plain text or in
a json document, depending the `Accept` header:
```shell
$ curl -X POST http://127.0.0.1:8080/post --data "Hello world !" -H "Accept: text/plain"  -H "Content-Type: text/plain"
Hello world !
```
The `/echo` endpoint do quite the same but returns the received payload in a json object: 
```shell
$ curl -X POST http://127.0.0.1:8080/post --data "Hello world !" -H "Accept: text/plain"  -H "Content-Type: text/plain"
{"post_body":"Hello world !"}
```

## Pagination
There are two endpoints that deserve paginated elements.

The `/paginate` one return a a JSON array that contains some json objects `{"id": i, "label": "element_" + i}`. It takes some parameters:
- `total` : The total number of generated elements.
- `offset` : From which index of element do you want to retrieve them ?
- `limit` : How many elements do you want to retrieve ?

```shell
$ curl 'http://127.0.0.1:8080/paginate?total=100&offset=0&limit=5' | jq .
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   146    0   146    0     0  48666      0 --:--:-- --:--:-- --:--:-- 48666
[
  {
    "id": 1,
    "label": "element_1"
  },
  {
    "id": 2,
    "label": "element_2"
  },
  {
    "id": 3,
    "label": "element_3"
  },
  {
    "id": 4,
    "label": "element_4"
  },
  {
    "id": 5,
    "label": "element_5"
  }
]

$ curl 'http://127.0.0.1:8080/paginate?total=100&offset=5&limit=5' | jq .
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   148    0   148    0     0  37000      0 --:--:-- --:--:-- --:--:-- 49333
[
  {
    "id": 6,
    "label": "element_6"
  },
  {
    "id": 7,
    "label": "element_7"
  },
  {
    "id": 8,
    "label": "element_8"
  },
  {
    "id": 9,
    "label": "element_9"
  },
  {
    "id": 10,
    "label": "element_10"
  }
]
```

The second enpoint is `paginateNestedArray`. It takes the same parameters and returns the same elements, but, the element array is a nested array in a root object:
```shell
$ curl 'http://127.0.0.1:8080/paginateNestedArray?total=100&offset=0&limit=5' | jq .
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   201    0   201    0     0  15461      0 --:--:-- --:--:-- --:--:-- 15461
{
  "offset": 0,
  "limit": 5,
  "total": 100,
  "size": 5,
  "elements": [
    {
      "id": 1,
      "label": "element_1"
    },
    {
      "id": 2,
      "label": "element_2"
    },
    {
      "id": 3,
      "label": "element_3"
    },
    {
      "id": 4,
      "label": "element_4"
    },
    {
      "id": 5,
      "label": "element_5"
    }
  ]
}

$ curl 'http://127.0.0.1:8080/paginateNestedArray?total=100&offset=5&limit=5' | jq .
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   203    0   203    0     0  29000      0 --:--:-- --:--:-- --:--:-- 29000
{
  "offset": 5,
  "limit": 5,
  "total": 100,
  "size": 5,
  "elements": [
    {
      "id": 6,
      "label": "element_6"
    },
    {
      "id": 7,
      "label": "element_7"
    },
    {
      "id": 8,
      "label": "element_8"
    },
    {
      "id": 9,
      "label": "element_9"
    },
    {
      "id": 10,
      "label": "element_10"
    }
  ]
}
```

## Test backoff retry
There are two endpoints to test HTTP client retry with backoff.

### On 503 error
The `/retry503` endpoint responds three out of four times a HTTP `503` error with this payload:
```json
{
  "error": "You have still to retry '%s' times."
}
```
The fourth call will return a `200` success HTTP with this one:
```json
{
  "success": "true"
}
```
If you prefer to return a success response before or after the fourth call you can overwrite this with this property `-Dvalidation-server.noauth-controller.retry-503-attempts-success=<nb attempts>`.


### On timeout
In the same way, the `/retryTimeout` will wait for `3000` milliseconds three out of four
time before responding with a `200` successful HTTP with this payload:
```json
{
  "message": "Wait for timeout will be disable in '%s' attempts."
}
```
The fourth call will not wait before to respond.
The `3000ms` default delay can be overwritten with this propery `-Dvalidation-server.noauth-controller.retry-timeout-attempts-delay=<delay>`.
If you prefer to skip the delay before or after the fourth call you can overwrite this with this property `-Dvalidation-server.noauth-controller.retry-timeout-attempts-success=<nb attempts>`.

# OAuth2.0 validation endpoints 
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
