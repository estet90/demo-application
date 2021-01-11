package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;
import ru.craftysoft.util.module.common.logging.LightMdc;

public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    public static StaticMDCBinder getSingleton() {
        return SINGLETON;
    }

    public MDCAdapter getMDCA() {
        return LightMdc.INSTANCE;
    }
}
