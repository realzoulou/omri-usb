package eu.hradio.httprequestwrapper.annotations.json;

import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonProperty {
    String name();
    
    boolean isJsonArray() default false;
    
    boolean isJsonObject() default false;
}
