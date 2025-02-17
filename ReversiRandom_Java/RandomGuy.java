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
//        int myMove = generator.nextInt(numValidMoves);
//        System.out.println("Random Move: " + myMove);


        int depth = 3;
        boolean maximizing_player = true;
        int[] result = minimax(state, depth, round, maximizing_player);
        int myMove = result[1];
        System.out.println("My move: " + myMove);
        return myMove;
    }

    private int[] minimax(int state[][], int depth, int round, boolean maximizing_player) {
        int maxEval = -1000;
        int minEval = 1000;
        int eval;
        int bestMove;
        int bestMoveIndex = -1;
        int tempState[][] = new int[8][8];
        int futureValidMoves[] = new int[64];
        int futureNumValidMoves;


        // Evaluates the heuristic value of the state
        if (depth == 0) {
            // get corner heuristic
            // get mobitilty heuristic

            return new int[]{get_coin_parity_heuristic(state), -1};
        }

//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                tempState[i][j] = state[i][j];
//            }
//        }

        getValidMoves(round, state);
        futureNumValidMoves = numValidMoves;
        if (numValidMoves >= 0) System.arraycopy(validMoves, 0, futureValidMoves, 0, numValidMoves);

        if (maximizing_player) {
            for (int moveIndex = 0; moveIndex < futureNumValidMoves; moveIndex++) {
                int move = futureValidMoves[moveIndex];
                int i = move / 8;
                int j = move % 8;
                if (state[i][j] == 0) {
                    tempState = getNewState(state, move, me, round);
                    System.out.println("Temporary State");
                    printState(tempState);
                    int[] result = minimax(tempState, depth-1, round+1, false);
                    eval = result[0];
                    System.out.println("Eval: " + eval);
                    if (eval > maxEval) {
                        maxEval = eval;
                        bestMove = move;
                        bestMoveIndex = moveIndex;
                        System.out.println("Best Move: " + bestMove + " MaxEval: " + maxEval + " Move Index: " + moveIndex);
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
                    tempState = getNewState(state, move, 1, round);
                    System.out.println("Temporary State");
                    printState(tempState);
                    int[] result = minimax(tempState, depth-1, round+1, true);
                    eval = result[0];
                    System.out.println("Eval: " + eval);
                    if (eval < minEval) {
                        minEval = eval;
                        bestMove = move;
                        bestMoveIndex = moveIndex;
                        System.out.println("Best Move: " + bestMove + " MinEval: " + minEval + " Move Index: " + moveIndex);
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

        return newState;
    }

    private void printState(int state[][]) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(state[i][j]);
            }
            System.out.println();
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
            System.out.println("Valid Moves:");
            for (i = 0; i < numValidMoves; i++) {
                System.out.println(validMoves[i] / 8 + ", " + validMoves[i] % 8);
            }
        }
        else {
            System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j)) {
                            validMoves[numValidMoves] = i*8 + j;
                            numValidMoves ++;
                            System.out.println(i + ", " + j);
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
        int my_tiles = 0;
        int opp_tiles = 0;
        int heuristic = 0;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                if (state[i][j] == me) {
                    my_tiles++;
                }
                else if (state[i][j] != 0) {
                    opp_tiles++;
                }
            }
        }

        System.out.println("Future State");
        printState(state);

        if (me == 1) {
            heuristic = 100 * (opp_tiles - my_tiles) / (my_tiles + opp_tiles);
        }
        else {
            heuristic = 100 * (my_tiles - opp_tiles) / (my_tiles + opp_tiles);
        }

        System.out.println("RandomGuy Tiles: " + my_tiles);
        System.out.println("Human Tiles: " + opp_tiles);

        return heuristic;
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
