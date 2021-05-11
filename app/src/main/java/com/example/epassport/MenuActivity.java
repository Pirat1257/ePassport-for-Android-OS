package com.example.epassport;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private Button downloadButton;
    private Button chPassButton;
    private ImageView faceshot;
    private TextView type;
    private TextView state;
    private TextView passNo;
    private TextView surname;
    private TextView names;
    private TextView nation;
    private TextView birth;
    private TextView sex;
    private TextView issue;
    private TextView auth;
    private TextView expiry;

    private String pass;
    private String ip;

    private AppDatabase db;
    private DG1TableDao dg1TableDao;

    private MyCrypto myCrypto;
    private String sessionKey;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        // Привязываем элементы
        downloadButton = findViewById(R.id.downloadButton);
        chPassButton = findViewById(R.id.chPassButton);
        faceshot = findViewById(R.id.faceshotImageView);
        type = findViewById(R.id.typeTextView);
        state = findViewById(R.id.stateTextView);
        passNo = findViewById(R.id.passNoTextView);
        surname = findViewById(R.id.surnameTextView);
        names = findViewById(R.id.namesTextView);
        nation = findViewById(R.id.nationTextView);
        birth = findViewById(R.id.birthTextView);
        sex = findViewById(R.id.sexTextView);
        issue = findViewById(R.id.issueTextView);
        auth = findViewById(R.id.authTextView);
        expiry = findViewById(R.id.expiryTextView);
        // Получаем информацию от прошлого Activity
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("pass").toString();
        ip = arguments.get("ip").toString();
        // Инициализация базы данных
        db = App.getInstance().getDatabase();
        dg1TableDao = db.dg1TableDao();
        // Создание класса для работы с криптографией
        myCrypto = new MyCrypto();
        sessionKey = null;
        // Если базы данных не пустые, производим вывод информации
        DG1Table dg1_head = dg1TableDao.selectById(0);
        if (dg1_head != null) {
            try {
                type.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.documentType.getBytes("UTF-16LE"), Base64.DEFAULT)));
                state.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.issuingState.getBytes("UTF-16LE"), Base64.DEFAULT)));
                passNo.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.documentNumber.getBytes("UTF-16LE"), Base64.DEFAULT)));
                surname.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.surname.getBytes("UTF-16LE"), Base64.DEFAULT)));
                names.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.name.getBytes("UTF-16LE"), Base64.DEFAULT)));
                nation.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.nationality.getBytes("UTF-16LE"), Base64.DEFAULT)));
                birth.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.dateOfBirth.getBytes("UTF-16LE"), Base64.DEFAULT)));
                sex.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.sex.getBytes("UTF-16LE"), Base64.DEFAULT)));
                issue.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.issuingState.getBytes("UTF-16LE"), Base64.DEFAULT)));
                auth.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.authority.getBytes("UTF-16LE"), Base64.DEFAULT)));
                expiry.setText(myCrypto.decrypt(pass, Base64.decode(dg1_head.dateOfExpiryOrValidUntilDate.getBytes("UTF-16LE"), Base64.DEFAULT)));
                // Вывод фото
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(getApplicationInfo().dataDir + "/pictures/" + "/faceshot.jpg/");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Path path = Paths.get(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Download/" + "faceshot.jpg");
                byte[] face = Files.readAllBytes(path);
                Bitmap img = getImage(myCrypto.decrypt2(pass, face));
                faceshot.setImageBitmap(img);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Обработка нажатия на кнопку DOWNLOAD
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Пока затычка, вообще тут должен производится вызов асинхронной таски с работой
                DG1Table new_dg1 = new DG1Table();
                new_dg1.id = 0;
                dg1TableDao.delete(new_dg1);
                try {
                    new_dg1.id = 0;
                    new_dg1.documentType = myCrypto.encrypt(pass, "some documentType".getBytes("UTF-16LE"));
                    new_dg1.issuingState = myCrypto.encrypt(pass, "some issuingState".getBytes("UTF-16LE"));
                    new_dg1.documentNumber = myCrypto.encrypt(pass, "some documentNumber".getBytes("UTF-16LE"));
                    new_dg1.surname = myCrypto.encrypt(pass, "some surname".getBytes("UTF-16LE"));
                    new_dg1.name = myCrypto.encrypt(pass, "some name".getBytes("UTF-16LE"));
                    new_dg1.nationality = myCrypto.encrypt(pass, "some nationality".getBytes("UTF-16LE"));
                    new_dg1.dateOfBirth = myCrypto.encrypt(pass, "some dateOfBirth".getBytes("UTF-16LE"));
                    new_dg1.sex = myCrypto.encrypt(pass, "some sex".getBytes("UTF-16LE"));
                    new_dg1.dateOfIssue = myCrypto.encrypt(pass, "some Issue".getBytes("UTF-16LE"));
                    new_dg1.authority = myCrypto.encrypt(pass, "some authority".getBytes("UTF-16LE"));
                    new_dg1.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(pass, "some dataOfExpiry".getBytes("UTF-16LE"));

                    // Типа получение фото
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(Environment.getExternalStorageDirectory().
                                getAbsoluteFile() + "/Download/" + "hui.jpg");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    Bitmap img = BitmapFactory.decodeStream(bis);
                    // Получвем от пк битовую строку с фото, по идее на пк делается bitmap и переводится в байты и отправляется
                    byte[] face = getBytes(img);
                    byte[] encryptedFace = myCrypto.encrypt2(pass, face);
                    FileOutputStream localFile = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Download/" + "faceshot.jpg");
                    localFile.write(encryptedFace);
                    localFile.close();

                    dg1TableDao.insert(new_dg1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Обработка нажатия на кнопку CHANGE PASSWORD
        chPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ChangePassActivity.class);
                intent.putExtra("pass", pass);
                startActivity(intent);

            }
        });
    }

    // convert from bitmap to byte array
    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}