package br.pucminas.diaw.sentinela.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Encaminha as rotas do React Router para o index.html gerado pelo Vite.
 * Assim os endpoints GET /login, /register e /recoverpassword exigidos pela
 * atividade continuam respondendo mesmo com o SPA cuidando da navegacao.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String SPA_ENTRY_POINT = "forward:/index.html";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName(SPA_ENTRY_POINT);
        registry.addViewController("/register").setViewName(SPA_ENTRY_POINT);
        registry.addViewController("/recoverpassword").setViewName(SPA_ENTRY_POINT);
        registry.addViewController("/resetpassword").setViewName(SPA_ENTRY_POINT);
        registry.addViewController("/dashboard").setViewName(SPA_ENTRY_POINT);
        registry.addViewController("/profile").setViewName(SPA_ENTRY_POINT);
    }
}
