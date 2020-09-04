package linuxoraclehealthcheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
/**
 * @PluginDesc:Oracle Health Check On Linux
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :File IO Management
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 */
public class FileClassFinder1 {
	private JarFile file;
	@SuppressWarnings("unused")
	private boolean trouble;

	public FileClassFinder1(String filePath) {
		try {
			file = new JarFile(filePath);
		} catch (IOException e) {
			trouble = true;
		}
	}

	public List<String> findCls(String pkg) {
		ArrayList<String> classes = new ArrayList<String>();
		Enumeration<JarEntry> entries = file.entries();
		while (entries.hasMoreElements()) {
			JarEntry cls = entries.nextElement();
			if (!cls.isDirectory()) {
				String fileName = cls.getName();
				String className = fileName.replaceAll("/", ".").replaceAll(File.pathSeparator, ".").substring(0,
						fileName.lastIndexOf('.'));				
				if (className.startsWith(pkg))
					classes.add(className.substring(pkg.length() + 1));
			}
		}		
		return classes;
	}
}