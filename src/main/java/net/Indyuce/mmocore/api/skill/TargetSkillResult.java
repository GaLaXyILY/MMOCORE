package net.Indyuce.mmocore.api.skill;

import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.skill.Skill.SkillInfo;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.MMORayTraceResult;

public class TargetSkillResult extends SkillResult {
	private LivingEntity target;

	public TargetSkillResult(PlayerData data, SkillInfo skill, double range) {
		super(data, skill);

		if (isSuccessful()) {
			MMORayTraceResult result = MMOLib.plugin.getVersion().getWrapper().rayTrace(data.getPlayer(), data.getPlayer().getEyeLocation().getDirection(), range);
			if (result == null)
				abort(CancelReason.OTHER);
			else
				target = (LivingEntity) result.getHit();
		}
	}

	public LivingEntity getTarget() {
		return target;
	}
}
