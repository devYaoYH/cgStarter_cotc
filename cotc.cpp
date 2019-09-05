#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>

// Defining Macros
#define is_valid(x, y) (x >= 0 && x < 23 && y >= 0 && y < 21)

using namespace std;

// Class Declarations
class Point{
	public:
		Point();
		Point(int, int);
		~Point() {}
		int x, y, q, r, z;
};

class Barrel{
	public:
		Barrel();
		Barrel(Barrel& b);
		Barrel(int, int, int);
		~Barrel();
		int x, y, rum;
		const Point* pos;
};

class Ship{
	public:
		Ship();
		Ship(Ship& s);
		Ship(int, int, int, int, int);
		~Ship();
		int x, y, direction, speed, rum;
		const Point* pos;
};

class State{
	public:
		State();
		~State();
		vector<Ship> myShips, enShips;
		vector<Barrel> barrels;
};

// Global Variable Definitions
Point* COORDS[23][21];

// Point Class Definitions
Point::Point(): x(-1), y(-1), q(-1), r(-1), z(-1){
}

Point::Point(int x, int y): x(x), y(y), q(-1), r(-1), z(-1){
}

// Barrel Class Definitions
Barrel::Barrel(): x(-1), y(-1), rum(-1), pos(nullptr){
}

Barrel::Barrel(Barrel& b): x(b.x), y(b.y), rum(b.rum), pos(b.pos){
}

Barrel::Barrel(int x, int y, int rum): x(x), y(y), rum(rum){
	if (is_valid(x, y)){
		pos = COORDS[x][y];
	}
}

Barrel::~Barrel(){
	pos = nullptr;
}

// Ship Class Definitions
Ship::Ship(): x(-1), y(-1), direction(-1), speed(-1), rum(-1), pos(nullptr){
}

Ship::Ship(Ship& s): x(s.x), y(s.y), direction(s.direction), speed(s.speed), rum(s.rum), pos(s.pos){
}

Ship::Ship(int x, int y, int dir, int spd, int rum): x(x), y(y), direction(dir), speed(spd), rum(rum), pos(nullptr){
	if (is_valid(x, y)){
		pos = COORDS[x][y];
	}
}

Ship::~Ship(){
	pos = nullptr;
}

// State Class Definitions
State::State(){
}

State::~State(){
}

// MAIN LOOP
int main()
{
	// Setup
	for (int x=0;x<23;++x){
		for (int y=0;y<21;++y){
			COORDS[x][y] = new Point(x, y);
		}
	}

    // game loop
    while (1) {
        int myShipCount; // the number of remaining ships
        cin >> myShipCount; cin.ignore();
        int entityCount; // the number of entities (e.g. ships, mines or cannonballs)
        cin >> entityCount; cin.ignore();
        for (int i = 0; i < entityCount; i++) {
            int entityId;
            string entityType;
            int x;
            int y;
            int arg1;
            int arg2;
            int arg3;
            int arg4;
            cin >> entityId >> entityType >> x >> y >> arg1 >> arg2 >> arg3 >> arg4; cin.ignore();
        }
        for (int i = 0; i < myShipCount; i++) {

            // Write an action using cout. DON'T FORGET THE "<< endl"
            // To debug: cerr << "Debug messages..." << endl;

            cout << "MOVE 11 10" << endl; // Any valid action, such as "WAIT" or "MOVE x y"
        }
    }
}