package com.sunmi.a4printerdemo;

import android.Manifest;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private final int DEF_TIME_PRINT = 10000;   //打印过程大约10s，循环测试时，可以加上这个间隔
    private final String[] Permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};   //买一送一， READ_EXTERNAL_STORAGE会自动添加
    private List<String> pList = new ArrayList<>();     //用于多个权限请求，本Demo会读写文件，所以授权放这里

    private PantumPrinter mPrinter;
    private boolean bPrinterConnected = false;

    private TextView tvPrinterStatus;
    private Button btnPrinterBitmap;
    private Button btnPrinterFile;
    private Button btnPrinterText;
    private Button btnPrinterImage;
    private CheckBox cbPrinterModeFile;
    private CheckBox cbRotate90File;
    private CheckBox cbPrinterModeText;
    private CheckBox cbPrinterModeImage;

    private Button btnPrinterCirculate;
    private Button btnPrinterStop;
    private CheckBox cbPrinterText;
    private CheckBox cbPrinterImage;
    private EditText etPrinterTimes;
    private EditText etPrinterInterval;

    private Button btnPdfSelect;
    private EditText etPdfPath;
    private EditText etTextArea;
    private ImageView ivImageArea;
    private TextView tvNoteArea;
    private ScrollView svNoteArea;

    //循环打印线程
    private Thread thread;
    private boolean bExit = false;
    //记录单次点击的总次数
    private int totalTextTimes;
    private int totalImageTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initPrinter();

        mPrinter.codesss.observe(this, new Observer<Integer>(){
            @Override
            public void onChanged(@Nullable Integer integer) {
                Toast.makeText(MainActivity.this, "codes: " + integer, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void initView() {
        tvPrinterStatus = (TextView) findViewById(R.id.tv_printer_status);
        btnPrinterBitmap = (Button) findViewById(R.id.btn_print_bitmap);
        btnPrinterFile = (Button) findViewById(R.id.btn_printer_pdf);
        btnPrinterText = (Button) findViewById(R.id.btn_printer_text);
        btnPrinterImage = (Button) findViewById(R.id.btn_printer_image);
        cbPrinterModeFile = (CheckBox) findViewById(R.id.cb_printer_mode_pdf);
        cbRotate90File = (CheckBox) findViewById(R.id.cb_rotete90_pdf);
        cbPrinterModeText = (CheckBox) findViewById(R.id.cb_printer_mode_text);
        cbPrinterModeImage = (CheckBox) findViewById(R.id.cb_printer_mode_image);

        btnPrinterCirculate = (Button) findViewById(R.id.btn_printer_circulate);
        btnPrinterStop = (Button) findViewById(R.id.btn_printer_stop);
        cbPrinterText = (CheckBox) findViewById(R.id.cb_printer_text);
        cbPrinterImage = (CheckBox) findViewById(R.id.cb_printer_image);
        etPrinterTimes = (EditText) findViewById(R.id.et_printer_times);
        etPrinterInterval = (EditText) findViewById(R.id.et_printer_interval);

        btnPdfSelect = (Button) findViewById(R.id.btn_pdf_select);
        etPdfPath = (EditText) findViewById(R.id.et_pdf_path);
        etTextArea = (EditText) findViewById(R.id.et_text_area);
        ivImageArea = (ImageView) findViewById(R.id.iv_image_area);
        tvNoteArea = (TextView) findViewById(R.id.tv_note_area);
        svNoteArea = (ScrollView) findViewById(R.id.sv_note_area);
    }

    protected void initData(){
        btnPrinterBitmap.setOnClickListener(this);
        btnPrinterFile.setOnClickListener(this);
        btnPrinterText.setOnClickListener(this);
        btnPrinterImage.setOnClickListener(this);
        btnPrinterCirculate.setOnClickListener(this);
        btnPrinterStop.setOnClickListener(this);
        btnPdfSelect.setOnClickListener(this);
        ivImageArea.setOnClickListener(this);

        totalTextTimes = 0;
        totalImageTimes = 0;

        pList.clear();
        for (String p : Permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED){
                pList.add(p);
            }
        }
        if (pList.size() > 0) {
            ActivityCompat.requestPermissions(this, pList.toArray(new String[0]), 1);
            //可以用PermissionUtils等授权结果回调
        }

        BitmapUtils.CopyAssets(this, "demo", Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void initPrinter(){
        mPrinter = new PantumPrinter();
        mPrinter.bindPrinterService(getApplicationContext(), new PantumPrinter.ServiceCallback() {
            @Override
            public void onServiceConnected() {
                bPrinterConnected = true;
                mPrinter.getStatus(new PantumPrinter.PrinterStatusListener() {
                    @Override
                    public void onStatusChanged(final ModuleStatus status) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvPrinterStatus.setText(status.toString());
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected() {
                bPrinterConnected = false;
            }
        });
    }

    @Override
    public void onClick(View v){
        String strDate = TimeUtils.formatTime(new Date(), true);
        if (!bPrinterConnected){
            append(strDate + ":模块未连接\r\n");
            return;
        }

        switch (v.getId()){
            case R.id.btn_print_bitmap:
                append(strDate + ":" + btnPrinterFile.getText().toString() + "\r\n");

                int A4_WIDTH_MAX = 4736;
                int A4_HEIGHT_MAX = 6784;

                Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.bpjs);

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, A4_WIDTH_MAX / 4, A4_HEIGHT_MAX / 4, true);

                String msg0 = mPrinter.printBitmap(resizedBitmap, cbPrinterModeFile.isChecked());
                Toast.makeText(this, msg0, Toast.LENGTH_LONG).show();

                break;
            case R.id.btn_printer_pdf:
                append(strDate + ":" + btnPrinterFile.getText().toString() + "\r\n");

                String msg = mPrinter.printFile(etPdfPath.getText().toString(), cbPrinterModeFile.isChecked(), cbRotate90File.isChecked(), this);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_printer_text:
                append(strDate + ":" + btnPrinterText.getText().toString() + "x" + ++totalTextTimes + "\r\n");

                String msg2 = mPrinter.printText(etTextArea.getText().toString(), cbPrinterModeText.isChecked());
                Toast.makeText(this, msg2, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_printer_image:
                append(strDate + ":" + btnPrinterImage.getText().toString() + "x" + ++totalImageTimes + "\r\n");

                String msg3 = mPrinter.printImage(((BitmapDrawable)ivImageArea.getDrawable()).getBitmap(), cbPrinterModeImage.isChecked());
                Toast.makeText(this, msg3, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_printer_circulate:
                if (thread != null && thread.isAlive())
                    return;

                append(strDate + ":" + btnPrinterCirculate.getText().toString()
                        + "[" + (cbPrinterText.isChecked()? btnPrinterText.getText().toString() : "") + ","
                        + (cbPrinterImage.isChecked()? btnPrinterImage.getText().toString() : "") + ","
                        + etPrinterTimes.getText().toString() + ","
                        + etPrinterInterval.getText().toString() + "]" + "\r\n");

                final int times = Integer.valueOf(etPrinterTimes.getText().toString());
                final int interval = Integer.valueOf(etPrinterInterval.getText().toString());
                final boolean bText = cbPrinterText.isChecked();
                final boolean bImage = cbPrinterImage.isChecked();

                bExit = false;
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (int i=0; i<times;i++){
                                if (bExit)
                                    return;
                                if (bText)
                                    btnPrinterText.callOnClick();
                                if (bImage)
                                    btnPrinterImage.callOnClick();

                                Thread.sleep(interval + DEF_TIME_PRINT);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();

                break;
            case R.id.btn_printer_stop:
                append(strDate + btnPrinterStop.getText().toString() + "\r\n");
                bExit = true;
                break;
            case R.id.btn_pdf_select:
                Intent intentPdf = new Intent(Intent.ACTION_GET_CONTENT);
                intentPdf.setType("application/pdf");
                //回调见onActivityResult
                startActivityForResult(intentPdf, 0x2);
                break;
            case R.id.iv_image_area:
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*");
                //回调见onActivityResult
                startActivityForResult(intent, 0x1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1 && data != null) {
            ivImageArea.setImageURI(data.getData());
        }
        else if (requestCode == 0x2 && data != null) {
            final String absPath = BitmapUtils.getFilePathByUri(getApplicationContext(), data.getData());
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etPdfPath.setText(absPath);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (mPrinter != null)
            mPrinter.unbindPrinterService();
        mPrinter = null;
        bPrinterConnected = false;
    }

    private void append(final String message) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvNoteArea.append(message);
                    svNoteArea.post(new Runnable() {
                        @Override
                        public void run() {
                            svNoteArea.smoothScrollTo(0, tvNoteArea.getBottom());
                        }
                    });
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
