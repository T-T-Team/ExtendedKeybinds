package tnt.tarkovcraft.extendedkeybinds.input;

public interface TickableKeyMapping {

    void extendedkeybinds$preTick(long now);

    void extendedkeybinds$postTick();
}
