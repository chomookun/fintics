package org.chomookun.fintics.web.api.v1.broker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.BrokerSearch;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.web.api.v1.broker.dto.BrokerRequest;
import org.chomookun.fintics.web.api.v1.broker.dto.BrokerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "broker")
@RestController
@RequestMapping("/api/v1/brokers")
@PreAuthorize("hasAuthority('broker')")
@RequiredArgsConstructor
public class BrokerRestController {

    private final BrokerService brokerService;

    @Operation(summary = "Returns list of brokers")
    @GetMapping
    public ResponseEntity<List<BrokerResponse>> getBrokers(
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault Pageable pageable
    ) {
        BrokerSearch brokerSearch = BrokerSearch.builder()
                .name(name)
                .build();
        Page<Broker> brokerPage = brokerService.getBrokers(brokerSearch, pageable);
        List<BrokerResponse> brokerResponses = brokerPage.getContent().stream()
                .map(BrokerResponse::from)
                .toList();
        long total = brokerPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("broker", pageable, total))
                .body(brokerResponses);
    }

    @Operation(summary = "Returns details of the specified broker")
    @GetMapping("{brokerId}")
    public ResponseEntity<BrokerResponse> getBroker(@PathVariable("brokerId") String brokerId) {
        BrokerResponse brokerResponse = brokerService.getBroker(brokerId)
                .map(BrokerResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(brokerResponse);
    }

    @Operation(summary = "Creates new broker")
    @PostMapping
    @PreAuthorize("hasAuthority('broker:edit')")
    public ResponseEntity<BrokerResponse> createBroker(@RequestBody BrokerRequest brokerRequest) {
        Broker broker = Broker.builder()
                .name(brokerRequest.getName())
                .sort(brokerRequest.getSort())
                .brokerClientId(brokerRequest.getBrokerClientId())
                .brokerClientProperties(brokerRequest.getBrokerClientProperties())
                .build();
        Broker savedBroker = brokerService.saveBroker(broker);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BrokerResponse.from(savedBroker));
    }

    @Operation(summary = "Modifies the specified broker")
    @PutMapping("{brokerId}")
    @PreAuthorize("hasAuthority('broker:edit')")
    public ResponseEntity<BrokerResponse> modifyBroker(@PathVariable("brokerId") String brokerId, @RequestBody BrokerRequest brokerRequest) {
        Broker broker = brokerService.getBroker(brokerId).orElseThrow();
        broker.setName(brokerRequest.getName());
        broker.setSort(brokerRequest.getSort());
        broker.setBrokerClientId(brokerRequest.getBrokerClientId());
        broker.setBrokerClientProperties(brokerRequest.getBrokerClientProperties());
        Broker savedBroker = brokerService.saveBroker(broker);
        return ResponseEntity.ok(BrokerResponse.from(savedBroker));
    }

    @Operation(summary = "Deletes the specified broker")
    @DeleteMapping("{brokerId}")
    @PreAuthorize("hasAuthority('broker:edit')")
    public ResponseEntity<Void> deleteBroker(@PathVariable("brokerId") String brokerId) {
        brokerService.deleteBroker(brokerId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Changes broker sort")
    @PatchMapping("{brokerId}/sort")
    @PreAuthorize("hasAuthority('broker:edit')")
    public ResponseEntity<Void> changeBrokerSort(@PathVariable("brokerId") String brokerId, @RequestParam("sort") Integer sort) {
        brokerService.changeBrokerSort(brokerId, sort);
        return ResponseEntity.ok().build();
    }

}
