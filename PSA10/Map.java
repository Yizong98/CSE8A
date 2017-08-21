import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observer;
import java.awt.*;

// load from a file
import java.io.IOException;
import java.io.File;
import java.util.Scanner;


// edit background
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * Map: class to represent a 2d world that can hold turtles and
 * display them (same as the World class). 
 * It additionally allows to organize the world in cells, i.e., a map, 
 * where the space can be free (therefore we can put a turtle on it) 
 * or occupied by a wall or obstacle.
 * This class also has methods to plan how to move the turtle from 
 * a given start cell to and end cell.
 * 
 * ANA (22-Nov 2013, UCSD).
 */

public class Map extends World
{
  ////////////////// fields ///////////////////////
  
  // to be able to load the map from a txt file: encoding expected in the file
  
  // occupancy of the map cells (constant all the time, once we load the map)
  private int[][] ocupancyMap; //0: free; 1:obstacle
  
  // cost of going from that cell to the goal (it changes every time we change the goal)
  private int[][] costMap; //0: GOAL; the higher, the further from the GOAL  
  
  // fixed dimensions for everymap (initialized always to default value in the constructor)
  private int maxMapDim;
  private int cellSize;

  // dimensions of the map loaded depending on the map file (.txt file) used to initialize the map
  private int widthInCells;
  private int heightInCells;
  
  private Cell currentGoalCell;

  private static final int defaultInfCost = 10000; 
  // max number of cells is 25*25 (625), so cost 10000 is like saying "Infinite", there is no way to have such a high "cost".
  
 ////////////////// the constructors ///////////////

  /**
   * Constructor to load a map from a file
   **/
  public Map(String mapFileName)
  {
    
    // DEFAULT values: 
       
    // Our maps are always going to be 1000x1000 worlds. We may intialize or not all possible cells (25 in each direction).
    // Each cell is going to be 40 points width and high in the picture.
    // this is calling to the constructor of World:
    super(25*40,25*40);
    // max number of cells in each direction
    this.maxMapDim = 25;
    // size of one cell (in "painting" units)
    this.cellSize = 40;
    
    this.ocupancyMap = new int[maxMapDim][maxMapDim];
    this.costMap = new int[maxMapDim][maxMapDim];
    
    //Set Default Values for map grid (occupancy and initial cost)
    for(int i = 0; i < ocupancyMap.length; ++i) {
      for(int j = 0; j < ocupancyMap[i].length; ++j) {
        // set all occupacy to unknown
        ocupancyMap[i][j] = -1;  
        // and all costs to 1000000
        costMap[i][j] = defaultInfCost; // max number of cells is 25*25 (625), so cost 10000 is like saying "Infinite", there is no way to have such a high "cost".
      }
    }
       
    this.loadMapFromFile(mapFileName);
    this.paintMap();
    this.setAutoRepaint(true);  
    this.getFrame().setTitle(mapFileName);

    
    this.currentGoalCell = new Cell(1,1);
    
    System.out.println("You have created a map loaded from file " + mapFileName);   
  }
  
  
 
  ///////////////// methods ///////////////////////////
  
  
  ///////// getters and setters ///////////
  /*
   * getter for the value of the cost map in a certain cell
   */
  public int getCostMapValueAt(Cell aCell){
     return costMap[aCell.getX()][aCell.getY()];
  }
  
   /*
   * setter for the value of the cost map in a certain cell
   */
  public boolean setCostMapValueAt(Cell aCell, int cost){
     costMap[aCell.getX()][aCell.getY()] = cost;
     return true;
  }
  
  /*
  * This method returns a boolean that tells us if the given cell is free or occupied
  */
  public boolean isFree(Cell aCell){
     // System.out.println("Cell " + aCell + " is " + this.ocupancyMap[aCell.getX()][aCell.getY()] );
     return (this.ocupancyMap[aCell.getX()][aCell.getY()]==0);
  }
  
  /*
  * This method returns the numbers of cells in the map in the x direction (width of the map in cells)
  */
  public int getMapWidth(){
     return (this.widthInCells);
  }
  
