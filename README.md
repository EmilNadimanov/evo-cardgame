## Test task for Evolution

The structure of the description relies on the corresponding [section](https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md#Documentation) of the task.

### Known limitations of your solution
The task was not fully completed, mostly due to the lack of experience with applications that rely on persistent connection.

### Key design decisions made, especially if you considered multiple options
1. In this application I stick to Tagless Final pattern for three reasons.
   1. First, the idea that all methods return values wrapped in an effect(`F[_]`) because it would help composing effectful calls in for-comprehensions. It's neat, easy to read and write.
   2. Second, it is convenient to see what sort of behavior is required from `F[_]` in a class or in a method.
   3. Third, you pals might have expected me to use this sort of abstraction. Honestly, I don't see a lot of benefit from TF in this app, because it is mostly the same sort of abstraction over IO, i.e. `: Sync` and/or `: Monad` most of the time.  
2. **Connection layer**. It was no completed, however I'd like to share some thoughts. I wanted to implement the connection via WebSocket, because It provides:
   1. Persistent connection as opposed to non-persistent HTTP interface
   2. Something that would look suitable even for a real-world version of a card-game. Long polling seemed too limited, so I opted for web socket. People on the web also say that polling is slower.
3. **The App itself.** I did not finish the runnable application. Structure-wise, it was supposed to have the following structure:
   1. Instance of a game manages the gaming process. It contains a deck of cards and two players. It deals cards and managed the score of players.
   2. Both DoubleCardGame and SingleCardGame are implemented using the same class. I noticed that these games have so much in common, that they can share not only the interface, but also the implementation. All the differences between them can be described through configs.
   3. Each game instance wrapped in a session, named "Table", which reacts to players' actions, i.e. tells the Game what to do in response to these actions.
   4. Players use web-interface in a browser to play the game. The client-side app would most likely be a simple JS app.

### How to test and - if applicable - launch the solution (unless it uses the standard approach by the standard build tool).
Sadly one cannot launch the application, as due to time bounds only domain was comletely described.