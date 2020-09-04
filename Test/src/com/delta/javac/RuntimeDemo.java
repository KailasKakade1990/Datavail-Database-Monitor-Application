package com.delta.javac;

public class RuntimeDemo {

	public static void main(String[] args) {
		try {

			// print a message
			System.out.println("Executing notepad.exe");

			// create a process and execute notepad.exe

			String cmd = " systemctl list-unit-files | grep mysql.service";

			Process process = Runtime.getRuntime().exec(cmd);

			// print another message
			System.out.println("Notepad should now open.");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}