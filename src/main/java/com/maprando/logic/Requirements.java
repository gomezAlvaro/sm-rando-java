package com.maprando.logic;

/**
 * Factory methods for creating common requirement patterns.
 * Matches Rust MapRandomizer Requirement impl patterns.
 */
public final class Requirements {
    private Requirements() {} // Utility class

    /**
     * Requirement for morph ball capability.
     * Rust: Requirement::Item(Item::Morph)
     */
    public static Requirement canMorph() {
        return Requirement.item("MORPH_BALL");
    }

    /**
     * Requirement for bomb placement capability.
     * Rust: Requirement::make_and(vec![Item::Morph, Item::Bombs])
     */
    public static Requirement canPlaceBombs() {
        return Requirement.and(
            Requirement.item("MORPH_BALL"),
            Requirement.item("BOMBS")
        );
    }

    /**
     * Requirement for power bomb usage.
     * Rust: Requirement::make_and(vec![Item::Morph, PowerBombs(1)])
     */
    public static Requirement canUsePowerBombs() {
        return Requirement.and(
            Requirement.item("MORPH_BALL"),
            Requirement.powerBombs(1)
        );
    }

    /**
     * Requirement for surviving heat.
     * Rust: Requirement::make_or(vec![Item::Varia, Item::Gravity])
     */
    public static Requirement canSurviveHeat() {
        return Requirement.or(
            Requirement.item("VARIA_SUIT"),
            Requirement.item("GRAVITY_SUIT")
        );
    }

    /**
     * Requirement for surviving lava.
     * Rust: Requirement::Item(Item::Gravity)
     */
    public static Requirement canSurviveLava() {
        return Requirement.item("GRAVITY_SUIT");
    }

    /**
     * Requirement for grapple beam usage.
     */
    public static Requirement canGrapple() {
        return Requirement.item("GRAPPLE_BEAM");
    }

    /**
     * Requirement for ice beam.
     */
    public static Requirement hasIceBeam() {
        return Requirement.item("ICE_BEAM");
    }

    /**
     * Requirement for speed booster.
     */
    public static Requirement hasSpeedBooster() {
        return Requirement.item("SPEED_BOOSTER");
    }

    /**
     * Requirement for space jump.
     */
    public static Requirement hasSpaceJump() {
        return Requirement.item("SPACE_JUMP");
    }

    /**
     * Requirement for screw attack.
     */
    public static Requirement hasScrewAttack() {
        return Requirement.item("SCREW_ATTACK");
    }

    /**
     * Requirement for collecting a missile tank.
     * Rust: Item::Missile
     */
    public static Requirement canCollectMissile() {
        return Requirement.free(); // Missiles are always collectible
    }

    /**
     * Requirement for collecting a super missile tank.
     * Rust: Item::Super
     */
    public static Requirement canCollectSuperMissile() {
        return Requirement.free(); // Super missiles are always collectible
    }

    /**
     * Requirement for collecting a power bomb tank.
     * Rust: Requirement::make_and(vec![Item::Morph, PowerBombs(1)])
     */
    public static Requirement canCollectPowerBomb() {
        return Requirement.and(
            Requirement.item("MORPH_BALL"),
            Requirement.powerBombs(1)
        );
    }

    /**
     * Requirement for collecting an energy tank.
     * Rust: Item::ETank
     */
    public static Requirement canCollectEnergyTank() {
        return Requirement.free(); // Energy tanks are always collectible
    }

    /**
     * Example complex requirement: Yellow door with heat frames.
     * Rust: Requirement::make_and(vec![
     *     Requirement::Item(Item::Morph),
     *     Requirement::PowerBombs(1),
     *     Requirement::HeatFrames(110),
     * ])
     */
    public static Requirement yellowDoorWithHeat() {
        return Requirement.and(
            Requirement.item("MORPH_BALL"),
            Requirement.powerBombs(1),
            Requirement.heatFrames(110)
        );
    }

    /**
     * Example complex requirement: Green door (requires Grapple or Speed booster).
     */
    public static Requirement greenDoor() {
        return Requirement.or(
            Requirement.item("GRAPPLE_BEAM"),
            Requirement.item("SPEED_BOOSTER")
        );
    }

    /**
     * Example complex requirement: Red door (requires any beam + supers).
     */
    public static Requirement redDoor() {
        return Requirement.and(
            Requirement.or(
                Requirement.item("ICE_BEAM"),
                Requirement.item("WAVE_BEAM"),
                Requirement.item("Spazer"),
                Requirement.item("PLASMA_BEAM")
            ),
            Requirement.supers(1)
        );
    }
}
