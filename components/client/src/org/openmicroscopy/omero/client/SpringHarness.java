/*
 * Created on Jul 21, 2004
 */
package org.openmicroscopy.omero.client;

import java.net.URL;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/** provides all objects needed for working with the meta package 
 * within the Spring Framework.
 * @author moore
 */
public class SpringHarness {

    private final static String springConfFile = "classpath:org/openmicroscopy/omero/client/spring.xml";
    public static URL path;
    public static ApplicationContext ctx;
    
    static {
        path = SpringHarness.class.getClassLoader().getResource(springConfFile);
        if (path==null){
            throw new RuntimeException("Can't find spring.xml on classpath");
        }
        ctx = new FileSystemXmlApplicationContext(path.toString());
    }
	
    
}
