package com.example.epassport;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button confirmPassButton;
    private EditText passEditText;
    private AppDatabase db;
    private PassTableDao passTableDao;
    private String passHash = "";
    private MyCrypto myCrypto;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        passTableDao = db.passTableDao();
        PassTable pt_head = passTableDao.selectById(0);
        myCrypto = new MyCrypto();
        // Если элемента не существует, значит и базы нет и вообще это первый запуск
        if (pt_head == null) {
            pt_head = new PassTable();
            pt_head.id = 0;
        }
        // В противном случае получаем хеш пароля
        else {
            passHash = pt_head.pass_hash;
        }
        // Привязываем элементы
        confirmPassButton = findViewById(R.id.confirm_pass_button);
        passEditText = findViewById(R.id.pass_editText);
        // Запрашиваем разрешения
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        // Обработака нажатия кнопки CONFIRM
        PassTable finalPt_head = pt_head;
        confirmPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверка пустой ли пароль
                if (passEditText.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Password is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                // Считываем пароль
                String password = passEditText.getText().toString();
                // Генерируем хеш
                String dummy_hash = myCrypto.sha256(password.getBytes());
                // Хеш отсутствует, значит первый запуск
                if (passHash.isEmpty()) {
                    finalPt_head.pass_hash = dummy_hash;
                    // Добавляем в базу проверочный элемент
                    passTableDao.insert(finalPt_head);
                    // Вызываем новое активити
                    Intent intent = new Intent(MainActivity.this, ReaderConnectActivity.class);
                    intent.putExtra("pass", passEditText.getText().toString());
                    startActivity(intent);
                    finish();
                    return;
                }
                // Если пароль не пустой и есть хеш, то сравниваем с хешем
                else {
                    // Проверка хешей
                    if(dummy_hash.equals(passHash)) {
                        // Вызываем новое активити
                        Intent intent = new Intent(MainActivity.this, ReaderConnectActivity.class);
                        intent.putExtra("pass", passEditText.getText().toString());
                        startActivity(intent);
                        finish();
                        return;
                    }
                    else {
                        passEditText.setText("");
                        Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}