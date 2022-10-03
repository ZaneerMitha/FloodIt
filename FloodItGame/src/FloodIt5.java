import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;
import java.util.ArrayDeque;

// a class that helps with displaying the board, creating the links
class Utils {
  Utils() {}
  
  // gives a cell a random color based on number produced from the random object
  Color randColor(int randNum) {
    
    if (randNum == 0) {
      return Color.RED;
    }
    if (randNum == 1) {
      return Color.ORANGE;
    }
    if (randNum == 2) {
      return Color.YELLOW;
    }
    if (randNum == 3) {
      return Color.GREEN;
    }
    if (randNum == 4) {
      return Color.BLUE;
    }
    else {
      return Color.PINK;
    }
  }
  
  // makes a row of cells
  ArrayList<Cell> makeRow(int width, int rowNumber) {
    ArrayList<Cell> row = new ArrayList<Cell>();
    
    for (int i = 0; i < width; i = i + 1) {
      row.add(new Cell(i, rowNumber, false));
    }
    
    return row;
  }
  
  
  // FOR TESTING PURPOSES. used for testing makeRow()
  public ArrayList<Cell> makeRowForTest(int width, int rowNumber, Random rand) {
    ArrayList<Cell> row = new ArrayList<Cell>();
    
    for (int i = 0; i < width; i = i + 1) {
      row.add(new Cell(i, rowNumber, false, rand));
    }
    
    return row;
  }
  
  // makes a 2D array of cells, top left is (0,0)
  public ArrayList<ArrayList<Cell>> makeBoard(int length) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    
    for (int i = 0; i < length; i = i + 1) {
      board.add(makeRow(length, i));
    }
    
    return board;
  }
  
  
  // FOR TESTING PURPOSES. used for testing makeBoard()
  public ArrayList<ArrayList<Cell>> makeBoardForTest(int length, Random rand) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();

    for (int i = 0; i < length; i = i + 1) {
      board.add(makeRowForTest(length, i, rand));
    }
    
    return board;
  } 
}


// Represents a single square of the game area
class Cell {
  int x;
  int y;
  Color color;
  boolean flooded;
 
 
 // this can be used for testing, seeding comes in where you create the random object
 // you seed during the tests
 Cell(int x, int y, boolean flooded, Random rand) {
   int randNum = rand.nextInt(6);
   // new Random().nextInt(6) --> can't control what the number will be
   
   // we're going to use X and Y to see if two cells are next to each other
   // the TA said this is fine
   this.x = x;
   this.y = y;
   this.flooded = false;
   this.color = new Utils().randColor(randNum);
 }
 
 // this constructor produces something truly random (not for tests)
 // creates a random object, not a seed
 Cell(int x, int y, boolean flooded) {
   this(x, y, flooded, new Random());
   
 }
 
 // Empty constructor for testing purposes
 Cell(){}
 

  // displays the cell as a rectangle with a random color
  public WorldImage draw() {
    return new RectangleImage(25, 25, OutlineMode.SOLID, this.color);
  }
}

// represents the display of our game
class FloodItWorld extends World {

  static int BOARD_SIZE = 10;
  static int SCENE_SIZE = 500;
  int score;
  Utils util = new Utils();
  int time;
  ArrayDeque<Cell> floodedCells;
  Color currentColor;
  int counter = 0;
  int numFlooded;
  

  // All the cells of the game
  ArrayList<ArrayList<Cell>> board;

  FloodItWorld() {
    this.board = this.util.makeBoard(FloodItWorld.BOARD_SIZE);
    this.score = 0;
    this.time = 0;
    this.floodedCells = new ArrayDeque<Cell>();
  }

  // constructor used for seeding a board in testing
  FloodItWorld(ArrayList<ArrayList<Cell>> board) {
    this.board = board;
    this.score = 0;
    this.time = 0;
    this.floodedCells = new ArrayDeque<Cell>();
  }
  
  // Made to test worldEnds() method by seeding the end of the world
  FloodItWorld(int floodedCellsNum) {
    this.board = util.makeBoard(FloodItWorld.BOARD_SIZE);
    this.score = 0;
    this.time = 0;
    this.floodedCells = new ArrayDeque<Cell>();
    
    this.floodedCells = new ArrayDeque<Cell>();
    
    for (int i = 0; i < floodedCellsNum; i++) {
      floodedCells.push(new Cell());
    }
  }

