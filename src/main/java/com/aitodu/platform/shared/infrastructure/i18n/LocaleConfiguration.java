package com.aitodu.platform.shared.infrastructure.i18n;

import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class LocaleConfiguration {
  @Bean
  LocaleResolver localeResolver() {
    var r = new AcceptHeaderLocaleResolver();
    r.setDefaultLocale(Locale.US);
    r.setSupportedLocales(
        List.of(
            Locale.US,
            Locale.ENGLISH,
            Locale.forLanguageTag("es-419"),
            Locale.forLanguageTag("es")));
    return r;
  }
}
