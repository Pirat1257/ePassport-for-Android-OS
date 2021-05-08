package com.example.epassport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

public class ChangePassActivity extends AppCompatActivity {

    private Button confirmButton;
    private EditText oldPass;
    private EditText newPass;

    private AppDatabase db;
    private PassTableDao passTableDao;

    private List<PassTable> pt_list;
    private Iterator<PassTable> pt_it;

    private String pass;
    private Intent answerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        // Привязываем элементы
        confirmButton = findViewById(R.id.confirmNewPassButton);
        oldPass = findViewById(R.id.oldPassEditText);
        newPass = findViewById(R.id.newPassEditText);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        passTableDao = db.passTableDao();
        // Получаем информацию от MenuActivity
        answerIntent = new Intent();
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("pass").toString();
        // Действие при нажатии на кнопку CONFIRM
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPass.getText().toString().isEmpty())
                {
                    Toast.makeText(ChangePassActivity.this, "Incorrect old password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (oldPass.getText().toString().compareTo(pass) != 0)
                {
                    oldPass.setText("");
                    Toast.makeText(ChangePassActivity.this, "Incorrect old password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (newPass.getText().toString().isEmpty())
                {
                    Toast.makeText(ChangePassActivity.this, "Incorrect new password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (newPass.getText().toString().compareTo(oldPass.getText().toString()) == 0)
                {
                    newPass.setText("");
                    Toast.makeText(ChangePassActivity.this, "Incorrect new password", Toast.LENGTH_LONG).show();
                    return;
                }
                // Прошли все проверки
                // Производим обновление информации в таблице PassTable
                String new_pass = newPass.getText().toString();
                pt_list = passTableDao.getAll();
                pt_it = pt_list.iterator();
                PassTable pt = null;
                if(pt_it.hasNext()) {
                    pt = pt_it.next();
                    pt.pass_hash = get_hash(new_pass);
                }
                passTableDao.update(pt);
                // Производим обновление информации в таблице DG1-16...
                //
                //
                //
            }
        });
    }

    // Utility function
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    // Вычисление хеша
    private String get_hash(String text) {
        // Генерируем хеш
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes());
            return bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}