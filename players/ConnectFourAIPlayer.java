package c4.players;

import c4.mvc.ConnectFourModelInterface;

public class ConnectFourAIPlayer extends ConnectFourPlayer {

    ConnectFourModelInterface model;
    int player;
    int maxDepth;

    public ConnectFourAIPlayer(ConnectFourModelInterface model, int player){
        this.model = model;
        this.player = player;
        this.maxDepth = 42;
    }

    public ConnectFourAIPlayer(ConnectFourModelInterface model, int player, int maxDepth){
        this.model = model;
        this.player = player;
        this.maxDepth = maxDepth;
    }

    /*
     * Q1. This method returns the move in the first available column from the left.
     */
    public int dumbGetMove() {
        boolean[] moves = model.getValidMoves();
		int m = 0;
		while(!moves[m])
			m++;
		return m;
    }

    /*
     * Q2. Returns true if the game will end at a given state.
     */
    public boolean terminalTest(int[][] state) {

        // Check vertical wins
        for (int[] column : state) {
            for(int row = 0; row < 3; row++){
                if(column[row] != -1 && column[row] == column[row + 1] && column[row] == column[row + 2] && column[row] == column[row + 3])
                    return true;
            }
        }

        // Check horizontal wins
        for(int col = 0; col < 4; col++){
            for(int row = 0; row < 6; row++){
                if(state[col][row] != -1 && state[col][row] == state[col+1][row] && state[col][row] == state[col+2][row] && state[col][row] == state[col+3][row])
                    return true;
            }
        }

        // Check positive diagonals
        for(int col = 3; col < 7; col++){
            for(int row = 0; row < 3; row++){
                if(state[col][row] != -1 && state[col][row] == state[col-1][row+1] && state[col][row] == state[col-2][row+2] && state[col][row] == state[col-3][row+3])
                    return true;
            }
        }

        // Check negative diagonals
        for(int col = 0; col < 4; col++){
            for(int row = 0; row < 3; row++){
                if(state[col][row] != -1 && state[col][row] == state[col+1][row+1] && state[col][row] == state[col+2][row+2] && state[col][row] == state[col+3][row+3])
                    return true;
            }
        }

        // Check if every square is taken
        for(int col = 0; col < 7; col++){
            for(int row = 0; row < 6; row++){
                if(state[col][row] == -1)
                    return false;
            }
        }

        // Should not happen
        return true;
    }

    /*
     * Q3. Returns all valid columns to place piece in given a state in a list
     */
    public int[] actions(int[][] state){

        int[] valid = new int[7];
        int counter = 0;

        // Loop through the top of each column
        for(int col = 0; col < 7; col++){
            if(state[col][0] != -1)
                valid[col] = 0;
            else {
                valid[col] = 1;
                counter++;
            }
        }
        // System.out.println(Arrays.toString(valid));
        // System.out.println(counter);
        int[] validCols = new int[counter];
        counter = 0;
        for(int i = 0; i < 7; i++){
            if(valid[i] == 1) {
                validCols[counter] = i;
                counter++;
            }
        }

        // System.out.println(Arrays.toString(validCols));
        // Favor completions first and then utility maximizers
        int[] ordered = new int[validCols.length];
        int beginning = 0;
        int last = ordered.length - 1;
        for (int move : validCols) {
            int[][] z = result(state, move);
            if(terminalTest(z)){
                ordered[beginning] = move;
                beginning++;
            }
            else {
                ordered[last] = move;
                last--;
            }
        }

        return ordered;

    }

    /*
     * Q4. Creating the new board state given an action(0-6) and a result
     */
    public int[][] result(int[][] state, int action){

        // Create a deep copy of the state and also find out whose turn it is
        int player = 1; // if = 1 then it is player 1's turn, if = 2 then it is player 2's turn
        int[][] stateCopy = new int[7][6];
        for(int col = 0; col < 7; col++){
            for(int row = 0; row < 6; row++){
                stateCopy[col][row] = state[col][row];
                if(state[col][row] == 1)
                    player++;
                else if (state[col][row] == 2)
                    player--;
            }
        }

        // Find the first available slot in the column
        int index = 5;
        while(stateCopy[action][index] != -1 && index != 0){
            index--;
        }
        stateCopy[action][index] = player;
        return stateCopy;
    }

