package game; // تأكد أن اسم الحزمة يطابق ما اخترته

public class State {
    public String[][] board = new String[4][4];
    public String nextPlayer;
    public int heuristicValue;
    public int depth;

    public State() {
        this.depth = 0;
    }

    public State(String[][] board, State previousState, String nextPlayer, int heuristicValue, int depth) {
        this.board = board;
        this.nextPlayer = nextPlayer;
        this.heuristicValue = heuristicValue;
        this.depth = depth;
    }
}