  /*
  * This method returns the numbers of cells in the map in the y direction (height of the map in cells)
  */
  public int getMapHeight(){
     return (this.heightInCells);
  }
  
  
  /*
  * This method returns the dimension in pixels of this map cells (squared).
  */
  public int getCellSize(){
     return (this.cellSize);
  }
  
   /*
   * setter for the goal cell
   */
  public boolean setGoal(Cell aCell){
     this.currentGoalCell = aCell;
     return true;
  }
      
  
   ///////// methods to load the map ///////////
  /*
   * This method reads the map configuration from a file and initializes the instance variables with the corresponding values. 
   * Therefore, it will be usually called within the Constructor.
   * Assumes that the txt file that describes the map is as follows: 
   *    - Each line has only 0s and 1s separated by blank spaces; 
   *    - No more than 25 cells in either direction will be considered (if the file has more digits, they are discarded).
   * For example the following represents a map with 6x5 cells. All around there are obstacles (1) and the central area 4x3 is free.
   * 1 1 1 1 1 1
   * 1 0 0 0 0 1
   * 1 0 0 0 0 1
   * 1 0 0 0 0 1
   * 1 1 1 1 1 1
   */
  private boolean loadMapFromFile(String aFileName) {
    
    File mapfile = new File(aFileName);
    String line;
    
    this.heightInCells = 0;
    this.widthInCells= 0;
    int max_width= 0;
    
    try { 
      Scanner sc = new Scanner(mapfile);
      heightInCells = 0;   
      // parse all the lines/rows. If there are more than 25, we skip the rest
      while (sc.hasNextLine() && heightInCells < maxMapDim) {
          line = sc.nextLine();
          
          // this will set the columns of row "height" that are ocuppied to "1"
          widthInCells = processLine(line, heightInCells); 
          
          // we want to store the max. width found
          if (widthInCells>max_width)
            max_width = widthInCells;
          
          System.out.println(line);
          heightInCells++;
      }
         
    }catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    
    System.out.println("Loaded a " + heightInCells + "x" + max_width + " map");
    this.widthInCells = max_width;
    
    return true;
  }
 
  /*
   * This method parses the lines from the map file to load them in the Map class fields.
   * Assumes each line has only 0s and 1s separated by blank spaces
   */
  private int processLine(String aLine, int y){
    //use a second Scanner to parse the content of each line 
    Scanner scanner = new Scanner(aLine);
    int currentCell;
    int x = 0;
    
    while (scanner.hasNextInt() && x < maxMapDim){
      //assumes each line has only 0s and 1s separated by blank spaces
      currentCell = scanner.nextInt();    
      ocupancyMap[x][y] = currentCell;
      
      x++;
    }

    return x;
  }
  
  
  ///////// methods to display the map ///////////
  
  /*
   * This method paints a cell in the map (with coordinates (xCell, yCell) with the given color (cellColor) ).
   * (It is overloaded to include or not a message/number as the first parameter message) 
   */ 
  public void fillCell(int xCell, int yCell, Color cellColor)  
  {
    this.fillCell("", xCell, yCell, cellColor);
  }
  
  public void fillCell(String message,int xCell, int yCell, Color cellColor){
    // get the Graphics2D 
    Graphics g = this.getPicture().getGraphics();
    Graphics2D g2 = (Graphics2D) g;
    
    // get the font information for the message
    Font font = new Font("Arial",Font.BOLD,24);
  
    double xPos = xCell*cellSize+cellSize/2;
    double yPos = yCell*cellSize+cellSize/2;
  

    g2.setColor(cellColor);
    // draw a square centered in the given point
    g2.fill(new Rectangle2D.Double(xPos-cellSize/2, yPos-cellSize/2,cellSize, cellSize));
    
    
    
    // draw a number in the center of the cell
    g2.setColor(Color.RED);
    g2.setFont(font);
    g2.drawString(message,(float) (xPos-cellSize/3), (float) (yPos+cellSize/3));
      
    // DRAW MORE STUFF!
    /*g2.setColor(Color.GREEN); 
    g2.fillRoundRect((int) xPos, (int)  yPos, cellSize/3,cellSize/3,1,1);
    g2.setColor(Color.WHITE);
    g2.fillOval((int) xPos, (int) yPos, cellSize/5,cellSize/5);*/
     
  }
   
