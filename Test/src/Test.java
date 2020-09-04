import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Test {
	public static void main(String[] args) {

		// The name of the file to open.
		String fileName = "temp.txt";
		int counter = 0;

		// This will reference one line at a time
		String line = null;
		FileReader fileReader = null;

		try {
			// FileReader reads text files in the default encoding.
			fileReader = new FileReader(fileName);

			// Always wrap FileReader in BufferedReader.
			@SuppressWarnings("resource")
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				counter++;
				if (counter == 3 || counter == 8 || counter == 12) {
					// do your code
				}
			}

		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		} finally {
			if (fileReader != null) {
				// Always close files.

			}
		}
	}
}