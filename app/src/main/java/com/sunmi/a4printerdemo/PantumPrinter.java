package com.sunmi.a4printerdemo;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.sunmi.a4printerservice.IA4PrinterAidl;
import com.sunmi.a4printerservice.ICallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by mayflower on 2020.8.6
 * get me at jiangli@sunmi.com
 */

public class PantumPrinter {
    private static final String TAG = PantumPrinter.class.getSimpleName();
    public static final String FILE_DIR = Environment.getExternalStorageDirectory().toString() + "/YourPath";
    public static final int A4_WIDTH_MAX = 4736;
    public static final int A4_HEIGHT_MAX = 6784;

    public MutableLiveData<Integer> codesss = new MutableLiveData<>();

    private Context mContext;
    private IA4PrinterAidl mPrinter;

    //单一线程线程池，虽然打印是异步的，但这里Demo要测试循环打印
    private ExecutorService singleThreadExecutor = null;
    private boolean bPrinting = false;

    public interface ServiceCallback{
        void onServiceConnected();
        void onServiceDisconnected();
    }
    private ServiceCallback serviceCallback = null;

    public void bindPrinterService(Context context, ServiceCallback callback){
        Log.i(TAG, "bindPrinterService:" + context);
        if (context == null){
            Log.e(TAG, "context is null");
            return;
        }
        this.mContext = context;
        this.serviceCallback = callback;
        //也放这里初始化吧
        if (singleThreadExecutor == null)
            singleThreadExecutor = Executors.newSingleThreadExecutor();

        Intent intent = new Intent();
        intent.setPackage("com.sunmi.a4printerservice");
        intent.setAction("com.sunmi.a4printerservice.PrinterService");
        mContext.bindService(intent, scPTPrinter, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection scPTPrinter = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected.");
            mPrinter = IA4PrinterAidl.Stub.asInterface(iBinder);
            if (serviceCallback != null)
                serviceCallback.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected.");
            mPrinter = null;
            if (serviceCallback != null)
                serviceCallback.onServiceDisconnected();
        }
    };

    public void unbindPrinterService(){
        Log.i(TAG, "unbindPrinterService.");
        if (mPrinter != null)
            mContext.unbindService(scPTPrinter);
    }

