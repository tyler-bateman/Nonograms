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

  public class NonogramSolver{
    public char fillChar = (char)9632;
    public char xChar = '-';
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
          //stackSolve;
          printPicture();
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
        main:while(row < picture.length) {
          //Skips over filled cells
          while(picture[row][col] != ' ') {
            if(col == picture[0].length - 1) {
              row ++;
              col = 0;
              if(row >= picture.length) {
                break main;
              }
            } else {
              col++;
            }
          }
          //Fill in the first empty cell
          fill(row, col);
          int[] guess = {row, col};
          guessStack.push(guess);
          fillStack.push(guess);

          //Fill in cells made obvious by the guess
          boolean successRight = propagateRight(fillStack, row, col);
          boolean successDown = propagateLeft(fillStack, row, col);

          if(successRight && successDown) {
          //If the guess didn't result in a contradiction, move on
            if(col == picture[0].length - 1) {
              row ++;
              col = 0;
            } else {
              col++;
            }
          } else {
          //If the guess resulted in a contradiction, revert all changes since the guess
            int[] cur = fillStack.pop();
            while(!cur.equals(guess)) {
              clear(cur[0], cur[1]);
              cur = fillStack.pop();
            }
            //Cross out the cell instead
            cross(row, col);
            //Fill in cells made obvious by guess
            successRight = fillKnownSpacesH(fillStack, row, col);
            successDown = fillKnownSpacesV(fillStack, row, col);

            if(successRight && successDown) {
            //If the guess didn't result in a contradiction, move on
              if(col == picture[0].length - 1) {
                row++;
                col = 0;
              } else {
                col++;
              }
            } else {
            // If the guess resulted in a contradiction, revert all changes since
            // the last unexhasted uncertain cell
              int[] prevGuess = guesStack.pop();
              while(picture[prevGuess[0]][prevGuess[1]] == xChar) {
                prevGuess = guessStack.pop();
              }
              int[] cur = fillStack.pop();
              while(!cur.equals(prevGuess)) {
                clear(cur[0], cur[1]);
                cur = fillStack.pop();
              }
            }
          }
        }
      }

      /** Fills out the cells that can be easily solved in a row
        * Returns true if successful, otherwise false
        * Pre: picture[row][0..col] != ' '
               picture[row][col] == xChar
               col < picture[0].length - 1
        * Post: the coordinates of all modified cells are pushed to fillStack
        */
      private boolean fillKnownSpacesH(fillStack, row, col) {
        int startBlock = getBlock(true, row, col);
        int usedSpace = rows[row].length - startBlock - 1; //The amount of space used when the clues for this row are compressed as much as possible
        for(int i = startBlock; i < rows[row].length; i++) {
          usedSpace += rows[row][i];
        }
        int extraSpace = picture[i].length - col - usedSpace;
        int blockEnd = col + extraSpace + 1; // The end of the block being partially filled
        for(int i = 0; i < rows[row].length; i++) {
          for(int j = blockEnd; j < blockEnd + rows[row][i] - extraSpace; j++) {
            if(j >= picture[row].length || picture[row][j] == xChar) {
              return false;
            }
            fill(row, j);
            fillStack.push({row, j});
            if(!extrapolateDown(fillStack, row, col)) {
              return false;
            }
          }
          if(blockEnd != 0 && extraSpace == 0) {
            if(picture[row][blockEnd - 1] == fillChar){
              return false;
            }
            cross (row, blockEnd - 1);
            fillStack.push({row, blockEnd - 1});
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
      private boolean fillKnownSpacesV(fillStack, row, col) {
        int startBlock = getBlock(false, row, col);
        int usedSpace = rows[row].length - startBlock - 1; //The amount of space used when the clues for this column are compressed as much as possible
        for(int i = startBlock; i < cols[col].length; i++) {
          usedSpace += cols[col][i];
        }
        int extraSpace = picture.length - row - usedSpace;
        int blockEnd = row + extraSpace + 1;
        for(int i = 0; i < cols[col].length; i++) {
          for(int j = blockEnd; j < blockEnd + cols[col][i] - extraSpace; j++) {
            if(j >= picture.length || picture[j][col] == xChar) {
              return false;
            }
            fill(j, col);
            fillStack.push({j, col});
          }
          if(blockEnd != 0 && extraSpace == 0) {
            if(picture[blockEnd - 1][col] == fillChar) {
              return false;
            }
            cross(blockEnd - 1, col);
            fillStack.push({blockEnd - 1, col});
            if(!extrapolateRight(fillStack, row, col)) {
              return false;
            }
          }
          blockEnd += cols[col][i] + 1;
        }
        return true;
      }

      /** Fills in the immediate known filled spaces right of a filled space
        * Returns false if it results in a contradiction, otherwise returns true
        * Pre: picture[row][col] == fillChar
        */
      private boolean propagateRight(Stack<int[]> fillStack, int row, int col) {
        int block = getBlock(true, row, col);
        if(block != -1) {
          int blockSize = rows[row][block];
          //Find the start of the block being propagated
          int blockStart = col;
          while(blockStart > 0 && picture[row][blockStart - 1] != xChar) {
            blockStart--;
          }

          //Fill in the known portion of the block
          for(int i = col; i < blockStart + blockSize; i++) {
            if(picture[row][i] == xChar) {
              return false;
            }
            fill(row, i);
            fillStack.push({row, i});
          }
          //If the whole block is filled put an x on the end
          if(blockStart == col) {
            if(picture[row][col + blockSize] == fillChar){
              return false;
            }
            cross(row, col + blockSize);
            fillStack.push({row, col + blockSize});
          }
          //propagate all the filled in blocks
          for(int i = col; i < blockStart + blockSize; i++) {
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
          int blockSize = cols[col][block];
          //Find the start of the block being propagated
          int blockStart = row;
          while(blockStart > 0 && picture[blockStart - 1] != xChar) {
            blockStart--;
          }
          //Fill in the known portion of the block
          for(int i = row; i < blockStart + blockSize; i++) {
            fill(i, col);
            fillStack.push({i, col});
          }
          //If the whole block is filled put an x on the end
          if(blockStart == col) {
            cross(row + blockSize, col);
            fillStack.push({row + blockSize, col});
          }
          //propagate all the filled in blocks
          for(int i = row; i < blockStart + blockSize; i++) {
            if(!propagateRight(fillStack, i, col)) {
              return false;
            }
          }
        }
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
        if(orientation) { //Horizontal
          for(int i = 0; i < col; i++) {
            //Handles the ambiguous case where there is an unknown space
            if(picture[row][i] == ' ') {
              int blockStart = i;
              while(blockStart > 0 && picture[row][blockStart - 1] != xChar) {
                blockStart--;
              }
              if(col - blockStart <= rowClues[block] + 1) {
              //The cell must be part of the current block
                return block;
              } else if(block != rowClues.length - 1 && picture[row][blockStart] == fillChar && (col - blockStart) < (rowClues[block] + rowClues[block + 1] + 2)) {
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
              if(col - blockStart <= colClues[block] + 1) {
                //The cell must be part of the current block
                return block;
              } else if(block != colClues.length - 1 && picture[blockStart][col] == fillChar && (row - blockStart) < (colClues[block] + colClues[block + 1] + 2)){
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
            //Counts the blocks leading up to the cell in question
            if(picture[i][col] == xChar && picture[i - 1][col] == fillChar){
              block++;
            }
          }
        }
        return block;
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
