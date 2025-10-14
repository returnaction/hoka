package com.unioncoders.smartsupportbackend.model;

import lombok.Data;
import java.util.Map;

@Data
public class SciboxResponse {
    private String category;
    private String subcategory;
    private Map<String, String> entities;
}
