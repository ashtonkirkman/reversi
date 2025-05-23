import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.math.*;
import java.text.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class RandomGuy {

    public Socket s;
	public BufferedReader sin;
	public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;
    
    int validMoves[] = new int[64];
    int numValidMoves;

    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},         {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };
    
    
    // main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
    public RandomGuy(int _me, String host) {
        me = _me;
        initClient(host);

        int myMove;
        
        while (true) {
            System.out.println("Read");
            readMessage();

            System.out.println("me " + me);

            if (turn == me) {
                System.out.println("Move");
                getValidMoves(round, state);
                myMove = move();
                //myMove = generator.nextInt(numValidMoves);        // select a move randomly
                getValidMoves(round, state);
                String sel = validMoves[myMove] / 8 + "\n" + validMoves[myMove] % 8;
                
                System.out.println("Selection: " + validMoves[myMove] / 8 + ", " + validMoves[myMove] % 8);
                
                sout.println(sel);
            }
        }
        //while (turn == me) {
        //    System.out.println("My turn");
            
            //readMessage();
        //}
    }
    
    // You should modify this function
    // validMoves is a list of valid locations that you could place your "stone" on this turn
    // Note that "state" is a global variable 2D list that shows the state of the game
    private int move() {
        // just move randomly for now
        int myMove;
//        if (me == 1) {
//            myMove = generator.nextInt(numValidMoves);
//            System.out.println("Random Move: " + myMove);
//        }

//        else {
        int depth = 8;
        boolean maximizing_player = true;
        int[] result = minimax(state, depth, round, maximizing_player, -100000, 100000);
        myMove = result[1];
//        System.out.println("My move: " + myMove);
//        }
        return myMove;
    }

    private int[] minimax(int state[][], int depth, int round, boolean maximizing_player, int alpha, int beta) {
        int maxEval = -100000;
        int minEval = 100000;
        int eval;
        int bestMove;
        int bestMoveIndex = 0;
        int tempState[][] = new int[8][8];
        int futureValidMoves[] = new int[64];
        int futureNumValidMoves;
        int original_me = me;

//        System.out.println("Minimax call - Depth: " + depth + ", Maximizing: " + maximizing_player);

        // Evaluates the heuristic value of the state
        if (depth == 0) {
            int heuristicValue = 0;


//            if (round < 16) {
//                heuristicValue = (int)((get_coin_parity_heuristic(state) * 0.15) +
//                        (get_corner_control(state) * 0.45) +
//                        (get_mobility_heuristic(state) * 0.4));
//            } else if (round < 45) {
//                heuristicValue = (int)((get_coin_parity_heuristic(state) * 0.3) +
//                        (get_corner_control(state) * 0.5) +
//                        (get_mobility_heuristic(state) * 0.2));
//            } else {
//                heuristicValue = (int)((get_coin_parity_heuristic(state) * 0.35) +
//                        (get_corner_control(state) * 0.55) +
//                        (get_mobility_heuristic(state) * 0.1));
//            }

            heuristicValue = get_coin_parity_heuristic(state);


//            System.out.println("Leaf node reached. Heuristic value: " + heuristicValue);
            // get corner heuristic
            // get mobitilty heuristic

            return new int[]{heuristicValue, 0};
        }

        int currentPlayer = maximizing_player ? 2 : 1;

//        me = currentPlayer;
        getValidMoves(round, state);
//        System.out.println("Valid moves at depth " + depth + ": " + Arrays.toString(Arrays.copyOf(validMoves, numValidMoves)));
        futureNumValidMoves = numValidMoves;
        if (numValidMoves >= 0) System.arraycopy(validMoves, 0, futureValidMoves, 0, numValidMoves);
//        me = original_me;
        if (maximizing_player) {
            for (int moveIndex = 0; moveIndex < futureNumValidMoves; moveIndex++) {
                int move = futureValidMoves[moveIndex];
                int i = move / 8;
                int j = move % 8;
                if (state[i][j] == 0) {
//                    System.out.println("Current State");
//                    printState(state);
                    tempState = getNewState(state, move, currentPlayer, round);
//                    System.out.println("Future State");
//                    printState(tempState);
                    int[] result = minimax(tempState, depth-1, round+1, false, alpha, beta);
                    eval = result[0];
                    if (eval > maxEval) {
                        maxEval = eval;
                        bestMove = move;
                        bestMoveIndex = moveIndex;
//                        System.out.println("New best move found: " + bestMove + " with eval: " + maxEval);
                    }

                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return new int[]{maxEval, bestMoveIndex};
        }

        else {
            for (int moveIndex = 0; moveIndex < futureNumValidMoves; moveIndex++) {
                int move = futureValidMoves[moveIndex];
                int i = move / 8;
                int j = move % 8;
                if (state[i][j] == 0) {
//                    System.out.println("Current State");
//                    printState(state);
                    tempState = getNewState(state, move, currentPlayer, round);
//                    System.out.println("Future State");
//                    printState(tempState);
                    int[] result = minimax(tempState, depth-1, round+1, true, alpha, beta);
                    eval = result[0];
                    if (eval < minEval) {
                        minEval = eval;
                        bestMoveIndex = moveIndex;
                        bestMove = move;
//                        System.out.println("New best move found: " + bestMove + " with eval: " + minEval);
                    }

                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
//                        System.out.println("Beta is less than alpha. Pruning.");
                        break;
                    }
                }
            }
            return new int[]{minEval, bestMoveIndex};
        }
    }

    private static int[][] getNewState(int state[][], int move, int player, int round) {
        int newState[][] = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                newState[i][j] = state[i][j];
            }
        }

        int row = move / 8;
        int col = move % 8;

        if (round < 4) {
            newState[row][col] = player;
            return newState;
        }

        for (int[] dir : DIRECTIONS) {
            int r = row + dir[0];
            int c = col + dir[1];
            ArrayList<int[]> captured = new ArrayList<>();

            while (r >= 0 && r < 8 && c >= 0 && c < 8 && newState[r][c] != 0 && newState[r][c] != player) {
                captured.add(new int[]{r, c});
                r += dir[0];
                c += dir[1];
            }

            if (r >= 0 && r < 8 && c >= 0 && c < 8 && newState[r][c] == player) {
                for (int[] pos : captured) {
                    newState[pos[0]][pos[1]] = player;
                }
            }
        }

        newState[row][col] = player;

        return newState;
    }

    private void printState(int state[][]) {
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                System.out.print(state[i][j]);
            }
