package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ClassName: PageParams
 * Package: com.xuecheng.base.model
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/1 - 17:56
 * @Version: 1.0
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageParams {
    private Long pageNo=1L;

    private Long pageSize=10L;
}
