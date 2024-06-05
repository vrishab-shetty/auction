package me.vrishab.auction.auction;

import jakarta.transaction.Transactional;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.user.User;
import me.vrishab.auction.user.UserNotFoundException;
import me.vrishab.auction.user.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuctionService {

    private final AuctionRepository auctionRepo;

    private final UserRepository userRepo;

    public AuctionService(AuctionRepository auctionRepo, UserRepository userRepo) {
        this.auctionRepo = auctionRepo;
        this.userRepo = userRepo;
    }

    public Auction findById(String id) {
        return this.auctionRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new AuctionNotFoundException(UUID.fromString(id)));
    }

    public List<Auction> findAll(PageRequestParams pageSettings) {
        Pageable pageable = Pageable.unpaged();
        if (pageSettings != null && pageSettings.getPageNum() != null && pageSettings.getPageSize() != null)
            pageable = pageSettings.createPageRequest();

        return this.auctionRepo.findAll(pageable).toList();
    }

    public Auction add(String userId, Auction auction) {
        UUID id = UUID.fromString(userId);
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        auction.getItems().forEach(item -> {
                    item.setAuctionId(auction.getId());
                    item.setSeller(user.getEmail());
                }
        );

        user.addAuction(auction);

        return auctionRepo.save(auction);
    }
}
