package legend.game.types;

import legend.core.memory.Value;
import legend.core.memory.types.MemoryRef;
import legend.core.memory.types.Pointer;
import legend.core.memory.types.UnsignedByteRef;

public class MenuStruct08 implements MemoryRef {
  private final Value ref;

  public final Pointer<LodString> text_00;
  public final UnsignedByteRef _04;

  public MenuStruct08(final Value ref) {
    this.ref = ref;

    this.text_00 = ref.offset(4, 0x0L).cast(Pointer.deferred(4, LodString::new));
    this._04 = ref.offset(1, 0x4L).cast(UnsignedByteRef::new);
  }

  @Override
  public long getAddress() {
    return this.ref.getAddress();
  }
}
