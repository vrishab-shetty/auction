package me.vrishab.auction.auction;

import me.vrishab.auction.system.PageRequestParams;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepo;

    public AuctionService(AuctionRepository auctionRepo) {
        this.auctionRepo = auctionRepo;
    }

    public Auction findById(String id) {
        return this.auctionRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new AuctionNotFoundException(UUID.fromString(id)));
    }

    public List<Auction> findAll(PageRequestParams pageSettings) {
        Pageable pageable = Pageable.unpaged();
        if (pageSettings != null && pageSettings.getPageNum() != null && pageSettings.getPageSize() != null) {
            pageable = PageRequest.of(pageSettings.getPageNum(), pageSettings.getPageSize());
        }

        return this.auctionRepo.findAll(pageable).toList();
    }
}
