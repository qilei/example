package com.sean.annotation;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created with IntelliJ IDEA.
 * User: huixiao200068
 * Date: 13-3-20
 * Time: 上午11:43
 * To change this template use File | Settings | File Templates.
 */
public class Scanner {
    // 文件分隔符"\"
    private static final String FILE_SEPARATOR = File.separator;
    // package扩展名分隔符
    private static final String PACKAGE_SEPARATOR = ".";
    // java类文件的扩展名
    private static final String CLASS_FILE_EXT = ".class";
    // jar类文件的扩展名
    private static final String JAR_FILE_EXT = ".jar";
    // 应用程序classPath,形如D:/Workspaces/aop/WebRoot/WEB-INF/classes/
    private static final String APP_CLASS_PATH = Scanner.class.getClassLoader().getResource("").getPath().substring(1);

    /**
     * 获取项目的所有classpath ，包括 APP_CLASS_PATH 和所有的jar文件
     */
    private static Set<String> getAllClassPath() throws Exception {
        Set<String> set = new LinkedHashSet<String>();
        URL[] urlAry = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
        for (URL url : urlAry) {
            set.add(url.getPath());
        }
        return set;
    }

    /**
     * 获取文件下的所有文件(递归)
     */
    private static Set<File> getAllFiles(File file) {
        Set<File> files = new LinkedHashSet<File>();
        if (!file.isDirectory()) {
            files.add(file);
        } else {
            File[] subFiles = file.listFiles();
            for (File f : subFiles) {
                files.addAll(getAllFiles(f));
            }
        }
        return files;
    }

    /**
     * 获取文件下的所有.class文件
     */
    private static Set<File> getAllClass(File file) {
        // 获取所有文件
        Set<File> files = getAllFiles(file);
        Set<File> classes = new LinkedHashSet<File>();
        // 只保留.class 文件
        for (File f : files) {
            if (f.getName().endsWith(CLASS_FILE_EXT)) {
                classes.add(f);
            }
        }
        return classes;
    }

    /**
     * 得到文件夹下所有class的全包名
     */
    private static Set<String> getClassNameFromDir(File file) {
        Set<File> files = getAllClass(file);
        Set<String> names = new LinkedHashSet<String>();
        for (File f : files) {
            String fname = f.getAbsolutePath();
            // 下面的代码是把形如D:/Workspaces/aop/WebRoot/WEB-INF/classes/com/supben/main/Scanner.class
            // 的文件名转化成com.supben.main.Scanner,即去掉头部的APP_CLASS_PATH，和尾部的CLASS_FILE_EXT，再将"/"换成"."
            int begin = APP_CLASS_PATH.length();
            int end = fname.indexOf(CLASS_FILE_EXT);
            String tmpName = fname.substring(begin, end);
            names.add(tmpName.replace(FILE_SEPARATOR, PACKAGE_SEPARATOR));
        }
        return names;
    }

    /**
     * 获取jar文件里的所有class文件名
     */
    private static Set<String> getClassNameFromJar(JarFile jarFile) throws Exception {
        Enumeration entries = jarFile.entries();
        Set<String> classes = new LinkedHashSet<String>();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(CLASS_FILE_EXT)) {
                int end = name.indexOf(CLASS_FILE_EXT);
                String tmpName = name.substring(0, end);
                classes.add(tmpName.replace("/", PACKAGE_SEPARATOR));
            }
        }
        return classes;
    }

    /**
     * 扫描 工程下的所有类，不包括jar文件中的类
     */
    public static void scan() throws Exception {
        Set<String> allclassPath = getAllClassPath();
        for (String path : allclassPath) {
            System.out.println("打印项目自写class列表开始--------------------------------");
            Set<String> classes = getClassNameFromDir(new File(APP_CLASS_PATH));
            for (String s : classes) {
                System.out.println(s);
            }

            System.out.println("打印jar里边class列表开始--------------------------------");
            if (path.endsWith(JAR_FILE_EXT)) {
                Set<String> jarclasses = getClassNameFromJar(new JarFile(new File(path.substring(1))));
                for (String s : jarclasses) {
                    System.out.println(s);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        long begin = System.currentTimeMillis();
        scan();
        System.out.println(System.currentTimeMillis() - begin);
    }



}