    public String printFile(final String path, final boolean bCut, final boolean rotate90, final Context context){
        final int[] codes = {-999};
        if (mPrinter == null) {
            Log.d(TAG, "printImage,mPrinter is null");
            return "printImage,mPrinter is null";
        }
        try {
            Log.d(TAG, "printFile start");

            //jiwo
            mPrinter.printImage(path, bCut ? 0 : 1, new ICallback.Stub() {
                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    codes[0] = code;
                    codesss.postValue(codes[0]);
                    Log.i(TAG, "printImage,onPrintResult:" + code + ", " + msg + ", " + path);
                }
            });

            /* mPrinter.printImageEx(path, bCut ? 0 : 1, rotate90, new ICallback.Stub() {
                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    codes[0] = code;
                    codesss.postValue(codes[0]);
                    Log.i(TAG, "printImage,onPrintResult:" + code + ", " + msg + ", " + path);
                }
            }); */
        } catch (RemoteException e){
            e.printStackTrace();
            return e.getMessage().toString();
        }
        return "Success... code: " + codes[0] + " path: " + path + " bCut: " + bCut + " rotate90: " + rotate90;
    }

    public String printText(final String data, final boolean bCut){
        final int[] codes = {-999};
        if (mPrinter == null){
            Log.e(TAG, "printText:mPrinter is null");
            return "printText:mPrinter is null";
        }
        try {
            Log.d(TAG, "printText start");

            final Bitmap[] bitmap = {BitmapUtils.text2Image(data, 30, BitmapUtils.createEmptyImage(A4_WIDTH_MAX / 4, A4_HEIGHT_MAX / 4))};

            final String path = FILE_DIR;
            final String name = "/" + System.currentTimeMillis() + ".jpg";
            BitmapUtils.saveBitmap(path, name, bitmap[0]);

            mPrinter.printImage(path + name, bCut ? 0 : 1, new ICallback.Stub() {
                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    codes[0] = code;
                    codesss.postValue(codes[0]);
                    Log.i(TAG, "printText,onPrintResult:" + code + ", " + msg + ", " + path + name);
                    BitmapUtils.deleteFile(path + name);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "Success... code: " + codes[0] + "  data: " + data;
    }

    /* Jiwo */
    public String printBitmap(final Bitmap data, final boolean bCut){
        final int[] codes = {-999};
        if (mPrinter == null){
            Log.e(TAG, "printText:mPrinter is null");
            return "printText:mPrinter is null";
        }
        try {
            Log.d(TAG, "printText start");

            final String path = FILE_DIR;
            final String name = "/" + System.currentTimeMillis() + ".jpg";
            BitmapUtils.saveBitmap(path, name, data);

            mPrinter.printImage(path + name, bCut ? 0 : 1, new ICallback.Stub() {
                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    codes[0] = code;
                    codesss.postValue(codes[0]);
                    Log.i(TAG, "printText,onPrintResult:" + code + ", " + msg + ", " + path + name);
                    BitmapUtils.deleteFile(path + name);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "Success... code: " + codes[0] + "  data: " + data;
    }

    public String printImage(final Bitmap bitmap, final boolean bCut) {
        final int[] codes = {-999};
        if (mPrinter == null) {
            Log.d(TAG, "printImage,mPrinter is null");
            return "printImage,mPrinter is null";
        }
        try {
            Log.d(TAG, "printImage start");
            //保存到本地
            final String path = FILE_DIR;
            final String name = "/" + System.currentTimeMillis() + ".jpg";
            BitmapUtils.saveBitmap(path, name, bitmap);

            //这个bCut充当A4,A5的判断吧
            //测试 pdf
            /* mPrinter.printImageEx(Environment.getExternalStorageDirectory().toString() + "/测试.pdf",
                    bCut ? 0 : 1, false, new ICallback.Stub() {
                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    Log.i(TAG, "printImage,onPrintResult:" + code + ", " + msg + ", " + path + name);
                    BitmapUtils.deleteFile(path + name);
                }
            }); */


            //Jiwo
            mPrinter.printImage(path + name, bCut ? 0 : 1, new ICallback.Stub() {
                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    codes[0] = code;
                    codesss.postValue(codes[0]);
                    Log.i(TAG, "printText,onPrintResult:" + code + ", " + msg + ", " + path + name);
                    BitmapUtils.deleteFile(path + name);
                }
            });

            /* mPrinter.printImageEx(path + name, bCut ? 0 : 1, true, new ICallback.Stub() {
                //            mPrinter.printImage(path + name, bCut ? 0 : 1, new ICallback.Stub() {
                @Override
                public void onPrintResult(int code, String msg) throws RemoteException {
                    codes[0] = code;
                    codesss.postValue(codes[0]);
                    Log.i(TAG, "printImage,onPrintResult:" + code + ", " + msg + ", " + path + name);
                    BitmapUtils.deleteFile(path + name);
                }
            }); */

        } catch (RemoteException e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "Success code: " + codes[0];
    }

    public interface PrinterStatusListener{
        void onStatusChanged(ModuleStatus status);
    }
    private PrinterStatusListener statusListener = null;

    public void getStatus(PrinterStatusListener listener) {
        if (listener == null) {
            Log.e(TAG, "getStatus,listener is null");
            return;
        }
        statusListener = listener;

        new Thread(new Runnable() {
            @Override
            public void run() {
                int newStatus = -1;
                while (mPrinter != null){
                    try {
                        newStatus = mPrinter.getPrinterStatus();
                        if (curStatus != newStatus){
                            curStatus = newStatus;
                            moduleStatus = translateStatus(curStatus);
                            Log.i(TAG, "getStatus:" + moduleStatus.toString() + "(" + curStatus +")");
                            if (statusListener != null)
                                statusListener.onStatusChanged(moduleStatus);
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    //打印机服务返回的状态是int值
    private int curStatus = -1;
    //转换成咱们的ModuleStatus,方便管理
    private ModuleStatus moduleStatus = ModuleStatus.OFFLINE;

    public ModuleStatus translateStatus(int iStatus){
        ModuleStatus status;
        switch (iStatus){
            case 11:
                status = ModuleStatus.INIT;
                break;
            case 12:
                status = ModuleStatus.SLEEP;
                break;
            case 13:
                status = ModuleStatus.PREPARE;
                break;
            case 14:
                status = ModuleStatus.TONER_LESS;
                break;
            case 15:
                status = ModuleStatus.MULTIFUNCTIONAL_CARTON_LESS;
                break;
            case 16:
                status = ModuleStatus.STANDARD_CARTON_LESS;
                break;
            case 17:
                status = ModuleStatus.READY;
                break;
            case 18:
                status = ModuleStatus.PRINTING;
                break;
            case 19:
                status = ModuleStatus.FATAL_ERROR;
                break;

            case 21:
                status = ModuleStatus.FRONT_COVER_OPEN;
                break;
            case 22:
                status = ModuleStatus.BACK_COVER_OPEN;
                break;
            case 23:
                status = ModuleStatus.TONER_UNINSTALLED;
                break;
            case 24:
                status = ModuleStatus.TONER_MISMATCHING;
                break;
            case 25:
                status = ModuleStatus.TONER_EMPTY;
                break;
            case 26:
                status = ModuleStatus.PAPER_JAM_EXIT;
                break;
            case 27:
                status = ModuleStatus.PAPER_JAM_INSIDE_STILL;
                break;
            case 28:
                status = ModuleStatus.PAPER_JAM_EXIT_STILL;
                break;
            case 29:
                status = ModuleStatus.PAPER_JAM_INSIDE;
                break;

            case 31:
                status = ModuleStatus.DUPLEX_PAPER_JAM;
                break;
            case 32:
                status = ModuleStatus.DUPLEX_UNINSTALLED;
                break;
            case 33:
                status = ModuleStatus.CARTON_MISMATCHING;
                break;
            case 34:
                status = ModuleStatus.CARTON_UNINSTALLED;
                break;
            case 35:
                status = ModuleStatus.CARTON_PAPER_EMPTY_WHEN_PRINTING;
                break;
            case 36:
                status = ModuleStatus.CARTON_PAPER_MISMATCHING;
                break;
            case 37:
                status = ModuleStatus.PAPER_JAM_ENTRY;
                break;
            case 38:
                status = ModuleStatus.CARTON_PAPER_MISMATCHING_WITH_SETTING;
                break;
            case 39:
                status = ModuleStatus.CARTON_PAPER_EMPTY_WHEN_STANDBY_OR_READY;
                break;

            case 41:
                status = ModuleStatus.DRUM_UNINSTALLED;
                break;
            case 42:
                status = ModuleStatus.DRUM_MISMATCHING;
                break;
            case 43:
                status = ModuleStatus.DRUM_EMPTY;
                break;
            case 44:
                status = ModuleStatus.AUTO_CARTON_EMPTY;
                break;
            case 45:
                status = ModuleStatus.HAND_CARTON_EMPTY;
                break;
            case 46:
                status = ModuleStatus.PAPER_MOVE_FAIL;
                break;
            case 47:
                status = ModuleStatus.PAPER_MISMATCHING;
                break;
            case 48:
                status = ModuleStatus.DUPLEX_PAPER_MISMATCHING;
                break;
            case 49:
                status = ModuleStatus.PAPER_MOVE_MISMATCHING_WITH_SETTING;
                break;

            case 77:
                status = ModuleStatus.UNKNOWN_EXCEPTION;
                break;
            case -1:
                status = ModuleStatus.PARA_LENGTH_ERROR;
                break;
            case -5:
                status = ModuleStatus.FUNCTION_CLOSED;
                break;
            case -8:
                status = ModuleStatus.MALLOC_FAIL;
                break;
            case -11:
                status = ModuleStatus.LICENSE_FAIL;
                break;
            case -99:
                status = ModuleStatus.UNKNOWN_ERROR;
                break;

            default:
                status = ModuleStatus.UNKNOWN;
                break;
        }
        return status;
    }

    public boolean bStatusAvailable(){
        if (mPrinter == null)
            return false;
        try {
            int status = mPrinter.getPrinterStatus();
            switch (translateStatus(status)) {
                case SLEEP:
                case PREPARE:
                case TONER_LESS:
                case MULTIFUNCTIONAL_CARTON_LESS:
                case STANDARD_CARTON_LESS:
                case READY:
                    return true;
                default:
                    return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
