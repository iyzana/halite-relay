# Halite Relay

TCP relay for halite bots,
so that you don't have to share your bot
but can still let others play against it


## Installation

Build with `./gradlew build` on linux
and `gradlew build` on windows.
The executable jar will be at `build/libs/halite-relay-all.jar`

## Usage

`java -jar halite-relay-all.jar <quoted bot command> [port]`

To let the halite binary connect with the tcp relay,
you'll have to have `nc` (netcat) installed.

Run halite like this for a game between two instances of a remote bot:

```sh
./halite "nc <ip|localhost> <port>" "nc <ip|localhost> <port>"
```
