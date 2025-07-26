package com.microservices.api;

import com.microservices.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.common.model.ElasticQueryWebClientResponseModel;
import com.microservices.service.ElasticQueryWebClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QueryController {
    private final ElasticQueryWebClient queryWebClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("elasticQueryWebClientRequestModel",
                ElasticQueryWebClientResponseModel.builder().build());
        return "home";
    }

    @PostMapping("/query-by-text")
    public String queryByText(@Valid ElasticQueryWebClientRequestModel requestModel, Model model) {
        LOGGER.info("Querying with text {}", requestModel.getText());

        List<ElasticQueryWebClientResponseModel> responseModels = queryWebClient.getDataByText(requestModel);

        model.addAttribute("elasticQueryWebClientResponseModels", responseModels);
        model.addAttribute("searchText", requestModel.getText());
        model.addAttribute("elasticQueryWebClientRequestModel",
                ElasticQueryWebClientResponseModel.builder().build());

        return "home";
    }
}