# MoneyTransfer

RESTful API for money transfers between accounts.

- [x] Written in Java
- [x] Simple and to the point
- [x] Written using Vert.x and without any heavy frameworks
- [x] In-memory datastore
- [x] Standalone program
- [x] Unit and integration tests



# Tech stack

- Java 8
- [Maven](https://maven.apache.org/)
- [Vert.x]([https://vertx.io/](https://vertx.io/))
- [google/gson](https://github.com/google/gson)
- [JUnit 4](https://junit.org/junit4/)
- [AssertJ]([https://joel-costigliola.github.io/assertj/](https://joel-costigliola.github.io/assertj/))
- [REST-assured]([http://rest-assured.io/](http://rest-assured.io/))

## How to build the application

```
mvn clean package 
```

## How to run the application 

The application runs be default on port `8080`. After you built it, you can run it by using
```
java -jar target/MoneyTransfer-1.0-SNAPSHOT-fat.jar 
```
If you cannot built it you can run it using the fat jar provided in the `jars` folder.

If you want to run multiple instances use
```
java -jar target/MoneyTransfer-1.0-SNAPSHOT-fat.jar -instances 4
```
If you want to run using a different configuration (for example on a different port) use
```
java -jar target/MoneyTransfer-1.0-SNAPSHOT-fat.jar -conf src/main/conf/config.json
```

## How to run the tests

Unit tests:
```
mvn clean test
```
All tests:
```
mvn clean verify
```
## How to use the application

### Accounts

#### Create an account

```
POST http://localhost:8080/accounts
{"user":"alex", "currency":"USD", "balance":100}
```
Response:
```
{"id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd","user":"alex","currency":"USD","balance":100}
```
#### Get an account 
```
GET http://localhost:8080/accounts/56d3b507-9175-4cd6-b2bb-3a83613dd8bd
```
Response:
```
{"id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd","user":"alex","currency":"USD","balance":100}
```
#### Get all accounts 
```
GET http://localhost:8080/accounts
```
Response:
```
[{"id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd","user":"alex","currency":"USD","balance":100}]
```
#### Update an account 
```
PUT http://localhost:8080/accounts/56d3b507-9175-4cd6-b2bb-3a83613dd8bd
{"balance":10}
```
Response:
```
{"id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd","user":"alex","currency":"USD","balance":10}
```
#### Delete an account
```
DELETE http://localhost:8080/accounts/56d3b507-9175-4cd6-b2bb-3a83613dd8bd
```
### Transfers
#### Create a transfer (and execute it)
```
POST http://localhost:8080/transfers
{"source-id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd", "dest-id":"26df4b98-ac89-418f-b383-a9d5df4024bb", "amount":10}
```
Response:
```
{"id":"63686614-fd19-409a-9712-2bc2dfa87bfd","source":{"id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd","user":"alex","currency":"USD","balance":90},"destination":{"id":"26df4b98-ac89-418f-b383-a9d5df4024bb","user":"ben","currency":"USD","balance":20},"amount":10}
```
**Note** that it is not allowed to transfer money from one account to itself, to transfer money between accounts that do not have the same currency or to transfer a non-positive amount (less than or equal to zero) amount of money.
Also note that the transfer is executed right after it is created with no confirmation from the user. 
#### Get a transfer
```
GET http://localhost:8080/transfers/63686614-fd19-409a-9712-2bc2dfa87bfd
```
Response:
```
{"id":"63686614-fd19-409a-9712-2bc2dfa87bfd","source":{"id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd","user":"alex","currency":"USD","balance":90},"destination":{"id":"26df4b98-ac89-418f-b383-a9d5df4024bb","user":"ben","currency":"USD","balance":20},"amount":10}
```
#### Get all transfers
```
GET http://localhost:8080/transfers
```
Response:
```
[{"id":"63686614-fd19-409a-9712-2bc2dfa87bfd","source":{"id":"56d3b507-9175-4cd6-b2bb-3a83613dd8bd","user":"alex","currency":"USD","balance":90},"destination":{"id":"26df4b98-ac89-418f-b383-a9d5df4024bb","user":"ben","currency":"USD","balance":20},"amount":10}]
```
**Note** that is is not possible to delete or update transfers (design choice).