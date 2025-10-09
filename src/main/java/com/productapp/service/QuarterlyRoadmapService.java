package com.productapp.service;

import com.productapp.dto.QuarterlyRoadmapRequest;
import com.productapp.entity.BacklogEpic;
import com.productapp.entity.QuarterlyRoadmap;
import com.productapp.entity.RoadmapItem;
import com.productapp.repository.BacklogEpicRepository;
import com.productapp.repository.QuarterlyRoadmapRepository;
import com.productapp.repository.RoadmapItemRepository;
import com.productapp.repository.EpicEffortRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuarterlyRoadmapService {

    private static final Logger logger = LoggerFactory.getLogger(QuarterlyRoadmapService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private QuarterlyRoadmapRepository quarterlyRoadmapRepository;

    @Autowired
    private RoadmapItemRepository roadmapItemRepository;

    @Autowired
    private BacklogEpicRepository backlogEpicRepository;

    @Autowired
    private EpicEffortRepository epicEffortRepository;

    @Transactional
    public QuarterlyRoadmap createOrUpdateRoadmap(Long productId, QuarterlyRoadmapRequest request) {

        // Find or create roadmap
        Optional<QuarterlyRoadmap> existingRoadmapOpt = quarterlyRoadmapRepository
                .findByProductIdAndYearAndQuarter(productId, request.getYear(), request.getQuarter());

        QuarterlyRoadmap roadmap;
        Set<String> oldEpicIds = new HashSet<>();

        if (existingRoadmapOpt.isPresent()) {
            roadmap = existingRoadmapOpt.get();

            // Get old epic IDs before deletion
            List<RoadmapItem> oldItems = roadmapItemRepository.findByRoadmapId(roadmap.getId());
            oldEpicIds = oldItems.stream()
                    .map(RoadmapItem::getEpicId)
                    .collect(Collectors.toSet());

            // Delete existing items
            roadmapItemRepository.deleteByRoadmapId(roadmap.getId());
        } else {
            roadmap = new QuarterlyRoadmap();
            roadmap.setProductId(productId);
            roadmap.setYear(request.getYear());
            roadmap.setQuarter(request.getQuarter());
        }
        
        // Save roadmap first to ensure we have an ID
        roadmap = quarterlyRoadmapRepository.save(roadmap);

        // Collect new epic IDs
        Set<String> newEpicIds = new HashSet<>();

        // Convert and save new items
        if (request.getRoadmapItems() != null && !request.getRoadmapItems().isEmpty()) {
            for (QuarterlyRoadmapRequest.RoadmapItem requestItem : request.getRoadmapItems()) {
                newEpicIds.add(requestItem.getEpicId());

                RoadmapItem item = new RoadmapItem();
                item.setRoadmap(roadmap);
                item.setEpicId(requestItem.getEpicId());
                item.setEpicName(requestItem.getEpicName());
                item.setEpicDescription(requestItem.getEpicDescription());
                item.setPriority(requestItem.getPriority());
                item.setStatus(requestItem.getStatus());
                item.setEstimatedEffort(requestItem.getEstimatedEffort());
                item.setAssignedTeam(requestItem.getAssignedTeam());
                item.setReach(requestItem.getReach());
                item.setImpact(requestItem.getImpact());
                item.setConfidence(requestItem.getConfidence());
                item.setRiceScore(requestItem.getRiceScore());
                item.setEffortRating(requestItem.getEffortRating());

                // Set initiative and theme information if provided
                item.setInitiativeId(requestItem.getInitiativeId());
                item.setInitiativeName(requestItem.getInitiativeName());
                item.setThemeId(requestItem.getThemeId());
                item.setThemeName(requestItem.getThemeName());
                item.setThemeColor(requestItem.getThemeColor());
                item.setTrack(requestItem.getTrack());

                // Parse dates if provided
                if (requestItem.getStartDate() != null && !requestItem.getStartDate().isEmpty()) {
                    item.setStartDate(LocalDate.parse(requestItem.getStartDate(), DATE_FORMATTER));
                }
                if (requestItem.getEndDate() != null && !requestItem.getEndDate().isEmpty()) {
                    item.setEndDate(LocalDate.parse(requestItem.getEndDate(), DATE_FORMATTER));
                }

                // Save each item individually to ensure proper persistence
                RoadmapItem savedItem = roadmapItemRepository.save(item);

                // Sync status to backlog_epics if the epic exists
                syncStatusToBacklogEpic(productId, requestItem.getEpicId(), requestItem.getStatus());
            }
        }

        // Handle removed epics - reset status to 'backlog' and cleanup capacity planning
        Set<String> removedEpicIds = new HashSet<>(oldEpicIds);
        removedEpicIds.removeAll(newEpicIds);

        if (!removedEpicIds.isEmpty()) {
            logger.info("Resetting status to 'backlog' for {} removed epics", removedEpicIds.size());
            for (String epicId : removedEpicIds) {
                // Reset status to 'backlog'
                resetEpicToBacklog(productId, epicId);

                // Delete epic_efforts for this epic from capacity planning
                try {
                    epicEffortRepository.deleteByEpicIdAndProductId(epicId, productId);
                    logger.info("Removed capacity planning efforts for epic {}", epicId);
                } catch (Exception e) {
                    logger.warn("Failed to delete epic efforts for epic {}: {}", epicId, e.getMessage());
                }
            }
        }

        // Force flush to ensure data is written to database within this transaction
        quarterlyRoadmapRepository.flush();
        roadmapItemRepository.flush();

        return roadmap;
    }

    /**
     * Sync status from roadmap item to backlog epic
     */
    private void syncStatusToBacklogEpic(Long productId, String epicId, String status) {
        try {
            Optional<BacklogEpic> backlogEpicOpt = backlogEpicRepository.findByProductIdAndEpicId(productId, epicId);
            if (backlogEpicOpt.isPresent() && status != null && !status.isEmpty()) {
                BacklogEpic backlogEpic = backlogEpicOpt.get();
                backlogEpic.setStatus(status);
                backlogEpicRepository.save(backlogEpic);
                logger.debug("Synced status '{}' to backlog epic: {}", status, epicId);
            }
        } catch (Exception e) {
            logger.warn("Failed to sync status to backlog epic {}: {}", epicId, e.getMessage());
        }
    }

    /**
     * Reset epic status to 'backlog' when removed from roadmap
     */
    private void resetEpicToBacklog(Long productId, String epicId) {
        try {
            Optional<BacklogEpic> backlogEpicOpt = backlogEpicRepository.findByProductIdAndEpicId(productId, epicId);
            if (backlogEpicOpt.isPresent()) {
                BacklogEpic backlogEpic = backlogEpicOpt.get();
                backlogEpic.setStatus("backlog");
                backlogEpicRepository.save(backlogEpic);
                logger.info("Reset epic {} status to 'backlog'", epicId);
            }
        } catch (Exception e) {
            logger.warn("Failed to reset status to 'backlog' for epic {}: {}", epicId, e.getMessage());
        }
    }
}