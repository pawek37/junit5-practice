package com.dmdev.integration;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class SubscriptionDaoITest extends IntegrationTestBase {
    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        Subscription sub1 = subscriptionDao.insert(getSubscription("Netflix"));
        Subscription sub2 = subscriptionDao.insert(getSubscription("Spotify"));
        Subscription sub3 = subscriptionDao.insert(getSubscription("OnlyFans"));

        List<Subscription> actualResult = subscriptionDao.findAll();

        Assertions.assertThat(actualResult).hasSize(3);
        List<Integer> subIds = actualResult.stream()
                .map(Subscription::getId)
                .toList();
        Assertions.assertThat(subIds).contains(sub1.getId(),sub2.getId(),sub3.getId());

    }

    @Test
    void findById() {
        Subscription sub1 = subscriptionDao.insert(getSubscription("Netflix"));
        Subscription sub2 = subscriptionDao.insert(getSubscription("Spotify"));
        Subscription sub3 = subscriptionDao.insert(getSubscription("OnlyFans"));

        Optional<Subscription> actualResult = subscriptionDao.findById(sub1.getId());

        Assertions.assertThat(actualResult).isPresent();
        Assertions.assertThat(actualResult.get()).isEqualTo(sub1);
    }
    @Test
    void NoFindById() {
        int nonExistedId = 999;

        Subscription sub1 = subscriptionDao.insert(getSubscription("Netflix"));
        Subscription sub2 = subscriptionDao.insert(getSubscription("Spotify"));
        Subscription sub3 = subscriptionDao.insert(getSubscription("OnlyFans"));

        Optional<Subscription> actualResult = subscriptionDao.findById(999);

        Assertions.assertThat(actualResult).isEmpty();
    }

    @Test
    void delete() {
        Subscription sub1 = subscriptionDao.insert(getSubscription("Netflix"));

        boolean delete = subscriptionDao.delete(sub1.getId());

        Assertions.assertThat(delete).isTrue();
        Assertions.assertThat(subscriptionDao.findAll()).isEmpty();
    }
    @Test
    void tryToDeleteIfUserDoNotExist() {
        subscriptionDao.insert(getSubscription("FakeSub"));

        boolean delete = subscriptionDao.delete(21);

        Assertions.assertThat(delete).isFalse();
        Assertions.assertThat(subscriptionDao.findAll()).hasSize(1);
    }

    @Test
    void update() {
        Subscription sub1 = subscriptionDao.insert(getSubscription("Netflix"));
        sub1.setName("Testing");

        Subscription actualResult = subscriptionDao.update(sub1);

        Assertions.assertThat(actualResult).isEqualTo(sub1);
        Assertions.assertThat(actualResult.getName()).isEqualTo("Testing");
    }
    @Test
    void updateNonExistedId() {
        Subscription subNoExist = getSubscription("Ghost");
        subNoExist.setName("NoGhost");
        subNoExist.setId(999);
        Subscription updatedSubNoExist = subscriptionDao.update(subNoExist);


        Assertions.assertThat(subNoExist).isEqualTo(updatedSubNoExist);
        Assertions.assertThat(subscriptionDao.findById(updatedSubNoExist.getId())).isEmpty();

    }

    @Test
    void insert() {
        Subscription sub = subscriptionDao.insert(getSubscription("Netflix"));
        Subscription netflix = Subscription.builder()
                .id(sub.getId())
                .userId(1)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build();
        Assertions.assertThat(sub).isEqualTo(netflix);
    }

    @Test
    void findByUserId() {
        Subscription netflixUser1 = subscriptionDao.insert(getSubscription("Netflix"));
        Subscription spotifyUser1 = subscriptionDao.insert(getSubscription("Spotify"));
        Subscription otherUserNetflix = subscriptionDao.insert(Subscription.builder()
                .userId(2)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build());

        List<Subscription>listOfUsersSubscription = Arrays.asList(netflixUser1,spotifyUser1,otherUserNetflix);

        List<Subscription> actualResult = subscriptionDao.findByUserId(netflixUser1.getUserId());

        Assertions.assertThat(actualResult).hasSize(2);
        Assertions.assertThat(actualResult).contains(netflixUser1,spotifyUser1);
        Assertions.assertThat(actualResult).isNotEqualTo(listOfUsersSubscription);
    }
    @Test
    void NoResultFindByUserId() {

        int nonExistentId = 999;

        Subscription netflixUser1 = subscriptionDao.insert(getSubscription("Netflix"));
        Subscription spotifyUser1 = subscriptionDao.insert(getSubscription("Spotify"));
        Subscription otherUserNetflix = subscriptionDao.insert(Subscription.builder()
                .userId(2)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build());

        List<Subscription>listOfUsersSubscription = Arrays.asList(netflixUser1,spotifyUser1,otherUserNetflix);

        List<Subscription> actualResult = subscriptionDao.findByUserId(nonExistentId);

        Assertions.assertThat(actualResult).isEmpty();

    }

    private static Subscription getSubscription(String name) {
        return Subscription.builder()
                .userId(1)
                .name(name)
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build();
    }
}