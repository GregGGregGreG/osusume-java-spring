package com.tokyo.beach.pricerange;

import com.tokyo.beach.cuisine.CuisineFixture;
import com.tokyo.beach.restaurant.RestaurantFixture;
import com.tokyo.beach.restaurants.pricerange.PriceRange;
import com.tokyo.beach.restaurants.pricerange.PriceRangeDataMapper;
import com.tokyo.beach.restaurants.restaurant.Restaurant;
import com.tokyo.beach.user.UserFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.tokyo.beach.TestDatabaseUtils.buildDataSource;
import static com.tokyo.beach.TestDatabaseUtils.truncateAllTables;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class PriceRangeDataMapperTest {
    private PriceRangeDataMapper priceRangeDataMapper;
    private JdbcTemplate jdbcTemplate = new JdbcTemplate(buildDataSource());

    @Before
    public void setUp() throws Exception {
        priceRangeDataMapper = new PriceRangeDataMapper(jdbcTemplate);
    }

    @After
    public void tearDown() {
        truncateAllTables(jdbcTemplate);
    }

    @Test
    public void test_getAll_returnsPriceRanges() throws Exception {
        PriceRange persistedPriceRange1 = new PriceRangeFixture()
                .withRange("Price Range #1")
                .persist(jdbcTemplate);

        PriceRange persistedPriceRange2 = new PriceRangeFixture()
                .withRange("Price Range #2")
                .persist(jdbcTemplate);


        List<PriceRange> actualPriceRanges = priceRangeDataMapper.getAll();


        List<PriceRange> expectedPriceRanges = asList(
                persistedPriceRange1,
                persistedPriceRange2
        );

        assertEquals(expectedPriceRanges, actualPriceRanges);
    }

    @Test
    public void test_get_returnsPriceRange() throws Exception {
        PriceRange persistedPriceRange = new PriceRangeFixture()
                .withRange("Price Range #1")
                .persist(jdbcTemplate);

        PriceRange actualPriceRange = priceRangeDataMapper.getPriceRange(persistedPriceRange.getId()).get();

        assertEquals(persistedPriceRange, actualPriceRange);
    }

    @Test
    public void test_findForRestaurant_findsPriceRangeForRestaurant() throws Exception {
        PriceRange persistedPriceRange = new PriceRangeFixture().withRange("Price Range #1").persist(jdbcTemplate);
        Restaurant persistedRestaurant = new RestaurantFixture()
                .withPriceRange(persistedPriceRange)
                .withCuisine(new CuisineFixture().persist(jdbcTemplate))
                .withUser(new UserFixture().withEmail("email1").persist(jdbcTemplate))
                .persist(jdbcTemplate);

        PriceRange actualPriceRange = priceRangeDataMapper.findForRestaurant(
                persistedRestaurant.getId()
        );

        assertEquals(persistedPriceRange, actualPriceRange);
    }

}
