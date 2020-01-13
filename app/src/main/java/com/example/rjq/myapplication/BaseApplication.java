package com.example.rjq.myapplication;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class BaseApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        ArrayList<TextView> list = new ArrayList();
        list.add(new Button(this));
        list.add(new EditText(this));
        list.add(new TextView(this));

        //生产者
        ArrayList<? extends TextView> list1 = new ArrayList<Button>();
        TextView textView = list1.get(0);
//        Button textView2 = list1.get(0); //报错
//        list1.add(new TextView(this)); //报错
//        list1.add(new Button(this));   //报错
//        list1.add(new View(this));     //报错

        //消费者
        ArrayList<? super TextView> list2 = new ArrayList<View>();  //只能add TextView：Button EditText都是TextView(多态)
        list2.add(new Button(this));
        list2.add(new EditText(this));
        list2.add(new TextView(this));
//        list2.add(new View(this));    //报错
        Object button = list2.get(0);           //只能得到Object类型的
//        View view = list2.get(0);       //报错
    }
}
