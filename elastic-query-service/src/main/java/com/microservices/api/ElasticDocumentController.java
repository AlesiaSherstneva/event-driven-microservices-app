package com.microservices.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class ElasticDocumentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticDocumentController.class);
}