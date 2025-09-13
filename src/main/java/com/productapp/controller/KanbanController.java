package com.productapp.controller;

import com.productapp.entity.KanbanItem;
import com.productapp.entity.Product;
import com.productapp.entity.RoadmapItem;
import com.productapp.repository.ProductRepository;
import com.productapp.repository.RoadmapItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v3/products/{productId}/kanban")
@CrossOrigin(origins = "*")
public class KanbanController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RoadmapItemRepository roadmapItemRepository;
    
    @GetMapping
    public ResponseEntity<?> getKanbanItems(@PathVariable String productId) {
        try {
            Optional<Product> productOpt = productRepository.findById(Long.parseLong(productId));
            if (!productOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }

            Product product = productOpt.get();

            // Get all published roadmap items - this is our only source of truth
            List<RoadmapItem> roadmapItems = roadmapItemRepository.findPublishedByProductId(Long.parseLong(productId));

            // Convert roadmap items to kanban items format
            List<KanbanItem> kanbanItems = roadmapItems.stream()
                .map(roadmapItem -> {
                    KanbanItem kanbanItem = new KanbanItem();
                    kanbanItem.setId(roadmapItem.getId()); // Use actual roadmap item ID
                    kanbanItem.setTitle(roadmapItem.getEpicName());
                    kanbanItem.setDescription(roadmapItem.getEpicDescription());
                    // Use actual status from roadmap item and map it to Kanban status format
                    String kanbanStatus = mapRoadmapStatusToKanbanStatus(roadmapItem.getStatus());
                    kanbanItem.setStatus(kanbanStatus);
                    kanbanItem.setPosition(roadmapItem.getId().intValue()); // Use ID for ordering
                    kanbanItem.setPriority(roadmapItem.getPriority());
                    kanbanItem.setAssignee(roadmapItem.getAssignedTeam());
                    kanbanItem.setDueDate(roadmapItem.getEndDate() != null ? roadmapItem.getEndDate().atStartOfDay() : null);
                    kanbanItem.setEpicId(roadmapItem.getEpicId());
                    kanbanItem.setLabels("roadmap-item");
                    kanbanItem.setCreatedAt(roadmapItem.getCreatedAt());
                    kanbanItem.setUpdatedAt(roadmapItem.getUpdatedAt());
                    return kanbanItem;
                })
                .collect(Collectors.toList());

            // Group items by status and sort by position within each status
            Map<String, List<KanbanItem>> groupedItems = kanbanItems.stream()
                .collect(Collectors.groupingBy(
                    KanbanItem::getStatus,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> list.stream()
                            .sorted(Comparator.comparing(KanbanItem::getPosition))
                            .collect(Collectors.toList())
                    )
                ));

            // Ensure all columns exist even if empty
            if (!groupedItems.containsKey("COMMITTED")) groupedItems.put("COMMITTED", new java.util.ArrayList<>());
            if (!groupedItems.containsKey("TODO")) groupedItems.put("TODO", new java.util.ArrayList<>());
            if (!groupedItems.containsKey("IN_PROGRESS")) groupedItems.put("IN_PROGRESS", new java.util.ArrayList<>());
            if (!groupedItems.containsKey("DONE")) groupedItems.put("DONE", new java.util.ArrayList<>());


            return ResponseEntity.ok(groupedItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching kanban items: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createKanbanItem(@PathVariable String productId,
                                               @RequestBody KanbanItem kanbanItem) {
        // Kanban items are managed through the roadmap - no direct creation allowed
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body("Kanban items are managed through the roadmap. Please add items via the roadmap planner.");
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateKanbanItem(@PathVariable String productId,
                                               @PathVariable Long itemId,
                                               @RequestBody KanbanItem updatedItem) {
        // Kanban items are managed through the roadmap - no direct editing allowed
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body("Kanban items are managed through the roadmap. Please edit items via the roadmap planner.");
    }
    
    @PutMapping("/{itemId}/move")
    public ResponseEntity<?> moveKanbanItem(@PathVariable String productId,
                                             @PathVariable Long itemId,
                                             @RequestBody Map<String, Object> moveData) {
        try {
            String newStatus = (String) moveData.get("status");

            // Find the roadmap item by ID
            Optional<RoadmapItem> roadmapItemOpt = roadmapItemRepository.findById(itemId);
            if (!roadmapItemOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Roadmap item not found");
            }

            RoadmapItem roadmapItem = roadmapItemOpt.get();

            // Update the item's status
            String roadmapStatus = mapKanbanStatusToRoadmapStatus(newStatus);
            roadmapItem.setStatus(roadmapStatus);
            RoadmapItem saved = roadmapItemRepository.save(roadmapItem);


            // Return the item in Kanban format
            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("title", saved.getEpicName());
            response.put("status", newStatus);
            response.put("position", saved.getId().intValue());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error moving kanban item: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteKanbanItem(@PathVariable String productId,
                                               @PathVariable Long itemId) {
        // Kanban items are managed through the roadmap - no direct deletion allowed
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body("Kanban items are managed through the roadmap. Please delete items via the backlog or roadmap planner.");
    }
    
    private String mapKanbanStatusToRoadmapStatus(String kanbanStatus) {
        // Map Kanban statuses to corresponding Roadmap statuses
        switch (kanbanStatus) {
            case "COMMITTED":
                return "Committed";
            case "TODO":
                return "To-Do";
            case "IN_PROGRESS":
                return "In-Progress";
            case "DONE":
                return "Done";
            default:
                return "Committed"; // Default fallback
        }
    }

    private String mapRoadmapStatusToKanbanStatus(String roadmapStatus) {
        // Map Roadmap statuses to corresponding Kanban statuses
        if (roadmapStatus == null) {
            return "COMMITTED"; // Default for null status
        }

        switch (roadmapStatus) {
            case "Committed":
                return "COMMITTED";
            case "To-Do":
                return "TODO";
            case "In-Progress":
                return "IN_PROGRESS";
            case "Done":
                return "DONE";
            case "Complete":
                return "DONE";
            case "Carried Over":
                return "TODO";
            default:
                return "COMMITTED"; // Default fallback
        }
    }
}