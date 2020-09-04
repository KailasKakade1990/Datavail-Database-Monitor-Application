package com.delta.test;

public class TestClass {

	static int i1;
	static int b1;

	TestClass(int i) {
		this.i1 = i;
	}

	TestClass(int i, int b) {
		this.b1 = b;
	}

	public static void main(String[] args) {

		TestClass obj1 = new TestClass(10);
		TestClass obj2 = new TestClass(10, 20);

		obj1 = obj2;
		obj2 = obj1;

	}

}
