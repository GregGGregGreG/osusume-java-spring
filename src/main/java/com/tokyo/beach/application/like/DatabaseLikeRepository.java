package com.tokyo.beach.application.like;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class DatabaseLikeRepository implements LikeRepository {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseLikeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(long restaurantId, long userId) {
        SimpleJdbcInsert insertLike = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("likes")
                .usingColumns("restaurant_id", "user_id");

        Map<String, Object> params = new HashMap<>();
        params.put("restaurant_id", restaurantId);
        params.put("user_id", userId);

        insertLike.execute(params);
    }
}