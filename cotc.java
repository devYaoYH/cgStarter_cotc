import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    // CONFIG FLAGS
    private final boolean DEBUG_GAME_STATE = false;

    // Constants
    private final String SHIP = "SHIP";
    private final String BARREL = "BARREL";
    private final String MINE = "MINE";
    private final String CANNONBALL = "CANNONBALL";

    private enum Action{
        FAST, SLOW, RIGHT, LEFT, WAIT, FIRE, MINE;
    }

    private final Action[] SIMUL_MOVES = {Action.FAST, Action.SLOW, Action.RIGHT, Action.LEFT, Action.WAIT};
    private ArrayList<ArrayList<ArrayList<Action> > > SIMUL_COMBINATIONS = new ArrayList<ArrayList<ArrayList<Action> > >();

    // Adjacent Hex Grids (0 - 5 orientation)
    private final int[][][] ADJ_XY = {{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}}, {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}}};
    // Mapping of Orientation Step to Hex Grid Increment
    private final int[][] ADJ_HEX = {{1, -1, 0}, {1, 0, -1}, {0, 1, -1}, {-1, 1, 0}, {-1, 0, 1}, {0, -1, 1}};

    // Memoized Grid of Playable Coordinates
    // Reduce the need to re-compute the transformation everytime
    // we need to access loc for algo v.s. print in correct format
    private Point[][] COORDS = new Point[23][21];

    // Main Function
    public static void main(String args[]) {
        Player myPlayer = new Player();
        // Main Game Loop
        myPlayer.run();
    }

    // Util Funcs
    private static boolean is_valid(int x, int y){
        return (x >= 0 && x < 23) && (y >= 0 && y < 21);
    }

    // Standard BFS algo to transform 2D coords into Cubic Hex Coords
    private void hex_transform(){
        // Declare visited set (to not explore previously encountered nodes)
        HashSet<Point> visited = new HashSet<>();

        // Store fringe nodes in queue (to expand later)
        Queue<Point> q = new LinkedList<>();

        // Initialize (sx, sy) to (0, 0, 0) in Cubic Coords
        COORDS[0][0].q = 0;
        COORDS[0][0].r = 0;
        COORDS[0][0].z = 0;

        // Add it to queue to expand
        q.add(COORDS[0][0]);
        // Mark as expanded
        visited.add(COORDS[0][0]);

        // Start our BFS queue
        while(q.size() > 0){
            // Pop first coordinate
            Point p = q.remove();
            //DEBUG: Check mapping is correct
            // System.err.println(String.format("%s -> %s", p.xy(), p.hex()));

            // Iterate through possible neighbors (Exploration Step)
            for (int i=0;i<6;++i){
                // Generate Grid (XY) positions
                int nx = p.x + ADJ_XY[p.y%2][i][0];
                int ny = p.y + ADJ_XY[p.y%2][i][1];
                // Check valid && we haven't explored this step yet
                if (is_valid(nx, ny) && !visited.contains(COORDS[nx][ny])){
                    // Map to Cubic Hex Coords
                    COORDS[nx][ny].q = p.q + ADJ_HEX[i][0];
                    COORDS[nx][ny].r = p.r + ADJ_HEX[i][1];
                    COORDS[nx][ny].z = p.z + ADJ_HEX[i][2];
                    // Add step to expand next
                    q.add(COORDS[nx][ny]);
                    // Mark as explored
                    visited.add(COORDS[nx][ny]);
                }
            }
        }
    }

    // Generate all possible combination of moves our #number of ships
    private void gen_move_combinations(){
        // Initialize
        for (int i=0;i<4;++i) SIMUL_COMBINATIONS.add(new ArrayList<ArrayList<Action> >());
        // Memoize possible move combinations for 1 ship
        for (int i=0;i<SIMUL_MOVES.length;++i){
            ArrayList<Action> new_move_seq = new ArrayList<>();
            new_move_seq.add(SIMUL_MOVES[i]);
            SIMUL_COMBINATIONS.get(1).add(new_move_seq);
        }
        // Memoize possible move combinations for 2 ship
        for (int i=0;i<SIMUL_MOVES.length;++i){
            for (int j=0;j<SIMUL_MOVES.length;++j){
                ArrayList<Action> new_move_seq = new ArrayList<>();
                new_move_seq.add(SIMUL_MOVES[i]);
                new_move_seq.add(SIMUL_MOVES[j]);
                SIMUL_COMBINATIONS.get(2).add(new_move_seq);
            }
        }
        // Memoize possible move combinations for 3 ship
        for (int i=0;i<SIMUL_MOVES.length;++i){
            for (int j=0;j<SIMUL_MOVES.length;++j){
                for (int k=0;k<SIMUL_MOVES.length;++k){
                    ArrayList<Action> new_move_seq = new ArrayList<>();
                    new_move_seq.add(SIMUL_MOVES[i]);
                    new_move_seq.add(SIMUL_MOVES[j]);
                    new_move_seq.add(SIMUL_MOVES[k]);
                    SIMUL_COMBINATIONS.get(3).add(new_move_seq);
                }
            }
        }
        // System.err.println(Arrays.toString(SIMUL_COMBINATIONS.toArray()));
    }

    // Util Classes

    // Point class for recording positions
    private class Point {
        public int x, y;
        public int q, r, z;

        Point(int x, int y){
            this.x = x;
            this.y = y;
        }

        Point(Point p){
            this.x = p.x;
            this.y = p.y;
            this.q = p.q;
            this.r = p.r;
            this.z = p.z;
        }

        public String xy(){
            return String.format("(%d %d)", x, y);
        }

        public String hex(){
            return String.format("(%d %d %d)", q, r, z);
        }

        public Point move(int d, int len){
            Point new_p = new Point(this);
            new_p.q += ADJ_HEX[d][0]*len;
            new_p.r += ADJ_HEX[d][1]*len;
            new_p.z += ADJ_HEX[d][2]*len;
            new_p.x += ADJ_XY[this.y%2][d][0]*len;
            new_p.y += ADJ_XY[this.y%2][d][1]*len;
            return new_p;
        }

        public int dist(Point p){
            return Math.max(Math.max(Math.abs(p.q - this.q), Math.abs(p.r - this.r)), Math.abs(p.z - this.z));
        }

        @Override
        public boolean equals(Object obj){
            if (this == obj) return true;
            else if (obj == null || obj.getClass() != this.getClass()) return false;
            else{
                Point other = (Point) obj;
                // System.err.println("x: " + this.x + "y: " + this.y + "|ox: " + other.x + " oy: " + other.y);
                if (this.x == other.x && this.y == other.y) return true;
                else return false;
            }
        }

        @Override
        public int hashCode(){
            return this.x*100 + this.y;
        }
    }

    // Game Object Classes
    // Barrel Class
    private class Barrel{

        public int x, y, rum;
        public int id;
        public Point pos;

        Barrel(int id, int x, int y, int rum){
            this.id = id;
            this.pos = COORDS[x][y];
            this.x = x;
            this.y = y;
            this.rum = rum;
        }

        Barrel(Barrel b){
            this.id = b.id;
            this.pos = b.pos;
            this.x = b.x;
            this.y = b.y;
            this.rum = b.rum;
        }
    }

    // Mine Class
    private class Mine{

        public int x, y;
        public int id;
        public Point pos;

        Mine(int id, int x, int y){
            this.id = id;
            this.pos = COORDS[x][y];
            this.x = x;
            this.y = y;
        }

        Mine(Mine m){
            this.id = m.id;
            this.pos = m.pos;
            this.x = m.x;
            this.y = m.y;
        }
    }

    // Mine Class
    private class Cannonball{

        public int x, y, ttt; //Records when this cannonball was shot
        public int id, origin; //Id of cannonball object | Id of origin ship
        public Point pos; //Target destination

        Cannonball(int id, int x, int y, int origin_id, int ttt){
            this.id = id;
            this.pos = COORDS[x][y];
            this.x = x;
            this.y = y;
            this.origin = origin_id;
            this.ttt = ttt;
        }

        Cannonball(Cannonball c){
            this.id = c.id;
            this.pos = c.pos;
            this.x = c.x;
            this.y = c.y;
            this.origin = c.origin;
            this.ttt = c.ttt;
        }
    }

    // Ship Class
    private class Ship {

        public int direction, speed, rum;
        public int id;
        public int x, y;
        public Point pos;

        Ship(int id, int x, int y, int dir, int spd, int rum){
            this.id = id;
            this.pos = COORDS[x][y];
            this.x = x;
            this.y = y;
            this.direction = dir;
            this.speed = spd;
            this.rum = rum;
        }

        Ship(Ship s){
            this.id = s.id;
            this.pos = s.pos;
            this.x = s.x;
            this.y = s.y;
            this.direction = s.direction;
            this.speed = s.speed;
            this.rum = s.rum;
        }
    }

    // Game State Class (Data Struct)
    private class State {

        public ArrayList<Barrel> barrels = new ArrayList<>();
        public ArrayList<Ship> myShips = new ArrayList<>();
        public ArrayList<Ship> enShips = new ArrayList<>();
        public ArrayList<Mine> mines = new ArrayList<>();
        public ArrayList<Cannonball> cannonballs = new ArrayList<>();

        State(){

        }

        State(State state){
            for (Barrel b: state.barrels){
                barrels.add(new Barrel(b));
            }
            for (Ship s: state.myShips){
                myShips.add(new Ship(s));
            }
            for (Ship s: state.enShips){
                enShips.add(new Ship(s));
            }
            for (Mine m: state.mines){
                mines.add(new Mine(m));
            }
            for (Cannonball b: state.cannonballs){
                cannonballs.add(new Cannonball(b));
            }
        }

        public void debug(){
            System.err.println("DEBUG GAME STATE INFO:");
            for (Barrel b: this.barrels){
                System.err.println(b.pos.xy() + b.rum);
            }
            for (Mine m: this.mines){
                System.err.println("M:" + m.pos.xy());
            }
            for (Cannonball c: this.cannonballs){
                System.err.println("C: ->" + c.pos.xy() + " ttt:" + c.ttt);
            }
            System.err.println("My Positions:");
            for (Ship s: this.myShips){
                System.err.println("MY S: " + s.pos.xy() + " rum:" + s.rum);
            }
            System.err.println("Enemy Positions:");
            for (Ship s: this.enShips){
                System.err.println("EN S: " + s.pos.xy() + " rum:" + s.rum);
            }
        }

        // Heuristic Scoring of current state (specify from who's perspective)
        public int score_state(boolean self){
            int score = 0;
            if (self){
                // Self Scoring
                for (Ship s: myShips){
                    score += s.rum;
                }
            }
            else{
                // Enemy Scoring
                for (Ship s: enShips){
                    score += s.rum;
                }
            }
            return score;
        }

        // Generate children nodes from current state
        public ArrayList<State> get_succs(boolean tick, boolean self){
            ArrayList<State> successors = new ArrayList<State>();

            // Decrease ships' rum and cannonballs' ttt
            if (tick){
                // Increase game round
                for (Ship s: myShips){
                    s.rum--;
                }
                for (Ship s: enShips){
                    s.rum--;
                }
                Iterator<Cannonball> it = cannonballs.iterator();
                while(it.hasNext()){
                    Cannonball c = it.next();
                    c.ttt--;
                    if (c.ttt < 0) it.remove();
                }
            }

            // Simulate ships
            ArrayList<Ship> simul_ships = myShips;
            if (!self){
                simul_ships = enShips;
            }

            // Apply possible simulations from our combination list
            int num_ships = simul_ships.size();
            for (int i=0;i<SIMUL_COMBINATIONS.get(num_ships).size();++i){
                ArrayList<Action> cur_actions = SIMUL_COMBINATIONS.get(num_ships).get(i);
                State new_state = new State(this);
                for (int j=0;j<num_ships;++j){
                    new_state.move_ship(simul_ships.get(j).id, cur_actions.get(j));
                }
                successors.add(new_state);
            }
            return successors;
        }

        // Simulation Steps
        // !!!Mutates current state!!!
        public void move_ship(int id, Action move){
            Ship ship = null;
            for (Ship s: myShips){
                if (s.id == id) ship = s;
            }
            for (Ship s: enShips){
                if (s.id == id) ship = s;
            }
            if (ship == null){
                System.err.println("Error, ship " + id + " NOT FOUND");
                return;
            }

            // Apply movement to ship
            switch(move){
                case FAST:
                    if (ship.speed < 2){
                        ship.speed += 1;
                    }
                    break;
                case SLOW:
                    if (ship.speed > 0){
                        ship.speed -= 1;
                    }
                    break;
                default:
                    break;
            }
            
            // Simulate movement of ship
            ship.pos = ship.pos.move(ship.direction, ship.speed);

            switch(move){
                case RIGHT:
                    ship.direction = (ship.direction + 5)%6;
                    break;
                case LEFT:
                    ship.direction = (ship.direction + 1)%6;
                    break;
                default:
                    break;
            }

            // Simulate turning
            Point bow = ship.pos.move(ship.direction, 1);
            Point stern = ship.pos.move((ship.direction + 3)%6, 1);
            if (DEBUG_GAME_STATE) System.err.println("Ship " + ship.pos.xy() + " dir: " + ship.direction + " speed: " + ship.speed + " bow:" + bow.xy() + " stern:" + stern.xy());
            //TODO: Check for collisions

            // Simulate picking up of barrels
            Iterator<Barrel> b_it = barrels.iterator();
            while(b_it.hasNext()){
                Barrel b = b_it.next();
                // System.err.println("Barrel at: " + b.pos.xy() + (b.pos == bow));
                if (b.pos.equals(ship.pos) || b.pos.equals(bow) || b.pos.equals(stern)){
                    ship.rum += b.rum;
                    ship.rum = Math.min(ship.rum, 100);
                    if (DEBUG_GAME_STATE) System.err.println("Picked up barrel! @ " + b.pos.xy());
                    b_it.remove();
                }
            }
            
            // Simulate landing of cannonballs
            Iterator<Cannonball> c_it = cannonballs.iterator();
            while(c_it.hasNext()){
                Cannonball c = c_it.next();
                if (c.ttt == 0){
                    if (c.pos.equals(bow) || c.pos.equals(stern)){
                        if (DEBUG_GAME_STATE) System.err.println("Hit by Cannonball! @ " + c.pos.xy());
                        ship.rum -= 25;
                    }
                    if (c.pos.equals(ship.pos)){
                        if (DEBUG_GAME_STATE) System.err.println("Hit by Cannonball! @ " + c.pos.xy());
                        ship.rum -= 50;
                    }
                    //TODO: Check for explode mines?
                    c_it.remove();
                }
            }
            
            // Simulate explosion of mines
            Iterator<Mine> m_it = mines.iterator();
            while(m_it.hasNext()){
                Mine m = m_it.next();
                if (m.pos.equals(ship.pos) || m.pos.equals(bow) || m.pos.equals(stern)){
                    ship.rum -= 25;
                    if (DEBUG_GAME_STATE) System.err.println("Stepped on mine! @ " + m.pos.xy());
                    //TODO: Explode mine (check for other ships in range)
                    m_it.remove();
                }
            }
        }

        // Getter/Setter
        public void add_my_ship(Ship s){
            myShips.add(s);
        }

        public void add_en_ship(Ship s){
            enShips.add(s);
        }

        public void add_barrel(Barrel b){
            barrels.add(b);
        }

        public void add_mine(Mine m){
            mines.add(m);
        }

        public void add_cannonball(Cannonball c){
            cannonballs.add(c);
        }
    }

    // Simulation Class
    private class Simul {

        private State init_state = null;
        private Long init_time = 0L;

        Simul(){

        }

        public void init(State state, Long time){
            init_state = state;
            init_time = time;
        }

        // Main AI Step generation function
        public ArrayList<Point> run(){
            //Store our combination of moves (for list of ships)
            ArrayList<Point> moves = new ArrayList<Point>();

            if (DEBUG_GAME_STATE){
                init_state.debug();
            }

            //Simulate 1 move into
            ArrayList<State> init_succs = new State(init_state).get_succs(true, true);
            if (DEBUG_GAME_STATE){
                for (State state: init_succs){
                    System.err.println("STATE:");
                    for (Ship s: state.myShips){
                        System.err.println("Ship: " + s.id + " Pos: " + s.pos.xy() + " Dir: " + s.direction + " Speed: " + s.speed + " Rum: " + s.rum);
                    }
                }
            }

            //Selection policy
            for (Ship s: init_state.myShips){
                moves.add(this.nearest_barrel(init_state, s.pos));
            }
            
            return moves;
        }

        // Selection policies
        public Point nearest_barrel(State state, Point p){
            // Wrong mahanttan distance used
            // Also super-inefficient O(n) algo
            int min_dist = 999999;
            Barrel near_b = null;
            Point s_pos = p;
            for (Barrel b: state.barrels){
                int cur_dist = s_pos.dist(b.pos);
                if (cur_dist < min_dist){
                    min_dist = cur_dist;
                    near_b = b;
                }
            }
            // Path using BFS to nearest barrel
            if (near_b != null){
                System.err.println(s_pos.hex() + " -> " + near_b.pos.hex() + ":" + s_pos.dist(s_pos));
                return near_b.pos;
            }
            return COORDS[12][10];
        }

    }

    // Game Loop
    private void run(){
        // Setup Stuff
        // Populate our grid with 2d XY Points
        for (int x=0;x<23;++x){
            for (int y=0;y<21;++y){
                COORDS[x][y] = new Point(x, y);
            }
        }
        // Use BFS to transform 2d coords to Cubic Hex Coords
        hex_transform();
        System.err.println(COORDS[13][13].xy());
        // Generate possible ship move combinations
        gen_move_combinations();

        // Initialize IO
        Scanner in = new Scanner(System.in);

        // Game turn counter
        int game_round = 0;

        // Simulation Engine
        Simul simul = new Simul();

        // game loop
        while (true) {
            game_round++;
            State game = new State();
            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                switch(entityType){
                    case (SHIP):
                        if (arg4 == 1){
                            // My Ship
                            game.add_my_ship(new Ship(entityId, x, y, arg1, arg2, arg3));
                        }
                        else{
                            // En Ship
                            game.add_en_ship(new Ship(entityId, x, y, arg1, arg2, arg3));
                        }
                        break;
                    case (BARREL):
                        game.add_barrel(new Barrel(entityId, x, y, arg1));
                        break;
                    case (MINE):
                        game.add_mine(new Mine(entityId, x, y));
                        break;
                    case (CANNONBALL):
                        game.add_cannonball(new Cannonball(entityId, x, y, arg1, arg2));
                        break;
                    default:
                        break;
                }
            }

            // Start our timer
            long initTime = System.nanoTime();

            // Start our simulation engine
            simul.init(game, initTime);
            // Run some selection policy using our game engine
            ArrayList<Point> my_moves = simul.run();
            for (Point p: my_moves){
                System.out.println(String.format("MOVE %d %d", p.x, p.y));
            }
        }
    }
}
