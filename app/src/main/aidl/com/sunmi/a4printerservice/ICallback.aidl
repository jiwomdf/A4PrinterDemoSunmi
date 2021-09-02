// ICallback.aidl
package com.sunmi.a4printerservice;

interface ICallback {

    /**
    * 返回打印机结果，只包含数据处理和发送结果（约1.8s），不包含打印机执行打印动作（约9s）
    * code：	异常代码 =0成功 <0失败,具体参看错误码表
    * msg:	异常描述
    */
    oneway void onPrintResult(int code, String msg);

}
