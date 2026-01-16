package com.sixdee.text2rule.view;

import com.sixdee.text2rule.model.RuleTree;

public interface TreeRenderer {
    /**
     * Renders the tree structure.
     * @param tree the tree to render
     */
    void render(RuleTree<?> tree);
}
