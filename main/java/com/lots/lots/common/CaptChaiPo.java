package com.lots.lots.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 默认值：
 * <p>
 * <code>
 * 字符数量（默认值：4个字符） int charQuantity = 4;<br>
 * <p>
 * 图片宽度（默认值：100） int width = 100;<br>
 * <p>
 * 图片高度（默认值：36） int height = 36;<br>
 * <p>
 * 干扰线数量（默认值：5） int interferingLineQuantity = 5;<br>
 * <p>
 * 字体大小（默认值：30） int fontSize = 30;
 * </code>
 *
 * @author lots
 * @since 2018年7月23日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptChaiPo {

    /**
     * 字符数量（默认值：4个字符）
     */
    @Builder.Default
    int charQuantity = 4;
    /**
     * 图片宽度（默认值：100）
     */
    @Builder.Default
    int width = 100;
    /**
     * 图片高度（默认值：36）
     */
    @Builder.Default
    int height = 36;
    /**
     * 干扰线数量（默认值：5）
     */
    @Builder.Default
    int interferingLineQuantity = 5;
    /**
     * 字体大小（默认值：30）
     */
    @Builder.Default
    int fontSize = 30;

}
