package com.tokyo.beach.restaurants.restaurant;

import com.tokyo.beach.restaurants.comment.CommentRepository;
import com.tokyo.beach.restaurants.comment.SerializedComment;
import com.tokyo.beach.restaurants.cuisine.Cuisine;
import com.tokyo.beach.restaurants.cuisine.CuisineDataMapper;
import com.tokyo.beach.restaurants.like.Like;
import com.tokyo.beach.restaurants.like.LikeDataMapper;
import com.tokyo.beach.restaurants.photos.NewPhotoUrl;
import com.tokyo.beach.restaurants.photos.PhotoDataMapper;
import com.tokyo.beach.restaurants.photos.PhotoUrl;
import com.tokyo.beach.restaurants.pricerange.PriceRange;
import com.tokyo.beach.restaurants.pricerange.PriceRangeDataMapper;
import com.tokyo.beach.restaurants.s3.S3StorageRepository;
import com.tokyo.beach.restaurants.user.User;
import com.tokyo.beach.restaurants.user.UserDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
public class RestaurantRepository {
    private final RestaurantDataMapper restaurantDataMapper;
    private final PhotoDataMapper photoDataMapper;
    private final UserDataMapper userDataMapper;
    private final PriceRangeDataMapper priceRangeDataMapper;
    private final LikeDataMapper likeDataMapper;
    private final CuisineDataMapper cuisineDataMapper;
    private CommentRepository commentRepository;
    private S3StorageRepository s3StorageRepository;

    @Autowired
    public RestaurantRepository(RestaurantDataMapper restaurantDataMapper,
                                PhotoDataMapper photoDataMapper,
                                UserDataMapper userDataMapper,
                                PriceRangeDataMapper priceRangeDataMapper,
                                LikeDataMapper likeDataMapper,
                                CuisineDataMapper cuisineDataMapper,
                                CommentRepository commentRepository,
                                S3StorageRepository s3StorageRepository
                                 ) {
        this.restaurantDataMapper = restaurantDataMapper;
        this.photoDataMapper = photoDataMapper;
        this.userDataMapper = userDataMapper;
        this.priceRangeDataMapper = priceRangeDataMapper;
        this.likeDataMapper = likeDataMapper;
        this.cuisineDataMapper = cuisineDataMapper;
        this.commentRepository = commentRepository;
        this.s3StorageRepository = s3StorageRepository;
    }

    public List<SerializedRestaurant> getAll(Long userId) {
        List<Restaurant> restaurantList = restaurantDataMapper.getAll();
        List<Long> ids = restaurantList.stream().map(Restaurant::getId).collect(toList());

        List<PhotoUrl> photos = photoDataMapper.findForRestaurants(ids);
        Map<Long, List<PhotoUrl>> restaurantPhotos = photos
            .stream()
            .collect(groupingBy(PhotoUrl::getRestaurantId));

        List<User> userList = userDataMapper.findForUserIds(
            restaurantList
                .stream()
                .map(Restaurant::getCreatedByUserId)
                .collect(toList())
        );
        Map<Long, User> createdByUsers = userList
                .stream()
                .collect(Collectors.toMap(User::getId, UnaryOperator.identity()));

        Map<Long, PriceRange> priceRangeMap = priceRangeDataMapper.getAll()
                .stream()
                .collect(Collectors.toMap(PriceRange::getId, UnaryOperator.identity()));

        Map<Long, Cuisine> cuisineMap = cuisineDataMapper.getAll()
                .stream()
                .collect(Collectors.toMap(Cuisine::getId, UnaryOperator.identity()));

        Map<Long, List<Like>> restaurantLikes = likeDataMapper.findForRestaurants(restaurantList)
                .stream()
                .collect(groupingBy(Like::getRestaurantId));

        return restaurantList
                .stream()
                .map((restaurant) -> new SerializedRestaurant(
                    restaurant,
                    restaurantPhotos.get(restaurant.getId()),
                    cuisineMap.get(restaurant.getCuisineId()),
                    priceRangeMap.get(restaurant.getPriceRangeId()),
                    createdByUsers.get(restaurant.getCreatedByUserId()),
                    emptyList(),
                    restaurantLikes.get(restaurant.getId()) == null ? false : restaurantLikes.get(restaurant.getId()).contains(new Like(userId.longValue(), restaurant.getId())),
                    restaurantLikes.get(restaurant.getId()) == null ? 0 : restaurantLikes.get(restaurant.getId()).size()
                ))
                .collect(toList());
    }