  // draws the entire board onto a canvas
  public WorldScene makeScene() {
    WorldImage boardImage = new EmptyImage();
    WorldScene scene = new WorldScene(FloodItWorld.SCENE_SIZE, FloodItWorld.SCENE_SIZE);

    for (ArrayList<Cell> row : this.board) {
      WorldImage currentRow = new EmptyImage();

      for (Cell cell : row) {
        currentRow = new BesideImage(currentRow, cell.draw());
      }

      boardImage = new AboveImage(boardImage, currentRow);
    }

    scene.placeImageXY(boardImage, FloodItWorld.SCENE_SIZE / 2, FloodItWorld.SCENE_SIZE / 2);
    
    WorldImage score = new TextImage(this.score + " / 17", 30,  Color.BLACK);
    WorldImage time = new TextImage((this.time / 28) + " Seconds", 20,  Color.BLACK);
    
    scene.placeImageXY(score, FloodItWorld.SCENE_SIZE / 2, (FloodItWorld.SCENE_SIZE / 5) * 4);
    scene.placeImageXY(time, FloodItWorld.SCENE_SIZE / 2, (FloodItWorld.SCENE_SIZE / 5));
    
    return scene;
  }
  
  // Creates the end of game scenario for when all cells are flooded
  public WorldEnd worldEnds() {
      int allFlooded = this.floodedCells.size();
      
      for (ArrayList<Cell> row : this.board) {
        for (Cell cell : row) {
          if (cell.flooded == true) {
            allFlooded = allFlooded + 1;
          }
        }
      }

      if (allFlooded == FloodItWorld.BOARD_SIZE * FloodItWorld.BOARD_SIZE) {
        return new WorldEnd(true, this.makeEndScene());
      }
      else {
        return new WorldEnd(false, this.makeEndScene());
      }
   }
  
  // Constructs the end scene based on whether or not the player has the right amount of moves
  public WorldScene makeEndScene() {
    WorldScene endScene = new WorldScene(FloodItWorld.SCENE_SIZE, FloodItWorld.SCENE_SIZE);
    
    if (this.score >= 17) {
      endScene.placeImageXY(new TextImage("Game Over, too many moves", Color.red),
          FloodItWorld.SCENE_SIZE / 2, FloodItWorld.SCENE_SIZE / 2);
      return endScene;
    }
    else {
      endScene.placeImageXY(new TextImage("You Win!", Color.red),
          FloodItWorld.SCENE_SIZE / 2, FloodItWorld.SCENE_SIZE / 2);
      return endScene;
    }
  }
  
  // Changes the color of an individual cell, to the color of the cell in the position given
  public void changeCellColor(Cell cell, Posn posn) {
    int x = posn.x - 125;
    int y = posn.y - 125;
    cell.color = this.board.get(y / 25).get(x / 25).color;
  }
  
  // Changes the color of all the cells that have been flooded
  public void changeFloodedCells(Cell cell, Color color) {
    cell.color = color;
  }
  
  // Uses the "r" key to reset the board, time and score
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = this.util.makeBoard(FloodItWorld.BOARD_SIZE);
      this.score = 0;
      this.time = 0;
    }
  }
  
  // every time the mouse clicks on a square, the top left square is changes to that color
  // then, the rest of the board is searched and flooded
  public void onMousePressed(Posn posn) {
    int column = (posn.x - 125) / 25;
    int row = (posn.y - 125) / 25;
    
    if (posn.x >= 125 && posn.x <= 375 && posn.y >= 125 && posn.y <= 375) {
      this.changeCellColor(this.board.get(0).get(0), posn);
      this.board.get(0).get(0).flooded = true;
      this.currentColor = this.board.get(row).get(column).color;
      
      this.score = this.score + 1;
      
      for (int i = 0; i < this.board.size() - 1; i++) {
        for (int z = 0; z < this.board.get(i).size() - 1; z++) {
          
          if (this.board.get(i).get(z).flooded) {
            if (z != this.board.size() - 1 && this.board.get(row).get(column).color.equals(this.board.get(i).get(z+1).color)) {
              this.board.get(i).get(z+1).flooded = true;
            }
            if (i != 0 && this.board.get(row).get(column).color.equals(this.board.get(i - 1).get(z).color)) {
              this.board.get(i - 1).get(z).flooded = true;
            }
            if (i != this.board.size() - 1 && this.board.get(row).get(column).color.equals(this.board.get(i + 1).get(z).color)) {
              this.board.get(i + 1).get(z).flooded = true;
            }
            if (z != 0 && this.board.get(row).get(column).color.equals(this.board.get(i).get(z - 1).color)) {
              this.board.get(i).get(z - 1).flooded = true;
            }
          }
        }
      }
    }
    
    this.counter = 1;
    
  }
  
  // increments the time every tick and also makes the scene every tick
  public void onTick() {
    this.time = this.time + 1;
    this.makeScene();
    
    if (this.counter >= 1) {
      for (ArrayList<Cell> currentRow : this.board) {
        for (Cell cell : currentRow) {
          if (cell.flooded && (cell.x + cell.y + 1 == this.counter)) {
            this.floodedCells.push(cell);
          }
        }
      }
    
      for (Cell cell : this.floodedCells) {
        changeFloodedCells(this.floodedCells.pop(), this.currentColor);
      }
    }

    this.counter = this.counter + 1;
    
  }
}

