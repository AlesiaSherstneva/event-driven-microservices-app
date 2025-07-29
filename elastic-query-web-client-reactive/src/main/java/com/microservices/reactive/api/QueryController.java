package com.microservices.reactive.api;

import com.microservices.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.common.model.ElasticQueryWebClientResponseModel;
import com.microservices.reactive.service.ElasticQueryWebClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring6.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class QueryController {
    private final ElasticQueryWebClient queryWebClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("elasticQueryWebClientRequestModel",
                ElasticQueryWebClientRequestModel.builder().build());
        return "home";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @PostMapping("/query-by-text")
    public String queryByText(@Valid ElasticQueryWebClientRequestModel requestModel, Model model) {
        LOGGER.info("Querying with text {}", requestModel.getText());

        Flux<ElasticQueryWebClientResponseModel> responseModel = queryWebClient.getDataByText(requestModel);
        responseModel = responseModel.log();

        IReactiveDataDriverContextVariable reactiveData = new ReactiveDataDriverContextVariable(responseModel, 1);
        model.addAttribute("elasticQueryClientResponseModels", reactiveData);
        model.addAttribute("searchText", requestModel.getText());
        model.addAttribute("elasticQueryWebClientResponseModel",
                ElasticQueryWebClientResponseModel.builder().build());

        LOGGER.info("Returning from reactive client controller for text: {}", requestModel.getText());

        return "home";
    }
}