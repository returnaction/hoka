package com.unioncoders.smartsupportbackend.controller;

import com.unioncoders.smartsupportbackend.model.ChangedAnswerResponse;
import com.unioncoders.smartsupportbackend.model.SupportRequest;
import com.unioncoders.smartsupportbackend.service.SciboxClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/normalise")
public class NormaliseController {

    private final SciboxClient sciboxClient;

    public NormaliseController(SciboxClient sciboxClient) {
        this.sciboxClient = sciboxClient;
    }

    @PostMapping
    public ResponseEntity<ChangedAnswerResponse> normalise(@RequestBody SupportRequest request) {
        ChangedAnswerResponse response = sciboxClient.normaliseText(request.getText());
        return ResponseEntity.ok(response);
    }
}
