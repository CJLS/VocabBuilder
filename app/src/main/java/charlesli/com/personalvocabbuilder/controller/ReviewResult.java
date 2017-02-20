package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import charlesli.com.personalvocabbuilder.R;

public class ReviewResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_result);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.result_toolbar);
        setSupportActionBar(myToolbar);

        Button continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        int difficultCount = intent.getIntExtra(getString(R.string.difficultCount), 0);
        int familiarCount = intent.getIntExtra(getString(R.string.familiarCount), 0);
        int easyCount = intent.getIntExtra(getString(R.string.easyCount), 0);
        int perfectCount = intent.getIntExtra(getString(R.string.perfectCount), 0);
        int moreFamiliarVocabCount = intent.getIntExtra(getString(R.string.moreFamiliarVocabCount), 0);

        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
        setUpPieChartConfig(pieChart);
        addPieChartData(pieChart, difficultCount, familiarCount, easyCount, perfectCount);
        setMoreFamiliarVocabCountFeedback(moreFamiliarVocabCount);
        setDayStreak(pieChart);
    }

    private void setUpPieChartConfig(PieChart pieChart) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("1 \n Day Streak");
        pieChart.setCenterTextSize(20f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
    }

    private void addPieChartData(PieChart pieChart, int difficultCount, int familiarCount, int easyCount, int perfectCount) {
        List<PieEntry> pieEntries = new ArrayList<PieEntry>();
        if (perfectCount > 0) {
            pieEntries.add(new PieEntry(perfectCount, "Perfect"));
        }
        if (easyCount > 0) {
            pieEntries.add(new PieEntry(easyCount, "Easy"));
        }
        if (familiarCount > 0) {
            pieEntries.add(new PieEntry(familiarCount, "Familiar"));
        }
        if (difficultCount > 0) {
            pieEntries.add(new PieEntry(difficultCount, "Difficult"));
        }
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Colors");
        pieDataSet.setSliceSpace(3f);
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.argb(220, 43, 94, 162));
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(15f);
        PieData data = new PieData(pieDataSet);
        PercentFormatter percentFormatter = new PercentFormatter();
        data.setValueFormatter(percentFormatter);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void setMoreFamiliarVocabCountFeedback(int moreFamiliarVocabCount) {
        TextView moreFamiliarVocabCountTV = (TextView) findViewById(R.id.vocabImprovementText);
        if (moreFamiliarVocabCount == 0) {
            moreFamiliarVocabCountTV.setText("Keep learning and improving!");
        } else {
            moreFamiliarVocabCountTV.setText("You are more familiar with " + moreFamiliarVocabCount + " vocab!");
        }
    }

    private void setDayStreak(PieChart pieChart) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.dayStreakSharedPreferences), MODE_PRIVATE);
        int yearLastReview = sharedPreferences.getInt(getString(R.string.yearKeySP), 2017);
        int dayOfYearLastReview = sharedPreferences.getInt(getString(R.string.dayOfYearKeySP), 1);
        int dayStreakCount = sharedPreferences.getInt(getString(R.string.dayStreakCountKeySP), 0);

        Calendar calendar = Calendar.getInstance();
        int yearToday = calendar.get(Calendar.YEAR);
        int dayOfYearToday = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        int yearYesterday = calendar.get(Calendar.YEAR);
        int dayOfYearYesterday = calendar.get(Calendar.DAY_OF_YEAR);

        if (yearToday == yearLastReview && dayOfYearToday == dayOfYearLastReview) {
            // Don't update dayStreakCount
        }
        else if (yearYesterday == yearLastReview && dayOfYearYesterday == dayOfYearLastReview) {
            dayStreakCount++;
        }
        else {
            dayStreakCount = 1;
        }

        pieChart.setCenterText(dayStreakCount + "\n" + "Day Streak");
        pieChart.invalidate();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.yearKeySP), yearToday);
        editor.putInt(getString(R.string.dayOfYearKeySP), dayOfYearToday);
        editor.putInt(getString(R.string.dayStreakCountKeySP), dayStreakCount);
        editor.apply();
    }
}
