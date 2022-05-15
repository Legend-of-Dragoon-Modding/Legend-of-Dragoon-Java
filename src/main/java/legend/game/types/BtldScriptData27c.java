package legend.game.types;

import legend.core.memory.Value;
import legend.core.memory.types.IntRef;
import legend.core.memory.types.MemoryRef;
import legend.core.memory.types.Pointer;
import legend.core.memory.types.ShortRef;
import legend.core.memory.types.UnsignedByteRef;
import legend.core.memory.types.UnsignedIntRef;
import legend.core.memory.types.UnsignedShortRef;

public class BtldScriptData27c implements MemoryRef {
  private final Value ref;

  public final UnsignedIntRef _00;

  public final Pointer<BattleStruct1a8> _144;
  public final Value _148; //TODO

  public final IntRef _174;
  public final IntRef _178;
  public final IntRef _17c;

  public final UnsignedShortRef _1bc;
  public final UnsignedShortRef _1be;
  public final UnsignedShortRef _1c0;

  public final UnsignedByteRef _1e5;

  public final ShortRef _26c;
  public final UnsignedShortRef _26e;
  public final ShortRef _270;
  public final ShortRef _272;
  public final UnsignedShortRef _274;
  public final UnsignedShortRef _276;
  public final UnsignedByteRef _278;

  public BtldScriptData27c(final Value ref) {
    this.ref = ref;

    this._00 = ref.offset(4, 0x00L).cast(UnsignedIntRef::new);

    this._144 = ref.offset(4, 0x144L).cast(Pointer.deferred(4, BattleStruct1a8::new));
    this._148 = ref.offset(4, 0x148L);

    this._174 = ref.offset(4, 0x174L).cast(IntRef::new);
    this._178 = ref.offset(4, 0x178L).cast(IntRef::new);
    this._17c = ref.offset(4, 0x17cL).cast(IntRef::new);

    this._1bc = ref.offset(2, 0x1bcL).cast(UnsignedShortRef::new);
    this._1be = ref.offset(2, 0x1beL).cast(UnsignedShortRef::new);
    this._1c0 = ref.offset(2, 0x1c0L).cast(UnsignedShortRef::new);

    this._1e5 = ref.offset(1, 0x1e5L).cast(UnsignedByteRef::new);

    this._26c = ref.offset(2, 0x26cL).cast(ShortRef::new);
    this._26e = ref.offset(2, 0x26eL).cast(UnsignedShortRef::new);
    this._270 = ref.offset(2, 0x270L).cast(ShortRef::new);
    this._272 = ref.offset(2, 0x272L).cast(ShortRef::new);
    this._274 = ref.offset(2, 0x274L).cast(UnsignedShortRef::new);
    this._276 = ref.offset(2, 0x276L).cast(UnsignedShortRef::new);
    this._278 = ref.offset(1, 0x278L).cast(UnsignedByteRef::new);
  }

  @Override
  public long getAddress() {
    return this.ref.getAddress();
  }
}
