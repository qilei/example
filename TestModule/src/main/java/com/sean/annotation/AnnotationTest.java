package com.sean.annotation;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: huixiao200068
 * Date: 13-3-19
 * Time: 下午5:30
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationTest {
    public void parseMethod(Class<?> cls) {
        Object obj;
        try{
            obj = cls.getConstructor(new Class[]{}).newInstance(new Object[]{});
            Method[] methods = cls.getDeclaredMethods();
            for(Method method : methods) {
                HelloWorld annotation = method.getAnnotation(HelloWorld.class);
                String name = "";
                if(annotation != null) {
                    name = annotation.name();
                }
                method.invoke(obj,name);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        AnnotationTest at = new AnnotationTest();
        at.parseMethod(SayHello.class);
        System.out.println(System.getProperty("user.dir"));
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources("com/sean/annotation");

        while(dirs.hasMoreElements()){
            System.out.println(dirs.nextElement());
        }

    }
}
