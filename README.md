#CotC Java starter code with Simulation Engine

## Pseudo-code Outline

Read input -> Update Game State -> Query Next Action

Within "Query Next Action":

Generate possible futures -> Run selection policy -> Return best course of action

## Brief structure of java AI bot program

```java
public static void main(String args[]){
	Player myPlayer = new Player();	//Enables us to use OOP
	myPlayer.run();	//Main function our player steps into
}

//////////////////
// GAME OBJECTS //
//////////////////

private class Simul {
	public void init(State s, long t){
		//Keep simul up to date with new game state
	}
	public ArrayList<Point> run(){
		//Generate Game Search Tree from init state
		//Run Selection policy
		//Return best action(s) to take
	}
}

// Main Function
private void run(){
	//Setup Precomputations
	Simul simul = new Simul();	//Declare our AI simulation engine

	//Start each turn
	while(true){
		//Setup game state
		State game = new State();
		
		//Read game input

		//Start AI Simul object
		simul.init(game, initTime);	//Pass our AI a time limit

		//Grab results from our AI and issue commands to ships
		ArrayList<Point> my_moves = simul.run();
		for (Point p: my_moves){
			System.out.println(String.format("MOVE %d %d", p.x, p.y));
		}
	}
}
```
