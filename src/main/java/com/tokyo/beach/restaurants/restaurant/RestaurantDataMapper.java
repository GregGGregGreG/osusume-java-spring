package com.tokyo.beach.restaurants.restaurant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tokyo.beach.restaurants.restaurant.RestaurantRowMapper.restaurantRowMapper;

@Repository
public class RestaurantDataMapper {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public RestaurantDataMapper(@SuppressWarnings("SpringJavaAutowiringInspection") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Restaurant> getAll() {
        return jdbcTemplate
                .query("SELECT * FROM restaurant ORDER BY created_at DESC",
                        restaurantRowMapper);
    }

    public Optional<Restaurant> get(long id) {
        List<Restaurant> restaurants = jdbcTemplate
                .query("SELECT * FROM restaurant WHERE id = ?",
                        restaurantRowMapper,
                        id
                );
        if (restaurants.size() == 1) {
            return Optional.of(restaurants.get(0));
        }

        return Optional.empty();
    }

    public Restaurant createRestaurant(NewRestaurant newRestaurant, Long createdByUserId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO restaurant (" +
                        "name, address, nearest_station, place_id, latitude, longitude, " +
                        "notes, cuisine_id, price_range_id, created_by_user_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "RETURNING " +
                        "id, name, address, nearest_station, place_id, latitude, longitude, " +
                        "notes, cuisine_id, created_by_user_id, price_range_id, created_at, updated_at",
                restaurantRowMapper,
                newRestaurant.getName(),
                newRestaurant.getAddress(),
                newRestaurant.getNearestStation(),
                newRestaurant.getPlaceId(),
                newRestaurant.getLatitude(),
                newRestaurant.getLongitude(),
                newRestaurant.getNotes(),
                newRestaurant.getCuisineId(),
                newRestaurant.getPriceRangeId(),
                createdByUserId
        );
    }

    public List<Restaurant> getRestaurantsPostedByUser(long userId) {
        return jdbcTemplate.query("SELECT * FROM restaurant WHERE created_by_user_id = ?",
                restaurantRowMapper,
                userId
        );
    }

    public List<Restaurant> getRestaurantsByIds(List<Long> restaurantIds) {
        MapSqlParameterSource parameters =  new MapSqlParameterSource();
        parameters.addValue("ids", restaurantIds);
        NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        return namedTemplate.query(
                "SELECT * FROM restaurant WHERE id IN (:ids)",
                parameters,
                restaurantRowMapper
        );
    }

    public Restaurant updateRestaurant(Long restaurantId, NewRestaurant restaurant) {
        return jdbcTemplate.queryForObject(
                "UPDATE restaurant SET " +
                        "(name, address, nearest_station, place_id, latitude, longitude, cuisine_id, price_range_id, notes, updated_at) =" +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, now()) " +
                        "WHERE id = ? " +
                        "RETURNING id, name, address, nearest_station, place_id, latitude, longitude, notes, cuisine_id, created_by_user_id, price_range_id, created_at, updated_at",
                restaurantRowMapper,
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getNearestStation(),
                restaurant.getPlaceId(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                restaurant.getCuisineId(),
                restaurant.getPriceRangeId(),
                restaurant.getNotes(),
                restaurantId
        );
    }

    public void delete(Long restaurantId) {
        jdbcTemplate.update("DELETE FROM restaurant WHERE id = ?", restaurantId);
    }
}
