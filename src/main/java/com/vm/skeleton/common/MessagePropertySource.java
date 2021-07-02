package com.vm.skeleton.common;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties
@PropertySource("classpath:messages.yml")
@Setter
@Getter
@Validated
public class MessagePropertySource {

    @NotEmpty
    private String ERR_01;

    @NotEmpty
    private String ERR_02;

    @NotEmpty
    private String ERR_03;

    @NotEmpty
    private String ERR_04;

    @NotEmpty
    private String ERR_05;

    @NotEmpty
    private String ERR_06;
}
