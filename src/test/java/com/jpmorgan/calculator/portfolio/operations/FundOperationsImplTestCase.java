package com.jpmorgan.calculator.portfolio.operations;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class FundOperationsImplTestCase {

	private static FundOperations fundOperations;
	
	@BeforeClass
	public static void init() throws IOException {
		fundOperations = new FundOperationsImpl();
		BufferedReader reader = generateFundsData( );
		fundOperations.parseFundData(reader, ",");
	}

	// get fund weight from portfolio
	@Test
	public void  testGetFundWeight(  ) {
		
		List<String> grandChildrenFunds =  fundOperations.getAllGrandChildrenFunds();
		assertNotNull( grandChildrenFunds );
		assertTrue( grandChildrenFunds.size() == 5 );
		
		
		String[] leafNodes = new String[5];
		grandChildrenFunds.toArray( leafNodes );
		assertEquals( "0.167", fundOperations.getFundWeight( leafNodes[0] ) );
		assertEquals( "0.167", fundOperations.getFundWeight( leafNodes[1] ) );
		assertEquals( "0.083", fundOperations.getFundWeight( leafNodes[2] ) );
		assertEquals( "0.333", fundOperations.getFundWeight( leafNodes[3] ) );
		assertEquals( "0.333", fundOperations.getFundWeight( leafNodes[4] ) );
	}
	
	// get weighted return of portfolio
	@Test
	public void  testGetWeightedReturn(  ) {
		
		String weightedReturn = fundOperations.getWeightedReturn( 3560 );
		assertEquals( "18.67", weightedReturn );
	}
	
	@Test
	public void  testGetAllGrandChildrenFunds(){
		
		List<String> grandChildrenFunds =  fundOperations.getAllGrandChildrenFunds();
		assertNotNull( grandChildrenFunds );
		assertTrue( grandChildrenFunds.size() == 5 );
		
		String[] leafNodes = new String[5];
		grandChildrenFunds.toArray( leafNodes );
		
		//expected leaf nodes
		assertEquals( "D", leafNodes[0] );
		assertEquals( "E", leafNodes[1] );
		assertEquals( "F", leafNodes[2] );
		assertEquals( "G", leafNodes[3] );
		assertEquals( "H", leafNodes[4] );
	}
	
	private static BufferedReader generateFundsData( ) {
		
		// there are 4 leaf nodes in this sample
		String fundsData = "A,B,1000\n"
				+ "A,C,2000\n"
				+ "B,D,500\n"
				+ "B,E,500\n"
				+ "B,F,250\n"
				+ "C,G,1000\n"
				+ "C,H,1000\n";
		
		// convert String into InputStream
		InputStream is = new ByteArrayInputStream( fundsData.getBytes() );

		// read it with BufferedReader
		BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
		
		return br;
	}
}
