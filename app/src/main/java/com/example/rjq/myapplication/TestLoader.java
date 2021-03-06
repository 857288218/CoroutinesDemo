package com.example.rjq.myapplication;

import android.util.Log;

//测试类的初始化：按顺序执行类变量的赋值和静态代码块。实例初始化不一定要在类初始化结束之后才开始初始化，以下instance例子
//如果是常量(static final修饰的)则在类的准备阶段就设置为指定的值，
// 顺序不确定:instance2例子，准备阶段给常量instance2赋指定值，str3，str2就已经赋完指定值，其他类变量也赋完零值了，这种情况(类中有属于自己类型的常量)instance是等其他常量，类变量赋值完最后赋值的
class TestLoader {
    private static int i = 1;
    public static TestLoader instance = new TestLoader();
    {
        Log.d("renjunqing", str+ i +str2 + str3);
    }
    private TestLoader() {
        Log.d("renjunqing", str+i+str2+str3);
    }
    private static String str = "str";
    private static final String str2 = "str2";

    //测试类准备阶段
    public static final TestLoader instance2 = new TestLoader(); //TestLoader类先去初始化了
    //构造方法中能正确输出str3。如果是static的，以上例子，则str输出为null
    private static final String str3 = "str3";
}
