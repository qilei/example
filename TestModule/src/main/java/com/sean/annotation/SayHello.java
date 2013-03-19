package com.sean.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: huixiao200068
 * Date: 13-3-19
 * Time: 下午5:27
 * To change this template use File | Settings | File Templates.
 */
public class SayHello {
    @HelloWorld(name = "sean")
    public void say(String name) {
        System.out.println(name + " says Hello World!");
    }
}
