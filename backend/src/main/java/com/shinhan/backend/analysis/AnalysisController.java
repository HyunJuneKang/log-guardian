package com.shinhan.backend.analysis;

import com.shinhan.backend.analysis.dto.AnalysisRunResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final SampleAnalysisService service;

    public AnalysisController(SampleAnalysisService service) { this.service = service; }

    @PostMapping("/run")
    public AnalysisRunResponse run() { return service.run(); }
}
