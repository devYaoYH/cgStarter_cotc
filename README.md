#CotC Java starter code with Simulation Engine

## Brief structure of AI bot algorithm

```java
public static void main(String args[]){
	Player myPlayer = new Player();	//Enables us to use OOP
	myPlayer.run();	//Main function our player steps into
}

//////////////////
// GAME OBJECTS //
//////////////////

// Main Function
private void run(){
	//Setup Precomputations

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