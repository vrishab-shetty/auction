package me.vrishab.auction.user;

import me.vrishab.auction.user.model.BillingDetails;
import me.vrishab.auction.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingDetailsRepository<T extends BillingDetails, ID> extends JpaRepository<T, ID> {
    List<T> findByOwner(String owner);

    List<T> findByUser(User user);

    void deleteByUser(User user);
}
