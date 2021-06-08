package com.example.epassport;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private DG1TableDao dg1TableDao;

    private List<PassTable> pt_list;
    private Iterator<PassTable> pt_it;

    private List<DG1Table> dg1_list;
    private Iterator<DG1Table> dg1_it;

    private String pass;
    private Intent answerIntent;

    private MyCrypto myCrypto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        // Привязываем элементы
        confirmButton = findViewById(R.id.confirmNewPassButton);
        oldPass = findViewById(R.id.oldPassEditText);
        newPass = findViewById(R.id.newPassEditText);
        myCrypto = new MyCrypto();
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        passTableDao = db.passTableDao();
        dg1TableDao = db.dg1TableDao();
        // Получаем информацию от MenuActivity
        answerIntent = new Intent();
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("pass").toString();
        // Действие при нажатии на кнопку CONFIRM
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
                if (pt_it.hasNext()) {
                    pt = pt_it.next();
                    pt.pass_hash = myCrypto.sha256(new_pass.getBytes());
                }
                passTableDao.update(pt);
                // Производим обновление информации в таблице DG1Table
                dg1_list = dg1TableDao.getAll();
                dg1_it = dg1_list.iterator();
                DG1Table dg1 = null;
                if (dg1_it.hasNext()) {
                    dg1 = dg1_it.next();
                    try {

                        dg1.documentType = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.documentType.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.issuingState = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.issuingState.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.surname = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.surname.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.name = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.name.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.documentNumber = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.documentNumber.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.nationality = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.nationality.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.dateOfBirth = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.dateOfBirth.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.sex = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.sex.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.dateOfIssue = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.dateOfIssue.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.authority = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.authority.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.dateOfExpiryOrValidUntilDate.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.faceshotHash = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.faceshotHash.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.sign1 = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.sign1.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.sign2 = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.sign2.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));
                        dg1.chipId = myCrypto.encrypt(new_pass, myCrypto.decrypt(pass, Base64.decode(dg1.chipId.getBytes("UTF-16LE"), Base64.DEFAULT)).getBytes("UTF-16LE"));

                        Path path = Paths.get(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                                "/Download/" + "faceshot.jpg");
                        byte[] face = Files.readAllBytes(path);
                        face = myCrypto.encrypt2(new_pass, myCrypto.decrypt2(pass, face));
                        FileOutputStream fos = new FileOutputStream(Environment.
                                getExternalStorageDirectory().getAbsoluteFile() + "/Download/" +
                                "faceshot.jpg");
                        fos.write(face);
                        fos.close();

                        dg1TableDao.update(dg1);

                        answerIntent.putExtra("new_pass", new_pass);
                        setResult(RESULT_OK, answerIntent);
                        finish();
                        return;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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