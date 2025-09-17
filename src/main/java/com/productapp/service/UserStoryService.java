package com.productapp.service;

import com.productapp.entity.UserStory;
import com.productapp.entity.Product;
import com.productapp.repository.UserStoryRepository;
import com.productapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserStoryService {

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<UserStory> getStoriesByEpicId(String epicId) {
        return userStoryRepository.findByEpicIdOrderByDisplayOrderAsc(epicId);
    }

    public List<UserStory> getStoriesByProductAndEpic(Long productId, String epicId) {
        return userStoryRepository.findByProductIdAndEpicId(productId, epicId);
    }

    public List<UserStory> getAllStoriesByProduct(Product product) {
        return userStoryRepository.findByProductOrderByEpicIdAscDisplayOrderAsc(product);
    }

    public Optional<UserStory> getStoryById(Long id) {
        return userStoryRepository.findById(id);
    }

    public Optional<UserStory> getStoryByIdAndProduct(Long id, Product product) {
        return userStoryRepository.findByIdAndProduct(id, product);
    }

    public Long getStoryCountByEpic(String epicId) {
        return userStoryRepository.countByEpicId(epicId);
    }

    public UserStory createUserStory(Long productId, String epicId, String title,
                                     String description, String acceptanceCriteria,
                                     String priority, Integer storyPoints, Long createdBy) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        UserStory userStory = new UserStory();
        userStory.setProduct(product);
        userStory.setEpicId(epicId);
        userStory.setTitle(title);
        userStory.setDescription(description);
        userStory.setAcceptanceCriteria(acceptanceCriteria);
        userStory.setPriority(priority != null ? priority : "Medium");
        userStory.setStoryPoints(storyPoints);
        userStory.setStatus("Draft");
        userStory.setCreatedBy(createdBy);

        // Set display order to be last in the epic
        Long currentCount = userStoryRepository.countByEpicId(epicId);
        userStory.setDisplayOrder(currentCount.intValue());

        return userStoryRepository.save(userStory);
    }

    public UserStory updateUserStory(Long id, Long productId, String title,
                                     String description, String acceptanceCriteria,
                                     String priority, Integer storyPoints, String status) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        UserStory userStory = userStoryRepository.findByIdAndProduct(id, product)
            .orElseThrow(() -> new IllegalArgumentException("User story not found with id: " + id));

        if (title != null && !title.trim().isEmpty()) {
            userStory.setTitle(title);
        }
        if (description != null) {
            userStory.setDescription(description);
        }
        if (acceptanceCriteria != null) {
            userStory.setAcceptanceCriteria(acceptanceCriteria);
        }
        if (priority != null) {
            userStory.setPriority(priority);
        }
        if (storyPoints != null) {
            userStory.setStoryPoints(storyPoints);
        }
        if (status != null) {
            userStory.setStatus(status);
        }

        return userStoryRepository.save(userStory);
    }

    public void deleteUserStory(Long id, Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        UserStory userStory = userStoryRepository.findByIdAndProduct(id, product)
            .orElseThrow(() -> new IllegalArgumentException("User story not found with id: " + id));

        userStoryRepository.delete(userStory);
    }

    @Transactional
    public void deleteStoriesByEpic(String epicId) {
        userStoryRepository.deleteByEpicId(epicId);
    }

    public void updateStoryOrder(Long storyId, Integer newOrder) {
        UserStory story = userStoryRepository.findById(storyId)
            .orElseThrow(() -> new IllegalArgumentException("User story not found with id: " + storyId));

        story.setDisplayOrder(newOrder);
        userStoryRepository.save(story);
    }
}