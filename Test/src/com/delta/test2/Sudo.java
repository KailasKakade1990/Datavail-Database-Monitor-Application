package com.delta.test2;

public class Sudo {

	public static void main(String[] args) {
		String str = "EXPIRED & LOCKED";
		str = str.replaceAll("&", "&amp;");
		System.out.println(str);

	}

}
