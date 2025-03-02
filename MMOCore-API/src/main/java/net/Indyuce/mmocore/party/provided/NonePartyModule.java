package net.Indyuce.mmocore.party.provided;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.party.PartyModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NonePartyModule implements PartyModule {

    @Override
    public @Nullable AbstractParty getParty(@NotNull PlayerData playerData) {
        return null;
    }
}
