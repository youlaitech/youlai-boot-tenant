package com.youlai.boot.common.util;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.event.AnalysisEventListener;

import java.io.InputStream;

/**
 * Excel 工具类
 *
 * @author haoxr
 * @since 2023/03/01
 */
public class ExcelUtils {
/**
 * 导入 Excel 数据
 */

    public static <T> void importExcel(InputStream is, Class clazz, AnalysisEventListener<T> listener) {
        EasyExcel.read(is, clazz, listener).sheet().doRead();
    }
}