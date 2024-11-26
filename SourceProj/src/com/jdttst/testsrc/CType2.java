package com.jdttst.testsrc;

import java.util.Map;

public class CType2 implements PType1<Integer>, PType2<Double, Boolean> {
	
	@Override
	public Integer doIt() { return 1; }
	@Override
	public Double doIt2() { return 2.0; }
	@Override
	public Boolean doIt3() { return false; }
	
	Map<String, Double> getMap() { return null; }
}
