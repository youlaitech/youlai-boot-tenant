package com.youlai.boot.core.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结构体
 *
 * @author Ray.Hao
 * @since 2022/2/18
 */
@Data
public class PageResult<T> implements Serializable {

    private String code;

    private String msg;

    private List<T> data;

    /**
     * 分页元信息
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Page page;

    public static <T> PageResult<T> success(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());

        List<T> records =
                (page == null || page.getRecords() == null)
                        ? Collections.emptyList()
                        : page.getRecords();
        result.setData(records);

        Page pageMeta = new Page();
        pageMeta.setPageNum(page != null ? page.getCurrent() : 1L);
        pageMeta.setPageSize(page != null ? page.getSize() : 0L);
        pageMeta.setTotal(page != null ? page.getTotal() : 0L);
        result.setPage(pageMeta);

        return result;
    }

    /**
     * 构建列表结果（无分页）。
     */
    public static <T> PageResult<T> success(List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(list != null ? list : Collections.emptyList());
        result.setPage(null);
        return result;
    }

    @Data
    public static class Page {

        private long pageNum;

        private long pageSize;

        private long total;
    }

}
