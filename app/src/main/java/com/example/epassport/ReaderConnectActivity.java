package com.example.epassport;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
    private EditText portEditText;
    private String pass;
    private String ip = "192.168.0.106";
    private String portString;
    private int port = 8060;
    private static Socket socket;
    private String sessionKey = null;
    private String public_key_str = null;
    private MyCrypto myCrypto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_connect);
        // Привязываем элементы
        confirmIpButton = findViewById(R.id.confirm_ip_button);
        ipEditText = findViewById(R.id.ip_editText);
        portEditText = findViewById(R.id.port_editText);
        // Генерируем сеансовый
        myCrypto = new MyCrypto();
        sessionKey = myCrypto.generate_key();
        // Действие при нажатии на кнопку CONFIRM
        confirmIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вызываем ассинхронную таску, которая пытается подключиться к пк
                ip = ipEditText.getText().toString();
                portString = portEditText.getText().toString();
                if (ip.isEmpty() == false && portString.isEmpty() == false) {
                    port = Integer.parseInt(portEditText.getText().toString());
                    FirstContact fc = new FirstContact();
                    fc.execute();
                }
            }
        });
    }

    class FirstContact extends AsyncTask<Void, String, String> {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(Void... voids) {

            // Устанавливаем соединение
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ip);
                socket = new Socket(address, port);
                // Настройка i/o
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                DataInputStream din = new DataInputStream(is);
                // Отправка запроса на получение ключа
                os.write("GiveKey".getBytes());
                int what = 0; // 0 - обмен ключами, 1 - получение зашифрованного ответа
                // Получение ответа
                while(true) {
                    int len = is.available();
                    if (len > 0) {
                        byte message[] = new byte[len];
                        for (int i = 0; i < len; i++) {
                            message[i] = din.readByte();
                        }
                        if (what == 0) { // Получение публичного ключа
                            // Переводим полученный открытый ключ в строку
                            public_key_str = new String(message, "UTF_8");
                            // Шифруем сенсовый ключ открытым ключем
                            String encrypted_sessionKey =  myCrypto.encryptByPublicKey(public_key_str, sessionKey);
                            // Отправка зашифрованного сессионного ключа
                            os.write(encrypted_sessionKey.getBytes());
                            what++;
                        }
                        else if (what == 1) { // Получение зашифрованного подтверждения - временное, для проверки корректности работы
                            String answer = new String(message, "UTF_8");

                            // Если пришел ok, то есть контакт
                            if (answer.equals("ok")) {
                                // Передаем информацию следующему Activity
                                Intent intent = new Intent(ReaderConnectActivity.this, MenuActivity.class);
                                Bundle arguments = getIntent().getExtras();
                                intent.putExtra("pass", arguments.get("pass").toString());
                                intent.putExtra("ip", ip); // Не удастся передать Socket
                                intent.putExtra("port", portString);
                                intent.putExtra("Key", sessionKey);
                                //App.getInstance().setSocket(socket);
                                //socket.setKeepAlive(true);
                                socket.close();
                                startActivity(intent);
                                finish();
                                break;
                            }
                            else {
                                return "Wrong pass or answer";
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "No one answered";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
        }
    }
}