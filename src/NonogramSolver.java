/** Author: Tyler Bateman
  * Date: 27 March 2019
  *
  * This program solves nonograms :)
  */

  import java.io.File;
  import java.io.FileNotFoundException;
  import java.util.Scanner;
  import java.util.Arrays;
  public class NonogramSolver{
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
        picture[row][col] = 9632;
      }

      /** cross out the given cell in the array */
      public void cross(int row, int col) {
        picture[row][col] = '-';
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
          boolean successful = validateRow(row) && validateCol(col);
          if(successful) {
            if(col < picture[0].length - 1) {
              backtrackSolve(row, col + 1);
            } else {
              backtrackSolve(row + 1, 0);
            }
          }
          fill(row, col);
          successful = validateRow(row) && validateCol(col);
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

      /** Validates a given row. If the filled spots are valid, return true.
        * otherwise return false */
      private boolean validateRow(int row) {
        //TODO update to be compatible with fillObvious method

        boolean filled = false;
        int block = 0;
        int blockLength = 0;
        int i;
        for(i = 0; i < picture[row].length; i++) {
          if(picture[row][i] == 9632) {
            if(filled) {
              blockLength++;
              if(blockLength > rows[row][block - 1]) {
                return false;
              }
            } else {
              blockLength = 1;
              block ++;
              filled = true;
              if(block > rows[row].length) {
                return false;
              }
            }
          } else if(picture[row][i] == '-') {
            if(filled) {
              filled = false;
              if(blockLength < rows[row][block - 1]) {
                return false;
              }
            }
          } else {
            break;
          }
        }
        if(filled) {
          i += rows[row][block - 1] - blockLength;
          if(i > picture[row].length) {
            return false;
          }
        }

        for(int j = block; j < rows[row].length; j++) {
          i += rows[row][j];
          if(i > picture[row].length) {
            return false;
          }
          i++;
        }
        return true;

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
          if(picture[i][col] == 9632) {
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
          } else if(picture[i][col] == '-') {
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
