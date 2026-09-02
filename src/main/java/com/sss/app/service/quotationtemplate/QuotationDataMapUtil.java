package com.sss.app.service.quotationtemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tiny builder so sample/real quotation-data maps read as literal data, not boilerplate. */
final class QuotationDataMapUtil {

    private QuotationDataMapUtil() {
    }

    static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
