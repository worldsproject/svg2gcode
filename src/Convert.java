
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Convert
{
	private boolean debug = false;
	private int feedRate = 5000;
	private int accuracy = 60;
	
	public Convert(int width, int height, int feedRate, int accuracy, boolean debug, String inFile, String outFile)
	{
		this.debug = debug;
		this.feedRate = feedRate;
		this.accuracy = accuracy;
		
		File in = new File(inFile);
		File out = new File(outFile);
		
		if(debug)
		{
			System.out.println("Reading From: " + in.getAbsolutePath());
			System.out.println("Writing To: " + out.getAbsolutePath());
		}
		
		Document xml = getDocument(in);
		
		if(debug)
			System.out.println("Normalizing Elements...");
		xml.getDocumentElement().normalize();
		
		if(debug)
			System.out.println("Retreiving all elements now.");
			
		NodeList lineElements = getElements(xml, "line");
		NodeList rectangleElements = getElements(xml, "rect");
		NodeList polylineElements = getElements(xml, "polyline");
		NodeList polygonElements = getElements(xml, "polygon");
		NodeList circleElements = getElements(xml, "circle");
		NodeList ellipseElements = getElements(xml, "ellipse");
		
		if(debug)
			System.out.println("Preparing to generate Gcode file.");
		
		BufferedWriter bw = openFile(out);
		
		writeLine("G21", bw);
		writeLine("G90", bw);
		writeLine("G0 X0 Y0 Z5", bw);
		
		generateLineElements(lineElements, bw);
		generateRectangleElements(rectangleElements, bw);
		generatePolylineElements(polylineElements, bw);
		generatePolygonElements(polygonElements, bw);
		generateCirleElements(circleElements, bw);
		generateEllipseElements(ellipseElements, bw);
		
		if(debug)
			System.out.println("Wrapping up now...");
		
		writeLine("G0 Z10", bw);
		writeLine("G0 X0 Y0 Z5", bw);
		
		try
		{
			bw.flush();
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if(debug)
			System.out.println("All done! Yay, it worked! *whew*");
	}
	
	private Document getDocument(File in)
	{
		if(debug)
			System.out.println("Preparing to read in XML document.");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try
		{
			db = dbf.newDocumentBuilder();
			return db.parse(in);
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private NodeList getElements(Document xml, String element)
	{
		if(debug)
			System.out.println("Retrieving all " + element + " elements");
		
		NodeList rv = xml.getElementsByTagName(element);
		
		int len = rv.getLength();
		
		if(debug)
			System.out.println("There are " + len + " " + element + " elements.");
		
		if(len != 0)
		{
			return rv;
		}
		else
		{
			if(debug)
				System.out.println("There are 0 elements, thus returning null");
			
			return null;
		}	
	}
		
	private BufferedWriter openFile(File out)
	{
		if(debug)
			System.out.println("Opening File.");
		
		BufferedWriter bw = null; 
		
		try
		{
			bw = new BufferedWriter(new FileWriter(out));
		}
		catch(IOException e)
		{
			System.out.println("Hit Error, exiting");
			e.printStackTrace();
		}
			
		return bw;
	}
	
	private void generateLineElements(NodeList lineElements, BufferedWriter out)
	{
		if(debug)
			System.out.println("Going through lines...");
		
		if(lineElements != null)
		{
			int len = lineElements.getLength();
			
			for(int i = 0; i < len; i++)
			{
				if(debug)
					System.out.println("Doing line " + (i + 1) + " of " + len + " lines.");
				
				Element temp = (Element)lineElements.item(i);
				
				int x1 = 0;
				int x2 = 0;
				int y1 = 0;
				int y2 = 0;
				
				try
				{
					x1 = Integer.parseInt(temp.getAttribute("x1"));
					x2 = Integer.parseInt(temp.getAttribute("x2"));
					y1 = Integer.parseInt(temp.getAttribute("y1"));
					y2 = Integer.parseInt(temp.getAttribute("y2"));
				}
				catch(NumberFormatException e)
				{
					if(debug)
						System.out.println("Looks like there is something wrong with the line, skipping...");
					continue;
				}
				
				writeLine("G1 F" + feedRate + " X" + x1 + " Y" + y1, out);
				writeLine("G0 Z0", out);
				writeLine("G0 F" + feedRate + " X" + x2 + " Y" + y2, out);
				writeLine("G0 Z5", out);
			}
		}
		else
		{
			if(debug)
				System.out.println("No lines to go through, moving on.");
		}
	}
	
	private void generateRectangleElements(NodeList rectangleElements, BufferedWriter out)
	{
		if(debug)
			System.out.println("Going through rectangles...");
		
		if(rectangleElements != null)
		{
			int len = rectangleElements.getLength();
			
			for(int i = 0; i < len; i++)
			{
				if(debug)
					System.out.println("Doing rectangle " + (i + 1) + " of " + len + " rectangles.");
				
				Element temp = (Element)rectangleElements.item(i);
				
				int x = 0;
				int y = 0;
				int width = 0;
				int height = 0;
				
				try
				{
					x = Integer.parseInt(temp.getAttribute("x"));
					y = Integer.parseInt(temp.getAttribute("y"));
					width = Integer.parseInt(temp.getAttribute("width"));
					height = Integer.parseInt(temp.getAttribute("height"));
				}
				catch(NumberFormatException e)
				{
					if(debug)
						System.out.println("Looks like there is something wrong with the rectangle, skipping...");
					continue;
				}
				
				writeLine("G0 X" + x + " Y" + y, out);
				writeLine("G0 Z0", out);
				writeLine("G91", out);
				writeLine("G0 X" + height, out);
				writeLine("G0 Y" + width, out);
				writeLine("G0 X" + (-height), out);
				writeLine("G0 Y" + (-width), out);
				writeLine("G90", out);
				writeLine("G0 Z5", out);
			}
		}
		else
		{
			if(debug)
				System.out.println("No Rectangles to go through, moving on.");
		}
	}
	
	private void generatePolylineElements(NodeList polylineElements, BufferedWriter out)
	{
		if(debug)
			System.out.println("Going through Polylines");
			
		if(polylineElements != null)
		{
			int len = polylineElements.getLength();
			
			for(int i = 0; i < len; i++)
			{
				if(debug)
					System.out.println("Doing polyline " + (i + 1) + " of " + len + " polyline.");
					
				Element temp = (Element)polylineElements.item(i);
				String p = temp.getAttribute("points");
				
				String[] points = p.split(" ");
				
				if(points.length < 3)
				{
					if(debug)
						System.out.println("There are not enough points for a polyline, skipping...");
					continue;
				}
				
				for(int j = 0; j < points.length; j++)
				{
					String[] xy = points[i].split(",");
					
					if(j == 0)
					{
						if(debug)
							System.out.println("First Line, positioning to start point.");
						
						writeLine("G0 X" + xy[0] + " Y" + xy[1], out);
						writeLine("G0 Z0", out);
						continue;
					}
					
					if(debug)
						System.out.println("Drawing line number " + (j + 1) + " of " + points.length + " lines. (Polyline #" + j + ")");
						
					writeLine("G1 F" + feedRate + " X" + xy[0] + " Y" + xy[1], out);
				}
				
				writeLine("G0 Z5", out);
			}
		}
		else
		{
			if(debug)
				System.out.println("No Polylines to go through, moving on.");
		}
	}
	
	private void generatePolygonElements(NodeList polygonElements, BufferedWriter out)
	{
		if(debug)
			System.out.println("Going through polygons");
			
		if(polygonElements != null)
		{
			int len = polygonElements.getLength();
			
			for(int i = 0; i < len; i++)
			{
				if(debug)
					System.out.println("Doing polygon " + (i + 1) + " of " + len + " polygons.");
					
				Element temp = (Element)polygonElements.item(i);
				String p = temp.getAttribute("points");
				
				String[] points = p.split(" ");
				
				if(points.length < 3)
				{
					if(debug)
						System.out.println("There are not enough points for a polygon, skipping...");
					continue;
				}
				
				String startX = "0";
				String startY = "0";
				
				for(int j = 0; j < points.length; j++)
				{
					String[] xy = points[i].split(",");
					
					if(j == 0)
					{
						if(debug)
							System.out.println("First Line, positioning to start point.");
							
						startX = xy[0];
						startY = xy[1];
						
						writeLine("G0 X" + xy[0] + " Y" + xy[1], out);
						writeLine("G0 Z0", out);
						continue;
					}
					
					if(debug)
						System.out.println("Drawing line number " + (j + 1) + " of " + points.length + " lines. (Polygon #" + i + ")");
						
					writeLine("G1 F" + feedRate + " X" + xy[0] + " Y" + xy[1], out);
					
					if(j == points.length -1)
					{
						if(debug)
							System.out.println("Drew all lines, returning to start of polygon.");
							
						if(xy[0].equals(startX) == false || xy[1].equals(startY) == false)
						{
							writeLine("G1 F" + feedRate + " X" + startX + " Y" + startY, out);
						}
					}
				}
				
				writeLine("G0 Z5", out);
			}
		}
		else
		{
			if(debug)
				System.out.println("No Polygons to go through, moving on.");
		}
	}
	
	private void generateCirleElements(NodeList circleElements, BufferedWriter out)
	{
		if(debug)
			System.out.println("Going through circles...");
		
		if(circleElements != null)
		{
			int len = circleElements.getLength();
			
			for(int i = 0; i < len; i++)
			{
				if(debug)
					System.out.println("Doing circle " + (i + 1) + " of " + len + " circles.");
				
				Element temp = (Element)circleElements.item(i);
				
				int x = 0;
				int y = 0;
				int radius = 0;
				
				try
				{
					x = Integer.parseInt(temp.getAttribute("cx"));
					y = Integer.parseInt(temp.getAttribute("cy"));
					radius = Integer.parseInt(temp.getAttribute("r"));
				}
				catch(NumberFormatException e)
				{
					if(debug)
						System.out.println("Somthing is wrong with circle #" + (i+1) + ", skipping");
					continue;
				}
				
				LinkedList<DPoint> list = new LinkedList<DPoint>();
				
				double amount = 360/accuracy;
				
				for(double k = 0; k < 360; k += amount)
				{
					double tempx = x + radius * (Math.cos(k));
					double tempy = y + radius * (Math.sin(k));
					
					list.add(new DPoint(tempx, tempy));
				}
				
				writeLine("G0 X" + list.removeFirst().x + " Y" + list.removeFirst().y, out);
				writeLine("G0 Z0", out);
				
				for(DPoint p : list)
				{
					writeLine("G1 F" + feedRate + " X" + p.x + " Y" + p.y, out);
				}
			}
		}
		else
		{
			if(debug)
				System.out.println("No circles to go through, moving on.");
		}
	}
	
	private void generateEllipseElements(NodeList ellipseElements, BufferedWriter out)
	{
		if(debug)
			System.out.println("Going through ellipse...");
		
		if(ellipseElements != null)
		{
			int len = ellipseElements.getLength();
			
			for(int i = 0; i < len; i++)
			{
				if(debug)
					System.out.println("Doing ellipse " + (i + 1) + " of " + len + " ellipse.");
				
				Element temp = (Element)ellipseElements.item(i);
				
				int x = 0;
				int y = 0;
				int radiusx = 0;
				int radiusy = 0;
				
				try
				{
					x = Integer.parseInt(temp.getAttribute("cx"));
					y = Integer.parseInt(temp.getAttribute("cy"));
					radiusx = Integer.parseInt(temp.getAttribute("rx"));
					radiusy = Integer.parseInt(temp.getAttribute("ry"));
				}
				catch(NumberFormatException e)
				{
					if(debug)
						System.out.println("Somthing is wrong with ellipse #" + (i+1) + ", skipping");
					continue;
				}
				
				if(debug)
					System.out.println("Drawing the top half of the ellipse.");
				
				writeLine("G0 X" + (x + radiusx) + " Y" + y, out);
				writeLine("G0 Z0", out);
				writeLine("G2 F" + feedRate + " X" + (x - radiusx) + " Y" + y + " J" + radiusy, out);
				
				if(debug)
					System.out.println("Drawing the bottom half of the ellipse.");
					
				writeLine("G2 F" + feedRate + " X" + (x + radiusx) + " Y" + y + " J" + (-radiusy), out);
				writeLine("G0 Z5", out);
			}
		}
		else
		{
			if(debug)
				System.out.println("No ellipses to go through, moving on.");
		}
	}
	
	private void writeLine(String line, BufferedWriter bw)
	{
		try
		{
			bw.write(line);
			bw.newLine();
		}
		catch (IOException e)
		{
			if(debug)
				System.out.println("We seem to have hit a fatal error while writing, exiting...");
			
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		int width = 200;
		int height = 200;
		int fr = 5000;
		int acc = 60;
		
		boolean debug = false;
		
		String inFile = null;
		String outFile = null;
		
		if(args.length == 0)
		{
			System.out.println("SVG to Gcode usage:");
			System.out.println("\tjava Convert -f file (-h height | -w width | -d | -o file | -r rate)");
			System.out.println("\t-f The location of the file. This is required.");
			System.out.println("\t-h The maximum print height. The default is 800mm.");
			System.out.println("\t-w The maximum print width. The default is 800mm.");
			System.out.println("\t-d Turn on debug messages.");
			System.out.println("\t-o Location of the output.");
			System.out.println("\t-r The feedrate of X and Y. The default is 1000mm/s");
			System.out.println("\t-A The degree of accuracy curves are drawn to.");
			
			System.exit(0);
		}
		
		for(int i =0; i < args.length; i++)
		{
			
			
			if(args[i].equals("-w"))
			{
				try
				{
					width = Integer.parseInt(args[i + 1]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Argument for '-w' must be an integer.");
					System.out.println("Defaulting to 200mm wide.");
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for '-w' must exist.");
					System.out.println("Defaulting to 200mm wide.");
				}
			}
			else if(args[i].equals("-h"))
			{
				try
				{
					height = Integer.parseInt(args[i + 1]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Argument for '-h' must be an integer.");
					System.out.println("Defaulting to 200mm high.");
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for '-h' must be an integer.");
					System.out.println("Defaulting to 200mm high.");
				}
			}
			else if(args[i].equals("-d"))
			{
				debug = true;
			}
			else if(args[i].equals("-f"))
			{
				try
				{
					inFile = args[i + 1];
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for '-f' must exist.");
					System.exit(2);
				}
			}
			else if(args[i].equals("-o"))
			{
				try
				{
					outFile = args[i + 1];
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for '-o' must exist.");
				}
			}
			else if(args[i].equals("-r"))
			{
				try
				{
					fr = Integer.parseInt(args[i + 1]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Argument for -r must be an integer.");
					System.out.println("Defaulting to 5000.");
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for -r must exist.");
					System.out.println("Defaulting to 5000.");
				}
			}
			else if(args[i].equals("-A"))
			{
				try
				{
					acc = Integer.parseInt(args[i + 1]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Argument for -A must be an integer.");
					System.out.println("Defaulting to 60.");
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for -A must exist.");
					System.out.println("Defaulting to 60.");
				}
			}
		}
		
		if(outFile == null)
		{
			outFile = "output.gcode";
			System.out.println("Output file is defaulting to ./output.gcode");
		}
		
		new Convert(width, height, fr, acc, debug, inFile, outFile);
	}

	private class DPoint
	{
		public double x = 0;
		public double y = 0;
		
		public DPoint(double a, double b)
		{
			x = a;
			y = b;
		}
	}
}
