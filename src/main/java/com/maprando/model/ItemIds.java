package com.maprando.model;

/**
 * Centralized constants for item and tech IDs.
 * Using these constants helps avoid typos and makes refactoring easier.
 */
public final class ItemIds {
    private ItemIds() {
        // Utility class - prevent instantiation
    }

    // ========== BEAMS ==========
    /** Charge Beam - Increases shot damage multiplier */
    public static final String CHARGE_BEAM = "CHARGE_BEAM";

    /** Ice Beam - Adds damage bonus, freezes enemies */
    public static final String ICE_BEAM = "ICE_BEAM";

    /** Wave Beam - Adds damage bonus, pierces walls */
    public static final String WAVE_BEAM = "WAVE_BEAM";

    /** Spazer Beam - Damage multiplier, mutually exclusive with Plasma */
    public static final String SPAZER_BEAM = "SPAZER_BEAM";

    /** Plasma Beam - Damage multiplier, pierces enemies */
    public static final String PLASMA_BEAM = "PLASMA_BEAM";

    // ========== MORPH BALL ABILITIES ==========
    /** Morph Ball - Enables rolling into ball */
    public static final String MORPH_BALL = "MORPH_BALL";

    /** Bomb - Can place bombs in morph ball */
    public static final String BOMB = "BOMB";

    /** Spring Ball - Can jump in morph ball */
    public static final String SPRING_BALL = "SPRING_BALL";

    /** Power Bomb - Can use power bombs in morph ball */
    public static final String POWER_BOMB = "POWER_BOMB";

    // ========== MOVEMENT ==========
    /** Hi-Jump Boots - Increases jump height */
    public static final String HI_JUMP_BOOTS = "HI_JUMP_BOOTS";

    /** Speed Booster - Enables speed booster and shinespark */
    public static final String SPEED_BOOSTER = "SPEED_BOOSTER";

    /** Space Jump - Infinite mid-air jumps */
    public static final String SPACE_JUMP = "SPACE_JUMP";

    /** Screw Attack - Jumping damages enemies */
    public static final String SCREW_ATTACK = "SCREW_ATTACK";

    // ========== SUITS ==========
    /** Varia Suit - Reduces damage taken by 50% */
    public static final String VARIA_SUIT = "VARIA_SUIT";

    /** Gravity Suit - Reduces damage taken by 75%, enables lava swimming */
    public static final String GRAVITY_SUIT = "GRAVITY_SUIT";

    // ========== UTILITY ==========
    /** X-Ray Scope - See hidden blocks */
    public static final String XRAY_SCOPE = "XRAY_SCOPE";

    /** Grapple Beam - Can grapple across ceilings */
    public static final String GRAPPLE_BEAM = "GRAPPLE_BEAM";

    // ========== TANKS ==========
    /** Missile Tank - Increases missile capacity by 5 */
    public static final String MISSILE_TANK = "MISSILE_TANK";

    /** Super Missile Tank - Increases super missile capacity by 5 */
    public static final String SUPER_MISSILE_TANK = "SUPER_MISSILE_TANK";

    /** Power Bomb Tank - Increases power bomb capacity by 5 */
    public static final String POWER_BOMB_TANK = "POWER_BOMB_TANK";

    /** Energy Tank - Increases energy capacity by 100 */
    public static final String ENERGY_TANK = "ENERGY_TANK";

    // ========== TECH IDs ==========
    /** Can roll into morph ball */
    public static final String TECH_CAN_MORPH = "can_morph";

    /** Can fit through small passages in morph ball */
    public static final String TECH_CAN_FIT_SMALL_SPACES = "can_fit_small_spaces";

    /** Can place bombs while in morph ball */
    public static final String TECH_CAN_PLACE_BOMBS = "can_place_bombs";

    /** Can destroy weak walls with bombs */
    public static final String TECH_CAN_BOMB_WEAK_WALLS = "can_bomb_weak_walls";

    /** Can use power bombs in morph ball */
    public static final String TECH_CAN_USE_POWER_BOMBS = "can_use_power_bombs";

    /** Can execute shinespark after building speed */
    public static final String TECH_CAN_SHINESPARK = "can_shinespark";

    /** Can build up speed by running */
    public static final String TECH_CAN_SPEED_BOOSTER = "can_speed_booster";

    /** Can use grapple beam to swing */
    public static final String TECH_CAN_GRAPPLE = "can_grapple";

    /** Can move through lava without damage */
    public static final String TECH_CAN_SWIM_LAVA = "can_swim_lava";

    /** Can move normally underwater */
    public static final String TECH_CAN_MOVE_UNDERWATER = "can_move_underwater";
}
