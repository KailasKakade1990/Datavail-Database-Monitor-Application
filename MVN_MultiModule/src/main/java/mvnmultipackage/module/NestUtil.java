package mvnmultipackage.module;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class NestUtil {

	/**
	 * Get all methods of a class.
	 * 
	 * @param clazz
	 *            The class.
	 * @return All methods of a class.
	 */
	public static Collection<Method> getMethods(Class<?> clazz) {
		// if (log.isDebugEnabled()) {
		// log.debug("getMethods(Class<?>) - start");
		// }

		Collection<Method> found = new ArrayList<Method>();
		while (clazz != null) {
			for (Method m1 : clazz.getDeclaredMethods()) {
				boolean overridden = false;

				for (Method m2 : found) {
					if (m2.getName().equals(m1.getName())
							&& Arrays.deepEquals(m1.getParameterTypes(), m2.getParameterTypes())) {
						overridden = true;
						break;
					}
				}

				if (!overridden)
					found.add(m1);
			}

			clazz = clazz.getSuperclass();
		}

		// if (log.isDebugEnabled()) {
		// log.debug("getMethods(Class<?>) - end");
		// }
		return found;
	}
}
