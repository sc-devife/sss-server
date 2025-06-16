package com.sss.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sss.app.entity.Login;
import com.sss.app.repository.LoginRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TestService {
    @Autowired
    private LoginRepository loginRepository;

    public JSONArray getGreeting() throws JsonProcessingException {

        Optional<Login> rows = loginRepository.findByUsername("Sai");
        // Unwrap Optional
        if (rows.isPresent()) {
            Login login = rows.get();
            return new JSONArray(new ObjectMapper().writeValueAsString(List.of(login)));
        } else {
            return new JSONArray(); // return empty JSON array if no data found
        }
        //return new JSONArray(new ObjectMapper().writeValueAsString(rows));
    }
}
