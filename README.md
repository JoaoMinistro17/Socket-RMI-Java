# FSD - Distributed Systems

This project consists in the client/server comunication using java sockets and RMI methods.

## Instalation
Clone this repository:
```
git clone https://github.com/your-username/FSD.git 
```
## Run
Get at least 2 terminals (in vscode split the terminal). You will need 1 for the server end and the rest for the client types (Socket or RMI). Make sure you start the server before you start any of the clients.

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
