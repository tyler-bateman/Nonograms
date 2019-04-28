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
          //backtrackSolve(0, 0);
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

      /** Fills in uncertain spots in the puzzle array(indicated by the certainty
        * array) with a backtracking algorithm */
      private void backtrackSolve(int row, int col) {
        if(row >= picture.length) {
          printPicture();
          System.out.println("Elapsed time: " + (System.nanoTime() - startTime) + " seconds");
          System.exit(0);
        } else if(certainty[row][col]) {
          if(col < picture[0].length - 1) {
            backtrackSolve(row, col + 1);
          } else {
            backtrackSolve(row + 1, 0);
          }
        } else {
          cross(row, col);
          boolean successful = validateLine(row, 'r', rows[row]) && validateCol(col, 'c', columns[col]);
          if(successful) {
            if(col < picture[0].length - 1) {
              backtrackSolve(row, col + 1);
            } else {
              backtrackSolve(row + 1, 0);
            }
          }
          fill(row, col);
          successful = validateLine(row, 'r', rows[row]) && validateCol(col, 'c', columns[col]);
          if(successful) {
            if(col < picture[0].length - 1) {
              backtrackSolve(row, col + 1);
            } else {
              backtrackSolve(row + 1, 0);
            }
          }
          clear(row, col);
          if(row == 0 && col == 0) {
            System.out.println("No solution found");
          }
        }
      }

      /* Solves the puzzle with a stack implementation of backtracking*/
      private void stackSolve() {
        Stack<int[]> guessStack = new Stack<int[]>();
        Stack<int[]> fillStack = new Stack<int[]>();
        int row = 0;
        int col = 0;
        main:while(row < picture.length) {
          //Skips over filled cells
          while(picture[row][col] = ' ') {
            if(col == picture[0].length - 1) {
              row ++;
              col = 0;
              if(row >= picture.length) {
                break main;
              }
            } else {
              x ++;
            }
          }

          fill(row, col);
          int[] guess = {row, col};
          guessStack.push(guess);

          int blockStart = x;
          while(blockStart > 0 && picture[blockStart][col] != xChar) {
            blockStart--;
          }
        }
      }
      /*Fills in the imediate known filled spaces due to a filled space
        Returns false if it results in a contradiction, otherwise returns true*/
      private boolean extrapolate(Stack<int[]> fillStack, int row, int col) {

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

      /** Validates a given line. If the filled spots are valid, return true.
        * otherwise return false
        * @param index the row or column to be validated
        * @param orientation specifies whether it is a row('r') or a column('c')
        *                    Orientation defaults to row
        * @param clues the clues given for the specified line
         */
      private boolean validateLine(int index, char orientation, int[] clues) {
        //Declare and initialize line
        char[] line = null;
        if(orientation == 'c') {
          line = new char[picture.length];
          for(int i = 0; i < picture.length; i++) {
            line[i] = picture[i][index];
          }
        } else {
          line = picture[index];
        }

        int cell = 0;
        int block = 0;
        //Confirm that there is enough space to complete the line
        while(block < clues.length) {
          if(cell >= line.length) {
            return false;
          }
          //Skip over x characters
          if(line[cell] == xChar ) {
            continue;
          }
          int i;
          for(i = cell; i < cell + clues[block]; i++) {
            if(i >= line.length) {
              return false;
            }
            if(line[i] == xChar) {
              block --;
              break;
            }
          }
          cell = i;
          do {
            cell ++;
          } while(cell < line.length && line[cell] == fillChar);

          block ++;
        }

        //Confirm that the blocks already filled do not overfill the clues
        int block = 0;
        int spaceStart = 0;
        char prevChar = xChar;
        boolean filled = true;
        for(cell = 0; cell < line.length; cell++) {
          if(line[cell] != xChar && prevChar = xChar) {
            spaceStart = cell;
          }
          if(line[cell] == fillChar && prevChar != fillChar) {
            for(int i = cell; i < Math.max(spaceStart + clues[block], cell); i++) {
              if(lines[i] == xChar) {
                cell = i;

              }
            }
          }

          prevChar = lines[cell];
        }

      }

      /** Validates a given row. If the filled spots are valid, return true.
        * otherwise return false */
      private boolean validateCol(int col) {
        //TODO update to be compatible with fillObvious method

        boolean filled = false;
        int block = 0;
        int blockLength = 0;
        int i;
        for(i = 0; i < picture.length; i++) {
          if(picture[i][col] == fillChar) {
            if(filled) {
              blockLength++;
              if(blockLength > columns[col][block - 1]) {
                return false;
              }
            } else {
              blockLength = 1;
              block++;
              filled = true;
              if(block > columns[col].length) {
                return false;
              }
            }
          } else if(picture[i][col] == xChar) {
            if(filled) {
              filled = false;
              if(blockLength < columns[col][block - 1]) {
                return false;
              }
            }
          } else {
            break;
          }
        }
        if(filled) {
          i += columns[col][block - 1] - blockLength;
          if(i > picture.length) {
            return false;
          }
        }
        //i--;
        for(int j = block; j < columns[col].length; j++) {
          i += columns[col][j];
          if(i > picture.length) {
            return false;
          }
          i++;
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
