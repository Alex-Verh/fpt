package com.example.fpt_footballplayertracker;

import android.content.Context;
import android.util.Log;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MqttManager {

    private static MqttManager instance;

    private final Mqtt5AsyncClient mqttClient;
    private static final String MQTT_BROKER = "broker.hivemq.com";
    private static final int MQTT_PORT = 1883;
    public static final String PULSE_TOPIC = "FPT/pulse";
    public static final String ACCEL_TOPIC = "FPT/accel";
    public static final String GPS_TOPIC = "FPT/gps";

    private final DatabaseHelper dbHelper;
    private final Map<String, Consumer<String>> listeners = new HashMap<>();

    // private constructor
    private MqttManager(Context ctx) {
        mqttClient = Mqtt5Client.builder()
                .identifier("FPT-APP-" + System.currentTimeMillis())
                .serverHost(MQTT_BROKER)
                .serverPort(MQTT_PORT)
                .buildAsync();

        dbHelper = new DatabaseHelper(ctx);

        mqttClient
                .connectWith()
                .keepAlive(60)
                .send()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT", "Connection failed", throwable);
                    } else {
                        Log.d("MQTT", "Connected successfully");
                        // pass method as parameter without calling it
                        listeners.keySet().forEach(this::subscribeToTopic);
                    }
                });
    }

    public static synchronized MqttManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new MqttManager(ctx);
        }
        return instance;
    }

    public void addTopicListener(String topic, Consumer<String> listener) {
        if (!mqttClient.getState().isConnected()) {
            Log.e("MQTT", "MQTT connection not established.");
        }

        listeners.put(topic, listener);
        subscribeToTopic(topic);
    }

    private void subscribeToTopic(String topic) {
        mqttClient.subscribeWith()
                .topicFilter(topic)
                .callback(this::handleMessage)
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT", "Subscription failed for topic: " + topic, throwable);
                    } else {
                        Log.d("MQTT", "Subscribed to " + topic);
                    }
                });
    }

    private void handleMessage(Mqtt5Publish publish) {
        String topic = publish.getTopic().toString();
        String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8).trim();
        Log.d("MQTT", "Received message: " + payload + " on topic: " + topic);

        switch (topic) {
            case GPS_TOPIC:
                dbHelper.insertGpsData(payload);
                break;
            case ACCEL_TOPIC:
                dbHelper.insertAccelData(payload);
                break;
            case PULSE_TOPIC:
                dbHelper.insertPulseData(payload);
                break;
        }

        Consumer<String> listener = listeners.get(topic);
        if (listener != null) {
            listener.accept(payload);
        }
    }

    // not sure where to call this function
    // idk if we should disconnect in the "onDesotry()" of main activity
    // or just let the OS handle app termination
    public void disconnect() {
        if (mqttClient != null) {
            mqttClient.disconnect();
        }
    }
}