// examples of the game
class ExamplesFloodIt {

  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;
  
  ArrayList<Cell> row1;
  ArrayList<Cell> row2;
  
  int BOARD_SIZE;
  int SCENE_SIZE;
  Utils util;
  
  FloodItWorld exampleGame;
  WorldScene emptyScene;

  void initData() {

    Random rand1 = new Random(3);

    this.c1 = new Cell(0, 0, false, rand1);
    this.c2 = new Cell(1, 0, false, rand1);

    this.c3 = new Cell(0, 1, false, rand1);
    this.c4 = new Cell(1, 1, false, rand1);

    this.row1 = new ArrayList<Cell>();
    row1.add(c1);
    row1.add(c2);

    this.row2 = new ArrayList<Cell>();
    row2.add(c3);
    row2.add(c4);

    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();

    this.BOARD_SIZE = 2;
    this.SCENE_SIZE = 500;
    this.util = new Utils();
    this.exampleGame = new FloodItWorld(board);
    this.emptyScene = new WorldScene(FloodItWorld.SCENE_SIZE, FloodItWorld.SCENE_SIZE);
  }

  boolean testRandColor(Tester t) {
    this.initData();

    return t.checkExpect(this.util.randColor(3), Color.GREEN)
        && t.checkExpect(this.util.randColor(5), Color.PINK)
        && t.checkExpect(this.util.randColor(4), Color.BLUE);
  }

  boolean testDraw(Tester t) {

    // can assign any number, doesn't have to 3 --> but this does not correspond to
    // randNum above
    Random rand = new Random(3);
    Cell c1 = new Cell(0, 0, false, rand);
    // you need rand in testDraw, so don't predefine c1 in initData()

    Random rand2 = new Random(5);
    Cell c2 = new Cell(20, 10, false, rand2);

    return t.checkExpect(c1.color, Color.YELLOW)
        && t.checkExpect(c1.draw(), new RectangleImage(25, 25, OutlineMode.SOLID, Color.YELLOW))
        && t.checkExpect(c2.color, Color.PINK)
        && t.checkExpect(c2.draw(), new RectangleImage(25, 25, OutlineMode.SOLID, Color.PINK));
  }

  boolean testMakeRow(Tester t) {

    Random rand1 = new Random(3);
    Random rand2 = new Random(3);

    Cell c1 = new Cell(0, 0, false, rand1);
    Cell c2 = new Cell(1, 0, false, rand1);

    Random rand3 = new Random(3);
    Random rand4 = new Random(3);

    Cell c3 = new Cell(0, 0, false, rand3);
    Cell c4 = new Cell(1, 0, false, rand3);
    Cell c5 = new Cell(2, 0, false, rand3);
    Cell c6 = new Cell(3, 0, false, rand3);

    return t.checkExpect(this.util.makeRowForTest(BOARD_SIZE, 0, rand2),
            new ArrayList<Cell>(Arrays.asList(c1, c2)))
        && t.checkExpect(this.util.makeRowForTest(BOARD_SIZE + 2, 0, rand4),
            new ArrayList<Cell>(Arrays.asList(c3, c4, c5, c6)));
  }

  boolean testMakeBoard(Tester t) {
    this.initData();

    Random rand1 = new Random(3);
    Random rand2 = new Random(3);

    Cell c1 = new Cell(0, 0, false, rand1);
    Cell c2 = new Cell(1, 0, false, rand1);

    Cell c3 = new Cell(0, 1, false, rand1);
    Cell c4 = new Cell(1, 1, false, rand1);

    return t.checkExpect(this.util.makeBoardForTest(BOARD_SIZE, rand2),
        new ArrayList<ArrayList<Cell>>(Arrays.asList(new ArrayList<Cell>(Arrays.asList(c1, c2)),
        new ArrayList<Cell>(Arrays.asList(c3, c4)))));
  }
  