   /*
   * This method draws the border of a cell in the map in black 
   */ 
  public void drawBorderOfCell(int xCell, int yCell){
      drawBorderOfCell( xCell,  yCell, Color.BLACK);
  }
  
  /*
   * This method draws the border of a cell in the map, with coordinates (xCell, yCell), in the given color (squareColor) 
   */        
  public void drawBorderOfCell(int xCell, int yCell, Color squareColor){   
    // get the Graphics2D 
    Graphics g = this.getPicture().getGraphics();
    
    Graphics2D g2 = (Graphics2D) g;
    
    // get the font information for the message
    Font font = new Font("Arial",Font.BOLD,24);
  
    double xPos = xCell*cellSize+cellSize/2;
    double yPos = yCell*cellSize+cellSize/2;
  

    g2.setColor(squareColor);
    // draw square centered in the given point (Points need to be MULTIPLE of 40 + 20!!)
    g2.drawRect((int) (xPos-cellSize/2), (int) (yPos-cellSize/2),cellSize, cellSize);
    

   }

  
 
 ///////// methods to estimate the cost from each cell to a given goal (endCell) within the map /////////// 
   /*
   * This method fills the cost map matrix of the grid in this map for a given start and end point
   */
  public boolean computeCostMap(Cell startCell, Cell endCell){
     
     boolean exitFound = false;
    
     int currentCostLevel = 0; 
     
     Cell[] cellsToExpand = new Cell[25*25];  
     Cell[] cellsToExpandNext = new Cell[25*25];
     
     int index = 0;
     int expandedCellsInThisIteration = -1;
     Cell currentCell = null;
     
     // first "iteration" will expand the exit cell, which has cost zero
     setCostMapValueAt(endCell, 0);          
     cellsToExpand[0] = endCell;
     cellsToExpand[1] = null; // point the end of the cells to expand
     
     while (!exitFound && expandedCellsInThisIteration!=0){
                
       // expand cells
       // try to set the new cost "level" into the neighbour of each cell that needs to be expanded    
         expandedCellsInThisIteration = 0;
         index = 0;
         while ( cellsToExpand[index]!=null ){
           
           // System.out.println("Expanding " + cellsToExpand[index]);
           
           currentCell = cellsToExpand[index];
           
           Cell neigh1 = new Cell(currentCell.getX()+1, currentCell.getY());
           Cell neigh2 = new Cell(currentCell.getX(), currentCell.getY()+1);
           Cell neigh3 = new Cell(currentCell.getX()-1, currentCell.getY());
           Cell neigh4 = new Cell(currentCell.getX(), currentCell.getY()-1);
             
           if ( expandCell(neigh1, currentCostLevel) ){
             cellsToExpandNext[expandedCellsInThisIteration] = neigh1;
             expandedCellsInThisIteration++;
             // if we reach the start, we found a path!
             if (neigh1.getX()==startCell.getX() && neigh1.getY()==startCell.getY())
               exitFound = true;
           }
           
           
           if ( expandCell(neigh2, currentCostLevel) ){
             cellsToExpandNext[expandedCellsInThisIteration] = neigh2;             
             expandedCellsInThisIteration++;
             // if we reach the start, we found a path!
             if (neigh2.getX()==startCell.getX() && neigh2.getY()==startCell.getY())
               exitFound = true;
           }
           
           if ( expandCell(neigh3, currentCostLevel) ){
             cellsToExpandNext[expandedCellsInThisIteration] = neigh3;             
             expandedCellsInThisIteration++;
             // if we reach the start, we found a path!
             if (neigh3.getX()==startCell.getX() && neigh3.getY()==startCell.getY())
               exitFound = true;
           }
           
           if ( expandCell(neigh4, currentCostLevel) ){
             cellsToExpandNext[expandedCellsInThisIteration] = neigh4;             
             expandedCellsInThisIteration++;
             // if we reach the start, we found a path!
             if (neigh4.getX()==startCell.getX() && neigh4.getY()==startCell.getY())
               exitFound = true;
           }
           
           index++;
         }
       
         // System.out.println(" cells expanded: " + expandedCellsInThisIteration);
         
         cellsToExpandNext[expandedCellsInThisIteration] = null;            
         
         currentCostLevel++;
         //System.out.println(" For next level: " + currentCostLevel);    
         
         // fill array with cells to expand in the next level    
         cellsToExpand[0] = null;
         for(int k = 0; k<=expandedCellsInThisIteration; k++) {
             cellsToExpand[k] = cellsToExpandNext[k];
             //System.out.println("Next to expand: " + cellsToExpand[k]);
         } 
         

         
     } // while !exitFound 
     
     return exitFound;
  } 
  
