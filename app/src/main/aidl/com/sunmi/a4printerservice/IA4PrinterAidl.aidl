// IA4PrinterAidl.aidl
package com.sunmi.a4printerservice;

import com.sunmi.a4printerservice.ICallback;

// Declare any non-default types here with import statements

interface IA4PrinterAidl {

    void printImage(String path, int mode, in ICallback callback);

    int getPrinterStatus();

    void printImageEx(String path, int mode, boolean rotate90, in ICallback callback);
}
