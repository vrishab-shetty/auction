package me.vrishab.auction.auction;

import jakarta.validation.Valid;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping("/auctions/{auctionId}")
    public Result findAuctionById(@PathVariable(name = "auctionId") String id) {
        return new Result(true, "Find Auctions", this.auctionService.findById(id));
    }

    @GetMapping("/auctions")
    public Result findAllAuctions(
            @ModelAttribute
            @Valid PageRequestParams pageParams) {
        return null;
    }

}
