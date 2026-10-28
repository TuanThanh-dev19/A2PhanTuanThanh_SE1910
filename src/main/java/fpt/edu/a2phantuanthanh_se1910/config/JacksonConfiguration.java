// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.config;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer strictIntegerValues() {
        // Reject role 1.5 or "1" instead of silently converting them to role 1.
        return builder -> builder.postConfigurer(mapper -> mapper.coercionConfigFor(LogicalType.Integer)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail));
    }
}
