package com.gachicraft.resourcesx.config;

import org.bukkit.Location;
import org.bukkit.Material;

public record OrePointConfig(String name, int id, Material ore, Location location, int cooldownSeconds, int[] counts) { }
