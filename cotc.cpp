#include <cstdio>
#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <unordered_set>
#include <queue>

// Defining Macros
#define is_valid(x, y) (x >= 0 && x < 23 && y >= 0 && y < 21)

using namespace std;

///////////////////////////
// Constants Declaration //
///////////////////////////
string SHIP = "SHIP";
string BARREL = "BARREL";
int ADJ_XY[6][2] = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}};
int ADJ_HEX[6][3] = {{1, -1, 0}, {1, 0, -1}, {0, 1, -1}, {-1, 1, 0}, {-1, 0, 1}, {0, -1, 1}};

// Class Declarations
class Point{
    public:
        Point();
        Point(int, int);
        int dist(Point*);
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
        Point* pos;
};

class Ship{
    public:
        Ship();
        Ship(Ship& s);
        Ship(int, int, int, int, int);
        ~Ship();
        int x, y, direction, speed, rum;
        Point* pos;
};

class State{
    public:
        State();
        ~State();
        void add_barrel(Barrel*);
        void add_my_ship(Ship*);
        void add_en_ship(Ship*);
        Barrel* find_nearest_rum(Ship*);
        vector<Ship*> myShips, enShips;
        vector<Barrel*> barrels;
};

/////////////////////////////////
// Global Variable Definitions //
/////////////////////////////////
Point* COORDS[23][21];

//////////////////////
// Helper Functions //
//////////////////////
void hex_transform(){
    unordered_set<Point*> v;
    queue<Point*> q;

    COORDS[0][0]->q = 0;
    COORDS[0][0]->r = 0;
    COORDS[0][0]->z = 0;
    q.push(COORDS[0][0]);
    v.insert(COORDS[0][0]);

    while (!q.empty()){
        Point* p = q.front();
        q.pop();
        for (int i=0;i<6;++i){
            int nx = p->x + ADJ_XY[i][0];
            int ny = p->y + ADJ_XY[i][1];
            if (is_valid(nx, ny) && v.find(COORDS[nx][ny]) == v.end()){
                COORDS[nx][ny]->q = p->q + ADJ_HEX[i][0];
                COORDS[nx][ny]->r = p->r + ADJ_HEX[i][1];
                COORDS[nx][ny]->z = p->z + ADJ_HEX[i][2];
                q.push(COORDS[nx][ny]);
                v.insert(COORDS[nx][ny]);
            }
        }
    }
}

////////////////////////
// Object Definitions //
////////////////////////
// Point Class Definitions
Point::Point(): x(-1), y(-1), q(-1), r(-1), z(-1){
}

Point::Point(int x, int y): x(x), y(y), q(-1), r(-1), z(-1){
}

int Point::dist(Point* p){
    return max(max(abs(p->q - this->q), abs(p->r - this->r)), abs(p->z - this->z));
}

// Barrel Class Definitions
Barrel::Barrel(): x(-1), y(-1), rum(-1), pos(nullptr){
}

Barrel::Barrel(Barrel& b): x(b.x), y(b.y), rum(b.rum), pos(b.pos){
}

Barrel::Barrel(int x, int y, int rum): x(x), y(y), rum(rum), pos(nullptr){
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

void State::add_barrel(Barrel* b){
    barrels.push_back(b);
}

void State::add_my_ship(Ship* s){
    myShips.push_back(s);
}

void State::add_en_ship(Ship* s){
    enShips.push_back(s);
}

Barrel* State::find_nearest_rum(Ship* s){
    int min_dist = 99999;
    Point* s_pos = s->pos;
    Barrel* near_b = nullptr;
    for (Barrel* b: barrels){
        int cur_dist = s_pos->dist(b->pos);
        if (cur_dist < min_dist){
            min_dist = cur_dist;
            near_b = b;
        }
    }
    return near_b;
}

State::~State(){
    for (Ship* s: myShips) delete s;
    for (Ship* s: enShips) delete s;
    for (Barrel* b: barrels) delete b;
    myShips.clear();
    enShips.clear();
    barrels.clear();
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
    hex_transform();

    // game loop
    while (1) {
        State game = State();
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
            if (entityType.compare(BARREL) == 0){
                // Barrel
                game.add_barrel(new Barrel(x, y, arg1));
            }
            else if (entityType.compare(SHIP) == 0){
                // Ship
                if (arg4 == 1){
                    game.add_my_ship(new Ship(x, y, arg1, arg2, arg3));
                }
                else{
                    game.add_en_ship(new Ship(x, y, arg1, arg2, arg3));
                }
            }
        }
        for (int i = 0; i < myShipCount; i++) {

            // Write an action using cout. DON'T FORGET THE "<< endl"
            // To debug: cerr << "Debug messages..." << endl;

            Barrel* near_b = game.find_nearest_rum(game.myShips[i]);
            cout << "MOVE " << near_b->x << " " << near_b->y << endl; // Any valid action, such as "WAIT" or "MOVE x y"
        }
    }
}