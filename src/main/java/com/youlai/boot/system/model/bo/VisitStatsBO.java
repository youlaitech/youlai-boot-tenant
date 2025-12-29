package com.youlai.boot.system.model.bo;

import lombok.Data;

/**
 * 访问统计（PV/UV）摘要
 *
 * @author Ray
 * @since 3.0.0
 */
@Data
public class VisitStatsBo {

    /**
     * 今日统计
     */
    private Integer todayCount;

    /**
     * 累计统计
     */
    private Integer totalCount;

    /**
     * 增长率（百分比或小数）
     */
    private Double growthRate;
}


