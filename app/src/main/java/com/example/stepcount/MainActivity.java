package com.example.stepcount;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.YAxis;

import java.util.ArrayList;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Переменные для отслеживания состояния и данных датчика
    public boolean active = true;
    private SensorManager sensorManager;
    private int count = 0;
    private TextView text;
    private TextView steps;
    private long lastUpdate;
    private LineChart chart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация переменных
        text = findViewById(R.id.textView2);
        steps = findViewById(R.id.textView3);
        text.setText(String.valueOf(count));
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        chart = findViewById(R.id.chart);

        // Добавление начальных данных в график
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0));
        LineDataSet dataSet = new LineDataSet(entries, "Калории");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Обновление графика

        // Регистрация слушателя датчика
        sensorManager.registerListener((SensorEventListener) this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        lastUpdate = System.currentTimeMillis();
    }


    @Override
    protected  void onResume(){
        super.onResume();
        // Повторная регистрация слушателя при возвращении к приложению
        sensorManager.registerListener(
                (SensorEventListener) this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected  void onPause(){
        super.onPause();
        // Отмена регистрации слушателя при паузе приложения
        sensorManager.unregisterListener((SensorEventListener) this);
    }

    public  void OnStoped(View view)
    {
        // Переключение состояния при нажатии кнопки "Стоп"
        active = !active;
        if(!active){
            Button button = findViewById(R.id.button);
            button.setText("ВОЗОБНОВИТЬ");
        }else{
            Button button = findViewById(R.id.button);
            button.setText("ПАУЗА");
        }
    }
    public void onSensorChanged(SensorEvent event)
    {
        // Обработка данных датчика при изменении его состояния
        if(active){
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float[] values = event.values;
                float x = values[0];
                float y = values[1];
                float z = values[2];

                float accelationSquareRoot = (x * x + y * y + z * z)/(SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
                long actualTime = System.currentTimeMillis();

                if(accelationSquareRoot >= 2){
                    if(actualTime - lastUpdate < 200){
                        return;
                    }

                    lastUpdate = actualTime;
                }
                count ++;
                text.setText(String.valueOf(count));

                double distanceInKm = (count * 144.5) / 100000;
                double caloriesBurned = 3.9 * 70.0 * (distanceInKm / 5.0);
                steps.setText("Шаги\nСожённые калории - " + String.valueOf((int)caloriesBurned));

                updateChart(count, (int)caloriesBurned);
            }
        }
    }
    private void updateChart(int steps, int calories) {
        // Получаем данные из графика
        LineData data = chart.getData();

        if (data != null) {
            // Получаем набор данных из графика
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

            // Если набор данных еще не существует, создаем его
            if (set == null) {
                set = new LineDataSet(new ArrayList<>(), "Калории");
                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                data.addDataSet(set);
            }

            // Добавляем новую точку в набор данных
            data.addEntry(new Entry(set.getEntryCount(), calories), 0);
            data.notifyDataChanged();

            // Уведомляем график о изменении данных
            chart.notifyDataSetChanged();
            chart.invalidate(); // Обновление графика
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Обработка изменения точности датчика
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                    Log.d("Sensor Status", "Sensor status : Unreliable " + accuracy);
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    Log.d("Sensor Status", "Sensor status : Accuracy low " + accuracy);
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    Log.d("Sensor Status", "Sensor status : Accuracy medium " + accuracy);
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    Log.d("Sensor Status", "Sensor status : Accuracy high " + accuracy);
                    break;
            }
        }
    }
}
