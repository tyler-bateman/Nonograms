/** Author: Tyler Bateman
  * Date: 27 March 2019
  *
  * This program solves nonograms :)
  */

  import java.io.File;
  import java.io.FileNotFoundException;
  import java.util.Scanner;
  import java.util.Arrays;
  import java.util.Stack;
  import java.util.HashSet;
  import java.util.Iterator;

  public class NonogramSolver{
    public static char fillChar = (char)9632;
    public static char xChar = '-';
    static double startTime;
    public static void main (String[] args) throws FileNotFoundException {
      startTime = System.nanoTime();

      String[] rowClues = {"5", "2,2", "1,1", "2,2", "5"};
      String[] colClues = {"5", "2,2", "1,1", "2,2", "5"};
      File input;

      if(args.length > 0) {
        input = new File(args[0]);
        Scanner sc = new Scanner(input);
        int rows = sc.nextInt();
        int cols = sc.nextInt();
        rowClues = new String[rows];
        colClues = new String[cols];
        for(int i = 0; i < rows; i++) {
          rowClues[i] = sc.next();
        }

        for(int i = 0; i < cols; i++) {
          colClues[i] = sc.next();
        }
      }


      Nonogram nonogram = new Nonogram(colClues, rowClues);
      nonogram.printPicture();
      System.out.println("Solving...");
      nonogram.solve();
    }


    static class Nonogram{

      private int[][] rows;
      private int[][] columns;

      private char[][] picture;
      private boolean[][] certainty;

      /** Constructor */
      public Nonogram(String[] columns, String[] rows) {
        this.picture = new char[rows.length][columns.length];
        for(int i = 0; i < picture.length; i++) {
          for(int j = 0; j < picture[i].length; j++) {
            picture[i][j] = ' ';
          }
        }
        this.certainty = new boolean[rows.length][columns.length];

        this.rows = parseClues(rows);
        this.columns = parseClues(columns);

      }

      /** Parses the clues given in the text files into usable arrays */
      private int[][] parseClues(String[] clues) {
        int[][] clueMatrix = new int[clues.length][];

        for(int i = 0; i < clues.length; i++) {
          int numClues = 0;
          if(!clues[i].isEmpty()) {
            numClues = 1;
            for(int j = 0; j < clues[i].length(); j++) {
              if(clues[i].charAt(j) == ',') {
                numClues++;
              }
            }
          }
          clueMatrix[i] = new int[numClues];

          int count = 0;
          int curInt = 0;
          for(int j = 0; j < clues[i].length(); j++) {
            if(clues[i].charAt(j) == ',') {
              clueMatrix[i][count] = curInt;
              curInt = 0;
              count++;
            } else if(Character.isDigit(clues[i].charAt(j))){
              curInt = curInt * 10 + Integer.parseInt("" + clues[i].charAt(j));
            }
          }
          clueMatrix[i][count] = curInt;
        }
        return clueMatrix;
      }

      /** fills the given cell in the array */
      public void fill(int row, int col) {
        picture[row][col] = fillChar;
      }

      /** cross out the given cell in the array */
      public void cross(int row, int col) {
        picture[row][col] = xChar;
      }

      /** Clear the given cell in the array */
      public void clear(int row, int col) {
        picture[row][col] = ' ';
      }

      /** Fills in the given cell and pushes the coordinates onto fillStack if
        * the cell is currently empty.
        *
        * If filling it results in the line being filled completely, crosses
        * out the remaining cells
        *
        * if the cell is already crossed out, or filling it would overfill a line,
        *  return false. Otherwise return true
        */
      public boolean cursoryFill(Stack<int[]> fillStack, int row, int col) {
        if(picture[row][col] == ' ') {
          fill(row, col);
          int[] coord = {row, col};
          fillStack.push(coord);

          int fillCount = 0;
          for(int i = 0; i < picture[row].length; i++) {
            if(picture[row][i] == fillChar) {
              fillCount++;
            }
          }
          int totalFilled = 0;
          for(int i = 0; i < rows[row].length; i++) {
            totalFilled += rows[row][i];
          }
          if(fillCount == totalFilled) {
          //if the line is completely filled, cross out the rest of the spaces
            for(int i = 0; i < picture[row].length; i++) {
              if(!cursoryCross(fillStack, row, col)) {
                return false;
              }
            }
          } else if(fillCount > totalFilled){
            return false;
          }
        } else if(picture[row][col] == xChar) {
          return false;
        }
        return true;
      }

      /** Crosses out the given cell and pushes the coordinates onto fillStack
        * if the cell is currently empty.
        *
        * if the cell is already filled, return false. Otherwise return true.
        */
      public boolean cursoryCross(Stack<int[]> fillStack, int row, int col) {
        if(picture[row][col] == ' ') {
          cross(row, col);
          int[] coord = {row, col};
          fillStack.push(coord);
        } else if(picture[row][col] == xChar) {
          return false;
        }
        return true;
      }

      /*Solves the puzzle by populating and printing the picture array*/
      public void solve() {
        int totalRowFilled = 0;
        for(int i = 0; i < rows.length; i++) {
          for(int j = 0; j < rows[i].length; j++) {
            totalRowFilled += rows[i][j];
          }
        }
        int totalColFilled = 0;
        for(int i = 0; i < columns.length; i++) {
          for(int j = 0; j < columns[i].length; j++) {
            totalColFilled += columns[i][j];
          }
        }
        if(totalRowFilled == totalColFilled) {
          fillObvious();
          printPicture();
          stackSolve();
          printPicture();
          System.out.println("Solved!");
        } else {
          System.out.println("No solution (Total filled mismatch)");
        }

      }

      /** Fills in spots that can be easily determined by counting through
        * individual rows and columns */
      private void fillObvious() {
        //Fill cells by row
        for(int i = 0; i < picture.length; i++) {
          int usedSpace = rows[i].length - 1; //The amount of space used when the clues for this row are compressed as much as possible
          for(int j = 0; j < rows[i].length; j++) {
            usedSpace += rows[i][j];
          }

          int extraSpace = picture[i].length - usedSpace;
          int blockEnd = extraSpace; //The end of the block being partially filled
          for(int j = 0; j < rows[i].length; j++) {
            for(int k = blockEnd; k < blockEnd + rows[i][j] - extraSpace; k++) {
              fill(i, k);
              certainty[i][k] = true;
            }
            if(blockEnd != 0 && extraSpace == 0) {
              cross(i, blockEnd - 1);
              certainty[i][blockEnd - 1] = true;
            }
            blockEnd += rows[i][j] + 1;
          }
        }

        //Fill cells by column
        for(int i = 0; i < picture[0].length; i++) {
          int usedSpace = columns[i].length - 1; //The amount of space used when the clues for this column are compressed as much as possible
          for(int j = 0; j < columns[i].length; j++) {
            usedSpace += columns[i][j];
          }

          int extraSpace = picture.length - usedSpace;
          int blockEnd = extraSpace; //The end of the block being partially filled
          for(int j = 0; j < columns[i].length; j++) {
            for(int k = blockEnd; k < blockEnd + columns[i][j] - extraSpace; k++) {
              fill(k, i);
              certainty[k][i] = true;
            }
            if(blockEnd != 0 && extraSpace == 0){
              cross(blockEnd - 1, i);
              certainty[blockEnd - 1][i] = true;
            }
            blockEnd += columns[i][j] + 1;
          }
        }
      }

      /* Solves the puzzle with a stack implementation of backtracking
       * Returns true if a solution is found, otherwise returns false
       */
      private boolean stackSolve() {
        Stack<int[]> guessStack = new Stack<int[]>();
        Stack<int[]> fillStack = new Stack<int[]>();
        int row = 0;
        int col = 0;
        int counter = 0;
        main:while(row < picture.length) {
          System.out.println(Arrays.deepToString(guessStack.toArray()));
          counter++;
          //Skips over filled cells
          while(picture[row][col] != ' ') {
            if(col == picture[0].length - 1) {
              row ++;
              col = 0;
              if(row >= picture.length) {
                break main;
              }
            } else {
              System.out.println("Already filled...");
              col++;
            }
          }
          //Fill in the first empty cell
          cursoryFill(fillStack, row, col);
          int[] guess = {row, col};
          guessStack.push(guess);

          //Fill in cells made obvious by the guess
          boolean successRight = propagateRight(fillStack, row, col);
          boolean successDown = propagateDown(fillStack, row, col);
          boolean noIncorrectLines = guessStack.isEmpty() || validateGuess(guess, fillStack);
          if(successRight && successDown && noIncorrectLines) {
          //If the guess didn't result in a contradiction, move on
          System.out.println("Successful fill");
          printPicture();
            if(col == picture[0].length - 1) {
              row ++;
              col = 0;
            } else {
              col++;
            }
          } else {
          //If the guess resulted in a contradiction, revert all changes since the guess
            printPicture();
            System.out.println("unsuccessful fill");
            int[] cur = fillStack.pop();
            while(!Arrays.equals(guess, cur)) {
              clear(cur[0], cur[1]);
              cur = fillStack.pop();
            }
            clear(row, col);
            //Cross out the cell instead

            printPicture();
            cursoryCross(fillStack, row, col);
            //Fill in cells made obvious by guess
            successRight = fillKnownSpacesH(fillStack, row, col);
            successDown = fillKnownSpacesV(fillStack, row, col);
            noIncorrectLines = validateGuess(guess, fillStack);

            System.out.println(successRight + " " + successDown + " " + noIncorrectLines);
            if(successRight && successDown && noIncorrectLines) {
              System.out.println("Successful cross");
              printPicture();
            //If the guess didn't result in a contradiction, move on
              if(col == picture[0].length - 1) {
                row++;
                col = 0;
              } else {
                col++;
              }
            } else {
              System.out.println("Unsuccessful cross");
              System.out.println("Block: " + getBlock(true, row, col));
              printPicture();
            // If the guess resulted in a contradiction, revert all changes since
            // the last unexhasted uncertain cell
              if(guessStack.isEmpty()) {
                System.out.println("A");
                return false;
              }
              int[] prevGuess = guessStack.pop();
              while(picture[prevGuess[0]][prevGuess[1]] == xChar) {
                if(guessStack.isEmpty()) {
                  System.out.println("B");
                  return false;
                }
                prevGuess = guessStack.pop();
              }
              row = prevGuess[0];
              col = prevGuess[1];
              cur = fillStack.pop();
              while(!Arrays.equals(cur, prevGuess)) {
                clear(cur[0], cur[1]);
                cur = fillStack.pop();
              }
            }
          }
          System.out.println(row + ", " + col);
        }
        return true;
      }

      /** Fills out the cells that can be easily solved in a row
        * Returns true if successful, otherwise false
        * Pre: picture[row][0..col] != ' '
               picture[row][col] == xChar
               col < picture[0].length - 1
        * Post: the coordinates of all modified cells are pushed to fillStack
        */
      private boolean fillKnownSpacesH(Stack<int[]> fillStack, int row, int col) {//TODO: Has bug
        int startBlock = getBlock(true, row, col);
        if(startBlock == - 1) return true;
        int usedSpace = rows[row].length - startBlock - 1; //The amount of space used when the clues for this row are compressed as much as possible
        for(int i = startBlock; i < rows[row].length; i++) {
          usedSpace += rows[row][i];
        }
        System.out.println("Used space: " + usedSpace);
        int extraSpace = picture[row].length - col - usedSpace;
        int blockEnd = col + extraSpace + 1; // The end of the block being worked on
        for(int i = startBlock; i < rows[row].length; i++) {
          for(int j = blockEnd; j < blockEnd + rows[row][i] - extraSpace; j++) {
            if(j >= picture[row].length || picture[row][j] == xChar) {
              return false;
            }
            cursoryFill(fillStack, row, j);
            if(!propagateDown(fillStack, row, col)) {
              return false;
            }
          }
          if(blockEnd != 0 && extraSpace == 0) {
            if(picture[row][blockEnd - 1] == fillChar){
              return false;
            }
            if(picture[row][blockEnd - 1] == ' '){
              cross (row, blockEnd - 1);
              int[] coord = {row, blockEnd - 1};
              fillStack.push(coord);
            }
            cursoryCross(fillStack, row, blockEnd - 1);
          }
          blockEnd += rows[row][i] + 1;
        }
        return true;
      }

      /** Fills out the cells thac can be easily solved in a column
        * Returns true if successful, otherwise false
        * Pre: picture[0..row][col] != ' '
        *      picture[row][col] == xChar
        *      row < picture.length - 1
        * Post: The coordinates of all modified cells are pushed to fillStack
        */
      private boolean fillKnownSpacesV(Stack<int[]> fillStack, int row, int col) {
        int startBlock = getBlock(false, row, col);
        if(startBlock == -1) return true;
        int usedSpace = rows[row].length - startBlock - 1; //The amount of space used when the clues for this column are compressed as much as possible
        for(int i = startBlock; i < columns[col].length; i++) {
          usedSpace += columns[col][i];
        }
        int extraSpace = picture.length - row - usedSpace;
        int blockEnd = row + extraSpace + 1;
        for(int i = startBlock; i < columns[col].length; i++) {
          for(int j = blockEnd; j < blockEnd + columns[col][i] - extraSpace; j++) {
            if(j >= picture.length || picture[j][col] == xChar) {
              return false;
            }
            cursoryFill(fillStack, j, col);
          }
          if(blockEnd != 0 && extraSpace == 0) {
            if(picture[blockEnd - 1][col] == fillChar) {
              return false;
            }
            cursoryCross(fillStack, blockEnd - 1, col);
            if(!propagateRight(fillStack, row, col)) {
              return false;
            }
          }
          blockEnd += columns[col][i] + 1;
        }
        return true;
      }

      /** Fills in the immediate known filled spaces right of a filled space
        * Returns false if it results in a contradiction, otherwise returns true
        * Pre: picture[row][col] == fillChar
        */
      private boolean propagateRight(Stack<int[]> fillStack, int row, int col) {
        int block = getBlock(true, row, col);
        //System.out.println(row + " " + col);
        if(block != -1) {
          int blockSize = rows[row][block];
          //Find the start of the block being propagated
          int blockStart = col;
          while(blockStart > 0 && picture[row][blockStart - 1] != xChar) {
            blockStart--;
          }

          //Fill in the known portion of the block
          for(int i = col; i < blockStart + blockSize; i++) {
            if(!cursoryFill(fillStack, row, i)) {
              return false;
            }
          }
          //If the whole block is filled put an x on the end
          if(blockStart == col && col + blockSize < picture[row].length) {
            if(!cursoryCross(fillStack, row, col + blockSize)){
              return false;
            }
          }
          //propagate all the filled in blocks
          for(int i = col + 1; i < blockStart + blockSize - 1; i++) {
            if(!propagateDown(fillStack, row, i)) {
              return false;
            }
          }
        }
        return true;
      }

      /** Fills in the immediately known filled below a filled space
        * Returns false if it results in a contradiction, otherwise returns true
        * Pre: picture[row][col] == fillChar
        */
      private boolean propagateDown(Stack<int[]> fillStack, int row, int col) {
        int block = getBlock(false, row, col);
        if(block != -1) {
          int blockSize = columns[col][block];
          //Find the start of the block being propagated
          int blockStart = row;
          while(blockStart > 0 && picture[col][blockStart - 1] != xChar) {
            blockStart--;
          }
          //Fill in the known portion of the block
          for(int i = row; i < blockStart + blockSize; i++) {
            if(!cursoryFill(fillStack, i, col)) {
              return false;
            }
          }
          //If the whole block is filled put an x on the end
          if(blockStart == col && row + blockSize < picture.length) {
            if(!cursoryCross(fillStack, row + blockSize, col)) {
              return false;
            }
          }
          //propagate all the filled in blocks
          for(int i = row + 1; i < blockStart + blockSize ; i++) {
            if(!propagateRight(fillStack, i, col)) {
              return false;
            }
          }
        }
        return true;
      }

      /* Returns the block being worked on at a given space
       * If the block is unknown, returns -1
       * @param orientation: true for horiziontal, false for vertical
       * @param row: the row of the cell in question
       * @param col: the column of the cell in question
       * Precondition: picture[row][col] == fillChar
       */
      private int getBlock(boolean orientation, int row, int col) {
        int block = 0;
        if(!orientation && row == 3 && col == 0) {
          printPicture();
        }
        if(orientation) { //Horizontal
          for(int i = 0; i < col; i++) {
            //Handles the ambiguous case where there is an unknown space
            if(picture[row][i] == ' ') {
              int blockStart = i;
              while(blockStart > 0 && picture[row][blockStart - 1] != xChar) {
                blockStart--;
              }
              if(col - blockStart <= rows[row][block] + 1) {
              //The cell must be part of the current block
                return block;
              } else if(block != rows[row].length - 1 && picture[row][blockStart] == fillChar && (col - blockStart) < (rows[row][block] + rows[row][block + 1] + 2)) {
              //The cell must be part of the next block
                return block + 1;
              } else {
              //The block cannot be (easily) determined
                return - 1;
              }
            } else {
              //Counts the blocks leading up to the cell in question
              if(i > 0 && picture[row][i] == xChar && picture[row][i - 1] == fillChar) {
                block ++;
              }
            }
          }
          return block;
        } else { //Vertical
          for(int i = 0; i < row; i++) {
            //Handles the ambiguous case where there is an unknown space
            if(picture[i][col] == ' ') {
              int blockStart = i;
              while(blockStart > 0 && picture[blockStart - 1][col] != xChar) {
                blockStart--;
              }
              if(col - blockStart <= columns[col][block] + 1) {
                //The cell must be part of the current block
                return block;
              } else if(block != columns[col].length - 1 && picture[blockStart][col] == fillChar && (row - blockStart) < (columns[col][block] + columns[col][block + 1] + 2)){
              //The cell must be part of the next block
                return block + 1;
              } else {
              //The block cannot be (easily) determined
                return -1;
              }
            } else {
              //Counts the blocks leading up to the cell in question
              if(i > 0 && picture[i][col] == xChar && picture[i - 1][col] == fillChar) {
                block++;
              }
            }
          }
        }
        return block;
      }

      /** Validates a row
        * returns true if the row is either incomplete or correct
        *   otherwise returns false
        */
      public boolean validateRow(int row) {
        int block = 0;
        int blockLength = 0;
        boolean isFilled = false;
        for(int i = 0; i < picture[0].length; i++) {
          if(picture[row][i] == ' ') {
            return true;
          } else if(picture[row][i] == fillChar) {
            if(isFilled) {
              blockLength ++;
            } else {
              blockLength = 1;
              block++;
            }
            isFilled = true;
          } else {
            isFilled = false;
            if(block >= rows[row].length || blockLength != rows[row][block]) {
              return false;
            }
          }
        }
        return block == rows[row].length - 1 && blockLength == rows[row][rows[row].length - 1];
      }

      /** Validates a column
        * returns true if the column is either incomplete of correct
        *   Otherwise returns false
        */
      public boolean validateCol(int col) {
        int block = 0;
        int blockLength = 0;
        boolean isFilled = false;
        for(int i = 0; i < picture.length; i++) {
          if(picture[i][col] == ' ') {
            return true;
          } else if(picture[i][col] == fillChar) {
            if(isFilled) {
              blockLength++;
            } else {
              blockLength = 1;
              block++;
            }
            isFilled = true;
          } else {
            isFilled = false;
            if(blockLength != columns[col][block]) {
              return false;
            }
          }
        }
        return block == columns[col].length - 1 && blockLength == columns[col][columns[col].length - 1];
      }

      /** Validates a guess
        * Returns true if all rows and columns modified by the guess are either
        *     incomplete or valid
        * Precondition: fillStack contains guess
        * Post: fillStack is unchanged
        */
      public boolean validateGuess(int[] guess, Stack<int[]> fillStack) {
        Stack<int[]> tmp = new Stack<int[]>();
        HashSet<Integer> modifiedRows = new HashSet<Integer>();
        HashSet<Integer> modifiedCols = new HashSet<Integer>();

        //populates sets with the indices of the modified rows and columns
        int[] cur = {0, 0};
        while(!Arrays.equals(cur, guess) && !fillStack.isEmpty()) {
          cur = fillStack.pop();
          tmp.add(cur);
          modifiedRows.add(cur[0]);
          modifiedCols.add(cur[1]);
        }
        while(!tmp.isEmpty()) {
          fillStack.push(tmp.pop());
        }
        //Validates all modified rows
        Iterator<Integer> rowIterator = modifiedRows.iterator();
        while(rowIterator.hasNext()) {
          if(!validateRow(rowIterator.next())) {
            return false;
          }
        }

        Iterator<Integer> colIterator = modifiedCols.iterator();
        while(colIterator.hasNext()) {
          if(!validateCol(colIterator.next())) {
            return false;
          }
        }
        return true;
      }
      /** Prints the puzzle including the clues and the contents of the picture
        * array */
      public void printPicture() {

        int maxRowClues = maxLength(rows);
        int maxColClues = maxLength(columns);
        for(int i = 0; i < maxColClues; i++) {
          for(int j = 0; j < maxRowClues; j++) {
            System.out.print("   ");
          }
          for(int j = 0; j < columns.length; j++) {
            if(columns[j].length >= maxColClues - i) {
              int clue = columns[j][i - (maxColClues - columns[j].length)];
              System.out.print(clue);
              if(clue < 10) {
                System.out.print(" ");
              }
            } else {
              System.out.print("  ");
            }
          }
          System.out.println();
        }

        for(int i = 0; i < rows.length; i++) {
          for(int j = 0; j < maxRowClues; j++) {
            if(rows[i].length >= maxRowClues - j) {
              int clue = rows[i][j - (maxRowClues - rows[i].length)];
              System.out.print(clue);
              if(clue >= 10) {
                System.out.print(" ");
              } else {
                System.out.print("  ");
              }
            } else {
              System.out.print("   ");
            }

          }
          for(int j = 0; j < picture[i].length; j++) {
            System.out.print(picture[i][j] + " ");
          }
          System.out.println();
        }
      }

      public int maxLength(int[][] arr) {
        int max = 0;
        for(int i = 0; i < arr.length; i++) {
          if(arr[i].length > max) {
            max = arr[i].length;
          }
        }
        return max;
      }

    }

  }
