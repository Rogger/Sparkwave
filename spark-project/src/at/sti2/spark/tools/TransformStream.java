package at.sti2.spark.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class TransformStream {

	public TransformStream(File inputFile){
		
		System.out.println("Transforming file " + inputFile.getName() + " to nTriples format...");
		
		try {
			//Openning input file
			BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFile));
			
			//Opening output file
			
			PrintWriter outputWriter = new PrintWriter(new FileWriter("output.nt"));
			
			String line;
			while ((line = inputFileReader.readLine())!= null){
				StringTokenizer lineTokenizer = new StringTokenizer(line);
				StringBuffer outputBuffer = new StringBuffer();
				while (lineTokenizer.hasMoreTokens()){
					String token = lineTokenizer.nextToken();
					outputBuffer.append('<');
					outputBuffer.append(token);
					outputBuffer.append("> ");
				}
				outputBuffer.append('.');
				outputWriter.println(outputBuffer.toString());
			}
			
			outputWriter.flush();
			outputWriter.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		if (args.length != 1){
			System.out.println("This program transforms .stream files to .nt files. As input it expects a .stream file name.");
			System.exit(0);
		}
		
		File inputFile = new File(args[0]);
		if (!inputFile.exists()){
			System.out.println("File with the given name does not exist.");
			System.exit(0);
		}
		
		new TransformStream(inputFile);
	}
}
