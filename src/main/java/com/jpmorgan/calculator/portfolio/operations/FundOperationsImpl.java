package com.jpmorgan.calculator.portfolio.operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jpmorgan.calculator.portfolio.Fund;

public class FundOperationsImpl implements FundOperations {

	public static final Logger LOGGER = LoggerFactory.getLogger(FundOperationsImpl.class);

	private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.###");

	private static final DecimalFormat TWO_PLACES_DECIMAL_FORMATTER = new DecimalFormat("#.##");

	private Fund rootOrGrandParentFund = null;

	// using LinkedHashMap if need is to iterate on map
	// in the way data is being persisted in the map
	private Map<String, Fund> fundMap = new LinkedHashMap<>();

	// leaf nodes for existing root/grandparent fund
	private List<String> grandChildrenFunds = new ArrayList<>();

	// Public methods

	@Override
	public void parseFundData(BufferedReader reader, String token) throws IOException {

		String fundData = null;
		Set<String> parentFunds = new HashSet<>();

		while ((fundData = reader.readLine()) != null) {

			String[] fundDetails = fundData.split(token);
			if (fundDetails.length < 3) {

				// ignore fund
				LOGGER.error("Ignoring fund {}, expected minimum length of 3", fundData);
				continue;
			}

			try {

				// attempt to parse and persist fund data
				int fundValue = Integer.parseInt(fundDetails[2]);
				persistFund(fundDetails[0], fundDetails[1], fundValue);
				// add parent fund
				parentFunds.add(fundDetails[0]);
			} catch (NumberFormatException ne) {
				// ignore fund
				LOGGER.error("Error parsing fundValue, fund {} ", fundData);
			}
		}

		// make an effort only if fundMap is not empty
		if (fundMap.size() > 0) {
			computeFundDetails(parentFunds);
		}
	}

	@Override
	public String getFundWeight(String grandChildFundName) {

		// get the fund details
		Fund fund = fundMap.get(grandChildFundName);
		// calculate fund weight
		float fundWeight = calculateFundWeight(fund, rootOrGrandParentFund);
		// format
		return DECIMAL_FORMATTER.format(fundWeight);
	}

	/**
	 * formula: return = (EMV-BMV)/BMV
	 */
	@Override
	public String getWeightedReturn( int endOfDayMarketValue ) {
		
		if (rootOrGrandParentFund == null) {
			// set rootFund
			throw new RuntimeException("RootOrGrandParent fund cannot be null");
		}

		if (rootOrGrandParentFund.getFundValue() == 0) {
			// set rootFund
			throw new RuntimeException("RootOrGrandParent fund value cannot be zero");
		}
		
		// calc weighted return
		return calculateWeightedReturn( endOfDayMarketValue );
	}
	
	/**
	 * formula: return = (EMV-BMV)/BMV
	 */
	@Override
	public void printWeightedReturn( int endMarketValue ) {

		String weightedReturn = getWeightedReturn( endMarketValue );

		// print weighted return
		LOGGER.info("Portfolio: {}, WeightedReturn: {}%, EndMarketValue: {}, BeginingMarketValue: {}",
				rootOrGrandParentFund.getFundName(), weightedReturn,
				endMarketValue, rootOrGrandParentFund.getFundValue());
	}

	

	// return all leaf nodes
	@Override
	public List<String> getAllGrandChildrenFunds() {

		return grandChildrenFunds;
	}

	/**
	 * Print all grand children fund weights
	 * 
	 * Iterate through fundMap by grandChildrenFunds
	 * 
	 * for each grandChild a. calc fund weight b. print rootFund, fund, fund
	 * weight
	 */
	@Override
	public void printGrandChildFundDetails( String grandChildFundName, String fundWeight ) {

		// get the fund details
		Fund fund = fundMap.get( grandChildFundName );
		
		// log GrandParent, GrandChild, FundWeight of grandChild
		LOGGER.info("{},{},{}", rootOrGrandParentFund.getFundName(), fund.getFundName(),
				fundWeight);
	}
	

	/**
	 *  Print all grand children fund weights in portfolio
	 */
	@Override
	public void printAllGrandChildrenFundWeights() {
		printGrandChildrenFundWeights(rootOrGrandParentFund);
		
	}

	// Private Methods

	private void persistFund(String parentFundName, String fundName, int fundValue) {
		// generate fund node and persist in map

		Fund fund = new Fund(parentFundName, fundName, fundValue);
		fundMap.put(fundName, fund);
	}

