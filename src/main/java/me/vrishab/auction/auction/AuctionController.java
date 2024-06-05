package me.vrishab.auction.auction;

import jakarta.validation.Valid;
import me.vrishab.auction.auction.converter.AuctionEditableDTOToAuctionConverter;
import me.vrishab.auction.auction.converter.AuctionToAuctionDTOConverter;
import me.vrishab.auction.auction.dto.AuctionDTO;
import me.vrishab.auction.auction.dto.AuctionEditableDTO;
import me.vrishab.auction.security.AuthService;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.Result;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class AuctionController {

    private final AuctionService auctionService;

    private final AuthService authService;

    private final AuctionEditableDTOToAuctionConverter auctionEditableDTOToAuctionConverter;

    private final AuctionToAuctionDTOConverter auctionToAuctionDTOConverter;

    public AuctionController(AuctionService auctionService, AuthService authService, AuctionEditableDTOToAuctionConverter auctionEditableDTOToAuctionConverter, AuctionToAuctionDTOConverter auctionToAuctionDTOConverter) {
        this.auctionService = auctionService;
        this.authService = authService;
        this.auctionEditableDTOToAuctionConverter = auctionEditableDTOToAuctionConverter;
        this.auctionToAuctionDTOConverter = auctionToAuctionDTOConverter;
    }

    @GetMapping("/auctions/{auctionId}")
    public Result findAuctionById(@PathVariable(name = "auctionId") String id) {
        Auction auction = this.auctionService.findById(id);

        return new Result(true, "Find Auctions", this.auctionToAuctionDTOConverter.convert(auction));
    }

    @GetMapping("/auctions")
    public Result findAllAuctions(
            @ModelAttribute
            @Valid PageRequestParams pageParams) {
        List<Auction> auctions = this.auctionService.findAll(pageParams);

        List<AuctionDTO> dtos = auctions.stream().map(
                        this.auctionToAuctionDTOConverter::convert
                )
                .toList();

        return new Result(true, "Find all Auctions", dtos);
    }

    @PostMapping("/auctions")
    public Result addAuction(Authentication auth, @Valid @RequestBody AuctionEditableDTO auctionEditableDTO) {
        String userId = authService.getUserInfo(auth);
        Auction auction = this.auctionEditableDTOToAuctionConverter.convert(auctionEditableDTO);
        Auction savedAuction = this.auctionService.add(userId, auction);
        AuctionDTO savedAuctionDTO = this.auctionToAuctionDTOConverter.convert(savedAuction);
        return new Result(true, "Add an Auction", savedAuctionDTO);
    }

}
