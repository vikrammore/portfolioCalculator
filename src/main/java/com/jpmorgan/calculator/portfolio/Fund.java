package com.jpmorgan.calculator.portfolio;

/**
 * Fund details
 * 
 * @author Vikram
 *
 */
public class Fund {

	// parent fund
	private String mParentFundName;
	
	// fund
	private String mFundName;

	// fund value
	private int mFundValue;

	public Fund( String parentFundName, String fundName, int fundValue ) {
		
		mParentFundName = parentFundName;
		mFundName = fundName;
		mFundValue = fundValue;
	}

	public String getParentFundName() {

		return mParentFundName;
	}

	public String getFundName() {

		return mFundName;
	}
	
	public int getFundValue() {
		
		return mFundValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mFundName == null) ? 0 : mFundName.hashCode());
		result = prime * result + mFundValue;
		result = prime * result + ((mParentFundName == null) ? 0 : mParentFundName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fund other = (Fund) obj;
		if (mFundName == null) {
			if (other.mFundName != null)
				return false;
		} else if (!mFundName.equals(other.mFundName))
			return false;
		if (mFundValue != other.mFundValue)
			return false;
		if (mParentFundName == null) {
			if (other.mParentFundName != null)
				return false;
		} else if (!mParentFundName.equals(other.mParentFundName))
			return false;
		return true;
	}

}
