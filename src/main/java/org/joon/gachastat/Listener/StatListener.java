package org.joon.gachastat.Listener;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.joon.gachastat.GachaStat;
import org.joon.gachastat.Manager.StatManager;

import java.util.List;
import java.util.UUID;

public class StatListener implements Listener {

    private final StatManager statManager;
    public StatListener(StatManager statManager) {
        this.statManager = statManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        checkArmor(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        statManager.statAttack.remove(uuid);
        statManager.statDefense.remove(uuid);
        statManager.statHp.remove(uuid);
    }

    @EventHandler
    public void InventoryClick(InventoryClickEvent e) {
        if(e.getInventory().getType() == InventoryType.CRAFTING) {
            Player player  = (Player) e.getWhoClicked();
            if(e.getSlotType() == InventoryType.SlotType.ARMOR){
                checkArmor(player);
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
            Player player = e.getPlayer();
            ItemStack item = e.getItem();
            if(item != null && item.getType() != Material.AIR ){
                checkArmor(player);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Player player = (Player) e.getDamager();
        String uuid = player.getUniqueId().toString();
        if(statManager.statAttack.containsKey(uuid)){
            double stat = statManager.statAttack.get(uuid);
            double damage = stat;
            e.setDamage(e.getDamage() + damage);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(statManager.statDefense.containsKey(player.getUniqueId().toString())){
                double defense = statManager.statDefense.get(player.getUniqueId().toString());
                double originalDamage = event.getDamage();
                double reducedDamage = Math.max(originalDamage - defense, 0); // 데미지 감소 후 0 이하로는 떨어지지 않도록 합니다.
                event.setDamage(reducedDamage);
            }
        }
    }

    private void checkArmor(Player player){
        String uuid = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerInventory inventory = player.getInventory();
                boolean attack = false;
                boolean hp = false;
                boolean defense = false;
                ItemStack[] armor = inventory.getArmorContents();

                for(ItemStack i : armor){
                    if(i!= null){
                        ItemMeta meta = i.getItemMeta();
                        if(meta.getLore() != null){
                            List<String> lore =meta.getLore();
                            if(lore.contains(net.md_5.bungee.api.ChatColor.of("#B6EDF3") + "[ 추가 능력치 ] ")){
                                String[] statList = meta.getLocalizedName().split("/");
                                if(statList.length > 0){
                                    for(String stat : statList){
                                        String statName = stat.split("-")[0];
                                        try{
                                            Double value = Double.parseDouble(stat.split("-")[1]);
                                            if(statName.equals("공격력")){
                                                attack = true;
                                                statManager.statAttack.put(uuid, value);
                                            }else if(statName.equals("체력")){
                                                hp = true;
                                                value += 20;
                                                statManager.statHp.put(uuid, value);
                                                AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                                                if (healthAttribute != null) {
                                                    // 기존 추가 체력 수정자가 있는지 확인하여 제거 (중복 방지)
                                                    for (AttributeModifier modifier : healthAttribute.getModifiers()) {
                                                        if (modifier.getName().equals("extra_health_modifier")) {
                                                            healthAttribute.removeModifier(modifier);
                                                        }
                                                    }

                                                    // 새로운 최대 체력 값을 설정합니다.
                                                    healthAttribute.setBaseValue(value);

                                                    // 플레이어의 현재 체력도 최대 체력에 맞추어 조정합니다.
                                                    if (player.getHealth() > value) {
                                                        player.setHealth(value);
                                                    }
                                                }
                                            }else if(statName.equals("방어력")){
                                                defense = true;
                                                statManager.statDefense.put(uuid, value);
                                            }
                                        }catch (NumberFormatException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if(!attack){
                    statManager.statAttack.remove(uuid);
                }
                if(!hp){
                    statManager.statHp.remove(uuid);
                    player.setMaxHealth(20);
                }
                if(!defense){
                    statManager.statDefense.remove(uuid);
                }
            }
        }.runTaskLater(GachaStat.getInstance(), 20L);

    }
}
