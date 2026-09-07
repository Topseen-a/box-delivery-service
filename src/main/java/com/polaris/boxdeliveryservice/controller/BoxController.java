package com.polaris.boxdeliveryservice.controller;

import com.polaris.boxdeliveryservice.dto.request.BoxCreateRequest;
import com.polaris.boxdeliveryservice.dto.request.ItemLoadRequest;
import com.polaris.boxdeliveryservice.model.Box;
import com.polaris.boxdeliveryservice.model.Item;
import com.polaris.boxdeliveryservice.service.BoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boxes")
@RequiredArgsConstructor
@Validated
public class BoxController {

    private final BoxService boxService;

    @PostMapping
    public ResponseEntity<Box> createBox(@Valid @RequestBody BoxCreateRequest request) {
        Box box = new Box();
        box.setTxref(request.txref());
        box.setWeightLimit(request.weightLimit());
        box.setBatteryCapacity(request.batteryCapacity());

        Box created = boxService.createBox(box);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/load")
    public ResponseEntity<Box> loadBox(@PathVariable Long id,
                                       @Valid @RequestBody List<ItemLoadRequest> items) {
        Box loaded = boxService.loadBox(id, items);
        return ResponseEntity.ok(loaded);
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<Item>> getItems(@PathVariable Long id) {
        return ResponseEntity.ok(boxService.getItemsForBox(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Box>> getAvailableBoxes() {
        return ResponseEntity.ok(boxService.getAvailableBoxes());
    }

    @GetMapping("{id}/battery")
    public ResponseEntity<Map<String, Object>> getBatteryLevel(@PathVariable Long id) {
        Integer battery = boxService.getBatteryLevel(id);
        return ResponseEntity.ok(Map.of("boxId", id, "batteryCapacity", battery));
    }
}
