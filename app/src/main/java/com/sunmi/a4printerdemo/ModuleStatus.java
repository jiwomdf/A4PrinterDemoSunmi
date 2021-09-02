package com.sunmi.a4printerdemo;

/**
 * created by mayflower on 2020.8.26
 * get me at jiangli@sunmi.com
 * 这个类的作用是统一管理多种模块的状态，配合translateStatus使用
 **/

public enum ModuleStatus {
    //通用
    READY,
    ONLINE,
    OFFLINE,
    UNKNOWN,

    //打印机常用
    COVER_OPEN,
    PAPER_EMPTY,
    PAPER_LESS,
    TOO_HOT,

    /**sunmi内置打印机********************************/
    COMMUNICATION_EXCEPTION,
    CUTTER_EXCEPTION,
    CUTTRT_RESUME,
    BLACK_DOT_MISSING,
    FIRMWARE_UPDATE_FAIL,
    /************************************************/

    /**A4打印机，重复的会注释掉，并备注重复项*************/
    //打印机初始化
    INIT,
    //打印机休眠
    SLEEP,
    //打印机预热中
    PREPARE,
    //打印机粉量低
    TONER_LESS,
    //打印机多功能纸盒纸少
    MULTIFUNCTIONAL_CARTON_LESS,
    //打印机标准纸盒纸少
    STANDARD_CARTON_LESS,
    //打印机待机(就绪)
//    STANDBY_OR_READY,    //同READY，这项太重要，不能重复
    //打印机打印中
    PRINTING,
    //打印机致命错误
    FATAL_ERROR,

    //打印机前盖打开
    FRONT_COVER_OPEN,
    //打印机后盖打开
    BACK_COVER_OPEN,
    //打印机未安装粉盒
    TONER_UNINSTALLED,
    //打印机粉盒不匹配
    TONER_MISMATCHING,
    //打印机粉盒用尽
    TONER_EMPTY,
    //打印机出纸口卡纸
    PAPER_JAM_EXIT,
    //打印机中间卡纸未排除
    PAPER_JAM_INSIDE_STILL,
    //打印机出纸口卡纸未排除
    PAPER_JAM_EXIT_STILL,
    //打印机中间卡纸
    PAPER_JAM_INSIDE,

    //打印机双面单元卡纸
    DUPLEX_PAPER_JAM,
    //打印机双面单元未安装
    DUPLEX_UNINSTALLED,
    //打印机无匹配纸盒
    CARTON_MISMATCHING,
    //打印机纸盒未安装
    CARTON_UNINSTALLED,
    //打印机纸盒缺纸(打印中)
    CARTON_PAPER_EMPTY_WHEN_PRINTING,
    //打印机纸盒无匹配纸张
    CARTON_PAPER_MISMATCHING,
    //打印机进纸处卡纸
    PAPER_JAM_ENTRY,
    //打印机纸盒设定纸张与实际纸张不匹配
    CARTON_PAPER_MISMATCHING_WITH_SETTING,
    //打印机纸盒缺纸(打印机待机(就绪)中)
    CARTON_PAPER_EMPTY_WHEN_STANDBY_OR_READY,

    //未安装鼓组件
    DRUM_UNINSTALLED,
    //鼓组件不匹配
    DRUM_MISMATCHING,
    //鼓组件用尽
    DRUM_EMPTY,
    //自动进纸盒缺纸
    AUTO_CARTON_EMPTY,
    //手动进纸盒缺纸
    HAND_CARTON_EMPTY,
    //进纸失败
    PAPER_MOVE_FAIL,
    //纸型不匹配
    PAPER_MISMATCHING,
    //双面打印出现纸型不匹配
    DUPLEX_PAPER_MISMATCHING,
    //纸张来源与实际进纸不匹配错误
    PAPER_MOVE_MISMATCHING_WITH_SETTING,

    //打印机未知异常状态
    UNKNOWN_EXCEPTION,
    //传入的打印机状态长度错误
    PARA_LENGTH_ERROR,
    //不支持查询打印机状态
    FUNCTION_CLOSED,
    //使用者类型内存分配失败
    MALLOC_FAIL,
    //SMLLD使用授权不通过
    LICENSE_FAIL,
    //未知异常错误
    UNKNOWN_ERROR,
    /**************************************************/

}
