package com.jdttst.testsrc.impl;

import com.jdttst.testsrc.base.IF1;
import com.jdttst.testsrc.base.IF2;

public class C2 implements IF2 {

	@Override
	public IF1 getIF1() {
		return new C1();
	}

}
