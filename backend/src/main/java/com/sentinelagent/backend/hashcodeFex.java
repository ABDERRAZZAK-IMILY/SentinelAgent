package com.sentinelagent.backend;


import com.sentinelagent.backend.agent.internal.port.ApiKeyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class hashcodeFex implements CommandLineRunner {

    private final ApiKeyService apiKeyService;

    public hashcodeFex(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public void run(String... args) {
        System.out.println("HASH=" + apiKeyService.hashApiKey("snt_uohSupuMllhrEf0PuK49h59g8FHVvnyp6sL_vKde7iA"));
    }


}
