import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    // Constants
    private final String SHIP = "SHIP";
    private final String BARREL = "BARREL";

    // Adjacent Hex Grids (0 - 5 orientation)
    private final int[][] ADJ_XY = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}};
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
            System.err.println(String.format("%s -> %s", p.xy(), p.hex()));

            // Iterate through possible neighbors (Exploration Step)
            for (int i=0;i<6;++i){
                // Generate Grid (XY) positions
                int nx = p.x + ADJ_XY[i][0];
                int ny = p.y + ADJ_XY[i][1];
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

        public int dist(Point p){
            return Math.max(Math.max(Math.abs(p.q - this.q), Math.abs(p.r - this.r)), Math.abs(p.z - this.z));
        }

        @Override
        public boolean equals(Object obj){
            if (this == obj) return true;
            else if (obj == null || obj.getClass() != this.getClass()) return false;
            else{
                Point other = (Point) obj;
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
        public Point pos;

        Barrel(int x, int y, int rum){
            this.pos = COORDS[x][y];
            this.x = x;
            this.y = y;
            this.rum = rum;
        }

        Barrel(Barrel b){
            this.pos = b.pos;
            this.x = b.x;
            this.y = b.y;
            this.rum = b.rum;
        }
    }

    // Ship Class
    private class Ship {

        public int direction, speed, rum;
        public int x, y;
        public Point pos;

        Ship(int x, int y, int dir, int spd, int rum){
            this.pos = COORDS[x][y];
            this.x = x;
            this.y = y;
            this.direction = dir;
            this.speed = spd;
            this.rum = rum;
        }

        Ship(Ship s){
            this.pos = s.pos;
            this.x = s.x;
            this.y = s.y;
            this.direction = s.direction;
            this.speed = s.speed;
            this.rum = s.rum;
        }
    }

    // Game State Class
    private class State {

        public ArrayList<Barrel> barrels = new ArrayList<>();
        public ArrayList<Ship> myShips = new ArrayList<>();
        public ArrayList<Ship> enShips = new ArrayList<>();

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
        }

        public Barrel nearest_barrel(Ship s){
            // Wrong mahanttan distance used
            // Also super-inefficient O(n) algo
            int min_dist = 999999;
            Barrel near_b = null;
            Point s_pos = s.pos;
            for (Barrel b: barrels){
                int cur_dist = s_pos.dist(b.pos);
                if (cur_dist < min_dist){
                    min_dist = cur_dist;
                    near_b = b;
                }
            }
            System.err.println(String.format("(%d %d %d) -> (%d %d %d): %d", s_pos.q, s_pos.r, s_pos.z, near_b.pos.q, near_b.pos.r, near_b.pos.z, near_b.pos.dist(s_pos)));
            return near_b;
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

        // Initialize IO
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
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
                            game.add_my_ship(new Ship(x, y, arg1, arg2, arg3));
                        }
                        else{
                            // En Ship
                            game.add_en_ship(new Ship(x, y, arg1, arg2, arg3));
                        }
                        break;
                    case (BARREL):
                        game.add_barrel(new Barrel(x, y, arg1));
                        break;
                    default:
                        break;
                }
            }
            for (int i = 0; i < myShipCount; i++) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                Barrel near_b = game.nearest_barrel(game.myShips.get(0));
                System.out.println(String.format("MOVE %d %d", near_b.x, near_b.y)); // Any valid action, such as "WAIT" or "MOVE x y"
            }
        }
    }
}