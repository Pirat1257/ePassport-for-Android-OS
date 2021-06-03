package com.example.epassport;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private Button downloadButton;
    private Button chPassButton;
    private Button checkButton;
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
    private int port;

    private Socket socket;

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
        checkButton = findViewById(R.id.checkbutton);
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
        port = Integer.parseInt(arguments.get("port").toString());
        // Инициализация базы данных
        db = App.getInstance().getDatabase();
        dg1TableDao = db.dg1TableDao();
        // Получение установленного сокета
        //socket = App.getInstance().getSocket();
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
                    fis = new FileInputStream(getApplicationInfo().dataDir + "/pictures/" +
                            "/faceshot.jpg/");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Path path = Paths.get(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                        "/Download/" + "faceshot.jpg");
                byte[] face = Files.readAllBytes(path);
                Bitmap img = getImage(myCrypto.decrypt2(pass, face));
                faceshot.setImageBitmap(img);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }




        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckTask ct = new CheckTask();
                ct.execute();
            }
        });









        // Обработка нажатия на кнопку DOWNLOAD
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DG1Table new_dg1 = new DG1Table();
                new_dg1.id = "0";
                dg1TableDao.delete(new_dg1);

                try {

                    // Работа с фотографией
                    File file = new File(Environment.getExternalStorageDirectory().
                            getAbsoluteFile() + "/Download/", "photo.jpg");
                    byte[] photoBytes = Files.readAllBytes(file.toPath());
                    String photoHash = myCrypto.sha256(photoBytes);

                    // Шифруем фотографию
                    byte[] enc = myCrypto.encrypt2(pass, photoBytes);
                    FileOutputStream fos = new FileOutputStream(Environment.
                            getExternalStorageDirectory().getAbsoluteFile() + "/Download/" +
                            "faceshot.jpg");
                    fos.write(enc);
                    fos.close();

                    // Сохранение
                    new_dg1.documentType = myCrypto.encrypt(pass, "P".getBytes("UTF-16LE"));
                    new_dg1.issuingState = myCrypto.encrypt(pass, "RUS".getBytes("UTF-16LE"));
                    new_dg1.documentNumber = myCrypto.encrypt(pass, "123456789".getBytes("UTF-16LE"));
                    new_dg1.surname = myCrypto.encrypt(pass, "IVANOV".getBytes("UTF-16LE"));
                    new_dg1.name = myCrypto.encrypt(pass, "MIKHAIL".getBytes("UTF-16LE"));
                    new_dg1.nationality = myCrypto.encrypt(pass, "RUSSIAN FEDERATION".getBytes("UTF-16LE"));
                    new_dg1.dateOfBirth = myCrypto.encrypt(pass, "13.01.1998".getBytes("UTF-16LE"));
                    new_dg1.sex = myCrypto.encrypt(pass, "M".getBytes("UTF-16LE"));
                    new_dg1.dateOfIssue = myCrypto.encrypt(pass, "08.07.2018".getBytes("UTF-16LE"));
                    new_dg1.authority = myCrypto.encrypt(pass, "МВД 12345".getBytes("UTF-16LE"));
                    new_dg1.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(pass, "08.07.2028".getBytes("UTF-16LE"));
                    new_dg1.faceshotHash = myCrypto.encrypt(pass, photoHash.getBytes("UTF-16LE"));
                    new_dg1.chipId = myCrypto.encrypt(pass, "12345".getBytes("UTF-16LE"));
                    // Подписи
                    new_dg1.sign1 = myCrypto.encrypt(pass, "sign1".getBytes("UTF-16LE"));
                    new_dg1.sign2 = myCrypto.encrypt(pass, "sign2".getBytes("UTF-16LE"));
                    dg1TableDao.insert(new_dg1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(MenuActivity.this, "Downloaded", Toast.LENGTH_LONG).show();
            }
        });

        // Обработка нажатия на кнопку CHANGE PASSWORD
        chPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MenuActivity.this, ChangePassActivity.class);
                //intent.putExtra("pass", pass);
                //startActivity(intent);

            }
        });
    }

    class CheckTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                socket = new Socket(ip, port);
                // Настройка i/o
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                DataInputStream din = new DataInputStream(is);
                // Отправка запроса на Проверку информации
                os.write("CheckInfo".getBytes());
                DG1Table dg1_head = dg1TableDao.selectById(0);
                int what = 0; // 0 - Подтверждение на CheckInfo
                              // 1 - Получение хеша фото

                while(true) {
                    int len = is.available();
                    if (len > 0) {
                        byte message[] = new byte[len];
                        for (int i = 0; i < len; i++) {
                            message[i] = din.readByte();
                        }
                        // 0 - Подтверждене на CheckInfo
                        if (what == 0) {
                            String m_str = new String(message, "UTF_8");
                            if (m_str.compareTo("ok") == 0) {
                                String decID = myCrypto.decrypt(pass, Base64.decode(dg1_head.chipId.
                                        getBytes("UTF-16LE"), Base64.DEFAULT));
                                os.write(decID.getBytes());
                                what++;
                            }
                        }
                        // 1 - Получение хеша фото
                        else if (what == 1) {
                            String m_str = new String(message, "UTF_8");
                            String hash = myCrypto.decrypt(pass, Base64.decode(dg1_head.faceshotHash.
                                    getBytes("UTF-16LE"), Base64.DEFAULT));
                            // Сверяем хеш
                            if (m_str.compareTo(hash) == 0) {
                                // Если он совпал, то авторизация успешна, передача остальной инфы
                                String allInfo = myCrypto.decrypt(pass, Base64.decode(dg1_head.documentType.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.issuingState.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.documentNumber.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.surname.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.name.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.nationality.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.dateOfBirth.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.sex.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.authority.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.dateOfExpiryOrValidUntilDate.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.sign1.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n" +
                                        myCrypto.decrypt(pass, Base64.decode(dg1_head.sign2.getBytes("UTF-16LE"), Base64.DEFAULT)) + "\n";

                                os.write(allInfo.getBytes());
                                what++;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class GetPassport extends AsyncTask<Void, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids) {
            DG1Table new_dg1 = new DG1Table();
            new_dg1.id = "0";
            dg1TableDao.delete(new_dg1);

            try {

                // Работа с фотографией
                File file = new File(Environment.getExternalStorageDirectory().
                        getAbsoluteFile() + "/Download/", "photo.jpg");
                byte[] photoBytes = Files.readAllBytes(file.toPath());
                String photoHash = myCrypto.sha256(photoBytes);

                // Шифруем фотографию
                byte[] enc = myCrypto.encrypt2(pass, photoBytes);
                FileOutputStream fos = new FileOutputStream(Environment.
                        getExternalStorageDirectory().getAbsoluteFile() + "/Download/" +
                        "faceshot.jpg");
                fos.write(enc);
                fos.close();

                // Скачивание информации
                new_dg1.documentType = myCrypto.encrypt(pass, "P".getBytes("UTF-16LE"));
                new_dg1.issuingState = myCrypto.encrypt(pass, "RUS".getBytes("UTF-16LE"));
                new_dg1.documentNumber = myCrypto.encrypt(pass, "123456789".getBytes("UTF-16LE"));
                new_dg1.surname = myCrypto.encrypt(pass, "IVANOV".getBytes("UTF-16LE"));
                new_dg1.name = myCrypto.encrypt(pass, "MIKHAIL".getBytes("UTF-16LE"));
                new_dg1.nationality = myCrypto.encrypt(pass, "RUSSIAN FEDERATION".getBytes("UTF-16LE"));
                new_dg1.dateOfBirth = myCrypto.encrypt(pass, "13.01.1998".getBytes("UTF-16LE"));
                new_dg1.sex = myCrypto.encrypt(pass, "M".getBytes("UTF-16LE"));
                new_dg1.dateOfIssue = myCrypto.encrypt(pass, "08.07.2018".getBytes("UTF-16LE"));
                new_dg1.authority = myCrypto.encrypt(pass, "МВД 12345".getBytes("UTF-16LE"));
                new_dg1.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(pass, "08.07.2028".getBytes("UTF-16LE"));
                new_dg1.faceshotHash = myCrypto.encrypt(pass, photoHash.getBytes("UTF-16LE"));
                new_dg1.chipId = myCrypto.encrypt(pass, "12345".getBytes("UTF-16LE"));
                dg1TableDao.insert(new_dg1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
            try {
                socket = new Socket(ip, port);
                // Настройка i/o
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                DataInputStream din = new DataInputStream(is);
                // Отправка запроса на получение информации
                os.write("GetInfo".getBytes());
                int what = 0; // 0 - Получение id, 1 - Получение фото
                // Получение ответа
                while (true) {
                    int len = is.available();
                    if (len > 0) {
                        byte message[] = new byte[len];
                        for (int i = 0; i < len; i++) {
                            message[i] = din.readByte();
                        }
                        // Это получение ID паспорта
                        if (what == 0) {
                            new_dg1.id = new String(message, "UTF_8");
                            os.write("ID ok".getBytes());

                            // Это получение длины фотографии
                            while (true) {
                                len = is.available();
                                if (len > 0) {
                                    byte message2[] = new byte[len];
                                    for (int i = 0; i < len; i++) {
                                        message[i] = din.readByte();
                                    }
                                    int file_len = Integer.parseInt(message2.toString());
                                    os.write(message2);
                                }
                            }
                        }
                    }
                }

                // Типа получение остальной информации
                //new_dg1.documentType = myCrypto.encrypt(pass, "some documentType".getBytes("UTF-16LE"));
                //new_dg1.issuingState = myCrypto.encrypt(pass, "some issuingState".getBytes("UTF-16LE"));
                //new_dg1.documentNumber = myCrypto.encrypt(pass, "some documentNumber".getBytes("UTF-16LE"));
                //new_dg1.surname = myCrypto.encrypt(pass, "some surname".getBytes("UTF-16LE"));
                //new_dg1.name = myCrypto.encrypt(pass, "some name".getBytes("UTF-16LE"));
                //new_dg1.nationality = myCrypto.encrypt(pass, "some nationality".getBytes("UTF-16LE"));
                //new_dg1.dateOfBirth = myCrypto.encrypt(pass, "some dateOfBirth".getBytes("UTF-16LE"));
                //new_dg1.sex = myCrypto.encrypt(pass, "some sex".getBytes("UTF-16LE"));
                //new_dg1.dateOfIssue = myCrypto.encrypt(pass, "some Issue".getBytes("UTF-16LE"));
                //new_dg1.authority = myCrypto.encrypt(pass, "some authority".getBytes("UTF-16LE"));
                //new_dg1.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(pass, "some dataOfExpiry".getBytes("UTF-16LE"));
                //dg1TableDao.insert(new_dg1);

                //return null;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            */

            return null;
        }
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