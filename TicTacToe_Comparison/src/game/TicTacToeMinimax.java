package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class TicTacToeMinimax extends JPanel {
    private static final int MAX_UTILITY =1000;
    private static final int MIN_UTILITY=-1000;
    private static final int DRAW=0;
    private static boolean IS_CUTOFF_OCCURRED =false;
    
    // لحساب العقد المستكشفة
    private static int nodesExplored=0; 
    // لحساب الوقت المستغرق
    private static long LAST_MOVE_TIME = 0;
    
    private static int MAX_DEPTH_REACHED=0;
    private static String DIFFICULTY_LEVEL="Hard";
    private static boolean isHumanTurn=false;
    private static JButton buttons[][] = new JButton[4][4];
    private static State humanMoveState=new State();
    private final static int DEPTH_CUTOFF_LEVEL=6;

    private ArrayList<State> listOfPossibleNextMoves =new ArrayList<>();

    public TicTacToeMinimax(){
        setLayout(new GridLayout(4,4));
        initializebuttons();
    }

    public void initializebuttons() {
        for(int i = 0; i <= 3; i++) {
            for(int j=0;j<=3;j++){
                buttons[i][j] = new JButton();
                buttons[i][j].setText("");
                buttons[i][j].addActionListener(new buttonListener());
                buttons[i][j].putClientProperty( "firstIndex", new Integer( i ) );
                buttons[i][j].putClientProperty( "secondIndex", new Integer( j ) );
                add(buttons[i][j]);
            }
        }
    }

    private class buttonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton buttonClicked = (JButton) e.getSource();
            if(isHumanTurn) {
                int[] humanInput=new int[2];
                humanInput[0] = (int)buttonClicked.getClientProperty("firstIndex");
                humanInput[1] =(int)buttonClicked.getClientProperty("secondIndex");
                buttons[humanInput[0]][humanInput[1]].setText("O");
                State newState = getSuccessor(humanMoveState, humanInput);
                printUpdatedBoard(newState.board);

                if(checkForWin(newState)==true) {
                    JOptionPane.showMessageDialog (null, "Congratulations. You won!", "Game results", JOptionPane.INFORMATION_MESSAGE);
                } else if(isTerminalNode(newState) == true) {
                    JOptionPane.showMessageDialog (null, "The game is draw", "Game results", JOptionPane.INFORMATION_MESSAGE);
                } else computerMove(newState);
            }
        }
    }

    public void startGame(){
        State rootNode= this.initState();
        printUpdatedBoard(rootNode.board);
        String[] possibleValues = { "Easy", "Medium", "Hard" };
        Object selectedValue = JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
        DIFFICULTY_LEVEL=selectedValue.toString();
        int reply=JOptionPane.showConfirmDialog(null, "Do you want to go first ?", "choose one", JOptionPane.YES_NO_OPTION);

        if(reply==JOptionPane.YES_OPTION){
            isHumanTurn=true;
            rootNode.nextPlayer ="O";
            humanMoveState=rootNode;
        } else{
            isHumanTurn=false;
            rootNode.nextPlayer ="X";
            this.computerMove(rootNode);
        }
    }

    public int evaluateHeuristicValue(State currentState) {
        if(checkForWin(currentState)==true){
                if(currentState.nextPlayer.equals("X")) return MIN_UTILITY;
                else if(currentState.nextPlayer.equals("O")) return MAX_UTILITY;
        }
        return DRAW;
    }

    public State getSuccessor(State currentState,int[]humanInput){
        if(this.isTerminalNode(currentState)==true)return null;
        else {
            if (currentState.nextPlayer == "X") return new State(this.updateBoard(currentState, humanInput), currentState,"O", this.evaluateHeuristicValue(currentState), currentState.depth + 1);
            else return new State(this.updateBoard(currentState, humanInput), currentState,"X", this.evaluateHeuristicValue(currentState), currentState.depth + 1);
        }
    }

    public boolean isTerminalNode(State currentState) {
        return this.checkForWin(currentState) || !(this.isEmptySquareOnBoard(currentState.board));
    }

    public boolean checkForWin(State currentState){
        return (this.checkForWinOnRows(currentState) || this.checkForWinOnColumns(currentState) || this.checkForWinOnDiagonals(currentState));
    }

    public boolean checkForWinOnRows(State currentState){
        for(int row = 0; row < currentState.board.length; row++) {
            int timesOfNodeRepeated = 0;
            String scanForElement = currentState.board[row][0];
            if (scanForElement == null) continue;
            for(int column = 1; column < currentState.board.length; column ++) {
                String nextString = currentState.board[row][column];
                if(nextString == null) break;
                else if (scanForElement.contentEquals(nextString) == false) break;
                else timesOfNodeRepeated++;
            }
            if(timesOfNodeRepeated == 3) return true;
        }
        return false;
    }

    public boolean checkForWinOnColumns(State currentState){
        for(int column = 0; column < currentState.board.length; column++) {
            int timesOfNodeRepeated = 0;
            String scanForElement = currentState.board[0][column];
            if (scanForElement == null) continue;
            for(int row = 1; row < currentState.board.length; row ++) {
                String nextString = currentState.board[row][column];
                if(nextString == null) break;
                else if (scanForElement.contentEquals(nextString) == false) break;
                else timesOfNodeRepeated++;
            }
            if(timesOfNodeRepeated == 3) return true;
        }
        return false;
    }

    public boolean checkForWinOnDiagonals(State currentState){
        String[][] aBoard = currentState.board;
        boolean isWinOnLeftDiagonal=false,isWinOnRightDiagonal=false;
        if(aBoard[0][0] != null && aBoard[1][1] != null && aBoard[2][2]!=null && aBoard[3][3]!=null) isWinOnLeftDiagonal=this.checkWinOnLeftDiagonal(currentState);
        if(aBoard[3][0] != null && aBoard[0][3] != null && aBoard[2][1]!=null && aBoard[1][2]!=null) isWinOnRightDiagonal= this.checkWinOnRightDiagonal(currentState);
        return isWinOnLeftDiagonal||isWinOnRightDiagonal;
    }

    public boolean checkWinOnLeftDiagonal(State currentState) {
        String[][] aBoard = currentState.board;
        return (aBoard[1][1].contentEquals(aBoard[0][0]) && aBoard[1][1].contentEquals(aBoard[2][2]) && aBoard[2][2].contentEquals(aBoard[3][3]));
    }

    public boolean checkWinOnRightDiagonal(State currentState) {
        String[][] aBoard = currentState.board;
        return (aBoard[3][0].contentEquals(aBoard[2][1]) && aBoard[2][1].contentEquals(aBoard[1][2]) && aBoard[1][2].contentEquals(aBoard[0][3]));
    }

    public boolean isEmptySquareOnBoard(String[][] board){
        boolean isEmptySquareOnBoard=false;
        int boardSize = board.length;
        for(int row = 0; row < boardSize; row++) {
            if(isEmptySquareOnBoard)break;
            for(int column = 0; column < boardSize; column++) {
                if(board[row][column] == null){
                    isEmptySquareOnBoard=true;
                    break;
                }
            }
        }
        return isEmptySquareOnBoard;
    }

    public ArrayList<int[]> scanAllEmptySquareOnBoard(State currentNode) {
        int boardSize = currentNode.board.length;
        ArrayList<int[]> anArrayList = new ArrayList<int[]>();
        for(int row = 0; row < boardSize; row++) {
            for(int column = 0; column < boardSize; column++) {
                if(currentNode.board[row][column] == null){
                    int[] arrayInput =new int[2];
                    arrayInput[0]=row;
                    arrayInput[1]=column;
                    anArrayList.add(arrayInput);
                }
            }
        }
        return anArrayList;
    }

    public String[][] updateBoard(State currentState, int[] emptySquareOnBoard) {
        String[][] newBoard = this.replicateBoard(currentState.board);
        newBoard[emptySquareOnBoard[0]][emptySquareOnBoard[1]] = currentState.nextPlayer;
        return newBoard;
    }

    public String[][] replicateBoard(String[][] board) {
        int boardSize = board.length;
        String[][] newBoard = new String[boardSize][boardSize];
        for(int row = 0; row < boardSize; row++) {
            for(int column = 0; column < boardSize; column++) newBoard[row][column] = board[row][column];
        }
        return newBoard;
    }

    public void computerMove(State currentState){
        isHumanTurn=false;
        State newState = this.initializeStateWithBoard(currentState.board);
        
        // تصفير العدادات قبل حركة الكمبيوتر
        nodesExplored = 0; 
        MAX_DEPTH_REACHED = 0;
        IS_CUTOFF_OCCURRED = false;
        
        long startTimeForThisMove = System.currentTimeMillis();
        newState = this.nextStateToMove(newState);
        long endTimeForThisMove = System.currentTimeMillis();
        LAST_MOVE_TIME = endTimeForThisMove - startTimeForThisMove;

        printStatistics();
        this.printUpdatedBoard(newState.board);
        if(this.checkForWin(newState) == true) JOptionPane.showMessageDialog (null, "Computer Won", "Game results", JOptionPane.INFORMATION_MESSAGE);
        else if(this.isTerminalNode(newState) == true) JOptionPane.showMessageDialog (null, "The game is draw", "Game results", JOptionPane.INFORMATION_MESSAGE);
        else {
            isHumanTurn=true;
            humanMoveState=newState;
        }
    }

    public State nextStateToMove(State currentState){
        State newState=new State();
        int numberOfEmptySquares=scanAllEmptySquareOnBoard(currentState).size();
        ArrayList<State> successors=new ArrayList<State>();

        if(numberOfEmptySquares==1){
            successors=this.getAllSuccessors(currentState);
            nodesExplored++;
            MAX_DEPTH_REACHED +=1;
            newState=successors.get(0);
        } else if(numberOfEmptySquares==16){
            successors=this.getAllSuccessors(currentState);
            Random r = new Random();
            nodesExplored++;
            MAX_DEPTH_REACHED +=1;
            newState=successors.get(r.nextInt(16));
        } else {
            MAX_DEPTH_REACHED=currentState.depth;
            if(DIFFICULTY_LEVEL.equals("Easy")) {
                successors=this.getAllSuccessors(currentState);
                Random r = new Random();
                nodesExplored++;
                newState = successors.get(r.nextInt(successors.size()));
            } else {
                currentState.heuristicValue=this.alphaBetaSearch(currentState, MIN_UTILITY, MAX_UTILITY);
                newState =(this.listOfPossibleNextMoves.size()>0)?this.getMaxNodeInList(this.listOfPossibleNextMoves):getAllSuccessors(currentState).get(0);
                this.listOfPossibleNextMoves.clear();
            }
        }
        return newState;
    }

    public State getMaxNodeInList(ArrayList<State>states) {
        State maxNode = states.get(0);
        for(int index = 0; index < states.size(); index++)
            if(maxNode.heuristicValue < states.get(index).heuristicValue) maxNode = states.get(index);
        return maxNode;
    }

    public int evaluationFunction(State state){
        int[] xArray={0,0,0,0};
        int[] oArray={0,0,0,0};
        int xCounter=0 , oCounter=0;
        IS_CUTOFF_OCCURRED =true;
        String[][]board=state.board;
        for(int row=0;row<board.length;row++){
            xCounter=0; oCounter=0;
            for(int column=0;column<board.length;column++){
                if(board[row][column]=="X") xCounter++;
                else if(board[row][column]=="O") oCounter++;
            }
            if(xCounter==0 && oCounter!=0) oArray[oCounter]+=1;
            if(xCounter!=0 && oCounter==0) xArray[xCounter]+=1;
        }
        for(int column=0;column<board.length;column++){
            xCounter=0; oCounter=0;
            for(int row=0;row<board.length;row++){
                if(board[column][row]=="X") xCounter++;
                else if(board[column][row]=="O") oCounter++;
            }
            if(xCounter==0 && oCounter!=0) oArray[oCounter]+=1;
            if(xCounter!=0 && oCounter==0) xArray[xCounter]+=1;
        }
        if(DIFFICULTY_LEVEL.equals("Hard")) state.heuristicValue = 6 * (xArray[2] - oArray[2]) + 3 * (xArray[1] - oArray[1]) + (xArray[0] - oArray[0]);
        else state.heuristicValue= 3*(oArray[0]+oArray[1]+oArray[2])-2*(xArray[0]+xArray[2]+xArray[1]);
        return state.heuristicValue;
    }

    public int maxValue(State state,int alpha,int beta, long time){
        nodesExplored++; // زيادة العداد مع كل استكشاف لعقدة جديدة
        
        if(MAX_DEPTH_REACHED < state.depth)MAX_DEPTH_REACHED=state.depth;
        if(isTerminalNode(state)) return this.evaluateHeuristicValue(state);
        long timeLimit=6000;
        if(DIFFICULTY_LEVEL.equals("Medium")) timeLimit=1000;
        if((System.currentTimeMillis()-time)>timeLimit || state.depth>DEPTH_CUTOFF_LEVEL) return this.evaluationFunction(state);
        
        state.heuristicValue=MIN_UTILITY;
        ArrayList<State> allSuccessors = this.getAllSuccessors(state);
        
        for(State successor : allSuccessors){
            state.heuristicValue=Math.max(state.heuristicValue,minValue(successor,alpha,beta,time));
            // التقليم معطل هنا للعمل كـ Minimax
        }
        if(state.depth == 1) listOfPossibleNextMoves.add(state);
        return state.heuristicValue;
    }

    public int minValue(State state,int alpha,int beta ,long time){
        nodesExplored++; // زيادة العداد مع كل استكشاف لعقدة جديدة
        
        if(MAX_DEPTH_REACHED < state.depth) MAX_DEPTH_REACHED=state.depth;
        if(isTerminalNode(state)) return this.evaluateHeuristicValue(state);
        long timeLimit=6000;
        if(DIFFICULTY_LEVEL.equals("Medium")) timeLimit=1000;
        if((System.currentTimeMillis()-time)>timeLimit || state.depth>DEPTH_CUTOFF_LEVEL) return evaluationFunction(state);
        
        state.heuristicValue=MAX_UTILITY;
        ArrayList<State> allSuccessors = this.getAllSuccessors(state);
        
        for(State successor : allSuccessors){
            state.heuristicValue=Math.min(state.heuristicValue,maxValue(successor,alpha,beta,time));
            // التقليم معطل هنا للعمل كـ Minimax
        }
        if(state.depth == 1) listOfPossibleNextMoves.add(state);
        return state.heuristicValue;
    }

    public int alphaBetaSearch(State currentNode, int alpha, int beta) {
        long start_time=System.currentTimeMillis();
        int result = this.maxValue(currentNode,alpha,beta,start_time);
        return result;
    }

    public ArrayList<State> getAllSuccessors(State currentState) {
        ArrayList<State> allSuccessors = new ArrayList<>();
        ArrayList<int[]> emptySquares = this.scanAllEmptySquareOnBoard(currentState);
        for(int[] pos : emptySquares) {
            allSuccessors.add(this.getSuccessor(currentState, pos));
        }
        return allSuccessors;
    }

    public State initializeStateWithBoard(String[][] board){
        State state = new State();
        state.board = board;
        state.nextPlayer = "X";
        return state;
    }

    public void printUpdatedBoard(String[][] board){
        System.out.println("Board :");
        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == null) buttons[i][j].setText("");
                else {
                    buttons[i][j].setText(board[i][j]);
                    buttons[i][j].setEnabled(false);
                }
            }
        }
    }

    public void printStatistics(){
        System.out.println("statistics :");
        if(IS_CUTOFF_OCCURRED) {
            System.out.println("Cutoff occurred");
        }
        System.out.println("Nodes Explored: " + nodesExplored);
        System.out.println("time spend: " + LAST_MOVE_TIME + " ms");
        System.out.println("--------------------------------");
    }

    public State initState(){ return new State(); }

    public static void main(String args[]){
        TicTacToeMinimax game =new TicTacToeMinimax();
        JFrame window = new JFrame("Tic-Tac-Toe: MINIMAX");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().add(game);
        window.setBounds(300,200,300,300);
        window.setVisible(true);
        game.startGame();
    }
}