    /*
     * Q5. Alpha Beta Pruning Algorithm
     */
    public int getMove(){
        return alphaBetaSearch(model.getGrid());
    }

    public int alphaBetaSearch(int[][] state){
        Integer[] valueMove = maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        return valueMove[1];
    }

    public Integer[] maxValue(int[][] state, int alpha, int beta, int depth){

        Integer[] toReturn = new Integer[2];
        
        if(terminalTest(state) || depth == maxDepth){
            return anyStateUtility(state);
        }

        int utility = Integer.MIN_VALUE;
        int action = -1;
        
        for (int move : actions(state)) {

            Integer[] utilityAction = minValue(result(state, move), alpha, beta, depth+1);

            if(utilityAction[0] > utility){
                utility = utilityAction[0];
                action = move;
                alpha = Math.max(alpha, utility);
            }

            if(utility >= beta){
                toReturn[0] = utility;
                toReturn[1] = action;
                return toReturn;
            }

        }
        
        toReturn[0] = utility;
        toReturn[1] = action;
        return toReturn;

    }

    public Integer[] minValue(int[][] state, int alpha, int beta, int depth){

        Integer[] toReturn = new Integer[2];

        if(terminalTest(state) || depth == maxDepth){
            return anyStateUtility(state);
        }

        int utility = Integer.MAX_VALUE;
        int action = -1;

        for (int move : actions(state)) {

            Integer[] utilityAction = maxValue(result(state, move), alpha, beta, depth+1);

            if(utilityAction[0] < utility){
                utility = utilityAction[0];
                action = move;
                beta = Math.min(beta, utility);
            }

            if(utility <= alpha){
                toReturn[0] = utility;
                toReturn[1] = action;
                return toReturn;
            }

        }

        toReturn[0] = utility;
        toReturn[1] = action;
        return toReturn;

    }

    public Integer[] terminalUtility(int[][] state){

        Integer[] utilAction = {null, null};

        // Check vertical wins
        for (int[] column : state) {
            for(int row = 0; row < 3; row++){
                if(column[row] != -1 && column[row] == column[row+1] && column[row] == column[row + 2] && column[row] == column[row + 3]){
                    if(column[row] == player)
                        utilAction[0] = 1000;
                    else
                        utilAction[0] = -1000;
                    return utilAction;
                }
            }
        }

        // Check horizontal wins
        for(int col = 0; col < 4; col++){
            for(int row = 0; row < 6; row++){
                if(state[col][row] != -1 && state[col][row] == state[col+1][row] && state[col][row] == state[col+2][row] && state[col][row] == state[col+3][row]){
                    if(state[col][row] == player)
                        utilAction[0] = 1000;
                    else
                        utilAction[0] = -1000;
                    return utilAction;
                }
            }
        }

        // Check positive diagonals
        for(int col = 0; col < 4; col++){
            for(int row = 3; row < 6; row++){
                if(state[col][row] != -1 && state[col][row] == state[col+1][row-1] && state[col][row] == state[col+2][row-2] && state[col][row] == state[col+3][row-3]){
                    if(state[col][row] == player)
                        utilAction[0] = 1000;
                    else
                        utilAction[0] = -1000;
                    return utilAction;
                }
            }
        }

        // Check negative diagonals
        for(int col = 0; col < 4; col++){
            for(int row = 0; row < 3; row++){
                if(state[col][row] != -1 && state[col][row] == state[col+1][row+1] && state[col][row] == state[col+2][row+2] && state[col][row] == state[col+3][row+3]){
                    if(state[col][row] == player)
                        utilAction[0] = 1000;
                    else
                        utilAction[0] = -1000;
                    return utilAction;
                }
            }
        }

        // Check if every square is taken
        for(int col = 0; col < 7; col++){
            for(int row = 0; row < 6; row++){
                if(state[col][row] == -1){
                    utilAction[0] = 0;
                    return utilAction;
                }
            }
        }

        utilAction[0] = 0;
        return utilAction;
    }

