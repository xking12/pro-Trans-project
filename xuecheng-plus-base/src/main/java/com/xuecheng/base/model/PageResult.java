package com.xuecheng.base.model;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName: PageResult
 * Package: com.xuecheng.base.model
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/4 - 14:51
 * @Version: 1.0
 */
public class PageResult<T> implements Serializable {
    //返回结果
    private List<T> resultItems;

    //返回的记录数
    private Long counts;

    //返回的页码
    private Long page;

    //每页展示数据的大小
    private Long pageSize;

    public PageResult(List<T> resultItems, Long page, Long pageSize) {
        this.resultItems = resultItems;
        this.page = page;
        this.pageSize = pageSize;
    }
}
