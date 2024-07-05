package me.vrishab.auction.auction;

import jakarta.validation.Valid;
import me.vrishab.auction.auction.converter.AuctionCreationDTOToAuctionConverter;
import me.vrishab.auction.auction.converter.AuctionToAuctionDTOConverter;
import me.vrishab.auction.auction.converter.AuctionUpdateDTOToAuctionConverter;
import me.vrishab.auction.auction.dto.AuctionCreationDTO;
import me.vrishab.auction.auction.dto.AuctionDTO;
import me.vrishab.auction.auction.dto.AuctionUpdateDTO;
import me.vrishab.auction.auction.dto.BidRequestDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.converter.ItemToAuctionItemDTO;
import me.vrishab.auction.item.dto.AuctionItemDTO;
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

    private final AuctionCreationDTOToAuctionConverter auctionCreationDTOToAuctionConverter;

    private final AuctionToAuctionDTOConverter auctionToAuctionDTOConverter;

    private final AuctionUpdateDTOToAuctionConverter auctionUpdateDTOToAuctionConverter;
    private final ItemToAuctionItemDTO itemToAuctionItemDTO;

    public AuctionController(AuctionService auctionService, AuthService authService, AuctionCreationDTOToAuctionConverter auctionCreationDTOToAuctionConverter, AuctionToAuctionDTOConverter auctionToAuctionDTOConverter, AuctionUpdateDTOToAuctionConverter auctionUpdateDTOToAuctionConverter, ItemToAuctionItemDTO itemToAuctionItemDTO) {
        this.auctionService = auctionService;
        this.authService = authService;
        this.auctionCreationDTOToAuctionConverter = auctionCreationDTOToAuctionConverter;
        this.auctionToAuctionDTOConverter = auctionToAuctionDTOConverter;
        this.auctionUpdateDTOToAuctionConverter = auctionUpdateDTOToAuctionConverter;
        this.itemToAuctionItemDTO = itemToAuctionItemDTO;
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
    public Result addAuction(
            Authentication auth,
            @Valid @RequestBody
            AuctionCreationDTO auctionCreationDTO
    ) {
        String userId = authService.getUserInfo(auth);
        Auction auction = this.auctionCreationDTOToAuctionConverter.convert(auctionCreationDTO);
        Auction savedAuction = this.auctionService.add(userId, auction);
        AuctionDTO savedAuctionDTO = this.auctionToAuctionDTOConverter.convert(savedAuction);
        return new Result(true, "Add an Auction", savedAuctionDTO);
    }

    @PutMapping("/auctions/{auctionId}")
    public Result updateAuction(
            Authentication auth,
            @PathVariable
            String auctionId,
            @Valid @RequestBody
            AuctionUpdateDTO auctionUpdateDTO) {

        String userId = authService.getUserInfo(auth);
        Auction auction = this.auctionUpdateDTOToAuctionConverter.convert(auctionUpdateDTO);
        Auction savedAuction = this.auctionService.update(userId, auction, auctionId);
        AuctionDTO savedAuctionDTO = this.auctionToAuctionDTOConverter.convert(savedAuction);
        return new Result(true, "Update an Auction", savedAuctionDTO);

    }

    @DeleteMapping("/auctions/{auctionId}")
    public Result deleteAuction(
            Authentication auth,
            @PathVariable
            String auctionId) {
        String userId = authService.getUserInfo(auth);
        this.auctionService.delete(userId, auctionId);
        return new Result(true, "Delete an Auction");
    }

    @PutMapping("/auctions/{auctionId}/items/{itemId}/bid")
    public Result placeBid(
            Authentication auth,
            @PathVariable
            String auctionId,
            @PathVariable String itemId,
            @RequestBody
            @Valid
            BidRequestDTO bidRequest
    ) {
        String userId = authService.getUserInfo(auth);
        Item auctionItem = this.auctionService.bid(userId, auctionId, itemId, bidRequest.bidAmount());

        AuctionItemDTO itemDTO  = this.itemToAuctionItemDTO.convert(auctionItem);

        return new Result(true, "Place a Bid", itemDTO);
    }

}
