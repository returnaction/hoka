package com.unioncoders.smartsupportbackend.controller;

import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import com.unioncoders.smartsupportbackend.model.SupportRequest;
import com.unioncoders.smartsupportbackend.service.SciboxClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyze")
public class SupportController {

    private final SciboxClient sciboxClient;

    public SupportController(SciboxClient sciboxClient) {
        this.sciboxClient = sciboxClient;
    }

    @PostMapping
    public ResponseEntity<SciboxResponse> classify(@RequestBody SupportRequest request) {
        SciboxResponse response = sciboxClient.classifyText(request.getText());
        return ResponseEntity.ok(response);
    }
}
