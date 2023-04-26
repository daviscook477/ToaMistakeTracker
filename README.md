# Cox Mistake Tracker

Tracks mistakes made by players throughout the Chambers of Xeric.

---
This plugin will track mistakes for you and your teammates in the Chambers of Xeric.

By default, when detecting a mistake, all players with this plugin will receive a public message of the mistake, a chat
overhead above the player who made the mistake, and the mistake will be added to the Cox Mistake Tracker side-panel.
Additionally, some mistakes are considered team mistakes, when these ones happen, players will still receive a public
message of the mistake, but the chat will be overhead the npc rather than any player, and the mistake will be added to
a team mistake tracking section of the Cox Mistake Tracker side-panel.

Individual mistakes being tracked:

* ![death](src/main/resources/com/coxmistaketracker/death.png) **Deaths** throughout the raid
    * ![death-ice-demon](src/main/resources/com/coxmistaketracker/death-ice-demon.png) **Death in Ice Demon**
    * ![death-crabs](src/main/resources/com/coxmistaketracker/death-crabs.png) **Death in Crabs** - Yes it's possible!
    * ![death-shamans](src/main/resources/com/coxmistaketracker/death-shamans.png) **Death in Shamans**
    * ![death-muttadiles](src/main/resources/com/coxmistaketracker/death-muttadiles.png) **Death in Muttadiles**
    * ![death-mystics](src/main/resources/com/coxmistaketracker/death-mystics.png) **Death in Mystics**
    * ![death-tekton](src/main/resources/com/coxmistaketracker/death-tekton.png) **Death in Tekton**
    * ![death-vanguards](src/main/resources/com/coxmistaketracker/death-vanguards.png) **Death in Vanguards**
    * ![death-vasa](src/main/resources/com/coxmistaketracker/death-vasa.png) **Death in Vasa**
    * ![death-vespula](src/main/resources/com/coxmistaketracker/death-vasa.png) **Death in Vasa**
    * ![death-guardians](src/main/resources/com/coxmistaketracker/death-guardians.png) **Death in Guardians**
    * ![death-tightrope](src/main/resources/com/coxmistaketracker/death-tightrope.png) **Death in Tightrope**
    * ![death-olm](src/main/resources/com/coxmistaketracker/death-olm.png) **Death in Olm**
