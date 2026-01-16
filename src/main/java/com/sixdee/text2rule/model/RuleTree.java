package com.sixdee.text2rule.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multi-RuleNode Tree implementation that supports maintaining unique addresses per level.
 * @param <T> data type
 */
public class RuleTree<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RuleTree.class);

    private RuleNode<T> root;
    private final Map<Integer, String> levelAddressMap;

    public RuleTree() {
        this.levelAddressMap = new HashMap<>();
    }

    public String getAddressForLevel(int level) {
        return levelAddressMap.computeIfAbsent(level, k -> {
            String id = UUID.randomUUID().toString();
            logger.info("Generated new Unique Address ID for Level {}: {}", level, id);
            return id;
        });
    }

    public void setRoot(RuleNode<T> root) {
        this.root = root;
    }

    public RuleNode<T> getRoot() {
        return root;
    }
}
