package com.jpmorgan.calculator.portfolio.operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public interface FundOperations {

	// read fund data and tokenize by token
	void parseFundData(BufferedReader reader, String token) throws IOException;

	// get fund weight from portfolio
	String getFundWeight(String grnadChildFundName);

	// get weighted return of portfolio
	String getWeightedReturn(int endOfDayMarketValue);

	List<String> getAllGrandChildrenFunds();

	// print the grand child fund weights vis a vis grandParent/Root fund
	void printAllGrandChildrenFundWeights( );

	// print the grand child fund weights vis a vis grandParent/Root fund
	void printGrandChildFundDetails(String fundName, String fundWeight);

	// print weighted return of portfolia against end MV
	void printWeightedReturn(int endOfDayMarketValue);

}