  /*
   * Method that expands the wave to the neighboring (neigh) cell if possible.
   * It checks if this cell should be getting the current "wave" cost value or stay with its current value.
   */
  private boolean expandCell(Cell neigh, int currentCostLevel){
    
    if (neigh.getX()>=this.getMapWidth() || neigh.getY()>=this.getMapHeight() || neigh.getX()<0 || neigh.getY()<0) {
       return false; // this neighbor is out of bounds
    }
    else{
       if (isFree(neigh) && ( (currentCostLevel+1) < getCostMapValueAt(neigh) ) ){ 
         setCostMapValueAt(neigh,currentCostLevel+1);         
           
         return true;       
       }else{
         return false;
       }
    }  
  }
  
  
  
  /**
   * Method to return a string with information about this Map
   * @return a string with information about this map
   */
  public String toString()
  {
    return ( "Map of dimensions (IN CELLS): " + this.getMapWidth() + ", " + this.getMapHeight() );
  }
  
  
 /*
  * This method paints all the cells of the map according to they "type" (empty, full, out of the map)
  */ 
  public void paintMap(){
     
     for(int i = 0; i < ocupancyMap.length; ++i) {
       for(int j = 0; j < ocupancyMap[i].length; ++j) {
         
         // set all occupacy to unknown
         if (ocupancyMap[i][j] == -1){
             // This cell has not been initialized, it's OUT of the map. Set to GRAY
             fillCell(i, j, Color.GRAY); 
         }else if (ocupancyMap[i][j] == 1) {
             // This cell has an obstacle. Set to BLACK.
             fillCell(i, j, Color.BLACK);  
         }
         else{
             drawBorderOfCell(i,j); //white
         } 
         
       }
     }
     
  } 
  
 /*
  * This method paints all the cells of the map according to they "type" (empty, full, out of the map)
  */ 
  public void displayCostMap(){
  
     String cost; 
    
     for(int i = 0; i < costMap.length; ++i) {
       for(int j = 0; j < costMap[i].length; ++j) {
         
         // display the cost only for cells that have been "used" (e.g., do not have the default value anymore)
         if (costMap[i][j] < defaultInfCost){
             cost = Integer.toString(costMap[i][j]);
             fillCell(cost, i, j, Color.WHITE);          
         }
         
       }
     }
     

  }
  
 ///////// methods to plan a path within the map - TO DO on PSA10 ///////////
  
  
  /*
   * findPath: Fills the cost map matrix for a given start and end point and finds the path (fills the finalPath array)
   */
  public Cell[] findPath(Cell startCell, Cell endCell)
  {
    boolean computeCost = this.computeCostMap(startCell, endCell);
    int derp = this.getCostMapValueAt(startCell)+1;
    Cell[] finalPath = new Cell[derp];
    finalPath[0] = startCell;
    return null;
  }
  
  
   
  /*
   * computeAndDisplayCost: Fills the cost map matrix (the class variable costMap) for a given start and end point
   * Returns true when the method is finished
   */
  public boolean computeAndDisplayCost(Cell startCell, Cell endCell)
  {
    Color red = new Color(255,0,0);
      boolean result = false;
      boolean computeCost = this.computeCostMap(startCell, endCell);
      if(computeCost == true) {
        this.displayCostMap();
        result = true;
      this.fillCell(endCell.getX(), endCell.getY(), red);
      }
    return result;
  }
  
  
} // end of Map class
