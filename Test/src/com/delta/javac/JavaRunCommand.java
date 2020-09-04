package com.delta.javac;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class JavaRunCommand {
	public void result() {
		try {
			Process p1 = Runtime.getRuntime().exec(new String[] { "ps", "-ef" });
			InputStream input = p1.getInputStream();
			Process p2 = Runtime.getRuntime().exec(new String[] { "grep", "mysql.service" });
			OutputStream output = p2.getOutputStream();
			IOUtils.copy(input, output);
			output.close(); //
			List<String> result = IOUtils.readLines(p2.getInputStream());
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		JavaRunCommand obj = new JavaRunCommand();
		obj.result();

	}
}