package org.redcastlemedia.multitallented.civs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CivsSingleton {
    SingletonLoadPriority priority() default SingletonLoadPriority.MEDIUM;

    enum SingletonLoadPriority {
        CRITICAL(100),
        HIGHEST(99),
        HIGHER(80),
        HIGH(75),
        MEDIUM(50),
        LOW(25),
        LOWEST(1);

        private final int value;
        public int getValue() {
            return this.value;
        }
        private SingletonLoadPriority(int value) {
            this.value = value;
        }
    }
}
