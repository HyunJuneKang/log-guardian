package com.shinhan.backend.alert;

import com.shinhan.backend.alert.dto.AlertDetailResponse;
import com.shinhan.backend.alert.dto.AlertsListResponse;
import com.shinhan.backend.analysis.SampleAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final SampleAnalysisService service;

    public AlertController(SampleAnalysisService service) { this.service = service; }

    @GetMapping
    public AlertsListResponse list() { return service.list(); }

    @GetMapping("/{id}")
    public AlertDetailResponse detail(@PathVariable long id) { return service.detail(id); }
}
