package me.vrishab.auction.system.configuration;

import me.vrishab.auction.user.model.BillingDetails;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomMixIns() {
        return builder -> {
            builder.mixIn(BillingDetails.class, BillingDetailsMixin.class);
        };
    }
}

