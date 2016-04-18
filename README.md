# WordWolfServer
WordWolf Server

Hello! Welcome to the server codebase for WordWolf, an Android app written in Java, a project by Jason Mortara. WordWolf is a word-find game designed from the ground up to support two players in a head-to-head match.

The WordWolf server is a multithreaded Java socket server whose job is to send and receive objects to and from the client Android app, and to perform a similar function to and from the MySQL users database.

The server is deployed to a remote Linux environment as a runnable JAR.

On startup, the server sets itself up to listen for new socket connections on a specific port. When a connection is made, the server spawns a new ClientHandler thread for communicating with that client, which would be an Android device. The server also loads up the same 172,000-word dictionary that the client uses, for word validation.

The ClientHandler's main job is to listen for incoming messages on an ObjectInputStream, process those messages based on things like player state and other factors, and then send back messages to the client via an ObjectOutputStream. All incoming messages (usually Requests) and outgoing messages (usually Responses) are native serialized Java objects bundled into a JAR library, and are found in another project here on GitHub. They are shared by both the client and the server, so objects come in as native Java objects and go out as the same.

The ClientHandler handles things like:

- Making a new account
- Logging a player in
- Sending out a list of available players
- Handling selecting a new potential opponent
- Generating a new randomized Game Board and sending the same board out to each client in a match
- Receiving words from the client in the form of GameMove objects which contain a list of TileData objects 
- Validating words on the server side (in addition to the client validation, to minimize cheating)
- Scoring words and sending out awarded points
- Handling Game Over state and notifying each player of the other's score, which defines a winner
- Handling rematch requests
- Handling error conditions such as disconnects

The server is an integral part of WordWolf!

All code is currently in progress and performs extensive logging, which would be turned off in release builds.

-Jason Mortara, 2016




