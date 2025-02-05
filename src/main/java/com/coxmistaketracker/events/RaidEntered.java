package com.coxmistaketracker.events;

import lombok.Value;

import java.util.List;

/**
 * An event denoting that a new raid has been entered.
 */
@Value
public class RaidEntered {

    List<String> raiderNames;
}