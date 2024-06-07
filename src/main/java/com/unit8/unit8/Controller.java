package com.unit8.unit8;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Controller {
    @FXML
    private Label cityLabel;

    @FXML
    private TextField cityInput;

    @FXML
    private Button submitButton;

    @FXML
    private Label temperatureLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label humidityLabel;

    @FXML
    private Label windLabel;

    @FXML
    private ImageView weatherIcon;

    @FXML
    private RadioButton metricRadioButton, imperialRadioButton;

    private static final String API_KEY = "60b966bc41ffb1241781056c49354713";

    private boolean imperialSystem = false;

    @FXML
    protected void onSelectMetric() {
        imperialSystem = false;
        imperialRadioButton.setSelected(false);
    }

    @FXML
    protected void onSelectImperial() {
        imperialSystem = true;
        metricRadioButton.setSelected(false);
    }

    @FXML
    protected void onSubmitButtonClick() {
        String cityName = cityInput.getText();

        if (cityName != null && !cityName.isEmpty()) {
            String weatherLabel = "Loading " + cityName + "'" + (cityName.endsWith("s") ? "" : "s") + " weather...";

            cityLabel.setText(weatherLabel);
            setLoading(true);
            setWeatherVisibility(false);

            fetchWeatherData(cityName);
        } else {
            cityLabel.setText("City name is missing");
        }
    }

    private void fetchWeatherData(String cityName) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
                String urlString = "https://api.openweathermap.org/data/2.5/weather?appid=" + API_KEY + "&units=" + (imperialSystem ? "imperial" : "metric") + "&q=" + encodedCityName;
                HttpURLConnection connection = null;

                try {
                    URL url = new URI(urlString).toURL();
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();
                        return content.toString();
                    } else {
                        String responseMessage = capitalize(connection.getResponseMessage());
                        return "[" + responseCode + "] " + responseMessage;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Exception: " + e.getMessage();
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
        };

        task.setOnSucceeded(_ -> {
            String result = task.getValue();

            if (result.startsWith("{") && result.endsWith("}")) parseAndDisplayWeatherData(result);
            else cityLabel.setText(result);

            setLoading(false);
        });


        task.setOnFailed(_ -> {
            cityLabel.setText("Failed to fetch weather data");
            setLoading(false);
        });

        new Thread(task).start();
    }

    private void parseAndDisplayWeatherData(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            int responseCode = jsonObject.getInt("cod");

            JSONObject coord = jsonObject.getJSONObject("coord");
            double lon = coord.getDouble("lon");
            double lat = coord.getDouble("lat");

            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            String main = weather.getString("main");
            String description = capitalize(weather.getString("description"));
            String icon = weather.getString("icon");

            JSONObject mainObject = jsonObject.getJSONObject("main");
            double temp = mainObject.getDouble("temp");
            double feelsLike = mainObject.getDouble("feels_like");
            double tempMin = mainObject.getDouble("temp_min");
            double tempMax = mainObject.getDouble("temp_max");
            int pressure = mainObject.getInt("pressure");
            int humidity = mainObject.getInt("humidity");

            int visibility = jsonObject.getInt("visibility");

            JSONObject wind = jsonObject.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");
            int windDeg = wind.getInt("deg");

            JSONObject clouds = jsonObject.getJSONObject("clouds");
            int cloudiness = clouds.getInt("all");

            JSONObject sys = jsonObject.getJSONObject("sys");
            String country = sys.getString("country");
            long sunrise = sys.getLong("sunrise");
            long sunset = sys.getLong("sunset");

            int timezone = jsonObject.getInt("timezone");
            String cityName = jsonObject.getString("name");
            int cityId = jsonObject.getInt("id");

            String tempUnit = imperialSystem ? "°F" : "°C";
            String speedUnit = imperialSystem ? "mph" : "m/s";

            setWeatherVisibility(true);
            temperatureLabel.setText("Temperature: " + temp + tempUnit);
            descriptionLabel.setText("Description: " + description);
            humidityLabel.setText("Humidity: " + humidity + "%");
            windLabel.setText("Wind: " + windSpeed + " " + speedUnit);

            cityLabel.setText("Weather for " + cityName + " (" + country + ")" + " loaded.");

            String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
            Image iconImage = new Image(iconUrl);
            weatherIcon.setImage(iconImage);

            System.out.println("City: " + cityName + " (" + country + ")");
            System.out.println("Coordinates: " + lat + ", " + lon);
            System.out.println("Weather: " + main + " (" + description + ")");
            System.out.println("Temperature: " + temp + tempUnit + " (feels like " + feelsLike + tempUnit + ")");
            System.out.println("Min Temperature: " + tempMin + tempUnit + ", Max Temperature: " + tempMax + tempUnit);
            System.out.println("Pressure: " + pressure + " hPa");
            System.out.println("Humidity: " + humidity + "%");
            System.out.println("Visibility: " + visibility + " meters");
            System.out.println("Wind: " + windSpeed + " " + speedUnit + " at " + windDeg + " degrees");
            System.out.println("Cloudiness: " + cloudiness + "%");
            System.out.println("Sunrise: " + sunrise + ", Sunset: " + sunset);
            System.out.println("Timezone: " + timezone);
            System.out.println("City ID: " + cityId);
            System.out.println("Icon: " + iconUrl);
            System.out.println("Response Code: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setWeatherVisibility(boolean visible) {
        weatherIcon.setVisible(visible);
        temperatureLabel.setVisible(visible);
        descriptionLabel.setVisible(visible);
        humidityLabel.setVisible(visible);
        windLabel.setVisible(visible);
    }

    private void setLoading(boolean isLoading) {
        submitButton.setDisable(isLoading);
        cityInput.setDisable(isLoading);
        metricRadioButton.setDisable(isLoading);
        imperialRadioButton.setDisable(isLoading);
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return "";
        else return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
}