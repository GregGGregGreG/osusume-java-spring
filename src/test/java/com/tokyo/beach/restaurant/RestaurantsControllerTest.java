package com.tokyo.beach.restaurant;

import com.tokyo.beach.restaurants.cuisine.Cuisine;
import com.tokyo.beach.restaurants.photos.NewPhotoUrl;
import com.tokyo.beach.restaurants.photos.PhotoDataMapper;
import com.tokyo.beach.restaurants.photos.PhotoUrl;
import com.tokyo.beach.restaurants.pricerange.PriceRange;
import com.tokyo.beach.restaurants.restaurant.*;
import com.tokyo.beach.restaurants.s3.S3StorageRepository;
import com.tokyo.beach.restaurants.user.User;
import com.tokyo.beach.restutils.RestControllerExceptionHandler;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.tokyo.beach.restutils.ControllerTestingUtils.createControllerAdvice;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class RestaurantsControllerTest {
    private RestaurantRepository restaurantRepository;
    private MockMvc mockMvc;
    private PhotoDataMapper photoDataMapper;
    private S3StorageRepository s3StorageRepository;

    @Before
    public void setUp() {
        restaurantRepository = mock(RestaurantRepository.class);
        photoDataMapper = mock(PhotoDataMapper.class);
        s3StorageRepository = mock(S3StorageRepository.class);

        RestaurantsController restaurantsController = new RestaurantsController(
                restaurantRepository,
                photoDataMapper,
                s3StorageRepository
        );

        mockMvc = standaloneSetup(restaurantsController)
                .setControllerAdvice(createControllerAdvice(new RestControllerExceptionHandler()))
                .build();
    }

    @Test
    public void test_getAll_returnsAListOfRestaurants() throws Exception {
        Restaurant restaurant = new RestaurantFixture()
                .withId(1)
                .withName("Afuri")
                .withAddress("Roppongi")
                .withNearestStation("Roppongi Station")
                .withNotes("とても美味しい")
                .withCreatedAt(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
                .withUpdatedAt(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
                .build();
        List<SerializedRestaurant> restaurants = singletonList(
                new SerializedRestaurant(
                        restaurant,
                        singletonList(new PhotoUrl(999, "http://www.cats.com/my-cat.jpg", 1)),
                        new Cuisine(20L, "Swedish"),
                        new PriceRange(1L, "100yen"),
                        new User(1L, "taro@email.com", "taro"),
                        emptyList(),
                        true,
                        2
                )
        );
        when(restaurantRepository.getAll(1L)).thenReturn(restaurants);
        mockMvc.perform(get("/restaurants").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Afuri")))
                .andExpect(jsonPath("$[0].address", equalTo("Roppongi")))
                .andExpect(jsonPath("$[0].nearest_station", equalTo("Roppongi Station")))
                .andExpect(jsonPath("$[0].cuisine.id", equalTo(20)))
                .andExpect(jsonPath("$[0].cuisine.name", equalTo("Swedish")))
                .andExpect(jsonPath("$[0].notes", equalTo("とても美味しい")))
                .andExpect(jsonPath("$[0].photo_urls[0].id", equalTo(999)))
                .andExpect(jsonPath("$[0].photo_urls[0].url", equalTo("http://www.cats.com/my-cat.jpg")))
                .andExpect(jsonPath("$[0].price_range.id", equalTo(1)))
                .andExpect(jsonPath("$[0].price_range.range", equalTo("100yen")))
                .andExpect(jsonPath("$[0].num_likes", equalTo(2)))
                .andExpect(jsonPath("$[0].liked", equalTo(true)))
                .andExpect(jsonPath("$[0].created_at", equalTo("1970-01-01T00:00:00.000Z")))
                .andExpect(jsonPath("$[0].updated_at", equalTo("1970-01-01T00:00:00.000Z")))
                .andExpect(jsonPath("$[0].created_by_user_name", equalTo("taro")));
    }

    @Test
    public void test_get_returnsARestaurant() throws Exception {
        Restaurant restaurant = new RestaurantFixture()
                .withId(1)
                .withName("Afuri")
                .withAddress("Roppongi")
                .withNotes("とても美味しい")
                .withPlaceId("abcd")
                .withLatitude(1.23)
                .withLongitude(2.34)
                .withCreatedAt(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
                .withUpdatedAt(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
                .build();
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
            restaurant,
            singletonList(new PhotoUrl(999, "http://www.cats.com/my-cat.jpg", 1)),
            new Cuisine(20L, "Swedish"),
            new PriceRange(1L, "100yen"),
            new User(1L, "taro@email.com", "taro"),
            emptyList(),
            true,
            2
        );
        when(restaurantRepository.get(1L, 1L)).thenReturn(Optional.of(serializedRestaurant));
        mockMvc.perform(get("/restaurants/1").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.name", equalTo("Afuri")))
                .andExpect(jsonPath("$.address", equalTo("Roppongi")))
                .andExpect(jsonPath("$.place_id", equalTo("abcd")))
                .andExpect(jsonPath("$.latitude", equalTo(1.23)))
                .andExpect(jsonPath("$.longitude", equalTo(2.34)))
                .andExpect(jsonPath("$.cuisine.id", equalTo(20)))
                .andExpect(jsonPath("$.cuisine.name", equalTo("Swedish")))
                .andExpect(jsonPath("$.notes", equalTo("とても美味しい")))
                .andExpect(jsonPath("$.photo_urls[0].id", equalTo(999)))
                .andExpect(jsonPath("$.photo_urls[0].url", equalTo("http://www.cats.com/my-cat.jpg")))
                .andExpect(jsonPath("$.price_range.id", equalTo(1)))
                .andExpect(jsonPath("$.price_range.range", equalTo("100yen")))
                .andExpect(jsonPath("$.num_likes", equalTo(2)))
                .andExpect(jsonPath("$.liked", equalTo(true)))
                .andExpect(jsonPath("$.created_at", equalTo("1970-01-01T00:00:00.000Z")))
                .andExpect(jsonPath("$.updated_at", equalTo("1970-01-01T00:00:00.000Z")))
                .andExpect(jsonPath("$.created_by_user_name", equalTo("taro")));
    }

    @Test
    public void test_getInvalidRestaurantId_throwsException() throws Exception {
        when(restaurantRepository.get(1L, 1L)).thenReturn(
                Optional.empty()
        );


        mockMvc.perform(get("/restaurants/1").requestAttr("userId", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":\"Invalid restaurant id.\"}"));
    }

    @Test
    public void test_create_createsARestaurantAndReturnsIt() throws Exception {
        Cuisine cuisine = new Cuisine(2, "Ramen");
        PriceRange priceRange = new PriceRange(1, "~900");
        List<PhotoUrl> photoUrls = singletonList(new PhotoUrl(1, "http://some-url", 1));
        Restaurant restaurant = new RestaurantFixture()
                .withId(1)
                .withName("Afuri")
                .withAddress("Roppongi")
                .withNearestStation("Roppongi Station")
                .withNotes("soooo goood")
                .withCreatedAt(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
                .withUpdatedAt(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")))
                .withPlaceId("some-place-id")
                .withLatitude(1.23)
                .withLongitude(2.34)
                .build();
        Long userId = 99L;
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
                restaurant,
                photoUrls,
                cuisine,
                priceRange,
                new User(userId, "email", "jiro"),
                emptyList(),
                false,
                0
        );
        ArgumentCaptor<NewRestaurant> newRestaurantArgument = ArgumentCaptor.forClass(NewRestaurant.class);
        ArgumentCaptor<Long> userIdArgument = ArgumentCaptor.forClass(Long.class);

        when(restaurantRepository.create(newRestaurantArgument.capture(), userIdArgument.capture())).
                thenReturn(serializedRestaurant);
        String payload =
            "{" +
            "\"restaurant\": " +
            "{" +
            "\"name\":\"Afuri\", " +
            "\"address\": \"Roppongi\", " +
            "\"nearest_station\": \"Roppongi Station\", " +
            "\"place_id\": \"some-place-id\", " +
            "\"latitude\": \"1.23\", " +
            "\"longitude\": \"2.34\", " +
            "\"notes\": \"soooo goood\", " +
            "\"photo_urls\": [{\"url\": \"http://some-url\"}], " +
            "\"cuisine_id\": \"2\", " +
            "\"price_range_id\": \"1\"" +
            "}" +
            "}";


        mockMvc.perform(
            post("/restaurants")
            .requestAttr("userId", userId)
            .contentType(APPLICATION_JSON_UTF8_VALUE)
            .content(payload)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Afuri")))
        .andExpect(jsonPath("$.address", is("Roppongi")))
        .andExpect(jsonPath("$.nearest_station", is("Roppongi Station")))
        .andExpect(jsonPath("$.place_id", is("some-place-id")))
        .andExpect(jsonPath("$.latitude", is(1.23)))
        .andExpect(jsonPath("$.longitude", is(2.34)))
        .andExpect(jsonPath("$.notes", is("soooo goood")))
        .andExpect(jsonPath("$.photo_urls[0].url", is("http://some-url")))
        .andExpect(jsonPath("$.cuisine.name", is("Ramen")))
        .andExpect(jsonPath("$.price_range.id", equalTo(1)))
        .andExpect(jsonPath("$.price_range.range", equalTo("~900")))
        .andExpect(jsonPath("$.created_at", Matchers.equalTo("1970-01-01T00:00:00.000Z")))
        .andExpect(jsonPath("$.updated_at", Matchers.equalTo("1970-01-01T00:00:00.000Z")))
        .andExpect(jsonPath("$.created_by_user_name", is("jiro")));

        assertEquals(userId, userIdArgument.getValue());
        assertEquals("Afuri", newRestaurantArgument.getValue().getName());
        assertEquals("Roppongi", newRestaurantArgument.getValue().getAddress());
        assertEquals("Roppongi Station", newRestaurantArgument.getValue().getNearestStation());
        assertEquals("some-place-id", newRestaurantArgument.getValue().getPlaceId());
        assertThat(1.23, is(newRestaurantArgument.getValue().getLatitude()));
        assertThat(2.34, is(newRestaurantArgument.getValue().getLongitude()));
        assertEquals("soooo goood", newRestaurantArgument.getValue().getNotes());
        assertEquals(1, newRestaurantArgument.getValue().getPhotoUrls().size());
        assertEquals("http://some-url", newRestaurantArgument.getValue().getPhotoUrls().get(0).getUrl());
        assertThat(2L, is(newRestaurantArgument.getValue().getCuisineId()));
        assertThat(1L, is(newRestaurantArgument.getValue().getPriceRangeId()));
    }

    @Test
    public void test_update_updatesRestaurantInformation() throws Exception {
        List<NewPhotoUrl> newPhotoUrls = singletonList(new NewPhotoUrl("http://some-url"));
        Restaurant restaurant = new RestaurantFixture()
                .withId(1)
                .withName("Updated Name")
                .withAddress("Updated Address")
                .withPlaceId("updated-place-id")
                .withLatitude(1.23)
                .withLongitude(2.34)
                .withNotes("")
                .build();
        NewRestaurant newRestaurant = new NewRestaurantFixture()
                .withName(restaurant.getName())
                .withAddress(restaurant.getAddress())
                .withPlaceId(restaurant.getPlaceId())
                .withLatitude(restaurant.getLatitude())
                .withLongitude(restaurant.getLongitude())
                .withNotes(restaurant.getNotes())
                .withCuisineId(2)
                .withPriceRangeId(null)
                .withPhotoUrls(newPhotoUrls)
                .build();
        Long userId = 99L;
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
                restaurant,
                singletonList(new PhotoUrl(1, "http://some-url", 1)),
                new Cuisine(2, "Ramen"),
                new PriceRange(1, "~900"),
                new User(userId, "email", "jiro"),
                emptyList(),
                false,
                0
        );

        ArgumentCaptor<Long> attributeRestaurantId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<NewRestaurant> attributeNewRestaurant = ArgumentCaptor.forClass(NewRestaurant.class);
        when(restaurantRepository.update(attributeRestaurantId.capture(), attributeNewRestaurant.capture())).
                thenReturn(serializedRestaurant);
        String updatedRestaurantPayload = "{\"restaurant\": " +
                "{\"name\":\"Updated Name\", " +
                "\"address\": \"Updated Address\", " +
                "\"place_id\": \"updated-place-id\", " +
                "\"latitude\": \"1.23\", " +
                "\"longitude\": \"2.34\", " +
                "\"notes\": \"some notes\"," +
                "\"photo_urls\": [{\"url\": \"http://some-url\"}], " +
                "\"cuisine_id\": \"2\"}" +
                "}";

        mockMvc.perform(
                patch("/restaurants/1")
                        .requestAttr("userId", userId)
                        .contentType(APPLICATION_JSON_UTF8_VALUE)
                        .content(updatedRestaurantPayload)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.address", is("Updated Address")))
                .andExpect(jsonPath("$.place_id", is("updated-place-id")))
                .andExpect(jsonPath("$.latitude", is(1.23)))
                .andExpect(jsonPath("$.longitude", is(2.34)))
                .andExpect(jsonPath("$.notes", is("")))
                .andExpect(jsonPath("$.photo_urls[0].url", is("http://some-url")))
                .andExpect(jsonPath("$.cuisine.name", is("Ramen")))
                .andExpect(jsonPath("$.created_by_user_name", is("jiro")))
                .andExpect(jsonPath("$.price_range.id", equalTo(1)))
                .andExpect(jsonPath("$.price_range.range", equalTo("~900")));

        assertEquals(attributeRestaurantId.getValue().longValue(), 1L);
        assertEquals(attributeNewRestaurant.getValue().getName(), "Updated Name");
        assertEquals(attributeNewRestaurant.getValue().getAddress(), "Updated Address");
        assertEquals(attributeNewRestaurant.getValue().getPlaceId(), "updated-place-id");
        assertThat(attributeNewRestaurant.getValue().getLatitude(), is(1.23));
        assertThat(attributeNewRestaurant.getValue().getLongitude(), is(2.34));
        assertEquals(attributeNewRestaurant.getValue().getNotes(), "some notes");
        assertEquals(attributeNewRestaurant.getValue().getPhotoUrls().size(), 1);
        assertEquals(attributeNewRestaurant.getValue().getPhotoUrls().get(0).getUrl(), "http://some-url");
        assertEquals(attributeNewRestaurant.getValue().getCuisineId().longValue(), 2L);
    }

    @Test
    public void test_deletePhoto_returnsOkHTTPStatus() throws Exception {
        when(photoDataMapper.get(
                anyLong()
        )).thenReturn(Optional.empty());


        ResultActions result = mockMvc.perform(delete("/restaurants/10/photoUrls/20")
                .requestAttr("userId", 11L)
        );


        result.andExpect(status().isOk());
    }

    @Test
    public void test_deletePhoto_deletesPhotoUrlMadeByCurrentUser() throws Exception {
        when(photoDataMapper.get(10))
                .thenReturn(Optional.of(
                        new PhotoUrl(
                                10,
                                "http://hoge/image.jpg",
                                20
                        )
                ));

        ResultActions result = mockMvc.perform(delete("/restaurants/20/photoUrls/10")
                .requestAttr("userId", 99));

        result.andExpect(status().isOk());
        verify(photoDataMapper, times(1)).get(10);
        verify(photoDataMapper, times(1)).delete(10);
        verify(s3StorageRepository, times(1)).deleteFile("http://hoge/image.jpg");
    }


    @Test
    public void test_deletePhoto_doesntDeleteNonExistentPhotoUrl() throws Exception {
        when(photoDataMapper.get(10))
                .thenReturn(Optional.empty()
                );

        ResultActions result = mockMvc.perform(delete("/restaurants/20/photoUrls/10")
                .requestAttr("userId", 99));

        result.andExpect(status().isOk());
        verify(photoDataMapper, times(1)).get(10);
        verify(photoDataMapper, never()).delete(10);
        verify(s3StorageRepository, never()).deleteFile(anyString());
    }

    @Test
    public void test_deleteRestaurant_returnsOk() throws Exception {
        ResultActions result = mockMvc.perform(
            delete("/restaurants/20").requestAttr("userId", 99)
        );

        result.andExpect(status().isOk());
    }

    @Test
    public void test_deleteRestaurant_callsRestaurantRepositoryWithRestaurantIdAndUserId() throws Exception {
        Restaurant restaurant = new RestaurantFixture()
                .withId(20)
                .withName("Afuri")
                .build();
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
            restaurant,
            emptyList(),
            new Cuisine(0L, "Not Specified"),
            new PriceRange(0L, "Not Specified"),
            new User(10L, "taro@email.com", "taro"),
            emptyList(),
            false,
            0
        );
        when(restaurantRepository.get(20L, 10L))
                .thenReturn(Optional.of(serializedRestaurant));

        ResultActions result = mockMvc.perform(delete("/restaurants/20")
                .requestAttr("userId", 10));

        result.andExpect(status().isOk());
        verify(restaurantRepository, times(1)).delete(20L, 10L);
    }
}
