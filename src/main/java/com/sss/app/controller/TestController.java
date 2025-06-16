package com.sss.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sss.app.service.TestService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestService testSer;

    @RequestMapping(value = "/hello", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> hello() throws JsonProcessingException {
        JSONArray outContent;
        outContent = testSer.getGreeting();

        return ResponseEntity.ok(outContent.toString());

    }

}
