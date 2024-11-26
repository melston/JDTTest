package com.jdttst.testsrc.impl;

import com.jdttst.testsrc.base.IF1;
import com.jdttst.testsrc.base.IF2;
import com.jdttst.testsrc.base.IF3;
import com.jdttst.testsrc.base.PType1;

public class C3 implements IF3 {
	
	public enum C3Enum {
		V1, V2, V3
	};

	@Override
	public IF1 getIF1() {
		return null;
	}

	@Override
	public PType1<Double> getPT1(IF1 f1, IF2 f2) {
		return null;
	}

	interface CIF4 {
		int internalMethod1(C3 c3);
	}
}