  boolean testWorldEnds(Tester t) {
    this.initData();
    
    // seeded using constructor that makes the world flooded a certain amount
    FloodItWorld halfFlooded = new FloodItWorld(50);
    FloodItWorld fullFlooded = new FloodItWorld(100);
    
    return t.checkExpect(this.exampleGame.worldEnds(), new WorldEnd(false, this.exampleGame.makeEndScene()))
        && t.checkExpect(halfFlooded.worldEnds(), new WorldEnd(false, halfFlooded.makeEndScene()))
        && t.checkExpect(fullFlooded.worldEnds(), new WorldEnd(true, fullFlooded.makeEndScene()));
  }
  
  void testMakeEndScene(Tester t) {
    this.initData();
    
    this.exampleGame = new FloodItWorld(100);
    this.emptyScene.placeImageXY(new TextImage("You Win!", Color.red),
        FloodItWorld.SCENE_SIZE / 2, FloodItWorld.SCENE_SIZE / 2);
    
    t.checkExpect(this.exampleGame.makeEndScene(), this.emptyScene);
    
    this.initData();
    this.exampleGame.score = 18;
    
    this.emptyScene.placeImageXY(new TextImage("Game Over, too many moves", Color.red),
        FloodItWorld.SCENE_SIZE / 2, FloodItWorld.SCENE_SIZE / 2);
    
    t.checkExpect(this.exampleGame.makeEndScene(), this.emptyScene); 
  }
  
  void testChangeFloodedCells(Tester t) {
    this.initData();
    t.checkExpect(c1.color, Color.YELLOW);
    t.checkExpect(c2.color, Color.YELLOW);
    
    this.exampleGame.changeFloodedCells(this.c1, Color.RED);
    t.checkExpect(c1.color, Color.RED);
    this.exampleGame.changeFloodedCells(this.c2, Color.GREEN);
    t.checkExpect(c2.color, Color.GREEN);
  }
  
  void testOnKeyEvent(Tester t) {
    this.initData();
    
    this.exampleGame.score = 50;
    this.exampleGame.time = 40;
    
    this.exampleGame.onKeyEvent("m");
    t.checkExpect(this.exampleGame.score, 50);
    t.checkExpect(this.exampleGame.time, 40);
    
    this.exampleGame.onKeyEvent("r");
    t.checkExpect(this.exampleGame.score, 0);
    t.checkExpect(this.exampleGame.time, 0);
  }
  
  void testOnTick(Tester t) {
    this.initData();
    
    this.exampleGame.time = 10;
    this.exampleGame.onTick();
    
    t.checkExpect(this.exampleGame.time, 11); 
  }
  
//  void testOnMousePressed(Tester t) {
//    this.initData();
//    
//    this.exampleGame.currentColor = Color.RED;
//    this.exampleGame.onMousePressed(new Posn(250, 250));
//    
//    t.checkExpect(this.exampleGame.score, 1);
//    // t.checkExpect(this.exampleGame.board.get(0).get(0).color, Color.RED);
//  }

//    boolean testMakeScene(Tester t) {
//      this.initData();
//      
//      WorldScene scene = new WorldScene(this.SCENE_SIZE, this.SCENE_SIZE);
//      
//      Random rand1 = new Random(3);
//      Random rand2 = new Random(3);
//      
//      Cell c1 = new Cell(0, 0, false, rand1);
//      Cell c2 = new Cell(1, 0, false, rand1);
//      
//      Random rand3 = new Random(3);
//      Random rand4 = new Random(3);
//      
//      Cell c3 = new Cell(0, 1, false, rand1);
//      Cell c4 = new Cell(1, 1, false, rand1);
//      
//      scene.placeImageXY(
//          new AboveImage(new BesideImage(c3.draw(), 
//              new BesideImage(c4.draw(), new EmptyImage())),
//              new AboveImage(new BesideImage(c1.draw(), 
//                  (new BesideImage(c2.draw(), new EmptyImage()))), new EmptyImage())), 
//          this.SCENE_SIZE/2, this.SCENE_SIZE / 2);
//      
//      return t.checkExpect(this.exampleGame.makeScene(), scene) ;
//    }

  FloodItWorld game1 = new FloodItWorld();

  // running the game
  void testBigBang(Tester t) {
    game1.bigBang(FloodItWorld.SCENE_SIZE, FloodItWorld.SCENE_SIZE, 1.0 / 28.0);
  }

}
