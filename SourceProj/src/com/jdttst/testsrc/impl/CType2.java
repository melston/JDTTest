package com.jdttst.testsrc.impl;

import java.util.Map;

import com.jdttst.testsrc.base.*;

public class CType2 implements PType1<Integer>, PType2<Double, Boolean> {
	
	@Override
	public Integer doIt(int a) { return 1; }
	@Override
	public int doIt1a() { return 2; }
	@Override
	public Double doIt2() { return 2.0; }
	@Override
	public Boolean doIt3(IF3 inst) { return false; }
	
	Map<String, Double> getMap() { return null; }
}
