import javax.swing.*; //JButton, etc.
import java.awt.*; // Border layout //put in * to access all libraries 
import javax.swing.border.*; //LineBorder
import java.io.*; 
import java.util.Scanner;
import java.lang.Math;
import javax.swing.event.*;
import java.awt.event.*;


public class Project extends WindowManager
					//extend WindowManager so that instance variables and data is inherited

{

	//declare instance variable buttons 
	private JButton compareButton; 
	private JButton quitButton; 
	private JButton downsample;
	private JButton clearButton;
	private JButton saveButton;


	private JLabel[][] grid; //2d array for GridLayout
	private Color currentColor; //currentColor stores color for each grid cell
	private JLabel statusbar; //statusbar to give user feedback
	
	private final static int setRows = 40; //initialize setRows, setCols, rowsDS, colsDS as constants
	private final static int setCols = 40;
	private final static int rowsDS = 10; 
	private final static int colsDS = 10;

	//arrays of type int[][] that will store RGB values 
	private static int[][] red;
	private static int[][] green;
	private static int[][] blue;

	//averageDS and sumDS will store downsampled values for average and sum
	private static float averageDS;
	private static int sumDS;

	private static float[] array; //array stores the values for average grayscale value for each reference value
	private static int indexOfAverageArrayValue; //index that is returned to the user as the answer

	private static boolean penStatus; //status of pen - whether it is on or off

	//arrays to hold downsampled grayscale values that can be 'written' to PPM file
	private static int[][] redWrite; 
	private static int[][] blueWrite;
	private static int[][] greenWrite;

	public Project(int gridRows, int gridCols)
	{
		super("Number recognition program", 500,800); //calls parent constructor in Window Manager with parameters
	
		//add a BorderLayout
		this.setLayout(new BorderLayout()); //add BorderLayout so that we can orient panels as we want 

		/*
			We now want a grid panel that can can 'listen' for the mouse and will change color when the mouse is dragged over it.
			We do this by constructing an object of type JPanel, assigning a set number of rows and columns to it. We then have to construct
			2D arrays of JLabel references, and then construct JLabel objects inside each box. 

		*/

		JPanel gridPanel = new JPanel(); //construct a JPanel object gridPanel which will serve as our grid
		gridPanel.setLayout(new GridLayout(gridRows, gridCols)); //call setLayout method using gridPanel object passing in GridLayout object

		grid = new JLabel[gridRows][gridCols];//constructs 2D array of JLabel References (each of them contains null - objects not constructed yet)

		for (int row = 0; row < gridRows; row++)
		{
			for (int col = 0; col < gridCols; col++)
			{
				grid[row][col] = new JLabel(); //explicity construct objects of type JLabel
				gridPanel.add(grid[row][col]); //add JLabel objects to each gridPanel cell

				//make each JLabel listen for mouse clicks
				grid[row][col].addMouseListener(this); //add mouse listener to each cell so that it can 'listen' for mouse clicks
				grid[row][col].setBackground(Color.BLACK); //set JLabel backgrounds black to make identification easier
				grid[row][col].setOpaque(true);
			}	
		} 

		
		this.add(gridPanel, BorderLayout.CENTER); //add the grid panel to the center of the window


		//We now want to construct our button objects and give them names.
		quitButton = new JButton("Quit"); 
		compareButton = new JButton("Compare!"); 
		downsample = new JButton("Downsample");
		clearButton = new JButton("Clear");
		saveButton = new JButton("Save");

		//We now add action listener to each of our buttons so that they can 'listen' for mouse clicks from the user 
		compareButton.addActionListener(this); 
		quitButton.addActionListener(this); 
		downsample.addActionListener(this);
		clearButton.addActionListener(this);
		saveButton.addActionListener(this);

		//construct statusbar. statusbar essentially gives the user feedback to let them know what's happening.
		statusbar = new JLabel("Sketch any number from 0-9 (included) that you'd like");

		JPanel outerPanel = new JPanel(); //construct outerPanel so that we can add the statusbar and buttonPanel to it

		JPanel buttonPanel = new JPanel(); //construct buttonPanel object to which we add all of our buttons
		
		//add our buttons to buttonPanel
		buttonPanel.add(downsample);
		buttonPanel.add(compareButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(quitButton);

		//add statusbar and buttonPanel to outerPanel and provide them with orientations 
		outerPanel.add(statusbar, BorderLayout.NORTH); 
		outerPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.add(outerPanel, BorderLayout.SOUTH); //adds outerPanel to south of current window
	}

	public void labelClicked(JLabel whichLabel)
	{
		whichLabel.setBackground(Color.WHITE); //sets color by calling setBackground method on whichLabel object - this makes each of our labels white when the mouse is dragged over them
		whichLabel.setOpaque(true);
	}

	public static void main(String[] args)
	{	
		Project window = new Project(setRows,setCols); //construct object of type Project so that our program opens

		//message dialogue for user
		JOptionPane.showMessageDialog(null, "Welcome. The point of my program is to recognize the number you sketch.\n \n"
		+ "This is my small attempt at implementing a program that uses classification in machine learning. \n \nInstructions are simple: \n"
		+ "Step 1) Sketch a number from 0 - 9 (shouldn't be too small or too large), press the downsample button and then the compare button!\n"
		+ "Step 2) Be amazed. Save your sketch if you'd like by hitting the save button.\n \n"
		+ "Note that this program isn't designed to give accurate results. It is going to give you possibilities for numbers that you've drawn","How my program works", JOptionPane.INFORMATION_MESSAGE); //construct object of class Lab13 inputting specified parameters for rows and cols
	}

	/*
		mousePressed, mouseReleased and mouseExited override the same methods declared in the parent class WindowManager.
		They are called when the mouse is pressed, when the mouse is released and when the mouse goes onto the next label.
	*/
	public void mousePressed(MouseEvent event)
    { 
    	penStatus = true; //when the mouse is pressed, penStatus is 'on'
    	statusbar.setText("Sketch, sketch, sketch!");
    	
    } 
    public void mouseReleased(MouseEvent event)
    {
    	penStatus = false; //when the mouse is released, penStatus is 'off'
    	statusbar.setText("Press downsample and then compare when you're done!");
    	
    }
    
    public void mouseExited(MouseEvent event)
    {
    	/*
			This method is called whenever the mouse leaves a JLabel instance as the mouse is dragged across the GUI.
			When the method is called, the background color of the JLabel that is exited is changed only if the pen is on.
    	*/

        if (penStatus == true)
        {	
        	Component pressedComponent = event.getComponent();

        	String componentType = pressedComponent.getClass().getName();

        	if (componentType.contains("JLabel"))
        	{
            	labelClicked ((JLabel) pressedComponent);
        	}
        }
       
    }

	public float referenceAverage(String input)
	{
		/*
			This method is called so that we can extract the grayscale values from the file that is fed in as the parameter.
			It is called upon in arrayOfAverageReferenceValues(). It also calculates the average for the grayscale values in 
			each reference image file so that we can compare this average with the one that we get for the grayscale values 
			when the user sketches his/her input.
		*/
		
		int countRow = 0; //initialize count variables to use in while loop below
		int countCol = 0;

		int pixelRed = 0; //variables to store RGB values
		int pixelGreen = 0;
		int pixelBlue = 0;

		int sumOfReferenceGS = 0; //variables to store sum and average of reference gray scale values
		float averageOfReferenceGS = 0; 

		 try
	 { //use a try catch block to account for any possibly exceptions.

		Scanner inputFile = new Scanner(new FileReader(input)); //construct object of class type Scanner with argument as input object of class type FileReader
		//essentially runs the file so we can get data from it
		
		
		String magicNum = inputFile.nextLine(); //this is the number that tells us that we are working with 3 colors - i.e P3
		String comment = inputFile.nextLine(); //extracts random comment
		int numberOfCols = inputFile.nextInt(); //extracts number of columns
		int numberOfRows = inputFile.nextInt(); //extracts number of rows
		int scale = inputFile.nextInt(); //extracts scale

		//the while loop works as long as there is an Int left to work with - this is accomplished with the hasNextInt() method associated with the Scanner class
		while(inputFile.hasNextInt())
		{
			pixelRed = inputFile.nextInt(); //first integer while loop runs into is red scale
			pixelGreen = inputFile.nextInt(); //second is green scale
			pixelBlue = inputFile.nextInt(); //third is blue scale
			sumOfReferenceGS += pixelRed + pixelBlue + pixelRed; //add the RGB values up

			countCol++; //increment Col so that we run through each column associated with the row 
			//ex - [0,1] , [0,2] [0,3] etc

			if (countCol == setCols) //when the count reaches max number of Cols, it is time to move onto the next row 
			{
				countCol = 0; //hence set value of countCol to 0
				countRow++; //increment value of row now so that we can move onto the next one 
			}
		}

		averageOfReferenceGS = sumOfReferenceGS/(numberOfCols*numberOfRows); //calculate sum of pixel values and divide by total number of cells
		
	}

		catch (Exception e)
		{ 
			System.out.println(e.getMessage()); //Catch exception and get message
		}

		return averageOfReferenceGS;
	}

	public void arrayOfAverageReferenceValues()
	{//this method calls the referenceAverage method to calculate average of pixel values for each corresponding reference image made by me

		array = new float[10];
		array[0] = referenceAverage("reference_zero.ppm");
		array[1] = referenceAverage("reference_one.ppm");
		array[2] = referenceAverage("reference_two.ppm");
		array[3] = referenceAverage("reference_three.ppm");
		array[4] = referenceAverage("reference_four.ppm");
		array[5] = referenceAverage("reference_five.ppm");
		array[6] = referenceAverage("reference_six.ppm");
		array[7] = referenceAverage("reference_seven.ppm");
		array[8] = referenceAverage("reference_eight.ppm");
		array[9] = referenceAverage("reference_nine.ppm");
	}

	//where I left off

	public void buttonClicked(JButton whichButton)
	{ //override method written in WindowManger //what to do if either button is pressed

		red = new int[setRows][setCols];
		green = new int[setRows][setCols];
		blue = new int[setRows][setCols];
		Color colorgrid; 

		for (int row = 0; row < setRows; row++)
				{ //use doubly nested for loops to write contents of all nested arrays to our file
					for (int col = 0; col < setCols; col++)
					{
						//the for loop gets the color from each grid using the getBackground() method and we then write the value of red, green and blue pixels to the output file
						colorgrid = grid[row][col].getBackground();
						red[row][col] = colorgrid.getRed();
						green[row][col] = colorgrid.getGreen();
						blue[row][col] = colorgrid.getBlue();
					} 
				}

		if (whichButton == this.downsample) 
		{ 

			int redDS = 0; //create temporary arrays for red, blue and green arrays so that they can be populated with data from the original arrays
			int greenDS = 0;
			int blueDS = 0;
			//sumDS = 0;
			int pixelAverage = 0;
			int sumPixelAverage = 0;

			int redCount = 0; //to keep a count for RGB values for 16 pixels so we can get their average
			int blueCount = 0;
			int greenCount = 0;

			int blueCountAverage = 0;
			int greenCountAverage = 0;
			int redCountAverage = 0;

			redWrite = new int[rowsDS][colsDS];
			blueWrite = new int[rowsDS][colsDS];
			greenWrite = new int[rowsDS][colsDS];

			//use a doubly nested for loop to populate our temporary arrays with the average of scale numbers of 4 pixels. Integrates 4 pixels into one pixel essentially.

			int countRow = 0;
			int countCol = 0;

			while (countRow < rowsDS)
			{
				for (int ir = (countRow*4); ir < (countRow*4)+4 ; ir++)
				{
					for (int ic = (countCol*4); ic < (countCol*4)+4; ic++)
					{
						redDS =  red[ir][ic];
						blueDS = blue[ir][ic];
						greenDS = green[ir][ic];

						redCount += redDS;
						blueCount += blueDS;
						greenCount += greenDS;

						//sumDS += redDS + blueDS + greenDS; 

					}		
				}

				redCountAverage = redCount/16;
				blueCountAverage = blueCount/16;
				greenCountAverage = greenCount/16;

				if (redCountAverage > 0 || greenCountAverage > 0 || blueCountAverage > 0)
				{
					redCountAverage = 255;
					blueCountAverage = 255;
					greenCountAverage = 255;
				}

				redWrite[countRow][countCol] = redCountAverage;
				blueWrite[countRow][countCol] = blueCountAverage;
				greenWrite[countRow][countCol] = greenCountAverage;

				sumPixelAverage += redCountAverage + blueCountAverage + greenCountAverage;
				//sumDS = 0;
				redCount = 0;
				blueCount = 0;
				greenCount = 0;

				countCol++;

				if (countCol == rowsDS)
				{
					countRow++;
					countCol = 0;
				}
			}


			averageDS = sumPixelAverage/ (rowsDS*colsDS);

			statusbar.setText("You have downsampled your image!");

		}


		else if (whichButton == this.saveButton)
		{ 
			try{

				BufferedWriter outputFile = new BufferedWriter (new FileWriter("shivsave.ppm")); //makes object outputFile using input object fileName which is parameter

				outputFile.write("P3" + "\n"); //write data to output file in specified format 
				outputFile.write("#PPM File" + "\n"); //we don't use System.out.println since we don't want to print these out while executing - we want these values to be saved to our new file
				outputFile.write(rowsDS + " " + colsDS + "\n");
				outputFile.write("255\n");
				
				for (int row = 0; row < rowsDS; row++)
				{ //use doubly nested for loops to write contents of all nested arrays to our file
					for (int col = 0; col < colsDS; col++)
					{
						outputFile.write(redWrite[row][col]+ " ");
						outputFile.write(greenWrite[row][col] + " ");
						outputFile.write(blueWrite[row][col] + " ");
					} 
				}

				outputFile.close(); //close file

				}

			catch(Exception e2)
			{ //catch exception
				System.out.println( e2.getMessage() );
			}

		}

		else if (whichButton == this.quitButton)
		{ //if button pressed is quitButton, quit program
			System.exit(0);
		}

		else if (whichButton == this.compareButton)
		{
		 	whatIsMyDigit(); //once compare button has been hit, call whatIsmyDigit() method to compare against values of averages stored
		 					 //in array

		 	//need to still return this value for digit in some sort of status bar at the bottom of the program
		}

		else if (whichButton == this.clearButton)
		{
			for (int row = 0; row < setRows; row++)
			{
				for (int col = 0; col < setCols; col++)
				{
					grid[row][col].setBackground(Color.BLACK); //set JLabel backgrounds black to make identification easier
					grid[row][col].setOpaque(true);
				}
			}

		}
	}

	public void whatIsMyDigit()
	{ 
		arrayOfAverageReferenceValues();

		float distance = Math.abs(array[0] - averageDS);

			for (int i = 1; i < array.length; i++)
			{
				float comparisonDistance = Math.abs(array[i] - averageDS);

				if(comparisonDistance < distance)
				{
					
					distance = comparisonDistance;
					indexOfAverageArrayValue = i;
				}
		}

			statusbar.setText("The number that you have sketched is possibly " + indexOfAverageArrayValue);
	}
}