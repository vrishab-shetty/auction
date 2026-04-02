package me.vrishab.auction.auction.dto;

public record NotificationEvent<T>(
        String type,
        T data
) {}
