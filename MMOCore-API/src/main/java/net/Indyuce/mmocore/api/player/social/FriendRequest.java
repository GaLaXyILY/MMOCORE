package net.Indyuce.mmocore.api.player.social;

import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.player.CooldownType;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Sound;

public class FriendRequest extends Request {
    public FriendRequest(PlayerData creator, PlayerData target) {
        super(creator, target);
    }

    @Override
    public void whenDenied() {
        getTarget().getPlayer().playSound(getTarget().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
    }

    @Override
    public void whenAccepted() {
        getCreator().getCooldownMap().resetCooldown(CooldownType.FRIEND_REQUEST);
        getCreator().addFriend(getTarget().getUniqueId());
        getTarget().addFriend(getCreator().getUniqueId());
        if (getCreator().isOnline()) {
            ConfigMessage.fromKey("now-friends", "player", getTarget().getPlayer().getName()).send(getCreator().getPlayer());
            ConfigMessage.fromKey("now-friends", "player", getCreator().getPlayer().getName()).send(getTarget().getPlayer());
        }
    }
}