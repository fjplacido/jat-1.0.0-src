package br.pucrio.inf.les.jat.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ChangePerformative {
	int performative() default 0;
}
