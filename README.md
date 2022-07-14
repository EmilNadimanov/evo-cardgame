## Test task for Evolution

The structure of the description relies on the corresponding [section](https://github.com/evolution-gaming/recruitment/blob/master/backend/GameServer.md#Documentation) of the task.

### Known limitations of your solution
- Connection logic and core game logic are not covered with tests. I have only tested the result manually
- I am 99% sure that I left some resources unhandled. The way each game session is implemented (read next section), I believe there might be some memory leaks with non-terminating fs2 stuff: streams, queues, topics.
- I tried to make the code as adaptable as I could, but connection level and game level turned out to be quite dependent on not-so-pretty Dependency injection.
- I did not implement termination of game sessions. Sorry.

### Key design decisions made, especially if you considered multiple options
**Architecture**
   I made a multi-project build. 

One project is `commons`, where reusable and expendable stuff is stored: Cards, Decks, Hands. The ability to compare cards was implemented with a type class for some reasons we might discuss in person. To be short, I found it the most convenient and pretty way to implement comparison of two same-type objects (e.g. given `C1 <: Card` and `C2 <: Card`, make comparing `C1` to `C2` impossible)

Second one is `server`, that might serve different games. Judging myself, I think I didn't do such a good job at making server parts expandable and reusable. I tried, but got tangled up in types and stuff. Sorry.

**Connection layer**.  I wanted to implement the connection via WebSocket, because It provides:
   1. Persistent connection as opposed to non-persistent HTTP interface. The progress should be shown live, not after a request.
   2. Something that would look suitable even for a real-world version of a card-game. Long polling seemed too limited, so I opted for web socket. People on the web also say that polling is slower, and I trust them - why would they lie?

**The App itself.**
   1. Websocket connection in https is based on fs2-Streams.
      1. Messages from players are stores in fs2-Queues, individual for each player. That's how we now what actions they want to take.
      2. Messages from server go to a Topic (one topic per session). Server acts on the pub side, players act as subs. That's how players get info about the game progress.
   2. Instance of a game manages the gaming process. It contains a deck of cards and two players. It deals cards and managed the score of players. In other words, it represent the state of the game and methods to change the state.
   3. Both DoubleCardGame and SingleCardGame are implemented using the same class. I noticed that these games have so much in common, that they can share not only the interface, but also the implementation. All the differences between them can be described through configs.
   4. Each game instance (i.e. game state) wrapped in a Session, which internally runs two concurrent fs2-Streams:
      1. One stream reads messages from Queues (see 3.1), find a proper reaction by the server and publish it in a Topic. E.g., we can see that another player decided to fold and can judge whether they bluff or not. 
      2. Another stream deals cards if required and calculates new score if both players took an action.
   5. Players use web-interface in a browser to play the game. The client-side app is a simple JS app. I'll be honest - I took the static stuff from [another repo](https://github.com/MartinSnyder/http4s-chatserver/tree/master/static) and adapted it.

**Techy stuff**
   1. In this application I stick to Tagless Final pattern for three reasons.
      1. First, the idea that all methods return values wrapped in an effect(`F[_]`) because it would help composing effectful calls in for-comprehensions. It's neat, easy to read and write.
      2. Second, it is convenient to see what sort of behavior is required from `F[_]` in a class or in a method.
      3. Third, you pals might have expected me to use this sort of abstraction. Honestly, I don't see a lot of benefit from TF in this app, because it is mostly the same sort of abstraction over IO, i.e. `: Sync` and/or `: Monad` most of the time.


### How to test and - if applicable - launch the solution (unless it uses the standard approach by the standard build tool).
   1. Launch `sbt` 
   2. Type `project server; run`
   3. Open two tabs in browser, open `localhost:8080`. 
   4. **Carefully** type in a name, and then either `SingleCardGame` or `DoubleCardGame`. If you make a typo, connection fails. I did not find a way around it :(
   5. Do the steps 3 - 5, but type a different name. If you type the same name, connection fails.
   6. At this point you are supposed to be able to play cards with yourself. Corner-cases like negative score are not covered. Enjoy the anarchy.
