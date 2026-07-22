package com.github.marcelorodrigo.dutytracker.gateway.controllers;

import java.net.URI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceLocation {

    public static URI fromCurrentRequest(String... pathSegments) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .pathSegment(pathSegments)
                .build()
                .encode()
                .toUri();
    }
}
