package com.jpmorgan.calculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jpmorgan.calculator.portfolio.operations.FundOperations;
import com.jpmorgan.calculator.portfolio.operations.FundOperationsImpl;

/**
 *  1. App reads data from command line
 *  2. stores in a temp file
 *  3. parses temp file to generate funds data
 *  4. iterate through all leaf nodes and prints fund weight
 *  5. read end of day market value from command line and prints weighted return
 *  6. deletes temp file and closes all resources on exit
 *  
 *
 */
public class App {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws IOException {

		String enterString = "Enter Fund Details on command line, continue to terminate scanning.";
		LOGGER.info(enterString);

		File fundsData = null;
		
		// read from console
		try (Scanner scanner = new Scanner(new InputStreamReader( System.in ))) {

			fundsData = readFundDataToTempFile( scanner );
			if (fundsData != null) {
				
				// apply operations, token is ","
				FundOperations operations = new FundOperationsImpl();
				applyOperations( fundsData, operations );

				// calc and print weighted return
				String enterEndOfMarketValueString = "Enter End of Market Fund Value on command line.";
				LOGGER.info( enterEndOfMarketValueString );
				String line = scanner.nextLine();
				try {
					// attempt to parse and persist fund data
					int endOfmarketValue = Integer.parseInt( line );
					operations.printWeightedReturn( endOfmarketValue );
				} catch ( NumberFormatException ne ) {
					// ignore fund
					LOGGER.error( "Error parsing fundValue, exiting... " );
				}
			}
		} finally {
			
			// delete file on exit
			if (fundsData != null) {
				// delete data file
				fundsData.deleteOnExit();
			}
		}
	}

	private static File readFundDataToTempFile( Scanner scanner ) throws IOException {

		String line = "";
		File temp = null;
		BufferedWriter writer = null;

		while (!(line = scanner.nextLine()).equalsIgnoreCase( "continue" )) {

			if (temp == null) {

				temp = File.createTempFile( "fundsData", ".out" );
				writer = new BufferedWriter( new FileWriter( temp ) );
			}
			// get command line data
			writer.write( line );
			// new line
			writer.write( System.lineSeparator() );
		}

		// dump to file
		if ( writer != null ) {
			writer.close();
		}

		// sanity check if file empty, dont do any thing
		if ( temp != null && temp.length() == 0) {

			LOGGER.error( "No fund data defined" );
			return null;
		}

		return temp;
	}

	private static void applyOperations(File fundsData, FundOperations operations) throws IOException {

		// create a reader against temp file
		try (BufferedReader reader = new BufferedReader( new FileReader( fundsData ) )) {

			// apply operations, token is ","
			operations.parseFundData( reader, "," );

			// print grand children fund weights against grandParent or root
			// fund

			List<String> grandChildrenFunds = operations.getAllGrandChildrenFunds();
			for (String grandChildFund : grandChildrenFunds) {
				
				String fundWeight = operations.getFundWeight( grandChildFund );
				operations.printGrandChildFundDetails( grandChildFund, fundWeight );
			}
		}
	}
}
