package com.jdttst.testsrc.impl;

import com.jdttst.testsrc.base.*;

public class CType1 implements PType1<Integer> {
	
	@Override
	public Integer doIt(int b) { return 1; }
	
	@Override
	public int doIt1a() { return 2; }
}