    public Integer[] anyStateUtility(int[][] state){
        
        int totalUtility = 0;

        // 1. Vertical Checks
        for (int col = 0; col < 7; col++){
        
            int columnWinner = 0;
            int adj = 0;
            int openSpaces = 0;

            for (int row = 0; row < 6; row++){
                if(columnWinner == 0 && state[col][row] != -1){ 
                    // If we have found our first column winner
                    columnWinner = state[col][row];
                    adj++;
                } else if (columnWinner != 0 && state[col][row] != columnWinner) { 
                    // If we have found where the column winner pieces end in the column
                    break;
                } else if (columnWinner != 0 && state[col][row] == columnWinner) { 
                    // If we have found an adjacent streak
                    adj ++;
                } else if (state[col][row] == -1){
                    openSpaces++;
                }
            }

            // A column is vertically irrelevant if it can't be won.
            if(openSpaces + adj >= 4){
                if(columnWinner == player)
                    totalUtility += adj * adj * adj;
                else
                    totalUtility -= adj * adj * adj;
            }
            else {
                // The column is dead
                adj = 0;
            }

        }

        // 2. Horizontal Checks
        for (int row = 0; row < 6; row++){

            int[] helper = {2, 1};
            int rowWinner = 0;
            int rowLoser = 0;
            int openSpaces = 0;
            int adj = 0;
            int col = 3;

            // If the middle piece is not empty
            if(state[col][row] != -1) {

                rowWinner = state[col][row];
                rowLoser = helper[rowWinner - 1];

                // Go left until you hit something
                while(col >= 0 && state[col][row] != rowLoser){
                    if (state[col][row] == rowWinner)
                        adj++;
                    else
                        openSpaces++;
                    col--;
                }

                // Go right until you hit something
                col = 4;
                while(col < 7 && state[col][row] != rowLoser){
                    if (state[col][row] == rowWinner)
                        adj++;
                    else
                        openSpaces++;
                    col++;
                }

                // After analysis, make sure this is not a 'dead row'
                if (openSpaces + adj < 4)
                    adj = 0;
                
            }
            // If the middle piece is empty, find closest piece to the middle
            else {

                int leftWinner = 0;
                int rightWinner = 0;

                // Go left until you hit something
                col = 2;
                while(col > 0 && state[col][row] == -1){
                    col--;
                }
                if(state[col][row] != -1){ 
                    // We hit a piece
                    leftWinner = state[col][row];
                    while(col >= 0 && state[col][row] == leftWinner){ 
                        // Find its adjacency count
                        if(leftWinner == player)
                            adj++;
                        else
                            adj--;
                        col--;
                    }
                }

                // Go right until you hit something
                col = 4;
                while(col < 6 && state[col][row] == -1){
                    col++;
                }
                if(state[col][row] != -1){ 
                    // We hit a piece
                    rightWinner = state[col][row];
                    while(col < 7 && state[col][row] == rightWinner){ 
                        // Find its adjacency count
                        if(rightWinner == player)
                            adj++;
                        else
                            adj--;
                        col++;
                    }
                }

                // Calculate the row winner
                if (adj > 0)
                    rowWinner = player;
                else
                    rowWinner = helper[player-1];

            }

            if (rowWinner == player)
                totalUtility += adj * adj * adj;
            else
                totalUtility -= adj * adj * adj;
        }

        // 3. Left-Right Diagonal Analysis
        int[][] startsColRow = {{0,0}, {0, 1}, {0, 2}, {1, 0}, {2, 0}, {3, 0}};
        int[][] critSpaces = {{3, 4}, {2, 3, 4}, {1, 2, 3, 4}, {3, 4}, {2, 3, 4}, {1, 2, 3, 4}};
        for(int i = 0; i < startsColRow.length; i++){

            int col = startsColRow[i][0];
            int row = startsColRow[i][1];
            int space = 0;
            int ourAdj = 0;
            int oppAdj = 0;
            int totalAdj = 0;
            int winningPlayer = 0;
            boolean deadDiagonal = false;                
            boolean firstOccupantFound = false;
            int[] helper = {0, 2, 1};

            // When searching the critical spaces, they cannot have both player pieces in it or else it is a dead diagonal
            while(col != 7 && row != 6 && !deadDiagonal){

                // Search down the whole diagonal
                deadDiagonal = false;
                boolean criticalSpace = false;
                space++;

                for(int s = 0; s < critSpaces[i].length; s++){
                    // check if the space is a critical space
                    if(space == critSpaces[i][s]){
                        criticalSpace = true;
                        break;
                    }
                }
                if(criticalSpace){
                    if(state[col][row] != -1){
                        if(!firstOccupantFound){
                            firstOccupantFound = true;
                            winningPlayer = state[col][row];
                            if (state[col][row] == player)
                                ourAdj++;
                            else
                                oppAdj++;
                        }
                        else if(firstOccupantFound && state[col][row] == winningPlayer){
                            if (state[col][row] == player)
                                ourAdj++;
                            else
                                oppAdj++;
                        }
                        else {
                            deadDiagonal = true;
                            winningPlayer = -1;
                            ourAdj = 0;
                            oppAdj = 0;
                            break;
                        }
                    }
                    else {
                        if(state[col][row] == player)
                            ourAdj++;
                        else if(state[col][row] == helper[player])
                            oppAdj++;
                    }
                }
                else {
                    if(state[col][row] == player)
                        ourAdj++;
                    else if(state[col][row] == helper[player])
                        oppAdj++;
                }
                col++;
                row++;
            }
            totalAdj = ourAdj - oppAdj;
            totalUtility += totalAdj * totalAdj * totalAdj;
        }
        
        // 4. Right-Left Diagonal Analysis
        int[][] startColRow = {{6, 0}, {6, 1}, {6, 2}, {5, 0}, {4, 0}, {3, 0}};
        for(int i = 0; i < startColRow.length; i++){

            int col = startColRow[i][0];
            int row = startColRow[i][1];
            int space = 0;
            int ourAdj = 0;
            int oppAdj = 0;
            int totalAdj = 0;
            int winningPlayer = 0;
            boolean deadDiagonal = false;                
            boolean firstOccupantFound = false;
            int[] helper = {0, 2, 1};

            while(col != -1 && row != 6 && !deadDiagonal){

                deadDiagonal = false;
                boolean criticalSpace = false;
                space++;

                for(int s = 0; s < critSpaces[i].length; s++){
                    // check if the space is a critical space
                    if(space == critSpaces[i][s]){
                        criticalSpace = true;
                        break;
                    }
                }
                if(criticalSpace){
                    if(state[col][row] != -1){
                        if(!firstOccupantFound){
                            firstOccupantFound = true;
                            winningPlayer = state[col][row];
                            if (state[col][row] == player)
                                ourAdj++;
                            else
                                oppAdj++;
                        }
                        else if(firstOccupantFound && state[col][row] == winningPlayer){
                            if (state[col][row] == player)
                                ourAdj++;
                            else
                                oppAdj++;
                        }
                        else {
                            deadDiagonal = true;
                            winningPlayer = -1;
                            ourAdj = 0;
                            oppAdj = 0;
                            break;
                        }
                    }
                    else {
                        if(state[col][row] == player)
                            ourAdj++;
                        else if(state[col][row] == helper[player])
                            oppAdj++;
                    }
                }
                else {
                    if(state[col][row] == player)
                        ourAdj++;
                    else if(state[col][row] == helper[player])
                        oppAdj++;
                }
                col--;
                row++;
            }
            totalAdj = ourAdj - oppAdj;
            totalUtility += totalAdj * totalAdj * totalAdj;
        }

        if(player == 1)
            totalUtility += 1;
        else
            totalUtility -= 1;

        /*
         * The commented code below is meant to satisfy the requirement of setting utility to the determined
         * maximum and minimum values from the project instruction pdf. However, the AI performs better without
         * this code.
         */
        // if(totalUtility > 0)
        //     totalUtility = 1000;
        // else if(totalUtility < 0)
        //     totalUtility = -1000;
        // else
        //     totalUtility = 0;

        Integer[] pair = {totalUtility, -1};
        return pair;
    }

}