//            System.out.println();
        }
    }

    private void getValidMoves(int round, int state[][]) {
        int i, j;
        
        numValidMoves = 0;
        if (round < 4) {
            if (state[3][3] == 0) {
                validMoves[numValidMoves] = 3*8 + 3;
                numValidMoves ++;
            }
            if (state[3][4] == 0) {
                validMoves[numValidMoves] = 3*8 + 4;
                numValidMoves ++;
            }
            if (state[4][3] == 0) {
                validMoves[numValidMoves] = 4*8 + 3;
                numValidMoves ++;
            }
            if (state[4][4] == 0) {
                validMoves[numValidMoves] = 4*8 + 4;
                numValidMoves ++;
            }
//            System.out.println("Valid Moves:");
//            for (i = 0; i < numValidMoves; i++) {
//                System.out.println(validMoves[i] / 8 + ", " + validMoves[i] % 8);
//            }
        }
        else {
//            System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j)) {
                            validMoves[numValidMoves] = i*8 + j;
                            numValidMoves ++;
//                            System.out.println(i + ", " + j);
                        }
                    }
                }
            }
        }
        
        
        //if (round > 3) {
        //    System.out.println("checking out");
        //    System.exit(1);
        //}
    }


    
    private boolean checkDirection(int state[][], int row, int col, int incx, int incy) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;
        
        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row+incy*i;
            c = col+incx*i;
        
            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;
        
            sequence[seqLen] = state[r][c];
            seqLen++;
        }
        
        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (me == 1) {
                if (sequence[i] == 2)
                    count ++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        return true;
                    break;
                }
            }
            else {
                if (sequence[i] == 1)
                    count ++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        return true;
                    break;
                }
            }
        }
        
        return false;
    }
    
    private boolean couldBe(int state[][], int row, int col) {
        int incx, incy;
        
        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;
            
                if (checkDirection(state, row, col, incx, incy))
                    return true;
            }
        }
        
        return false;
    }

    private int get_coin_parity_heuristic(int state[][]) {
        int i, j;
        int random_tiles = 0;
        int human_tiles = 0;
        int heuristic = 0;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                if (state[i][j] == 2) {
                    random_tiles++;
                }
                else if (state[i][j] != 0) {
                    human_tiles++;
                }
            }
        }

        heuristic = 100 * (random_tiles - human_tiles) / (random_tiles + human_tiles);

//        if (me == 1) {
//            heuristic = 100 * (human_tiles - random_tiles) / (random_tiles + human_tiles);
//        }
//        else {
//            heuristic = 100 * (random_tiles - human_tiles) / (random_tiles + human_tiles);
//        }

//        System.out.println("Human tiles: " + human_tiles);
//        System.out.println("RandomGuy tiles: " + random_tiles);

        return heuristic;
    }

    private double get_corner_control(int state[][]){
        int random_corners=0;
        int human_corners=0;
        int available_corners = 0;

        int[][] corners = {{0, 0}, {0, 7}, {7, 0}, {7, 7}};

        for (int[] corner : corners) {
            int i = corner[0], j = corner[1];
            if (state[i][j] == 2) { // AI's piece
                random_corners++;
            } else if (state[i][j] == 1) { // Opponent's piece
                human_corners++;
            } else if (couldBe(state, i, j)) { // Empty and playable
                available_corners++;
            }
        }

        double corner_heuristic = 100 * (random_corners - human_corners);
        double corner_move_bonus = 50 * (available_corners / 4.0);

        return corner_heuristic + corner_move_bonus;
    }

    private double get_mobility_heuristic(int state[][]){
        double mobility_heuristic = 0;
        int humanValidMoves, randomValidMoves;

        int original_me = me;

        me =2;
        getValidMoves(round,state);
        randomValidMoves= numValidMoves;

        me=1;
        getValidMoves(round, state);
        humanValidMoves = numValidMoves;

        me = original_me;

        mobility_heuristic = 100.0 *(randomValidMoves - humanValidMoves)/ (randomValidMoves + humanValidMoves);
        return mobility_heuristic;
    }
    
    public void readMessage() {
        int i, j;
        String status;
        try {
            //System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());
            
            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                
                System.exit(1);
            }
            
            //System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
            System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
            System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    state[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
        
        System.out.println("Turn: " + turn);
        System.out.println("Round: " + round);
        for (i = 7; i >= 0; i--) {
            for (j = 0; j < 8; j++) {
                System.out.print(state[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }
    
    public void initClient(String host) {
        int portNumber = 3333+me;
        
        try {
			s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
			sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            String info = sin.readLine();
            System.out.println(info);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    
    // compile on your machine: javac *.java
    // call: java RandomGuy [ipaddress] [player_number]
    //   ipaddress is the ipaddress on the computer the server was launched on.  Enter "localhost" if it is on the same computer
    //   player_number is 1 (for the black player) and 2 (for the white player)
    public static void main(String args[]) {
        new RandomGuy(Integer.parseInt(args[1]), args[0]);
    }
    
}
