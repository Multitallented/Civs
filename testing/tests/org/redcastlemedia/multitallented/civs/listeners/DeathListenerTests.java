package org.redcastlemedia.multitallented.civs.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianTests;
import org.redcastlemedia.multitallented.civs.protections.DeathListener;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeathListenerTests {

    private EntityDamageByEntityEvent damageEvent;
    private Player player1;
    private Player player2;
    private DeathListener deathListener;
    private EntityDamageEvent dEvent;
    private Civilian civilian1;
    private Player player3;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        new RegionManager();
        new CivilianManager();
        this.player1 = mock(Player.class);
        when(player1.getUniqueId()).thenReturn(new UUID(1, 3));
        this.player2 = mock(Player.class);
        when(player2.getUniqueId()).thenReturn(new UUID(1, 4));
        this.player3 = mock(Player.class);
        when(player3.getUniqueId()).thenReturn(new UUID(1, 5));
        this.damageEvent = new EntityDamageByEntityEvent(player2, player1, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 2);

        this.dEvent = new EntityDamageEvent(player1, EntityDamageEvent.DamageCause.LAVA, 1);
        CivilianTests.loadCivilian(player1);
        CivilianTests.loadCivilian(player2);
        this.civilian1 = CivilianManager.getInstance().getCivilian(player1.getUniqueId());
        this.deathListener = new DeathListener();
    }

    @Test
    public void damageShouldRegisterInCivilian() {
        this.deathListener.onEntityDamage(this.damageEvent);
        assertEquals(player2.getUniqueId(), civilian1.getLastDamager());
        assertTrue(civilian1.getLastDamage() > -1);
    }

    @Test
    public void damageShouldNotRegisterFromNonPlayer() {
        this.deathListener.onEntityDamage(this.dEvent);
        assertTrue(civilian1.getLastDamage() < 0);
        assertNull(civilian1.getLastDamager());
    }

    @Test
    public void lastDamageShouldUpdateWhenDamagerNotNull() {
        this.civilian1.setLastDamager(player2.getUniqueId());
        long prevTime = System.currentTimeMillis() - 600;
        this.civilian1.setLastDamage(prevTime);
        this.deathListener.onEntityDamage(this.dEvent);
        assertNotEquals(prevTime, this.civilian1.getLastDamage());
    }

    @Test
    public void lastDamageShouldNotUpdateOutOfCombatAndNullDamager() {
        this.civilian1.setLastDamager(player2.getUniqueId());
        long prevTime = System.currentTimeMillis() - 60001;
        this.civilian1.setLastDamage(prevTime);
        this.deathListener.onEntityDamage(this.dEvent);
        assertEquals(-1, this.civilian1.getLastDamage());
        assertNull(this.civilian1.getLastDamager());
    }

    @Test
    public void lastDamageShouldUpdate() {
        this.civilian1.setLastDamager(player3.getUniqueId());
        long prevTime = System.currentTimeMillis() - 3000;
        this.civilian1.setLastDamage(prevTime);
        this.deathListener.onEntityDamage(this.damageEvent);
        assertEquals(player2.getUniqueId(), this.civilian1.getLastDamager());
        assertNotEquals(prevTime, this.civilian1.getLastDamage());
        assertNotEquals(-1, this.civilian1.getLastDamage());
    }
}
