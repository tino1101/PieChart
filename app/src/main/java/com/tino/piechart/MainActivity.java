package com.tino.piechart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PieChartView pieChartView = findViewById(R.id.pie_chart_view);

        pieChartView.addItem(new PieChartView.Item("餐饮美食", 49f, 0xFFFEB343, 2450, true));
        pieChartView.addItem(new PieChartView.Item("生活日用", 22f, 0xFF24E8CE, 1100, true));
        pieChartView.addItem(new PieChartView.Item("交通出行", 18f, 0xFF2594FF, 900, true));
        pieChartView.addItem(new PieChartView.Item("服饰美容", 8f, 0xFF7525FF, 400));
        pieChartView.addItem(new PieChartView.Item("人情往来", 2f, 0xFFF9453D, 100));
        pieChartView.addItem(new PieChartView.Item("医疗保健", 1f, 0xFF999999, 50));
    }

}