	/**
	 * Assumption is not always operation would expect to start with rootFund,
	 * fund, fundValue
	 * 
	 * User can arbitarily type in any order and compute will try to guess the
	 * rootFund.
	 * 
	 * throws RuntimeException on rootFund being null
	 * 
	 * @param parentFunds
	 */
	private void computeFundDetails(Set<String> parentFunds) {

		// to identify rootFund, it should not be a key in fundMap
		// rootFund => is not a key in the map
		// e.g. B=>10,A
		// C=>10,A
		// D=>5, B
		// E=>5, B
		// F=>5, C
		// G=>5, C

		// Since A does not exist as a key in the
		// fundMap, that becomes the rootFun or
		// parent for all child and grand child funds

		Set<String> rootFunds = new HashSet<>();
		List<Fund> children = new ArrayList<>();

		for (Map.Entry<String, Fund> entry : fundMap.entrySet()) {

			String parentFund = entry.getValue().getParentFundName();

			// check if parent fund
			if (!fundMap.containsKey(parentFund)) {

				// means this is a potential grandParentOrRoot fund
				rootFunds.add(parentFund);
				// accumulator to accumulate all the funds linked to
				// parent fund
				children.add(entry.getValue());
			}

			String fundName = entry.getKey();
			// check if a lead node
			if (!parentFunds.contains(fundName)) {
				if (!grandChildrenFunds.contains(fundName)) {
					grandChildrenFunds.add(fundName);
				}
			}
		}

		// use case:
		// A,B,1000
		// B,D,500
		// B,E,500
		// C,F,1000
		// C,G,1000

		// this case system finds more than one root fund namely A & C
		// throw exception

		if (rootFunds.size() > 1) {

			// there can only be one root fund
			// throw exception
			StringBuffer buffer = new StringBuffer();
			for (String fund : rootFunds) {
				buffer.append(fund).append(",");
			}

			// replace last "," with empty string
			int lastIndex = buffer.lastIndexOf(",");
			buffer = buffer.replace(lastIndex, buffer.length(), "");

			throw new RuntimeException("Can't have more than one root funds: " + buffer.toString());
		}

		if (children.size() > 0) {

			// convert to stream and get total of all fund values in the stream
			int rootFundValue = children.stream().mapToInt(fund -> fund.getFundValue()).sum();

			// extract the root fund name
			String rootFundName = rootFunds.iterator().next();

			// rootOrGrandParent fund has not parentFund name=> always empty
			// string
			rootOrGrandParentFund = new Fund("", rootFundName, rootFundValue);

			return;
		}

		// use case:
		// A,A,1000
		// B,B,2000

		// this case system cannot find any root funds and throws exception

		throw new RuntimeException("No root fund linked to funds in portfolio");
	}

	/**
	 * 
	 * Convenience method which takes in a fund and can be extended to print
	 * fund weight against the fund.
	 * 
	 * 
	 * @param grandParentOrParentFund
	 */
	private void printGrandChildrenFundWeights(Fund grandParentOrParentFund) {

		if (grandParentOrParentFund == null) {

			// null indicates evaluate against root
			grandParentOrParentFund = rootOrGrandParentFund;
		}

		boolean isRootOrGrandParentFund = grandParentOrParentFund.getParentFundName().equals("") ? true : false;

		if (!isRootOrGrandParentFund) {

			throw new RuntimeException(
					"Fund " + grandParentOrParentFund.getFundName() + " is not rootOrGrandParentFund in Portfolio ");
		}

		for (String grandChildFund : grandChildrenFunds) {

			// get the fund details
			Fund fund = fundMap.get(grandChildFund);
			// calculate fund weight
			float fundWeight = calculateFundWeight(fund, grandParentOrParentFund);

			// log GrandParent, GrandChild, FundWeight of grandChild
			LOGGER.info("{},{},{}", grandParentOrParentFund.getFundName(), fund.getFundName(),
					DECIMAL_FORMATTER.format(fundWeight));
		}
	}

	// simple fund weight calculator
	private float calculateFundWeight(Fund fund, Fund grandParentOrParentFund) {

		if (grandParentOrParentFund == null) {
			// set rootFund
			throw new RuntimeException("RootOrGrandParent fund cannot be null");
		}

		if (grandParentOrParentFund.getFundValue() == 0) {
			// set rootFund
			throw new RuntimeException("RootOrGrandParent fund value cannot be zero");
		}

		// cast to float before divide by fund value
		float fundWeight = (float) fund.getFundValue() / grandParentOrParentFund.getFundValue();
		return fundWeight;
	}

	private String calculateWeightedReturn(int endOfMarketValue) {

		if (rootOrGrandParentFund == null) {
			// set rootFund
			throw new RuntimeException("RootOrGrandParent fund cannot be null");
		}

		if (rootOrGrandParentFund.getFundValue() == 0) {
			// set rootFund
			throw new RuntimeException("RootOrGrandParent fund value cannot be zero");
		}

		int beginingMarketValue = rootOrGrandParentFund.getFundValue();
		float weightedReturn = ((float) (endOfMarketValue - beginingMarketValue)) / (float) beginingMarketValue;

		// format
		return TWO_PLACES_DECIMAL_FORMATTER.format(100.0 * weightedReturn);
	}
}
