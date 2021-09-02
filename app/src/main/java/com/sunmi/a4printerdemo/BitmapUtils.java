package com.sunmi.a4printerdemo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by zhicheng.liu on 2018/4/4
 * address :liuzhicheng@sunmi.com
 * description :
 */

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    /**
     * 保存图片到本地
     * @param path
     * @param bitmap
     * @return
     */
    public static boolean saveBitmap(String path, String name, Bitmap bitmap){
        File checkPath = new File(path);
        if (!checkPath.exists()){
            try {
                checkPath.mkdirs();
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        File file = new File(path, name);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);  //0-100,100是不压缩
            fos.flush();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 删除文件
     * @param filePath
     */
    public static boolean deleteFile(String filePath){
        boolean flag = false;
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 创建一个空白图像
     */
    private static Bitmap bmp = null;
    public static Bitmap createEmptyImage(int w, int h){
        if (bmp == null)
            bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        return bmp;
    }

    /**
     * 文本转图像
     * @param text 文本内容，支持换行符
     * @param size 文字的尺寸（这仨的单位都是像素点）
     * @param bmp 原图
     */
    public static Bitmap text2Image(String text, int size, Bitmap bmp){
        try {
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setTextAlign(Paint.Align.LEFT);// 若设置为center，则文本左半部分显示不全
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);// 消除锯齿
            paint.setTextSize(size);

            ///////////////////////
            StringBuilder sb = new StringBuilder();
            char c;
            int posX = 0;
            int posY = size;
            //text中如果包含\r和\n,则需要拆分和剔除,因为这俩被打印机用作了分段符
            for (int i = 0; i < text.length(); i++) {
                c = text.charAt(i);
                if (c == '\r' || c == '\n') {
                    canvas.drawText(sb.toString(), posX, posY, paint);
                    posY += size;
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            canvas.drawText(sb.toString(), posX, posY, paint);
            /////////////////////

            canvas.save();
            canvas.restore();

            return bmp;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * pdf转bmp
     * @param
     */
    public static ArrayList<Bitmap> pdfToBitmap(String path, int width, int height) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try {
            Log.e("test2020", "000:" + path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                File pdfFile = new File(path);
                if (!pdfFile.exists()) {
                    Log.e("test2020", "111");
                    return null;
                }
                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
                Bitmap bitmap;
                final int pageCount = renderer.getPageCount();
                Log.e("test_sign", "图片de 张数： " +pageCount);
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
//                    int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
//                    int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    //todo 以下三行处理图片存储到本地出现黑屏的问题，这个涉及到背景问题
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    Rect r = new Rect(0, 0, width, height);
                    page.render(bitmap, r, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    bitmaps.add(bitmap);
                    // close the page
                    page.close();
                }
                // close the renderer
                renderer.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmaps;

    }


    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }else {
            // 以 file:// 开头的
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                path = uri.getPath();
                return path;
            }
            // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (columnIndex > -1) {
                            path = cursor.getString(columnIndex);
                        }
                    }
                    cursor.close();
                }
                return path;
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * copy 整个文件夹,比copy方便一点
     *
     * @param context
     * @param assetDir
     * @param dir
     */
    public static void CopyAssets(Context context, String assetDir, String dir) {
        String[] files;
        try {
            // 获得Assets一共有几多文件
            files = context.getAssets().list(assetDir);
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        File mWorkingPath = new File(dir);
        // 如果文件路径不存在
        if (!mWorkingPath.exists()) {
            // 创建文件夹
            if (!mWorkingPath.mkdirs()) {
                // 文件夹创建不成功时调用
            }
        }

        for (String file : files) {
            try {
                // 根据路径判断是文件夹还是文件
                if (!file.contains(".")) {
                    if (0 == assetDir.length()) {
                        CopyAssets(context, file, dir + file + "/");
                    } else {
                        CopyAssets(context, assetDir + "/" + file, dir + "/" + file + "/");
                    }
                    continue;
                }
                File outFile = new File(mWorkingPath, file);
                if (outFile.exists())
                    continue;
                InputStream in;
                if (0 != assetDir.length()) {
                    in = context.getAssets().open(assetDir + "/" + file);
                } else {
                    in = context.getAssets().open(file);
                }

                OutputStream out = new FileOutputStream(outFile);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
