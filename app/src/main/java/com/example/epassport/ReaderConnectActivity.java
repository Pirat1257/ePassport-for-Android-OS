package com.example.epassport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ReaderConnectActivity extends AppCompatActivity {

    private Button confirmIpButton;
    private EditText ipEditText;
    private String pass;
    private String ip = "192.168.0.106";
    private static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_connect);
        // Привязываем элементы
        confirmIpButton = findViewById(R.id.confirm_ip_button);
        ipEditText = findViewById(R.id.ip_editText);
        // Действие при нажатии на кнопку CONFIRM
        confirmIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вызываем ассинхронную таску, которая пытается подключиться к пк
                FirstContact fc = new FirstContact();
                fc.execute();
            }
        });
    }

    class FirstContact extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Устанавливаем соединение
                InetAddress address = InetAddress.getByName(ip);
                socket = new Socket(address, 8060);
                // Настройка i/o
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                DataInputStream din = new DataInputStream(is);
                // Отправка приветствия
                os.write("HelloWorld!".getBytes());
                // Получение ответа
                while(true) {
                    int len = is.available(); // Получаем количество готовых байтов
                    if (len > 0) {
                        byte message[] = new byte[len];
                        for (int i = 0; i < len; i++) {
                            message[i] = din.readByte();
                        }
                        // Переводим ответ в строку
                        String answer = new String(message, "UTF_8");
                        // Если пришел ok, то есть контакт
                        if (answer.equals("ok")) {
                            // Передаем информацию следующему Activity
                            Intent intent = new Intent(ReaderConnectActivity.this, MenuActivity.class);
                            Bundle arguments = getIntent().getExtras();
                            intent.putExtra("pass", arguments.get("pass").toString());
                            intent.putExtra("ip", ip); // Не удастся передать Socket
                            startActivity(intent);
                            finish();
                        }
                        else {
                            return "Wrong answer";
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "No one answered";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }
    }
}