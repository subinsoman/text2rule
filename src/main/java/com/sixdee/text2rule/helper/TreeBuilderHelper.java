package com.sixdee.text2rule.helper;

import com.sixdee.text2rule.dto.DecompositionResult;

import com.sixdee.text2rule.model.RuleTree;
import com.sixdee.text2rule.model.RuleNode;
import com.sixdee.text2rule.model.NodeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reconstructed TreeBuilderHelper.
 * TODO: Restore full LLM-based tree generation logic.
 */
public class TreeBuilderHelper {
    private static final Logger logger = LoggerFactory.getLogger(TreeBuilderHelper.class);

    public RuleTree<NodeData> buildTree(NodeData rootData) {
        // Fallback or initial implementation
        return buildTreeFromDecomposition(rootData, null);
    }

    public RuleTree<NodeData> buildTreeFromDecomposition(NodeData rootData, DecompositionResult decomposition) {
        // logger.info("Building tree for input: {}", rootData.getInput());

        RuleTree<NodeData> tree = new RuleTree<>();
        RuleNode<NodeData> root = new RuleNode<>(rootData);
        tree.setRoot(root);

        if (decomposition != null) {
            // Add Normal Statements as a child node
            if (decomposition.getNormalStatements() != null && !decomposition.getNormalStatements().trim().isEmpty()) {
                NodeData normalStatementsNode = new NodeData("NormalStatements", "", "", rootData.getModelName(), "",
                        decomposition.getNormalStatements());
                root.addChild(new RuleNode<>(normalStatementsNode));
            }

            // Add Schedule if present
            if (decomposition.getSchedule() != null && !decomposition.getSchedule().trim().isEmpty()) {
                NodeData scheduleNode = new NodeData("Schedule", "", "", rootData.getModelName(), "",
                        decomposition.getSchedule());
                root.addChild(new RuleNode<>(scheduleNode));
            }
        } else {
            logger.warn("Decomposition is null, creating simple root node only.");
        }

        return tree;
    }
}
