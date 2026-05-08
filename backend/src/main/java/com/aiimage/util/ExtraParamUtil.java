package com.aiimage.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Extra parameters utility for request tracing
 */
public class ExtraParamUtil {

    private static final ThreadLocal<ExtraParam> EXTRA_PARAM = ThreadLocal.withInitial(ExtraParam::new);

    public static ExtraParam getExtraParam() {
        return EXTRA_PARAM.get();
    }

    public static void destroy() {
        EXTRA_PARAM.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraParam {
        private String traceId;
        private String userId;
    }
}
