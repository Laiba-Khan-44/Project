package com.example.firsttry;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private EditText amountInput;
    private Spinner fromCurrency;
    private Spinner toCurrency;
    private Button convertButton;
    private TextView resultView;
    private final HashMap<String, Double> exchangeRates = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        amountInput = findViewById(R.id.amountInput);
        fromCurrency = findViewById(R.id.fromCurrency);
        toCurrency = findViewById(R.id.toCurrency);
        convertButton = findViewById(R.id.convertButton);
        resultView = findViewById(R.id.resultView);

        // Populate spinners with currency codes
        String[] currencies = {"USD", "EUR", "INR", "JPY", "GBP", "PKR", "CAD", "AUD","SAR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromCurrency.setAdapter(adapter);
        toCurrency.setAdapter(adapter);

        fetchExchangeRates();

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });
    }
    private void fetchExchangeRates() {
        new Thread(() -> {
            try {
                String apiKey = "4f2584639426f31ad38fc371";
                String urlString = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/USD";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject rates = jsonResponse.getJSONObject("conversion_rates");

                for (Iterator<String> it = rates.keys(); it.hasNext(); ) {
                    String key = it.next();
                    exchangeRates.put(key, rates.getDouble(key));
                }

                // Log exchange rates
                for (String currency : exchangeRates.keySet()) {
                    System.out.println(currency + ": " + exchangeRates.get(currency));
                }

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Exchange rates updated", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch exchange rates", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


//    private void fetchExchangeRates() {
//        new Thread(() -> {
//            try {
//                // Replace 'YOUR_API_KEY' with your actual API key
//                String apiKey = "4f2584639426f31ad38fc371";
//                String urlString = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/USD";
//
//                URL url = new URL(urlString);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String line;
//
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//                reader.close();
//
//                JSONObject jsonResponse = new JSONObject(response.toString());
//                JSONObject rates = jsonResponse.getJSONObject("rates");
//
//                for (Iterator<String> it = rates.keys(); it.hasNext(); ) {
//                    String key = it.next();
//                    exchangeRates.put(key, rates.getDouble(key));
//                }
//
//                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Exchange rates updated", Toast.LENGTH_SHORT).show());
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch exchange rates", Toast.LENGTH_SHORT).show());
//            }
//        }).start();
//    }

    private void convertCurrency() {
        String from = fromCurrency.getSelectedItem().toString();
        String to = toCurrency.getSelectedItem().toString();
        String amountStr = amountInput.getText().toString();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (exchangeRates.containsKey(from) && exchangeRates.containsKey(to)) {
            double fromRate = exchangeRates.get(from);
            double toRate = exchangeRates.get(to);
            double result = (amount / fromRate) * toRate;

            resultView.setText(String.format("%.2f %s", result, to));
        } else {
            Toast.makeText(this, "Currency rates not available", Toast.LENGTH_SHORT).show();
        }
    }
}