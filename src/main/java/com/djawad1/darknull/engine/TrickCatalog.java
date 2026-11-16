package com.djawad1.darknull.engine;

import java.util.List;
import java.util.Optional;

public final class TrickCatalog {
    private TrickCatalog() { }

    private static final List<TrickDefinition> TRICKS = List.of(
            new TrickDefinition(1, "Infinite Staircase", "Climb a built staircase; crossing its upper threshold loops you to its foot."),
            new TrickDefinition(2, "Endless Corridor", "Walk through a built corridor; its far end folds back to the entrance."),
            new TrickDefinition(3, "Lying Door", "Cross the open doorway and return to the same furnished room with changed details."),
            new TrickDefinition(4, "Bigger on the Inside", "A small exterior doorway folds into a hall many times larger."),
            new TrickDefinition(5, "Infinite Room", "Four exits lead back into the same room, with small changes after each loop."),
            new TrickDefinition(6, "Mirror Room", "A mannequin reflection mirrors your movement, then stops and turns toward you."),
            new TrickDefinition(7, "Weeping Blocks", "Stone figures move one block closer whenever they leave your field of view."),
            new TrickDefinition(8, "Shrinking Room", "Concentric walls close in over time, stopping before they can crush the player."),
            new TrickDefinition(9, "Reverse Gravity", "A ceiling-side traversal room uses a raised inverted-looking route; vanilla gravity remains unchanged."),
            new TrickDefinition(10, "Shifting Maze", "A generated maze rebuilds its passages when you turn sharply." )
    );

    public static List<TrickDefinition> all() { return TRICKS; }

    public static Optional<TrickDefinition> byId(int id) {
        return TRICKS.stream().filter(trick -> trick.id() == id).findFirst();
    }
}
