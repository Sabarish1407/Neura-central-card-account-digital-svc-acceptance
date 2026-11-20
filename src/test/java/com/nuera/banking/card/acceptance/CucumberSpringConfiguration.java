package com.nuera.banking.card.acceptance;

import com.nuera.banking.card.NeuraCentralCardAccountDigitalSvcApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = NeuraCentralCardAccountDigitalSvcApplication.class)
public class CucumberSpringConfiguration {
}
