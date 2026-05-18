package fr.univartois.butinfo.ihm;

public enum TileContent {

    PATH("path") {
        @Override
        public boolean isEmpty() {
            return true;
        }
    },

    WALL("wall") {
        @Override
        public boolean isEmpty() {
            return false;
        }
    };

    private final String name;

    TileContent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean isEmpty();
}
