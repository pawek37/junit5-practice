package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
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
    void delete() {
        Subscription sub1 = subscriptionDao.insert(getSubscription("Netflix"));

        boolean delete = subscriptionDao.delete(sub1.getId());

        Assertions.assertThat(delete).isTrue();
        Assertions.assertThat(subscriptionDao.findAll()).hasSize(0);
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
        sub1.setId(3);

        Subscription actualResult = subscriptionDao.update(sub1);

        Assertions.assertThat(actualResult).isEqualTo(sub1);
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
        Subscription netflix = subscriptionDao.insert(getSubscription("Netflix"));
        Subscription spotify = subscriptionDao.insert(getSubscription("Spotify"));
        Subscription otherUserNetflix = subscriptionDao.insert(Subscription.builder()
                .userId(2)
                .name("Netflix")
                .provider(Provider.APPLE)
                .expirationDate(Instant.parse("2029-10-05T14:30:00Z"))
                .status(Status.ACTIVE)
                .build());

        List<Subscription>listOfUserSubscription = Arrays.asList(netflix,spotify);

        List<Subscription> byUserId = subscriptionDao.findByUserId(netflix.getUserId());

        Assertions.assertThat(byUserId).hasSize(2);
        Assertions.assertThat(byUserId).contains(netflix,spotify);
        Assertions.assertThat(byUserId).isEqualTo(listOfUserSubscription);
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