* ![shamans-blob](src/main/resources/com/coxmistaketracker/shamans-blob.png) **Shamans Poison Blob** damage
* ![shamans-spawn](src/main/resources/com/coxmistaketracker/shamans-spawn.png) **Shamans Spawn Explosion** damage
* ![tekton-sparks](src/main/resources/com/coxmistaketracker/death.png) **Tekton Sparks** damage (the ones that have to be dodged when tekton's at the anvil)
* ![tekton-melee](src/main/resources/com/coxmistaketracker/tekton-melee.png) **Tekton Melee Hit** damage (w/o vengeance)
* ![ice-demon-ranged](src/main/resources/com/coxmistaketracker/ice-demon-boulder.png) **Ice Demon Ranged Hit** damage (w/o vengeance)
* ![ice-demon-mage](src/main/resources/com/coxmistaketracker/ice-demon-mage.png) **Ice Demon Mage Hit** damage (w/o vengeance)
* ![vasa-boulder](src/main/resources/com/coxmistaketracker/vasa-boulder.png) **Vasa Boulder Hit** damage (w/o vengeance)
* ![guardians-boulder](src/main/resources/com/coxmistaketracker/guardians-boulder.png) **Guardians Boulder Hit** damage
* ![olm-acid](src/main/resources/com/coxmistaketracker/olm-acid.png) **Olm Acid Standstill** damage (taking damage from the same acid pool for more than 1 tick)
* ![olm-falling-crystals](src/main/resources/com/coxmistaketracker/death.png) **Olm Falling Crystal** damage (either during the individual attack or in phase transitions)
* ![olm-prayer-orb](src/main/resources/com/coxmistaketracker/prayer-orb.png) **Olm Prayer Orb** damage
* ![olm-crystal-burst](src/main/resources/com/coxmistaketracker/death.png) **Olm Crystal Burst** damage (watch your feet!)
* ![olm-lightning-damage](src/main/resources/com/coxmistaketracker/death.png) **Olm Lightning** damage
* ![olm-teleport-damage](src/main/resources/com/coxmistaketracker/death.png) **Olm Teleports** damage
* ![olm-crystal-bomb](src/main/resources/com/coxmistaketracker/death.png) **Olm Crystal Bomb** damage (only counting the tile the bomb is on and next to as mistakes - otherwise it's fine to tank some bomb damage to maintain dps?)
* ![olm-left-claw-heal](src/main/resources/com/coxmistaketracker/left-claw-heal.png) **Olm Left Claw Heal** (someone hit the claw during healing special)
* ![olm-flame-wall](src/main/resources/com/coxmistaketracker/death.png) **Olm Flame Wall** damage
* ![olm-auto-mispray](src/main/resources/com/coxmistaketracker/death.png) **Olm Auto** mispray (note that this mistake is based off of what overhead should've been prayed not what overhead actually would've protected against the attack)
  * ex. the olm's first attack is with mage. having up no prot prayer or pray melee is a mistake here. 
  * ex cont. the olm's second attack is with ranged. having up anything other than pray mage is a mistake here (despite the fact that pray mage does not protect against this attack).
  * ex cont. the olm's third attack is with ranged. having up anything other than pray range is a mistake here.

Team mistakes being tracked:
* ![muttadiles-heal](src/main/resources/com/coxmistaketracker/muttadiles-heal.png) **Muttadiles Heal** (at the meat tree)
* ![vanguards-reset](src/main/resources/com/coxmistaketracker/vanguards-reset.png) **Vanguards Reset**
* ![vespula-lux-grub-hatched](src/main/resources/com/coxmistaketracker/lux-grub-hatched.png) **Vespula Lux Grub Hatched**
* ![vespula-lux-grub-hit](src/main/resources/com/coxmistaketracker/lux-grub-hit.png) **Vespula Lux Grub Hit**
* ![olm-center](src/main/resources/com/coxmistaketracker/death.png) **Olm Centers** (the olm faces the center of the room - disabled in solos)
* ![olm-claw-reset](src/main/resources/com/coxmistaketracker/claw-reset.png) **Olm Claw Reset** (both claws were not killed sufficiently close together in time)
* Whenever any olm special attack occurs. Notably, this isn't always a mistake for all parts of the fight/for all strategies. But it is still tracked either way.


* **Olm crystal burst** damage 1338gfx 30034o

* **Olm teleports** damage 
* **Olm burn** spread 1349p
* **Olm fire wall** damage 26209o
* **Olm falling crystal** (Crystal Phase) damage
* falling crystal attack 1353gfx 1447


* **Lizardman Shamans spawn** damage

* **Tekton** trap underneath -> not doing this
* **Vanguards** off-prayer damage "I'm getting stacked out" -> not doing this


Need to check
* What happens when trasmoging the projectiles to toa ones? Is the prayer orb projectile id maintained?
* does the olm get a different set of animation ids when it's glowing?

* I really need to setup a way to track team mistakes since the following mistakes shouldn't be for individual players
* I don't think the guardians boulder is registering on the right tick. wait no I think I'm just an idiot and made is a 3x3
  need to check to see whether it really is a 3x3 or if it's only the one tile. it really is a 3x3 it just registers a tick later

* why isn't the teleport damage mistake being tracked right?
* what is the vespula grub sting animation id? is it unique to the grub stinging -> yes it worked!!!
* flame wall detection logic (some number of ticks) 5809 appeared 5816 disappeared
* olm auto mistake detection
* **Olm standard attack** non-jad prayer miss (no announcement) 1339p mage - 1340p range
* shaman barney 1295 gfx obj it really is in a 5x5 around them not just a 3x3 -> yes it really is.
* ok the vespula grub sting animation is not sufficient. it *can* do that animation after stinging.
* I should check for the combination of vespula sting + grub hp ratio b/c just the sting animation is not enough.


Team mistakes:
* muttadile heal (big and small)
* vanguards reset
* olm center
* olm special occurs
* olm hand reset

For team mistakes I really want to have the chat message appear over the enemies rather than every individual team member.


Are these done correctly?
* **Olm melee hand** heal 29887ID -> doesn't seem like the tracker I was doing there worked
  going to need to figure out a different way to determine which player is healing the hand
* **Olm mage hand** reset 29884id
* **Olm melee hand** reset



1114a

Finished:
* **Olm crystal burst** occurs - not always a mistake but worth tracking - counted by phase (ex. 1, 2, 3, head for <8 players) 30033o (create) 30334o (burst) 1114a for the player
* **Olm lightning** occurs - not always a mistake but worth tracking - counted by phase (ex. 1, 2, 3, head for <8 players) 1356gfx
* **Olm teleports** occurs - not always a mistake but worth tracking - counted by phase (ex. 1, 2, 3, head for <8 players) 1359gfx
* **Tekton spark** damage - 659gfx
* **Olm head turn** center - disabled in solo raids a7342 a7340 for the head
* **Olm lightning** damage
* **Olm acid pools** damage 30032 object
* **Ice Demon attack** damage ranged 1324p mage 366p ice demon boulder gfx 1325 ice demon ice gfx 363
* **Vasa attack** damage 1329p 1330gfx
* **Guardians boulder** damage 645p 305gfx
* **Olm falling crystal** (Transition Phase) damage 1357 1358 1447gfx 1357gfx / 1358gfx falling crystal beetween first phases
* **Olm falling crystal** (Head Phase) damage 1353gfx falling crystal attack
* **Lizardman Shamans poison blob** damage 1293p
* **Muttadiles baby** reset
* **Muttadiles mother** reset
* Does the logic for only counting ice demon/vasa mistakes if the hit is > 0 work? yes!!
* **Olm crystal bomb** damage 29766 40gfx
* Does the olm falling crystal have some gfx appear on the tick it deals damage? yes -> great use that, no -> probably use the projectile id
* What is the falling crystal projectile id vs gfx id
* Why isn't the crystal burst detection working? -> the gfx appears after the player has already been pushed to a different tile so now I just detect it with the animation change
* **Vespula soldier** spawn -> had to do this based on the lux grub animation
* **Vanguards** reset
* **Olm prayer orb** damage 1343p ranged  1341p mage 1345p melee

Issues: Taking damage is sometimes not a mistake (though I think this is exclusively dependent on whether or not vengeance is up)

Mistakes that I would like to track:
* Off-prayer unavoidable damage w/o vengeance up (ex. not praying mage against mystics)
* Wrong color orbs hitting crystals in crabs / just using extra orbs in crabs (more than 5 - e.g. 4 crystals + 1 missed on entry)

---

## Screenshots

![panel-example](src/main/resources/com/coxmistaketracker/panel-example.png)

![death-example](src/main/resources/com/coxmistaketracker/death-example.png)

![crondis-example](src/main/resources/com/coxmistaketracker/crondis-example.png)

---

## Changes

#### 1.0

* Initial release