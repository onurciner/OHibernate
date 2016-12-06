package com.onurciner.ohibernatetools;

import com.onurciner.enums.ConditionType;
import com.onurciner.enums.LikeType;
import com.onurciner.enums.OrderByType;

import java.util.ArrayList;

/**
 * Created by Onur.Ciner on 30.11.2016.
 */

public class Conditions {

    private Boolean distinct = false;

    private Integer orConnector = 0;

    private Integer andConnector = 0;

    private ArrayList<LikeType> like = new ArrayList<>();

    private ArrayList<ConditionType> condition = new ArrayList<>();

    private OHash<String, Object> whereData = new OHash<>();

    private Integer limit = null;

    private OHash<String, OrderByType> orderbyData = new OHash<>();

    //----------->>GETTER and SETTER<<----------------

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public Integer getOrConnector() {
        return orConnector;
    }

    public void setOrConnector(Integer orConnector) {
        this.orConnector = orConnector;
    }

    public Integer getAndConnector() {
        return andConnector;
    }

    public void setAndConnector(Integer andConnector) {
        this.andConnector = andConnector;
    }

    public ArrayList<LikeType> getLike() {
        return like;
    }

    public void setLike(ArrayList<LikeType> like) {
        this.like = like;
    }

    public OHash<String, Object> getWhereData() {
        return whereData;
    }

    public void setWhereData(OHash<String, Object> whereData) {
        this.whereData = whereData;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public OHash<String, OrderByType> getOrderbyData() {
        return orderbyData;
    }

    public void setOrderbyData(OHash<String, OrderByType> orderbyData) {
        this.orderbyData = orderbyData;
    }

    public ArrayList<ConditionType> getCondition() {
        return condition;
    }

    public void setCondition(ArrayList<ConditionType> condition) {
        this.condition = condition;
    }
}
