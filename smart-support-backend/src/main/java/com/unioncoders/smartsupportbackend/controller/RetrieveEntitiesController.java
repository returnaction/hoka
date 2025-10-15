package com.unioncoders.smartsupportbackend.controller;

import com.unioncoders.smartsupportbackend.model.EntitiesResponse;
import com.unioncoders.smartsupportbackend.model.SupportRequest;
import com.unioncoders.smartsupportbackend.service.SciboxClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/entities")
public class RetrieveEntitiesController {

    private final SciboxClient sciboxClient;

    public RetrieveEntitiesController(SciboxClient sciboxClient) {
        this.sciboxClient = sciboxClient;
    }

    @PostMapping
    public ResponseEntity<EntitiesResponse> retrieveEntities(@RequestBody SupportRequest request) {
        EntitiesResponse response = sciboxClient.retrieveEntities(request.getText());
        return ResponseEntity.ok(response);
    }
}