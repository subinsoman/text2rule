package com.sixdee.text2rule.model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Represents a node in the multi-node tree.
 * @param <T> existing data type
 */
public class RuleNode<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private T data;
    private List<RuleNode<T>> children;

    public RuleNode(T data) {
        this.data = data;
        this.children = new ArrayList<>();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<RuleNode<T>> getChildren() {
        return children;
    }

    public void addChild(RuleNode<T> child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return "RuleNode{data=" + data + "}";
    }
}
