package com.pig.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Compensable {
    public Propagation propagation() default Propagation.REQUIRED;

    public String confirmMethod() default "";

    public String cancelMethod() default "";

    public boolean asyncConfirm() default false;

    public boolean asyncCancel() default false;

    public Class<? extends TransactionContextEditor> transactionContextEditor() default NullableTransactionContextEditor.class;
}
