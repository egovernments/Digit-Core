package org.egov.inbox.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.inbox.repository.builder.V2.InboxQueryBuilder;
import org.egov.inbox.service.V2.InboxServiceV2;
import org.egov.inbox.util.ResponseInfoFactory;
import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.InboxResponse;
import org.egov.inbox.web.model.V2.SearchRequest;
import org.egov.inbox.web.model.V2.SearchResponse;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v2")
@Slf4j
@Import({TracerConfiguration.class, MultiStateInstanceUtil.class})
public class InboxV2Controller {

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private InboxQueryBuilder inboxQueryBuilder;

    @Autowired
    private InboxServiceV2 inboxService;


    @PostMapping(value = "/_search")
    public ResponseEntity<InboxResponse> searchNewInbox(@Valid @RequestBody  InboxRequest inboxRequest) {
        InboxResponse inboxResponse = inboxService.getInboxResponse(inboxRequest);
        return new ResponseEntity<>(inboxResponse, HttpStatus.OK);
    }


    @PostMapping(value = "/_getFields")
    public ResponseEntity<SearchResponse> searchFields(@Valid @RequestBody SearchRequest searchRequest) {
        SearchResponse searchResponse = inboxService.getSpecificFieldsFromESIndex(searchRequest);
        return new ResponseEntity<>(searchResponse, HttpStatus.OK);
    }
}

