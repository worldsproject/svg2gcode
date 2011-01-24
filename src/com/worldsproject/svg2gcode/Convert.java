package com.worldsproject.svg2gcode;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Convert
{
	private boolean debug = false;
	
	public Convert(int width, int height, boolean debug, String inFile, String outFile)
	{
		this.debug = debug;
		
		File in = new File(inFile);
		File out = new File(outFile);
		
		Document xml = getDocument(in);
		
		if(debug)
			System.out.println("Normalizing Elements...");
		xml.getDocumentElement().normalize();
		
		NodeList lineElements = getLines(xml);
		
		if(debug)
			System.out.println("Preparing to generate Gcode file.");
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
		finally
		{
			System.out.println("Hit Error, exiting");
			System.exit(1);
		}
		
		return null;
	}
	
	private NodeList getLines(Document xml)
	{
		if(debug)
			System.out.println("Retrieving all Line Elements");
		
		NodeList rv = xml.getElementsByTagName("line");
		
		int len = rv.getLength();
		
		if(debug)
			System.out.println("There are " + len + " line elements.");
		
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
	
	public static void main(String[] args)
	{
		int width = 800;
		int height = 800;
		
		boolean debug = false;
		
		String inFile = null;
		String outFile = null;
		
		for(int i =0; i < args.length; i++)
		{
			if(args.length == 0)
			{
				System.out.println("SVG to Gcode usage:");
				System.out.println("\tjava Convert -f file (-h height | -w width | -d | -o file)");
				System.out.println("\t-f The location of the file. This is required.");
				System.out.println("\t-h The maximum print height.");
				System.out.println("\t-w The maximum print width.");
				System.out.println("\t-d Turn on debug messages.");
				System.out.println("\t-o Location of the output.");
				
				System.exit(0);
			}
			
			if(args[i].equals("-w"))
			{
				try
				{
					width = Integer.parseInt(args[i + 1]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Argument for '-w' must be an integer.");
					System.out.println("Defaulting to 800mm wide.");
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for '-w' must exist.");
					System.out.println("Defaulting to 800mm wide.");
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
					System.out.println("Defaulting to 800mm high.");
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					System.out.println("Argument for '-h' must be an integer.");
					System.out.println("Defaulting to 800mm high.");
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
		}
		
		if(outFile == null)
		{
			outFile = "output.gcode";
			System.out.println("Output file is defaulting to ./output.gcode");
		}
		
		new Convert(width, height, debug, inFile, outFile);
	}
}
