# FSD - Distributed Systems

This project consists in the client/server comunication using java sockets and RMI methods.

## Instalation
Clone this repository:
```
git clone https://github.com/JoaoMinistro17/FSD-Distributed_Systems.git
```
## Run
Use separate terminals for running both the server and the clients. Make sure you start the server before you start any of the clients.

Note: 
```javac``` is for compiling
```java``` is for running
### Terminal 1:
```
javac Server/Server.java
```
```
java Server/Server
```
### Terminal 2 (for Socket Client):
```
javac Client/Client.java
```
```
java Client/Client
```
### Terminal 2 (for RMI Client):
```
javac Client/RMICLient.java
```
```
java Client/RMIClient
```
This displays both comunication ends in a stock of 4 products and contains unfinished security measures in the requests and responses.
