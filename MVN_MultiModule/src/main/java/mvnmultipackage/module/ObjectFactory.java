package mvnmultipackage.module;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.ObjectFactoryBuilder;

public class ObjectFactory implements ObjectFactoryBuilder {

	public javax.naming.spi.ObjectFactory createObjectFactory(Object obj, Hashtable<?, ?> environment)
			throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	void makedata() {
		System.out.println("Kailas");
	}

}
