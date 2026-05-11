package com.leantech.admin.common;

import java.io.Serializable;

public class BaseController {

    protected R<Void> toAjax(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }

    protected R<Void> toAjax(boolean result) {
        return result ? R.ok() : R.fail();
    }
}
