package me.totalfreedom.totalfreedommod;

import com.google.common.collect.Multimap;
import io.papermc.lib.PaperLib;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MovementValidator extends FreedomService
{

    public static final int MAX_XYZ_COORD = 29999998;
    public static final int MAX_DISTANCE_TRAVELED = 100;

    @Override
    public void onStart()
    {
    }

    @Override
    public void onStop()
    {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        // Check absolute value to account for negatives
        if (Math.abs(Objects.requireNonNull(event.getTo()).getX()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getZ()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getY()) >= MAX_XYZ_COORD)
        {
            event.setCancelled(true); // illegal position, cancel it
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        final Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        assert to != null;
        if (to.getX() >= from.getX() + MAX_DISTANCE_TRAVELED || to.getY() >= from.getY() + MAX_DISTANCE_TRAVELED || to.getZ() >= from.getZ() + MAX_DISTANCE_TRAVELED)
        {
            event.setCancelled(true);
            player.kickPlayer(ChatColor.RED + "You were moving too quickly!");
        }
        // Check absolute value to account for negatives
        if (Math.abs(event.getTo().getX()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getZ()) >= MAX_XYZ_COORD || Math.abs(event.getTo().getY()) >= MAX_XYZ_COORD)
        {
            event.setCancelled(true);
            PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation());
        }

        if (exploitItem(event.getPlayer().getInventory().getHelmet()))
        {
            event.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your helmet slot.");
            event.setCancelled(true);
        }
        if (exploitItem(event.getPlayer().getInventory().getBoots()))
        {
            event.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your boots slot.");
            event.setCancelled(true);
        }
        if (exploitItem(event.getPlayer().getInventory().getLeggings()))
        {
            event.getPlayer().getInventory().setLeggings(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your leggings slot.");
            event.setCancelled(true);
        }
        if (exploitItem(event.getPlayer().getInventory().getChestplate()))
        {
            event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your chestplate slot.");
            event.setCancelled(true);
        }
        if (exploitItem(event.getPlayer().getInventory().getItemInMainHand()))
        {
            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your hand.");
            event.setCancelled(true);
        }
        if (exploitItem(event.getPlayer().getInventory().getItemInOffHand()))
        {
            event.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your offhand.");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        final Player player = event.getPlayer();

        // Validate position
        if (Math.abs(player.getLocation().getX()) >= MAX_XYZ_COORD || Math.abs(player.getLocation().getZ()) >= MAX_XYZ_COORD || Math.abs(player.getLocation().getY()) >= MAX_XYZ_COORD)
        {
            PaperLib.teleportAsync(player, player.getWorld().getSpawnLocation()); // Illegal position, teleport to spawn
        }
    }

    @EventHandler
    public void onPlayerHoldItem(PlayerItemHeldEvent event)
    {
        if (exploitItem(event.getPlayer().getInventory().getItemInMainHand()))
        {
            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your hand.");
        }
        if (exploitItem(event.getPlayer().getInventory().getItemInOffHand()))
        {
            event.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            event.getPlayer().sendMessage(ChatColor.RED + "An item with both negative infinity and positive infinity attributes was cleared from your offhand.");
        }
    }

    private Boolean exploitItem(ItemStack item)
    {
        if (item == null)
        {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null)
        {
            Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
            if (attributes != null)
            {
                Map<Attribute, Collection<AttributeModifier>> attrMap = attributes.asMap();

                // For every attribute...
                for (Attribute attr : attributes.keySet())
                {
                    // Default values
                    boolean posInf = false;
                    boolean negInf = false;

                    // For every AttributeModifier...
                    for (AttributeModifier modifier : attrMap.get(attr))
                    {
                        // Are they ∞ or -∞?
                        if (modifier.getAmount() == Double.POSITIVE_INFINITY)
                        {
                            posInf = true;
                        }
                        else if (modifier.getAmount() == Double.NEGATIVE_INFINITY)
                        {
                            negInf = true;
                        }
                    }

                    // Are both values set as true?
                    if (posInf && negInf)
                    {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}