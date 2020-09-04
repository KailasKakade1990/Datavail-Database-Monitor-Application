package mvnmultipackage.module;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Application {

	public static void getdata() {

		System.out.println("Main Method In Application");
	}

	public static void careateClassObjectUsingReflection1() throws IOException {

		ClassFinder cla = new ClassFinder();

		String path = "C:/Users/kailas.kakade/DELTA/Datavail.Delta.Agent.OracleHealthCheck/target/Datavail.Delta.Agent.OracleHealthCheck-0.0.1-SNAPSHOT.jar";
		System.out.println("Path::::+++++" + path);
		FileClassFinder fl = new FileClassFinder(path);
		System.out.println("Path::::+++++" + path);

		String packages = "oraclehealthcheck";

		fl.findCls(packages);
		Method method;

		Class<?> noparam[] = { String.class, Integer.class };
		Class<?> noparamString[] = new Class[1];

		System.out.println(fl.findCls("oraclehealthcheck"));

		for (int i = 0; i < fl.findCls("oraclehealthcheck").size(); i++) {
			if (fl.findCls("oraclehealthcheck").get(i).toString().contains("Plugin")) {

				System.out.println(fl.findCls("oraclehealthcheck").get(i));
				try {
					cls = Class.forName(fl.findCls("oraclehealthcheck").get(i).toString().replaceAll("class ", ""));

				} catch (ClassNotFoundException e) {

					e.printStackTrace();
				}

				Object obj = null;

				try {
					obj = cls.newInstance();
					method = cls.getDeclaredMethod("makedata", noparam);
					String returnstr = (String) method.invoke(obj, "kk");
					System.out.println("returned" + returnstr);
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		}

	}

	public static void careateClassObjectUsingReflection() throws IOException {

		Class<?> cls;

		Class<?> noparam[] = {};
		Class<?> noparamString[] = new Class[1];
		Class<?> noparamInteger[] = new Class[1];

		List<String> classNames = new ArrayList<String>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream("D:/file.jar"));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
				// This ZipEntry represents a class. Now, what class does it
				// represent?
				String className = entry.getName().replace('/', '.'); // including
																		// ".class"
				classNames.add(className.substring(0, className.length() - ".class".length()));

				System.out.println(" classNames " + classNames);
				int length = 0;

				System.out.println("class based on name " + classNames.get(length));
				length++;

			}
		}

		try {
			cls = Class.forName(classNames.get(1));

			Object obj = cls.newInstance();
			Method method = cls.getDeclaredMethod("getdata", noparam);

			method.invoke(obj, null);

			cls = Class.forName(classNames.get(2));

			Object obj1 = cls.newInstance();
			Method method1 = cls.getDeclaredMethod("getdata", noparam);
			method1.invoke(obj1, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static Class<?> cls;

	public static void main(String[] args) {

		try {
			careateClassObjectUsingReflection1();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ClassFinder cla = new ClassFinder();
		Method method;

		Class<?> noparam[] = { String.class };
		Class<?> noparamString[] = new Class[1];

		System.out.println(cla.find("mvnmultipackage.module"));

		String path = "module-0.0.1-SNAPSHOT.jar";

		FileClassFinder fl = new FileClassFinder(path);
		for (int i = 0; i < cla.find("mvnmultipackage.module").size(); i++) {
			if (cla.find("mvnmultipackage.module").get(i).toString().contains("Plugin")) {

				System.out.println(cla.find("mvnmultipackage.module").get(i));
				try {
					cls = Class.forName(cla.find("mvnmultipackage.module").get(i).toString().replaceAll("class ", ""));

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Object obj = null;

				try {
					obj = cls.newInstance();
					method = cls.getDeclaredMethod("makedata", noparam);
					String returnstr = (String) method.invoke(obj, "kk");
					System.out.println("returned" + returnstr);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}
}
