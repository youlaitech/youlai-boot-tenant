package com.youlai.boot.support.websocket.dto;

import lombok.Data;

@Data
public class DictChangeEvent {

    private String dictCode;
    private long timestamp;

    public DictChangeEvent(String dictCode) {
        this.dictCode = dictCode;
        this.timestamp = System.currentTimeMillis();
    }
}
