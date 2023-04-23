# Cox Mistake Tracker

Tracks mistakes made by players throughout the Chambers of Xeric.

---
This plugin will track mistakes for you and your teammates in the Chambers of Xeric.

By default, when detecting a mistake, all players with this plugin will receive a public message of the mistake, a chat
overhead above the player who made the mistake, and the mistake will be added to the Cox Mistake Tracker side-panel.

Current mistakes being tracked:

* ![death](src/main/resources/com/coxmistaketracker/death.png) **Deaths** throughout the raid
    * ![death-combat-puzzle](src/main/resources/com/coxmistaketracker/death-akkha.png) **Death in a Combat/Puzzle Room**
    * ![death-zebak](src/main/resources/com/coxmistaketracker/death-zebak.png) **Death in Olm**
* **Olm standard attack** non-jad prayer miss (no announcement) 1339p mage - 1340p range
* **Olm prayer orb** damage 1343p ranged  1341p mage 1345p melee

* **Olm crystal burst** damage 1338gfx 30034o

* **Olm teleports** damage 
* **Olm burn** spread 1349p
* **Olm fire wall** damage 26209o
* **Olm falling crystal** (Crystal Phase) damage
* falling crystal attack 1353gfx 1447

* **Lizardman Shamans poison blob** damage 1293p
* **Lizardman Shamans spawn** damage
* **Muttadiles baby** reset
* **Muttadiles mother** reset
* **Tekton** trap underneath
* **Vanguards** off-prayer damage "I'm getting stacked out"
* **Vanguards** reset
* **Vespula soldier** spawn


Need to check
* What happens when trasmoging the projectiles to toa ones? Is the prayer orb projectile id maintained?
* does the olm get a different set of animation ids when it's glowing?
* Shamans goo gfx
* Does the olm falling crystal have some gfx appear on the tick it deals damage? yes -> great use that, no -> probably use the projectile id
* What is the falling crystal projectile id vs gfx id
* Is there really nothing that can tell me the player got teleported animation/gfx-wise? I don't think there's a difference between teleporting 2 tiles
  versus just moving the 2 tiles on the same tick teleports occurs (unless the game prevents you from moving on the tick there's teleports in which case
* we could use any player movement on that tick as an indicator that they messed up)
* Why isn't the crystal burst detection working?
* What about getting a list of NPC ids so I can properly detect rooms


Are these done correctly?
* **Olm melee hand** heal 29887ID
* **Olm mage hand** reset 29884id
* **Olm melee hand** reset
* **Olm crystal bomb** damage 29766 40gfx




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


Issues: Taking damage is sometimes not a mistake (though I think this is exclusively dependent on whether or not vengeance is up)

Mistakes that I would like to track:
* Off-prayer unavoidable damage w/o vengeance up (ex. not praying mage against mystics)
* Avoidable damage w/o vengeance up (ex. vasa projectile)
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