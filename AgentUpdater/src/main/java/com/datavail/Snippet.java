package com.datavail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
 * @File_Desc:AgentUpdater Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class Snippet {
	public static void main() {
		String command = "ps -A -U " + System.getProperty("user.name") + " -d";
		try {
			String line;
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}