    public Optional<SerializedRestaurant> get(Long restaurantId, Long userId) {
        Optional<Restaurant> maybeRestaurant = restaurantDataMapper.get(restaurantId);

        if (maybeRestaurant.isPresent()) {

            Restaurant retrievedRestaurant = maybeRestaurant.get();
            User createdByUser = userDataMapper.findForRestaurantId(retrievedRestaurant.getId());

            List<PhotoUrl> photosForRestaurant = photoDataMapper.findForRestaurant(retrievedRestaurant.getId());
            Cuisine cuisineForRestaurant = cuisineDataMapper.findForRestaurant(retrievedRestaurant.getId());
            PriceRange priceRange = priceRangeDataMapper.findForRestaurant(retrievedRestaurant.getId());

            List<SerializedComment> comments = commentRepository.findForRestaurant(retrievedRestaurant.getId());

            List<Like> likes = likeDataMapper.findForRestaurant(retrievedRestaurant.getId());
            boolean currentUserLikesRestaurant = likes
                    .stream()
                    .map(Like::getUserId)
                    .anyMatch(Predicate.isEqual(userId));

            return Optional.of(new SerializedRestaurant(
                    retrievedRestaurant,
                    photosForRestaurant,
                    cuisineForRestaurant,
                    priceRange,
                    createdByUser,
                    comments,
                    currentUserLikesRestaurant,
                    likes.size()
            ));
        } else {
            return Optional.empty();
        }
    }

    public SerializedRestaurant create(NewRestaurant newRestaurant, Long userId) {
        Restaurant restaurant = restaurantDataMapper.createRestaurant(
                newRestaurant, userId.longValue()
        );
        User createdByUser = userDataMapper.findForRestaurantId(restaurant.getId());
        List<PhotoUrl> photosForRestaurant = photoDataMapper.createPhotosForRestaurant(
                restaurant.getId(),
                newRestaurant.getPhotoUrls()
        );
        Cuisine cuisine = cuisineDataMapper.findForRestaurant(restaurant.getId());
        PriceRange priceRange = priceRangeDataMapper.findForRestaurant(restaurant.getId());

        return new SerializedRestaurant(
                restaurant,
                photosForRestaurant,
                cuisine,
                priceRange,
                createdByUser,
                emptyList(),
                false,
                0L);
    }

    public SerializedRestaurant update(Long restaurantId, NewRestaurant newRestaurant) {
        Restaurant restaurant = restaurantDataMapper.updateRestaurant(
                restaurantId,
                newRestaurant
        );
        User createdByUser = userDataMapper.findForRestaurantId(restaurant.getId());

        List<PhotoUrl> existingPhotosForRestaurant = photoDataMapper.findForRestaurant(restaurant.getId());
        List<NewPhotoUrl> onlyNewPhotos = newRestaurant.getPhotoUrls()
                .stream()
                .filter(newPhotoUrl -> !existingPhotosForRestaurant
                        .stream()
                        .map(photoUrl -> photoUrl.getUrl())
                        .collect(toList())
                        .contains(newPhotoUrl.getUrl())
                ).collect(toList());
        List<PhotoUrl> newPhotosPersisted = photoDataMapper.createPhotosForRestaurant(
                restaurant.getId(),
                onlyNewPhotos
        );
        Cuisine cuisine = cuisineDataMapper.findForRestaurant(restaurant.getId());
        PriceRange priceRange = priceRangeDataMapper.findForRestaurant(restaurant.getId());
        List<SerializedComment> comments = commentRepository.findForRestaurant(restaurant.getId());

        List<Like> likes = likeDataMapper.findForRestaurant(restaurant.getId());
        boolean currentUserLikesRestaurant = likes
                .stream()
                .map(Like::getUserId)
                .anyMatch(Predicate.isEqual(createdByUser.getId()));

        return new SerializedRestaurant(
                restaurant,
                Stream.concat(
                        existingPhotosForRestaurant.stream(),
                        newPhotosPersisted.stream()
                ).collect(Collectors.toList()),
                cuisine,
                priceRange,
                createdByUser,
                comments,
                currentUserLikesRestaurant,
                likes.size()
        );
    }

    public void delete(Long restaurantId, Long userId) {
        Optional<Restaurant> maybeRestaurant = restaurantDataMapper.get(restaurantId);
        if (maybeRestaurant.isPresent() && maybeRestaurant.get().getCreatedByUserId() == userId) {
            List<PhotoUrl> photoUrls = photoDataMapper.findForRestaurant(restaurantId);
            restaurantDataMapper.delete(restaurantId);
            photoUrls.stream().forEach(photoUrl -> {
                s3StorageRepository.deleteFile(photoUrl.getUrl());
            });
        }
    }
}
