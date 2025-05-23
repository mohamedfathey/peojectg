package com.ProjectGraduation.aucation.service;

import com.ProjectGraduation.aucation.dto.BidResponseDTO;
import com.ProjectGraduation.aucation.entity.AuctionItem;
import com.ProjectGraduation.aucation.entity.Bid;
import com.ProjectGraduation.aucation.repository.AuctionItemRepository;
import com.ProjectGraduation.aucation.repository.BidRepository;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BidService {

    private final BidRepository bidRepo;
    private final AuctionItemRepository auctionRepo;
    private final UserRepository userRepo;

    public BidResponseDTO makeBid(User user, Long auctionId, double amount) {
        AuctionItem auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!auction.isActive() || auction.getEndTime().isBefore(LocalDateTime.now())) {
            return BidResponseDTO.failed("Auction is closed", auctionId);
        }

        if (auction.getStartTime().isAfter(LocalDateTime.now())) {
            return BidResponseDTO.failed("Auction hasn't started yet", auctionId);
        }

        if (auction.getHighestBidder() != null && auction.getHighestBidder().getId().equals(user.getId())) {
            return BidResponseDTO.failed("You already have the highest bid", auctionId);
        }

        // minimum bid check
        double minIncrement = auction.getCurrentBid() * 0.10;
        double minAcceptableBid = auction.getCurrentBid() + minIncrement;

        if (amount < minAcceptableBid) {
            return BidResponseDTO.failed("Minimum bid must be at least 10% higher than current bid (min: " + minAcceptableBid + ")", auctionId);
        }

        // First bid guarantee
        boolean isFirstBid = bidRepo.findByAuctionItemAndUser(auction, user).isEmpty();
        if (isFirstBid) {
            double guarantee = auction.getStartingBid() * 0.10;
            if (user.getWalletBalance() < guarantee) {
                return BidResponseDTO.failed("Insufficient balance for entry guarantee", auctionId);
            }

            user.setWalletBalance(user.getWalletBalance() - guarantee);
            user.setReservedBalance(user.getReservedBalance() + guarantee);
            userRepo.save(user);
        }

        // update the auction with the new bid
        auction.setCurrentBid(amount);
        auction.setHighestBidder(user);
        auctionRepo.save(auction);

        // Save the new bid
        Bid newBid = new Bid();
        newBid.setAuctionItem(auction);
        newBid.setBidAmount(amount);
        newBid.setUser(user);
        newBid.setBidTime(LocalDateTime.now());
        bidRepo.save(newBid);

        return BidResponseDTO.builder()
                .success(true)
                .message("Bid placed successfully")
                .auctionId(auctionId)
                .currentBid(amount)
                .highestBidder(user.getUsername())
                .bidTime(newBid.getBidTime())
                .build();
    }


    public List<BidResponseDTO> getBidsForAuction(Long auctionId) {
        AuctionItem auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        return bidRepo.findByAuctionItemOrderByBidAmountDesc(auction)
                .stream()
                .map(bid -> BidResponseDTO.builder()
                        .auctionId(auctionId)
                        .currentBid(bid.getBidAmount())
                        .highestBidder(bid.getUser().getUsername())
                        .bidTime(bid.getBidTime())
                        .success(true)
                        .message("Bid record")
                        .build())
                .toList();
    }
}
