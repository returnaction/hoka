package com.unioncoders.smartsupportbackend.controller;

import com.unioncoders.smartsupportbackend.model.ChangedAnswerResponse;
import com.unioncoders.smartsupportbackend.model.SupportRequest;
import com.unioncoders.smartsupportbackend.service.SciboxClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/change_question")
public class ChangeQuestionController {

    private final SciboxClient sciboxClient;

    public ChangeQuestionController(SciboxClient sciboxClient) {
        this.sciboxClient = sciboxClient;
    }

    @PostMapping
    public ResponseEntity<ChangedAnswerResponse> changeQuestionToSimilarText(@RequestBody SupportRequest request) {
        ChangedAnswerResponse response = sciboxClient.changeQuestionToSimilarText(request.getText());
        return ResponseEntity.ok(response);
    }
}