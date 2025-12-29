package com.youlai.boot.system.model.bo;

import lombok.Data;

/**
 * 通知公告展示对象
 *
 * @author Ray
 * @since 3.0.0
 */
@Data
public class NoticeBo {

    private Long id;

    private String title;

    private String content;

    private Integer type;

    /**
     * 发布人昵称
     */
    private String publisherName;

    private Integer level;

    private Integer publishStatus;

    private String publishTime;

    private String createTime;
}


