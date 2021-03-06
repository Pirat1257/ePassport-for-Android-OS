package com.example.epassport;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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

    private int REQUEST_NEW_PASS = 1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        // ?????????????????????? ????????????????
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
        // ???????????????? ???????????????????? ???? ???????????????? Activity
        Bundle arguments = getIntent().getExtras();
        pass = arguments.get("pass").toString();
        ip = arguments.get("ip").toString();
        sessionKey = arguments.get("sessionKey").toString();
        port = Integer.parseInt(arguments.get("port").toString());
        // ?????????????????????????? ???????? ????????????
        db = App.getInstance().getDatabase();
        dg1TableDao = db.dg1TableDao();
        // ???????????????? ???????????? ?????? ???????????? ?? ??????????????????????????
        myCrypto = new MyCrypto();
        // ???????? ???????? ???????????? ???? ????????????, ???????????????????? ?????????? ????????????????????
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

                // ?????????? ????????
                Path faceshotPath = Paths.get(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                        "/Download/" + "faceshot.jpg");
                byte[] face = Files.readAllBytes(faceshotPath);
                Bitmap img = getImage(myCrypto.decrypt2(pass, face));
                faceshot.setImageBitmap(img);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ?????????????????? ?????????????? ???? ???????????? DOWNLOAD
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DownloadTask dt = new DownloadTask();
                //dt.execute();
                hardcodedDownload();
            }
        });

        // ?????????????????? ?????????????? ???? ???????????? CHANGE PASSWORD
        chPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ChangePassActivity.class);
                intent.putExtra("pass", pass);
                startActivityForResult(intent, REQUEST_NEW_PASS);

            }
        });

        // ?????????????????? ?????????????? ???? ?????????? CHECK
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckTask ct = new CheckTask();
                ct.execute();
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    void hardcodedDownload() {
        DG1Table new_dg1 = new DG1Table();
        new_dg1.id = "0";
        dg1TableDao.delete(new_dg1);

        try {
            // ???????????? ?? ??????????????????????
            File file = new File(Environment.getExternalStorageDirectory().
                    getAbsoluteFile() + "/Download/", "photo.jpg");
            // ???????? ???????? ????????????????????
            if(file.exists()) {
                byte[] photoBytes = Files.readAllBytes(file.toPath());
                String photoHash = myCrypto.sha256(photoBytes);

                // ?????????????? ????????????????????
                byte[] enc = myCrypto.encrypt2(pass, photoBytes);
                FileOutputStream fos = new FileOutputStream(Environment.
                        getExternalStorageDirectory().getAbsoluteFile() + "/Download/" +
                        "faceshot.jpg");
                fos.write(enc);
                fos.close();

                // ????????????????????
                new_dg1.documentType = myCrypto.encrypt(pass, "P".getBytes("UTF-16LE"));
                new_dg1.issuingState = myCrypto.encrypt(pass, "RUS".getBytes("UTF-16LE"));
                new_dg1.documentNumber = myCrypto.encrypt(pass, "123456789".getBytes("UTF-16LE"));
                new_dg1.surname = myCrypto.encrypt(pass, "IVANOV".getBytes("UTF-16LE"));
                new_dg1.name = myCrypto.encrypt(pass, "MIKHAIL".getBytes("UTF-16LE"));
                new_dg1.nationality = myCrypto.encrypt(pass, "RUSSIAN FEDERATION".getBytes("UTF-16LE"));
                new_dg1.dateOfBirth = myCrypto.encrypt(pass, "13.01.1998".getBytes("UTF-16LE"));
                new_dg1.sex = myCrypto.encrypt(pass, "M".getBytes("UTF-16LE"));
                new_dg1.dateOfIssue = myCrypto.encrypt(pass, "08.07.2018".getBytes("UTF-16LE"));
                new_dg1.authority = myCrypto.encrypt(pass, "MVD 12345".getBytes("UTF-16LE"));
                new_dg1.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(pass, "08.07.2028".getBytes("UTF-16LE"));
                new_dg1.faceshotHash = myCrypto.encrypt(pass, photoHash.getBytes("UTF-16LE"));
                new_dg1.chipId = myCrypto.encrypt(pass, "12345".getBytes("UTF-16LE"));
                // ??????????????

                String sign1 = "P" + "RUS" + "123456789" + "IVANOV" + "MIKHAIL" +
                        "RUSSIAN FEDERATION" + "13.01.1998" + "M" + "MVD 12345" +
                        "08.07.2028";
                sign1 = myCrypto.sha256(sign1.getBytes());
                String sign2 = myCrypto.sha256(sign1.getBytes());

                new_dg1.sign1 = myCrypto.encrypt(pass, sign1.getBytes("UTF-16LE"));
                new_dg1.sign2 = myCrypto.encrypt(pass, sign2.getBytes("UTF-16LE"));
                dg1TableDao.insert(new_dg1);
                Toast.makeText(MenuActivity.this, "Downloaded", Toast.LENGTH_LONG).show();

                // ???????????????????? ?????????????????????????? ?? ??????????
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

                        // ?????????? ????????
                        Path path = Paths.get(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                                "/Download/" + "faceshot.jpg");
                        byte[] face = Files.readAllBytes(path);
                        Bitmap img = getImage(myCrypto.decrypt2(pass, face));
                        faceshot.setImageBitmap(img);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MenuActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else {
                Toast.makeText(MenuActivity.this, "ERROR - No file " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MenuActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(MenuActivity.this, "Download complete", Toast.LENGTH_LONG).show();
    }

    // ?????????????????????? ?????????? ?????? ???????????????? ??????????????????
    class CheckTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // ???????????? ?????? ???????????????????? ?????????? ??????????????????????
                socket = new Socket(ip, port);
                // ?????????????????? i/o
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                DataInputStream din = new DataInputStream(is);
                // ???????????????? ?????????????? ???? ???????????????? ????????????????????
                byte[] encCheckInfo = myCrypto.encryptAES(sessionKey, "CheckInfo".getBytes());
                os.write(encCheckInfo);
                DG1Table dg1_head = dg1TableDao.selectById(0);
                int what = 0; // 0 - ?????????????????????????? ???? CheckInfo
                              // 1 - ?????????????????? ???????? ????????
                while(true) {
                    int len = is.available();
                    if (len > 0) {
                        byte message[] = new byte[len];
                        for (int i = 0; i < len; i++) {
                            message[i] = din.readByte();
                        }
                        // 0 - ???????????????????????? ???? CheckInfo
                        if (what == 0) {
                            byte[] answerBytes = myCrypto.decryptAES(sessionKey, message);
                            String answer = new String(answerBytes, "UTF_8");
                            // ???????? ?????? ??????????????, ???? ???????????????????????? ???????????????? ID
                            if (answer.compareTo("ok") == 0) {
                                String chipID = myCrypto.decrypt(pass, Base64.decode(dg1_head.chipId.
                                        getBytes("UTF-16LE"), Base64.DEFAULT));
                                byte[] encChipID = myCrypto.encryptAES(sessionKey, chipID.getBytes());
                                os.write(encChipID);
                                what++;
                            }
                        }
                        // 1 - ?????????????????? ???????? ????????
                        else if (what == 1) {
                            byte[] answerBytes = myCrypto.decryptAES(sessionKey, message);
                            String answer = new String(answerBytes, "UTF_8");
                            String hash = myCrypto.decrypt(pass, Base64.decode(dg1_head.faceshotHash.
                                    getBytes("UTF-16LE"), Base64.DEFAULT));
                            // ?????????????? ??????
                            if (answer.compareTo(hash) == 0) {
                                // ???????? ???? ????????????, ???? ?????????????????????? ??????????????, ???????????????? ?????????????????? ????????
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

                                byte[] encAllInfo = myCrypto.encryptAES(sessionKey, allInfo.getBytes());
                                os.write(encAllInfo);
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

    // ?????????????????????????? ?????????? ?????? ???????????????????? ????????????????
    class DownloadTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String newHash = null;
            try {
                // ???????????? ?????? ???????????????????? ?????????? ??????????????????????
                socket = new Socket(ip, port);
                // ?????????????????? i/o
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                DataInputStream din = new DataInputStream(is);
                BufferedReader mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // ???????????????? ?????????????? ???? ?????????????????? ????????????????????
                byte[] encCheckInfo = myCrypto.encryptAES(sessionKey, "GetInfo".getBytes());
                os.write(encCheckInfo);
                int what = 0; // 0 - ?????????????????? ????????????????????
                              // 1 - ?????????????????? ?????????????????? ????????????
                while(true) {
                    int len = is.available();
                    if (len > 0) {
                        // 0 - ?????????????????? ????????????????????
                        if (what == 0) {
                            // ???????????????? ?????????? ????????????????
                            int size = Integer.parseInt(mBufferIn.readLine());
                            byte message[] = new byte[size];
                            // ?????????????????? ????????????????
                            for (int i = 0; i < size; i++) {
                                message[i] = din.readByte();
                            }
                            // ?????????????????? ??????
                            newHash = myCrypto.sha256(message);
                            // ?????????????? ????????????????????
                            byte[] enc = myCrypto.encrypt2(pass, message);
                            FileOutputStream fos = new FileOutputStream(Environment.
                                    getExternalStorageDirectory().getAbsoluteFile() + "/Download/" +
                                    "faceshot.jpg");
                            fos.write(enc);
                            fos.close();
                            byte[] encOk = myCrypto.encryptAES(sessionKey, "Ok".getBytes());
                            os.write(encOk);
                            what++;
                        }
                        // 1 - ?????????????????? ?????????????????? ????????????????????
                        else if (what == 1) {
                            byte message[] = new byte[len];
                            for (int i = 0; i < len; i++) {
                                message[i] = din.readByte();
                            }
                            byte[] allInfoButes = myCrypto.decryptAES(sessionKey, message);
                            String allInfo = new String(allInfoButes, "UTF_8");
                            // ???????????? ??????????
                            int index1 = 0;
                            int index2 = allInfo.indexOf('\n');
                            String chipId = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String documentType = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String issuingState = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String documentNumber = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String surname = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String name = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String nationality = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String dateOfBirth = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String sex = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String authority = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String dateOfExpiryOrValidUntilDate = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String sign1 = allInfo.substring(index1, index2);
                            index1 = index2 + 1;
                            index2 = allInfo.indexOf('\n', index1);
                            String sign2 = allInfo.substring(index1, index2);
                            // ?????????????????? ?????? ????????????????
                            DG1Table dg1_head = dg1TableDao.selectById(0);
                            if (dg1_head != null) {
                                dg1_head.chipId = myCrypto.encrypt(pass, chipId.getBytes("UTF-16LE"));
                                dg1_head.documentType = myCrypto.encrypt(pass, documentType.getBytes("UTF-16LE"));
                                dg1_head.issuingState = myCrypto.encrypt(pass, issuingState.getBytes("UTF-16LE"));
                                dg1_head.documentNumber = myCrypto.encrypt(pass, documentNumber.getBytes("UTF-16LE"));
                                dg1_head.surname = myCrypto.encrypt(pass, surname.getBytes("UTF-16LE"));
                                dg1_head.name = myCrypto.encrypt(pass, name.getBytes("UTF-16LE"));
                                dg1_head.nationality = myCrypto.encrypt(pass, nationality.getBytes("UTF-16LE"));
                                dg1_head.dateOfBirth = myCrypto.encrypt(pass, dateOfBirth.getBytes("UTF-16LE"));
                                dg1_head.sex = myCrypto.encrypt(pass, sex.getBytes("UTF-16LE"));
                                dg1_head.authority = myCrypto.encrypt(pass, authority.getBytes("UTF-16LE"));
                                dg1_head.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(pass, dateOfExpiryOrValidUntilDate.getBytes("UTF-16LE"));
                                dg1_head.sign1 = myCrypto.encrypt(pass, sign1.getBytes("UTF-16LE"));
                                dg1_head.sign2 = myCrypto.encrypt(pass, sign2.getBytes("UTF-16LE"));

                                dg1_head.faceshotHash = myCrypto.encrypt(pass, newHash.getBytes("UTF-16LE"));

                                dg1TableDao.update(dg1_head);
                            }
                            else {
                                DG1Table new_dg1 = new DG1Table();
                                new_dg1.id = "0";
                                new_dg1.chipId = myCrypto.encrypt(pass, chipId.getBytes("UTF-16LE"));
                                new_dg1.documentType = myCrypto.encrypt(pass, documentType.getBytes("UTF-16LE"));
                                new_dg1.issuingState = myCrypto.encrypt(pass, issuingState.getBytes("UTF-16LE"));
                                new_dg1.documentNumber = myCrypto.encrypt(pass, documentNumber.getBytes("UTF-16LE"));
                                new_dg1.surname = myCrypto.encrypt(pass, surname.getBytes("UTF-16LE"));
                                new_dg1.name = myCrypto.encrypt(pass, name.getBytes("UTF-16LE"));
                                new_dg1.nationality = myCrypto.encrypt(pass, nationality.getBytes("UTF-16LE"));
                                new_dg1.dateOfBirth = myCrypto.encrypt(pass, dateOfBirth.getBytes("UTF-16LE"));
                                new_dg1.sex = myCrypto.encrypt(pass, sex.getBytes("UTF-16LE"));
                                new_dg1.authority = myCrypto.encrypt(pass, authority.getBytes("UTF-16LE"));
                                new_dg1.dateOfExpiryOrValidUntilDate = myCrypto.encrypt(pass, dateOfExpiryOrValidUntilDate.getBytes("UTF-16LE"));
                                new_dg1.sign1 = myCrypto.encrypt(pass, sign1.getBytes("UTF-16LE"));
                                new_dg1.sign2 = myCrypto.encrypt(pass, sign2.getBytes("UTF-16LE"));

                                new_dg1.faceshotHash = myCrypto.encrypt(pass, newHash.getBytes("UTF-16LE"));

                                dg1TableDao.insert(new_dg1);
                            }
                            onProgressUpdate("New Passport downloaded");
                        }
                    }
                }
            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                    InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // ???????????????????? ?????????????????????????? ?? ??????????
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

                    // ?????????? ????????
                    Path path = Paths.get(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                            "/Download/" + "faceshot.jpg");
                    byte[] face = Files.readAllBytes(path);
                    Bitmap img = getImage(myCrypto.decrypt2(pass, face));
                    faceshot.setImageBitmap(img);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MenuActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    // convert from byte array to bitmap
    public Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ?? ???????????? ?????????????????? ????????????
        if (requestCode == REQUEST_NEW_PASS) {
            if (resultCode == RESULT_OK) {
                pass = data.getStringExtra("new_pass");
                // update_sources();
                Toast.makeText(MenuActivity.this, "Password updated", Toast.LENGTH_LONG).show();
            }
        }
    }
}