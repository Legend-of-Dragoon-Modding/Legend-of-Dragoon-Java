package legend.game;

import legend.core.DebugHelper;
import legend.core.cdrom.CdlFILE;
import legend.core.cdrom.FileLoadingInfo;
import legend.core.cdrom.SyncCode;
import legend.core.gpu.RECT;
import legend.core.gpu.TimHeader;
import legend.core.memory.Method;
import legend.core.memory.Value;
import legend.core.memory.types.ArrayRef;
import legend.core.memory.types.BiFunctionRef;
import legend.core.memory.types.BoolRef;
import legend.core.memory.types.CString;
import legend.core.memory.types.ConsumerRef;
import legend.core.memory.types.MemoryRef;
import legend.core.memory.types.Pointer;
import legend.core.memory.types.RunnableRef;
import legend.core.memory.types.TriConsumerRef;
import legend.core.memory.types.TriFunctionRef;
import legend.core.memory.types.UnboundedArrayRef;
import legend.core.memory.types.UnsignedIntRef;
import legend.game.types.BattleStruct;
import legend.game.types.BigStruct;
import legend.game.types.ExtendedTmd;
import legend.game.types.GsOT_TAG;
import legend.game.types.MrgEntry;
import legend.game.types.MrgFile;
import legend.game.types.RunningScript;
import legend.game.types.ScriptFile;
import legend.game.types.ScriptState;
import legend.game.types.SoundFile;
import legend.game.types.SpuStruct28;
import legend.game.types.SshdFile;
import legend.game.types.SssqFile;
import legend.game.types.TmdAnimationFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Function;

import static legend.core.Hardware.CDROM;
import static legend.core.Hardware.GPU;
import static legend.core.Hardware.MEMORY;
import static legend.core.Hardware.SPU;
import static legend.core.MemoryHelper.getMethodAddress;
import static legend.game.Bttl.FUN_800c7304;
import static legend.game.Bttl.FUN_800c882c;
import static legend.game.Bttl.FUN_800c8cf0;
import static legend.game.Bttl.FUN_800c90b0;
import static legend.game.Bttl.FUN_800d8f10;
import static legend.game.SBtld._80109a98;
import static legend.game.SInit.FUN_800fbec8;
import static legend.game.SMap.FUN_800edb8c;
import static legend.game.SMap.mrg10Addr_800c6710;
import static legend.game.Scus94491.decompress;
import static legend.game.Scus94491BpeSegment_8002.FUN_800201c8;
import static legend.game.Scus94491BpeSegment_8002.FUN_80020360;
import static legend.game.Scus94491BpeSegment_8002.FUN_80020ed8;
import static legend.game.Scus94491BpeSegment_8002.FUN_80022518;
import static legend.game.Scus94491BpeSegment_8002.FUN_8002a058;
import static legend.game.Scus94491BpeSegment_8002.FUN_8002a0e4;
import static legend.game.Scus94491BpeSegment_8002.FUN_8002ae0c;
import static legend.game.Scus94491BpeSegment_8002.FUN_8002bb38;
import static legend.game.Scus94491BpeSegment_8002.FUN_8002c0c8;
import static legend.game.Scus94491BpeSegment_8002.FUN_8002c86c;
import static legend.game.Scus94491BpeSegment_8002.SquareRoot0;
import static legend.game.Scus94491BpeSegment_8002.rand;
import static legend.game.Scus94491BpeSegment_8002.sssqResetStuff;
import static legend.game.Scus94491BpeSegment_8003.ClearImage;
import static legend.game.Scus94491BpeSegment_8003.DrawSync;
import static legend.game.Scus94491BpeSegment_8003.FUN_80036674;
import static legend.game.Scus94491BpeSegment_8003.FUN_80036f20;
import static legend.game.Scus94491BpeSegment_8003.FUN_8003b0d0;
import static legend.game.Scus94491BpeSegment_8003.FUN_8003b450;
import static legend.game.Scus94491BpeSegment_8003.FUN_8003c5e0;
import static legend.game.Scus94491BpeSegment_8003.GsClearOt;
import static legend.game.Scus94491BpeSegment_8003.GsDefDispBuff;
import static legend.game.Scus94491BpeSegment_8003.GsInitGraph;
import static legend.game.Scus94491BpeSegment_8003.GsSortClear;
import static legend.game.Scus94491BpeSegment_8003.GsSwapDispBuff;
import static legend.game.Scus94491BpeSegment_8003.LoadImage;
import static legend.game.Scus94491BpeSegment_8003.SetDispMask;
import static legend.game.Scus94491BpeSegment_8003.VSync;
import static legend.game.Scus94491BpeSegment_8003.beginCdromTransfer;
import static legend.game.Scus94491BpeSegment_8003.drawOTag;
import static legend.game.Scus94491BpeSegment_8003.gpuLinkedListSetCommandTransparency;
import static legend.game.Scus94491BpeSegment_8003.parseTimHeader;
import static legend.game.Scus94491BpeSegment_8003.resetDmaTransfer;
import static legend.game.Scus94491BpeSegment_8003.setProjectionPlaneDistance;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004c1f8;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004c390;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004c3f0;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004c8dc;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004cb0c;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004cf8c;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004d034;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004d41c;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004d52c;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004d648;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004d78c;
import static legend.game.Scus94491BpeSegment_8004.FUN_8004d91c;
import static legend.game.Scus94491BpeSegment_8004.SsSetRVol;
import static legend.game.Scus94491BpeSegment_8004._8004db88;
import static legend.game.Scus94491BpeSegment_8004._8004dd00;
import static legend.game.Scus94491BpeSegment_8004._8004dd04;
import static legend.game.Scus94491BpeSegment_8004._8004dd0c;
import static legend.game.Scus94491BpeSegment_8004._8004dd10;
import static legend.game.Scus94491BpeSegment_8004._8004dd14;
import static legend.game.Scus94491BpeSegment_8004._8004dd18;
import static legend.game.Scus94491BpeSegment_8004._8004dd1c;
import static legend.game.Scus94491BpeSegment_8004._8004dd24;
import static legend.game.Scus94491BpeSegment_8004._8004dd28;
import static legend.game.Scus94491BpeSegment_8004._8004dd38;
import static legend.game.Scus94491BpeSegment_8004._8004dd44;
import static legend.game.Scus94491BpeSegment_8004._8004dd48;
import static legend.game.Scus94491BpeSegment_8004._8004dda0;
import static legend.game.Scus94491BpeSegment_8004._8004ddcc;
import static legend.game.Scus94491BpeSegment_8004._8004ddd0;
import static legend.game.Scus94491BpeSegment_8004._8004ddd4;
import static legend.game.Scus94491BpeSegment_8004._8004ddd8;
import static legend.game.Scus94491BpeSegment_8004._8004f5d4;
import static legend.game.Scus94491BpeSegment_8004._8004f65c;
import static legend.game.Scus94491BpeSegment_8004._8004f664;
import static legend.game.Scus94491BpeSegment_8004._8004f698;
import static legend.game.Scus94491BpeSegment_8004._8004f6a4;
import static legend.game.Scus94491BpeSegment_8004._8004f6e4;
import static legend.game.Scus94491BpeSegment_8004._8004f6e8;
import static legend.game.Scus94491BpeSegment_8004._8004f6ec;
import static legend.game.Scus94491BpeSegment_8004._8004fa98;
import static legend.game.Scus94491BpeSegment_8004._8004fb00;
import static legend.game.Scus94491BpeSegment_8004._8004ff10;
import static legend.game.Scus94491BpeSegment_8004._8004ff14;
import static legend.game.Scus94491BpeSegment_8004.callbackArray_8004dddc;
import static legend.game.Scus94491BpeSegment_8004.callbackIndex_8004ddc4;
import static legend.game.Scus94491BpeSegment_8004.callback_8004dbc0;
import static legend.game.Scus94491BpeSegment_8004.fileCount_8004ddc8;
import static legend.game.Scus94491BpeSegment_8004.fileNamePtr_8004dda4;
import static legend.game.Scus94491BpeSegment_8004.initSound;
import static legend.game.Scus94491BpeSegment_8004.loadSshdAndSoundbank;
import static legend.game.Scus94491BpeSegment_8004.loadingSmapOvl_8004dd08;
import static legend.game.Scus94491BpeSegment_8004.loadingSstrmOvl_8004dd1e;
import static legend.game.Scus94491BpeSegment_8004.mainCallbackIndex_8004dd20;
import static legend.game.Scus94491BpeSegment_8004.ratan2;
import static legend.game.Scus94491BpeSegment_8004.renderFlags_8004dd36;
import static legend.game.Scus94491BpeSegment_8004.scriptFunctions_8004e098;
import static legend.game.Scus94491BpeSegment_8004.scriptPtrs_8004de58;
import static legend.game.Scus94491BpeSegment_8004.scriptStateUpperBound_8004de4c;
import static legend.game.Scus94491BpeSegment_8004.scriptSubFunctions_8004e29c;
import static legend.game.Scus94491BpeSegment_8004.setCdVolume;
import static legend.game.Scus94491BpeSegment_8004.setMainVolume;
import static legend.game.Scus94491BpeSegment_8004.setSpuDmaCompleteCallback;
import static legend.game.Scus94491BpeSegment_8004.sssqFadeIn;
import static legend.game.Scus94491BpeSegment_8004.sssqGetTempo;
import static legend.game.Scus94491BpeSegment_8004.sssqPitchShift;
import static legend.game.Scus94491BpeSegment_8004.sssqSetReverbType;
import static legend.game.Scus94491BpeSegment_8004.sssqSetTempo;
import static legend.game.Scus94491BpeSegment_8004.sssqTick;
import static legend.game.Scus94491BpeSegment_8004.sssqUnloadPlayableSound;
import static legend.game.Scus94491BpeSegment_8004.swapDisplayBuffer_8004dd40;
import static legend.game.Scus94491BpeSegment_8004.syncFrame_8004dd3c;
import static legend.game.Scus94491BpeSegment_8004.width_8004dd34;
import static legend.game.Scus94491BpeSegment_8005._80050068;
import static legend.game.Scus94491BpeSegment_8005._800500e8;
import static legend.game.Scus94491BpeSegment_8005._800500f8;
import static legend.game.Scus94491BpeSegment_8005._80050104;
import static legend.game.Scus94491BpeSegment_8005._8005019c;
import static legend.game.Scus94491BpeSegment_8005._800501bc;
import static legend.game.Scus94491BpeSegment_8005._8005a1e0;
import static legend.game.Scus94491BpeSegment_8005._8005a1e4;
import static legend.game.Scus94491BpeSegment_8005._8005a1ea;
import static legend.game.Scus94491BpeSegment_8005._8005a2a8;
import static legend.game.Scus94491BpeSegment_8005._8005a2ac;
import static legend.game.Scus94491BpeSegment_8005._8005a2b0;
import static legend.game.Scus94491BpeSegment_8005._8005a398;
import static legend.game.Scus94491BpeSegment_8005.linkedListHead_8005a2a0;
import static legend.game.Scus94491BpeSegment_8005.linkedListTail_8005a2a4;
import static legend.game.Scus94491BpeSegment_8005.orderingTables_8005a370;
import static legend.game.Scus94491BpeSegment_8005.sin_cos_80054d0c;
import static legend.game.Scus94491BpeSegment_8005.submapCut_80052c30;
import static legend.game.Scus94491BpeSegment_8007._8007a3a8;
import static legend.game.Scus94491BpeSegment_8007._8007a3ac;
import static legend.game.Scus94491BpeSegment_8007._8007a3c0;
import static legend.game.Scus94491BpeSegment_8007.joypadInput_8007a39c;
import static legend.game.Scus94491BpeSegment_8007.joypadPress_8007a398;
import static legend.game.Scus94491BpeSegment_8007.joypadRepeat_8007a3a0;
import static legend.game.Scus94491BpeSegment_8007.vsyncMode_8007a3b8;
import static legend.game.Scus94491BpeSegment_8009._8009a7c0;
import static legend.game.Scus94491BpeSegment_800b.CdlFILE_800bb4c8;
import static legend.game.Scus94491BpeSegment_800b.RunningScript_800bc070;
import static legend.game.Scus94491BpeSegment_800b.SInitBinLoaded_800bbad0;
import static legend.game.Scus94491BpeSegment_800b._800babc0;
import static legend.game.Scus94491BpeSegment_800b._800bb0fc;
import static legend.game.Scus94491BpeSegment_800b._800bb104;
import static legend.game.Scus94491BpeSegment_800b._800bb110;
import static legend.game.Scus94491BpeSegment_800b._800bb114;
import static legend.game.Scus94491BpeSegment_800b._800bb118;
import static legend.game.Scus94491BpeSegment_800b._800bb120;
import static legend.game.Scus94491BpeSegment_800b._800bb168;
import static legend.game.Scus94491BpeSegment_800b._800bb228;
import static legend.game.Scus94491BpeSegment_800b._800bb348;
import static legend.game.Scus94491BpeSegment_800b._800bc0b8;
import static legend.game.Scus94491BpeSegment_800b._800bc0b9;
import static legend.game.Scus94491BpeSegment_800b._800bc300;
import static legend.game.Scus94491BpeSegment_800b._800bc304;
import static legend.game.Scus94491BpeSegment_800b._800bc308;
import static legend.game.Scus94491BpeSegment_800b._800bc94c;
import static legend.game.Scus94491BpeSegment_800b._800bc960;
import static legend.game.Scus94491BpeSegment_800b._800bc980;
import static legend.game.Scus94491BpeSegment_800b._800bc9a8;
import static legend.game.Scus94491BpeSegment_800b._800bca68;
import static legend.game.Scus94491BpeSegment_800b._800bca6c;
import static legend.game.Scus94491BpeSegment_800b._800bd0f0;
import static legend.game.Scus94491BpeSegment_800b._800bd0fc;
import static legend.game.Scus94491BpeSegment_800b._800bd108;
import static legend.game.Scus94491BpeSegment_800b._800bd610;
import static legend.game.Scus94491BpeSegment_800b._800bd680;
import static legend.game.Scus94491BpeSegment_800b._800bd6e8;
import static legend.game.Scus94491BpeSegment_800b._800bd6f8;
import static legend.game.Scus94491BpeSegment_800b._800bd700;
import static legend.game.Scus94491BpeSegment_800b._800bd704;
import static legend.game.Scus94491BpeSegment_800b._800bd708;
import static legend.game.Scus94491BpeSegment_800b._800bd70c;
import static legend.game.Scus94491BpeSegment_800b._800bd710;
import static legend.game.Scus94491BpeSegment_800b._800bd714;
import static legend.game.Scus94491BpeSegment_800b._800bd740;
import static legend.game.Scus94491BpeSegment_800b._800bd758;
import static legend.game.Scus94491BpeSegment_800b._800bd768;
import static legend.game.Scus94491BpeSegment_800b._800bd774;
import static legend.game.Scus94491BpeSegment_800b._800bd780;
import static legend.game.Scus94491BpeSegment_800b._800bd781;
import static legend.game.Scus94491BpeSegment_800b._800bd782;
import static legend.game.Scus94491BpeSegment_800b._800bd808;
import static legend.game.Scus94491BpeSegment_800b._800bdc34;
import static legend.game.Scus94491BpeSegment_800b._800bee90;
import static legend.game.Scus94491BpeSegment_800b._800bee94;
import static legend.game.Scus94491BpeSegment_800b._800bee98;
import static legend.game.Scus94491BpeSegment_800b._800bf0cf;
import static legend.game.Scus94491BpeSegment_800b._800bf0d8;
import static legend.game.Scus94491BpeSegment_800b._800bf0e0;
import static legend.game.Scus94491BpeSegment_800b.currentlyLoadingFileInfo_800bb468;
import static legend.game.Scus94491BpeSegment_800b.doubleBufferFrame_800bb108;
import static legend.game.Scus94491BpeSegment_800b.drgnBinIndex_800bc058;
import static legend.game.Scus94491BpeSegment_800b.drgnMrg_800bc060;
import static legend.game.Scus94491BpeSegment_800b.fileLoadingInfoArray_800bbad8;
import static legend.game.Scus94491BpeSegment_800b.fileSize_800bb464;
import static legend.game.Scus94491BpeSegment_800b.fileSize_800bb48c;
import static legend.game.Scus94491BpeSegment_800b.fileTransferDest_800bb488;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.game.Scus94491BpeSegment_800b.loadedDrgnFiles_800bcf78;
import static legend.game.Scus94491BpeSegment_800b.numberOfTransfers_800bb490;
import static legend.game.Scus94491BpeSegment_800b.pregameLoadingStage_800bb10c;
import static legend.game.Scus94491BpeSegment_800b.scriptEffect_800bb140;
import static legend.game.Scus94491BpeSegment_800b.scriptStatePtrArr_800bc1c0;
import static legend.game.Scus94491BpeSegment_800b.scriptState_800bc0c0;
import static legend.game.Scus94491BpeSegment_800b.soundFileArr_800bcf80;
import static legend.game.Scus94491BpeSegment_800b.soundMrgPtr_800bd748;
import static legend.game.Scus94491BpeSegment_800b.soundMrgPtr_800bd76c;
import static legend.game.Scus94491BpeSegment_800b.soundMrgSshdPtr_800bd784;
import static legend.game.Scus94491BpeSegment_800b.soundMrgSssqPtr_800bd788;
import static legend.game.Scus94491BpeSegment_800b.soundbank_800bd778;
import static legend.game.Scus94491BpeSegment_800b.spu28Arr_800bca78;
import static legend.game.Scus94491BpeSegment_800b.spu28Arr_800bd110;
import static legend.game.Scus94491BpeSegment_800b.sssqChannelIndex_800bd0f8;
import static legend.game.Scus94491BpeSegment_800b.sssqTempoScale_800bd100;
import static legend.game.Scus94491BpeSegment_800b.sssqTempo_800bd104;
import static legend.game.Scus94491BpeSegment_800b.submapScene_800bb0f8;
import static legend.game.Scus94491BpeSegment_800b.timHeader_800bc2e0;
import static legend.game.Scus94491BpeSegment_800b.transferDest_800bb460;
import static legend.game.Scus94491BpeSegment_800b.transferIndex_800bb494;
import static legend.game.Scus94491BpeSegment_800c.DISPENV_800c34b0;
import static legend.game.Scus94491BpeSegment_800c.PSDIDX_800c34d4;
import static legend.game.Scus94491BpeSegment_800d.sceaTexture_800d05c4;

public final class Scus94491BpeSegment {
  private Scus94491BpeSegment() { }

  private static final Logger LOGGER = LogManager.getFormatterLogger(Scus94491BpeSegment.class);

  private static final Object[] EMPTY_OBJ_ARRAY = new Object[0];

  public static final BiFunctionRef<Long, Object[], Object> functionVectorA_000000a0 = MEMORY.ref(4, 0x000000a0L, BiFunctionRef::new);
  public static final BiFunctionRef<Long, Object[], Object> functionVectorB_000000b0 = MEMORY.ref(4, 0x000000b0L, BiFunctionRef::new);
  public static final BiFunctionRef<Long, Object[], Object> functionVectorC_000000c0 = MEMORY.ref(4, 0x000000c0L, BiFunctionRef::new);

  public static final Value temporaryStack_1f8003b4 = MEMORY.ref(4, 0x1f8003b4L);
  public static final Value oldStackPointer_1f8003b8 = MEMORY.ref(4, 0x1f8003b8L);
  public static final BoolRef isStackPointerModified_1f8003bc = MEMORY.ref(2, 0x1f8003bcL, BoolRef::new);
  public static final Value _1f8003c0 = MEMORY.ref(4, 0x1f8003c0L);
  public static final Value _1f8003c4 = MEMORY.ref(4, 0x1f8003c4L);
  public static final Value _1f8003c8 = MEMORY.ref(4, 0x1f8003c8L);
  public static final Value _1f8003cc = MEMORY.ref(4, 0x1f8003ccL);
  public static final Pointer<UnboundedArrayRef<GsOT_TAG>> tags_1f8003d0 = MEMORY.ref(4, 0x1f8003d0L, Pointer.of(4, UnboundedArrayRef.of(4, GsOT_TAG::new)));
  public static final Value _1f8003d4 = MEMORY.ref(4, 0x1f8003d4L);
  public static final Value linkedListAddress_1f8003d8 = MEMORY.ref(4, 0x1f8003d8L);
  public static final Value centreScreenX_1f8003dc = MEMORY.ref(2, 0x1f8003dcL);
  public static final Value centreScreenY_1f8003de = MEMORY.ref(2, 0x1f8003deL);
  public static final Value displayWidth_1f8003e0 = MEMORY.ref(4, 0x1f8003e0L);
  public static final Value displayHeight_1f8003e4 = MEMORY.ref(4, 0x1f8003e4L);
  public static final Value _1f8003e8 = MEMORY.ref(4, 0x1f8003e8L);
  public static final Value _1f8003ec = MEMORY.ref(2, 0x1f8003ecL);
  public static final Value _1f8003ee = MEMORY.ref(2, 0x1f8003eeL);

  public static final Pointer<BattleStruct> _1f8003f4 = MEMORY.ref(4, 0x1f8003f4L, Pointer.deferred(4, BattleStruct::new));
  public static final Value _1f8003f8 = MEMORY.ref(4, 0x1f8003f8L);
  public static final Value _1f8003fc = MEMORY.ref(4, 0x1f8003fcL);

  public static final Value _80010000 = MEMORY.ref(4, 0x80010000L);
  public static final Value _80010004 = MEMORY.ref(4, 0x80010004L);

  public static final Value _80010250 = MEMORY.ref(4, 0x80010250L);

  public static final ExtendedTmd extendedTmd_800103d0 = MEMORY.ref(4, 0x800103d0L, ExtendedTmd::new);
  public static final TmdAnimationFile tmdAnimFile_8001051c = MEMORY.ref(4, 0x8001051cL, TmdAnimationFile::new);

  /** TIM */
  public static final Value _80010544 = MEMORY.ref(4, 0x80010544L);

  public static final Value ovalBlobTimHeader_80010548 = MEMORY.ref(4, 0x80010548L);

  public static final CString BASCUS_94491drgn00_80010734 = MEMORY.ref(20, 0x80010734L, CString::new);

  public static final ArrayRef<RECT> rectArray28_80010770 = MEMORY.ref(4, 0x80010770L, ArrayRef.of(RECT.class, 28, 8, RECT::new));

  public static final Value _80010868 = MEMORY.ref(4, 0x80010868L);

  public static final Value _800108b0 = MEMORY.ref(4, 0x800108b0L);

  public static final Value _80011174 = MEMORY.ref(4, 0x80011174L);

  /**
   * String: CD_sync
   */
  public static final CString _80011394 = MEMORY.ref(8, 0x80011394L, CString::new);

  /**
   * String: CD_ready
   */
  public static final CString _8001139c = MEMORY.ref(9, 0x8001139cL, CString::new);

  /**
   * String: CD_cw
   */
  public static final CString _800113c0 = MEMORY.ref(6, 0x800113c0L, CString::new);

  public static final Value _80011db0 = MEMORY.ref(4, 0x80011db0L);
  public static final Value _80011db4 = MEMORY.ref(4, 0x80011db4L);
  public static final Value _80011db8 = MEMORY.ref(4, 0x80011db8L);
  public static final Value _80011dbc = MEMORY.ref(4, 0x80011dbcL);

  public static final Value _8011e210 = MEMORY.ref(4, 0x8011e210L);

  @Method(0x80011dc0L)
  public static void spuTimerInterruptCallback() {
    sssqTick();
    FUN_8002c0c8();

    if(mainCallbackIndex_8004dd20.get() == 0x3L) {
      gameState_800babc8.timestamp_a0.set(0);
    } else {
      gameState_800babc8.timestamp_a0.incr();
    }
  }

  public static final boolean SYNCHRONOUS = true;

  @Method(0x80011e1cL)
  public static void gameLoop() {
    final Runnable r = () -> {
      FUN_80012d58();
      processControllerInput();
      FUN_80011f24();
      FUN_80014d20();
      FUN_80022518();
      FUN_80011ec0();
      executeLoadersAndScripts();
      FUN_8001b410();
      FUN_80013778();
      FUN_800145c4();
      FUN_8001aa24();
      FUN_8002a058();
      FUN_8002a0e4();
      FUN_80020ed8();
      FUN_80017f94();
      _800bb0fc.addu(0x1L);
      endFrame();
    };

    if(SYNCHRONOUS) {
      GPU.r = r;
    }

    while(true) {
      if(!SYNCHRONOUS) {
        r.run();
      }

      DebugHelper.sleep(1);
    }
  }

  @Method(0x80011ec0L)
  public static void FUN_80011ec0() {
    // Empty
  }

  @Method(0x80011ec8L)
  public static void executeLoadersAndScripts() {
    if(loadSstrmAndSmap() != 0) {
      callback_8004dbc0.get((int)mainCallbackIndex_8004dd20.get()).callback_00.deref().run();

      executeScripts1();
      executeScripts2();
    }
  }

  @Method(0x80011f24L)
  public static void FUN_80011f24() {
//    oldStackPointer_1f8003b8.setu(sp);
    isStackPointerModified_1f8003bc.set(true);
//    sp = temporaryStack_1f8003b4.getAddress();

    FUN_80011f6c();

    isStackPointerModified_1f8003bc.set(false);
//    sp = oldStackPointer_1f8003b8.get();
  }

  @Method(0x80011f6cL)
  public static void FUN_80011f6c() {
    //LAB_80011f88
    for(int i = 0; i < 16; i++) {
      if(_8005a1ea.offset(i * 0xcL).getSigned() != 0) {
        _8005a1ea.offset(i * 0xcL).subu(0x1L);

        if(_8005a1ea.offset(i * 0xcL).get() == 0) {
          if(_8005a1e4.offset(i * 0xcL).get() == 0) {
            //LAB_80011fdc
            removeFromLinkedList(_8005a1e0.offset(i * 0xcL).get());
          } else {
            FUN_80012444(_8005a1e0.offset(i * 0xcL).get(), _8005a1e4.offset(i * 0xcL).get());
          }
        }
      }

      //LAB_80011ffc
      //LAB_80012000
    }
  }

  @Method(0x80012094L)
  public static void allocateLinkedList(long address, long size) {
    size = size - 0x18L & 0xffff_fffcL;
    address = address + 0x3L & 0xffff_fffcL;

    MEMORY.ref(4, address).offset(0x00L).setu(0);
    MEMORY.ref(4, address).offset(0x04L).setu(0xcL);
    MEMORY.ref(2, address).offset(0x08L).setu(0x3L);
    MEMORY.ref(4, address).offset(0x0cL).setu(address);
    MEMORY.ref(4, address).offset(0x10L).setu(size);
    MEMORY.ref(2, address).offset(0x14L).setu(0);
    MEMORY.ref(4, address).offset(size).offset(0x0cL).setu(address).addu(0xcL);
    MEMORY.ref(4, address).offset(size).offset(0x10L).setu(0);
    MEMORY.ref(2, address).offset(size).offset(0x14L).setu(0x3L);

    linkedListHead_8005a2a0.setu(address).addu(0xcL);
    linkedListTail_8005a2a4.setu(address).addu(0xcL).addu(size);
  }

  @Method(0x800120f0L)
  public static long addToLinkedListHead(long size) {
    size = size + 0xfL & 0xffff_fffcL;

    long currentEntry = linkedListHead_8005a2a0.get();
    long entryType = MEMORY.ref(2, currentEntry).offset(0x8L).get();

    //LAB_80012120
    while(entryType != 0x3L) {
      final long spaceAvailable = MEMORY.ref(4, currentEntry).offset(0x4L).get();

      if(entryType == 0) {
        if(spaceAvailable >= size) {
          MEMORY.ref(2, currentEntry).offset(0x8L).setu(0x1L);

          if(size + 0xcL < spaceAvailable) {
            MEMORY.ref(2, currentEntry).offset(size).offset(0x8L).setu(0);
            MEMORY.ref(4, currentEntry).offset(0x4L).setu(size);
            MEMORY.ref(4, currentEntry).offset(size).offset(0x0L).setu(currentEntry);
            MEMORY.ref(4, currentEntry).offset(size).offset(0x4L).setu(spaceAvailable - size);
            MEMORY.ref(4, currentEntry).offset(spaceAvailable).setu(currentEntry + size);
          }

          return currentEntry + 0xcL;
        }
      }

      //LAB_80012170
      currentEntry += MEMORY.ref(4, currentEntry).offset(0x4L).get();
      entryType = MEMORY.ref(2, currentEntry).offset(0x8L).get();
    }

    //LAB_8001218c
    assert false : "Failed to allocate entry on linked list";
    return 0;
  }

  @Method(0x80012194L)
  public static long addToLinkedListTail(long size) {
    size = size + 0xfL & 0xffff_fffcL;

    Value currentEntry = linkedListTail_8005a2a4;
    Value nextEntry = linkedListTail_8005a2a4.deref(4);
    long entryType = linkedListTail_8005a2a4.deref(4).deref(2).offset(0x8L).get();
    // Known entry types:
    // 0: empty space?
    // 2: used?
    // 3: end of list?

    //LAB_800121cc
    while(entryType != 0x3L) {
      final long spaceAvailable = nextEntry.deref(4).offset(0x4L).get();
      if(entryType == 0 && spaceAvailable >= size) {
        if(spaceAvailable > size + 0xcL) {
          currentEntry.deref(2).offset(-size).offset(0x8L).setu(0x2L); // Mark as used
          nextEntry.deref(2).offset(0x8L).setu(0); // Mark as empty space
          nextEntry.deref(4).offset(0x4L).setu(spaceAvailable - size);
          nextEntry.deref(4).offset(-size).offset(spaceAvailable).setu(nextEntry);
          currentEntry.deref(4).offset(-size).offset(0x4L).setu(size);
          currentEntry.deref(4).setu(currentEntry.get() - size);
          return currentEntry.get() - size + 0xcL;
        }

        //LAB_80012214
        nextEntry.deref(2).offset(0x8L).setu(0x2L); // Mark as used
        return nextEntry.get() + 0xcL;
      }

      //LAB_80012220
      currentEntry = nextEntry;
      nextEntry = nextEntry.deref(4);
      entryType = nextEntry.deref(2).offset(0x8L).get();
    }

    //LAB_8001223c
    assert false : "Failed to allocate entry on linked list (size 0x" + Long.toHexString(size) + ')';
    return 0;
  }

  @Method(0x80012244L)
  public static long FUN_80012244(final long address, long size) {
    if(address == 0) {
      return 0;
    }

    long s1 = address - 0xcL;
    size += 0xfL;
    size &= 0xffff_fffcL;
    long t1 = MEMORY.ref(4, s1).offset(0x4L).get();
    long v1 = MEMORY.ref(4, s1).get();
    final long s2;
    final long s0;
    if(MEMORY.ref(2, v1).offset(0x8L).get() == 0) {
      //LAB_800122a0
      s0 = v1;
      s2 = Math.min(size, t1);
      t1 += MEMORY.ref(4, v1).offset(0x4L).get();
    } else {
      //LAB_800122b0
      s0 = s1;
      s2 = 0;
    }

    //LAB_800122b4
    v1 = s1 + MEMORY.ref(4, s1).offset(0x4L).get();
    if(MEMORY.ref(2, v1).offset(0x8L).get() == 0) {
      t1 += MEMORY.ref(4, v1).offset(0x4L).get();
    }

    //LAB_800122e0
    final long t2 = t1 - size;
    if(t1 >= size) {
      if(s2 != 0) {
        long t0 = s0 + 0xcL;
        long a3 = s1 + 0xcL;
        long a2 = (s2 - 0xcL) / 4;
        if(t0 >= a3) {
          //LAB_8001233c
          if(s1 < s0) {
            //LAB_80012358
            while(a2-- > 0) {
              MEMORY.ref(4, t0).offset(a2 * 4).setu(MEMORY.ref(4, a3).offset(a2 * 4));
            }
          }
        } else {
          //LAB_80012314
          while(a2-- > 0) {
            MEMORY.ref(4, t0).setu(MEMORY.ref(4, a3));
            a3 += 0x4L;
            t0 += 0x4L;
          }
        }

        //LAB_8001237c
      }

      //LAB_80012380
      MEMORY.ref(2, s0).offset(0x8L).setu(0x1L);

      if(t2 >= 0xcL) {
        MEMORY.ref(2, s0).offset(size).offset(0x8L).setu(0);
        MEMORY.ref(4, s0).offset(0x4L).setu(size);
        MEMORY.ref(4, s0).offset(size).setu(s0);
        MEMORY.ref(4, s0).offset(size).offset(0x4L).setu(t2);
        MEMORY.ref(4, s0).offset(size).offset(t2).setu(s0 + size);
      } else {
        //LAB_800123b0
        MEMORY.ref(4, s0).offset(0x4L).setu(t1);
        MEMORY.ref(4, s0).offset(t1).setu(s0);
      }

      //LAB_800123b8
      return s0 + 0xcL;
    }

    //LAB_800123c0
    final long dataAddress = addToLinkedListHead(size - 0xcL);
    if(dataAddress == 0) {
      //LAB_800123dc
      return 0;
    }

    s1 += 0xcL;

    //LAB_800123e4
    v1 = s1;

    //LAB_800123f8
    for(int i = 0; i < (s2 - 0xcL) / 4; i++) {
      MEMORY.ref(4, dataAddress).offset(i * 4L).setu(MEMORY.ref(4, v1));
      v1 += 0x4L;

      //LAB_8001240c
    }

    removeFromLinkedList(s1);

    //LAB_8001242c
    return dataAddress;
  }

  @Method(0x80012444L)
  public static long FUN_80012444(long address, final long size) {
    long s0;
    long s1;
    long s2;
    long s3;
    long s4;
    long v0;
    long v1;
    long t0;
    long t1;
    long a2;
    long a3;

    if(address == 0) {
      return 0;
    }

    s1 = address - 0xcL;
    s2 = size + 0xfL & 0xffff_fffcL;
    long a1;
    s3 = MEMORY.ref(4, s1).offset(0x4L).get();
    v1 = Math.min(s2, s3);

    //LAB_80012494
    s4 = v1;
    s0 = addToLinkedListTail(s2 - 0xcL);
    if(s0 != 0) {
      s0 -= 0xcL;
      a1 = s0 + 0xcL;
      if(s1 < s0) {
        address = s1 + 0xcL;
        v1 = (s4 - 0xcL) / 4;

        //LAB_800124d0
        while(v1-- > 0) {
          MEMORY.ref(4, a1).setu(MEMORY.ref(4, address));
          address += 0x4L;
          a1 += 0x4L;

          //LAB_800124e4
        }

        removeFromLinkedList(s1 + 0xcL);

        return s0 + 0xcL;
      }

      //LAB_800124f8
      removeFromLinkedList(s0 + 0xcL);
    }

    //LAB_80012508
    if(MEMORY.ref(4, s1).deref(2).offset(0x8L).get() == 0) {
      s0 = MEMORY.ref(4, s1).get();
      s3 += MEMORY.ref(4, s0).offset(0x4L).get();
    } else {
      s0 = s1;
    }

    //LAB_80012530
    v1 = s1 + MEMORY.ref(4, s1).offset(0x4L).get();
    if(MEMORY.ref(2, v1).offset(0x8L).get() == 0) {
      s3 += MEMORY.ref(4, v1).offset(0x4L).get();
    }

    //LAB_8001255c
    t1 = s3 - s2;
    if(s3 >= s2) {
      t0 = s0 + t1;
      if(t1 >= 0xcL) {
        if(t0 != s1) {
          a1 = (s4 - 0xcL) / 4;

          if(t0 >= s1) {
            //LAB_800125c0
            if(s1 < t0) {
              //LAB_800125e0
              while(a1-- > 0) {
                MEMORY.ref(4, t0).offset(0xcL).offset(a1 * 4).setu(MEMORY.ref(4, s1).offset(0xcL).offset(a1 * 4));
              }
            }
          } else {
            if(a1-- > 0) {
              a2 = s1 + 0xcL;
              a3 = t0 + 0xcL;

              //LAB_80012598
              do {
                MEMORY.ref(4, a3).setu(MEMORY.ref(4, a2));
                a2 += 0x4L;
                a3 += 0x4L;
              } while(a1-- > 0);
            }
          }
        }

        //LAB_80012604
        MEMORY.ref(2, s0).offset(0x8L).setu(0);

        //LAB_80012608
        MEMORY.ref(2, t0).offset(0x8L).setu(0x2L);
        MEMORY.ref(4, s0).offset(0x4L).setu(t1);
        MEMORY.ref(4, s0).offset(t1).setu(s0);
        MEMORY.ref(4, t0).offset(0x4L).setu(s2);
        MEMORY.ref(4, t0).offset(s2).setu(t0);
        return t0 + 0xcL;
      }

      //LAB_80012630
      if(s0 != s1) {
        a1 = (s4 - 0xcL) / 4;
        a3 = s0 + 0xcL;

        if(s0 < s1) {
          a2 = s1 + 0xcL;

          //LAB_80012658
          while(a1-- > 0) {
            v0 = MEMORY.ref(4, a2).get();
            MEMORY.ref(4, a3).setu(v0);
            a2 += 0x4L;
            a3 += 0x4L;
          }
        } else {
          //LAB_80012680
          if(s1 < s0) {
            //LAB_8001269c
            a3 = s0 + 0xcL;
            while(a1-- > 0) {
              MEMORY.ref(4, a3).offset(a1 * 4).setu(MEMORY.ref(4, s1).offset(0xcL).offset(a1 * 4));
            }
          }
        }
      }

      //LAB_800126c0
      //LAB_800126c4
      MEMORY.ref(2, s0).offset(0x8L).setu(0x2L);
      MEMORY.ref(4, s0).offset(0x4L).setu(s3);
      MEMORY.ref(4, s0).offset(s3).setu(s0);
    } else {
      //LAB_800126d8
      if(addToLinkedListTail(s2 - 0xcL) == 0) {
        //LAB_800126f4
        return 0;
      }

      //LAB-800126fc
      a1 = s0;
      v1 = s1 + 0xcL;
      address = (s4 - 0xcL) / 4;

      //LAB_80012710
      while(address-- > 0) {
        MEMORY.ref(4, a1).setu(MEMORY.ref(4, v1));
        v1 += 0x4L;
        a1 += 0x4L;

        //LAB_80012724
      }

      //LAB_80012734
      removeFromLinkedList(s1 + 0xcL);
    }

    //LAB_80012740
    //LAB_80012744
    return s0;
  }

  @Method(0x80012764L)
  public static void removeFromLinkedList(long address) {
    address -= 0xcL;
    long a1 = MEMORY.ref(4, address).offset(0x4L).get(); // Remaining size?
    MEMORY.ref(2, address).offset(0x8L).setu(0); // Entry type?

    if(MEMORY.ref(2, address).offset(a1).offset(0x8L).get() == 0) {
      a1 += MEMORY.ref(4, address).offset(a1).offset(0x4L).get();
    }

    //LAB_80012794
    final long v1 = MEMORY.ref(4, address).get();
    if(MEMORY.ref(2, v1).offset(0x8L).get() == 0) {
      a1 += MEMORY.ref(4, v1).offset(0x4L).get();
      address = v1;
    }

    //LAB_800127bc
    MEMORY.ref(4, address).offset(0x4L).setu(a1);
    MEMORY.ref(4, address).offset(a1).setu(address);
  }

  @Method(0x800127ccL)
  public static long FUN_800127cc(final long address, final long a1, long a2) {
    _8004dd00.addu(0x1L);

    if(_8004dd00.get() >= 0x10L) {
      _8004dd00.setu(0);
    }

    //LAB_800127f0
    long a3 = _8004dd00.get();
    long v1 = _8005a1e0.offset(a3 * 0xcL).getAddress();

    //LAB_8001281c
    for(; a3 < 0x10L; a3++) {
      if(MEMORY.ref(4, v1).offset(0x4L).get() == 0) {
        //LAB_80012888
        if((int)a2 <= 0) {
          a2 = 0x1L;
        }

        //LAB_80012894
        MEMORY.ref(4, v1).offset(0x0L).setu(address);
        MEMORY.ref(4, v1).offset(0x4L).setu(a1);
        MEMORY.ref(2, v1).offset(0xaL).setu(a2);
        return a3;
      }

      v1 = v1 + 0xcL;
    }

    //LAB_80012840
    v1 = _8005a1e0.getAddress();

    //LAB_80012860
    for(a3 = 0; a3 < _8004dd00.get(); a3++) {
      if(MEMORY.ref(4, v1).offset(0x4L).get() == 0) {
        //LAB_80012888
        if((int)a2 <= 0) {
          a2 = 0x1L;
        }

        //LAB_80012894
        MEMORY.ref(4, v1).offset(0x0L).setu(address);
        MEMORY.ref(4, v1).offset(0x4L).setu(a1);
        MEMORY.ref(2, v1).offset(0xaL).setu(a2);
        return a3;
      }

      v1 = v1 + 0xcL;
    }

    //LAB_80012880
    return -0x1L;
  }

  @Method(0x800128a8L)
  public static long FUN_800128a8(final long a0) {
    if(a0 != 0) {
      return MEMORY.ref(4, a0).offset(-0x8L).get() - 0xcL;
    }

    //LAB_800128bc
    return 0;
  }

  @Method(0x800128c4L)
  public static long loadSstrmAndSmap() {
    if(loadingSstrmOvl_8004dd1e.get() == 0) {
      //LAB_80012910
      //LAB_80012900
      while(_8004dd14.get() != _8004dd18.get() && _8005a2a8.offset(_8004dd18.get() * 0xcL).offset(0x4L).get() == 0) {
        _8004dd18.addu(0x1L).and(0xfL);
        //LAB_80012910
      }

      //LAB_80012930
      if(loadingSstrmOvl_8004dd1e.get() == 0) {
        if(_8004dd1c.get() == 0) {
          if(_8004dd14.get() != _8004dd18.get()) {
            loadingSstrmOvl_8004dd1e.setu(0x1L);
            _8004dd10.setu(_8005a2a8.offset(_8004dd18.get() * 0xcL));
            loadFile(_8004db88.offset(_8004dd10.get() * 0x8L).getAddress(), _80010004.get(), getMethodAddress(Scus94491BpeSegment.class, "sstrmLoadedCallback", long.class, long.class, long.class), _8004dd18.get(), 0x11L);
            _8004dd18.addu(0x1L).and(0xfL);
          }
        }
      }
    }

    //LAB_800129c0
    //LAB_800129c4
    if(_8004dd24.getSigned() != -0x1L) {
      if(loadingSmapOvl_8004dd08.get() != 0) {
        return 0;
      }

      pregameLoadingStage_800bb10c.setu(0);
      _8004dd28.setu(mainCallbackIndex_8004dd20);
      mainCallbackIndex_8004dd20.set(_8004dd24);
      _8004dd24.setu(-0x1L);
      FUN_80019710();
      vsyncMode_8007a3b8.setu(0x2L);
      loadSmap((int)mainCallbackIndex_8004dd20.getSigned());

      if(mainCallbackIndex_8004dd20.get() == 0x6L) { // Starting combat
        FUN_8001c4ec();
      }
    }

    //LAB_80012a34
    //LAB_80012a38
    if(loadingSmapOvl_8004dd08.get() == 0 || (callback_8004dbc0.get((int)mainCallbackIndex_8004dd20.get()).uint_0c.get() & 0xff00L) != 0) {
      //LAB_80012a6c
      return 1;
    }

    //LAB_80012a70
    return 0;
  }

  @Method(0x80012a84L)
  public static long loadSmap(final int callbackIndex) {
    final long val = callback_8004dbc0.get(callbackIndex).ptr_04.get();

    if(val == 0 || val == _8004dd04.get()) {
      //LAB_80012ac0
      FUN_80012bd4(callbackIndex);
      loadingSmapOvl_8004dd08.setu(0);
      return 0x1L;
    }

    //LAB_80012ad8
    _8004dd04.setu(val);
    loadingSmapOvl_8004dd08.setu(0x1L);
    loadFile(val, _80010000.get(), getMethodAddress(Scus94491BpeSegment.class, "smapLoadedCallback", long.class, long.class, long.class), callbackIndex, 0x11L);
    return 0;
  }

  @Method(0x80012b1cL)
  public static void FUN_80012b1c(final long a0, final long callback, final long callbackParam) {
    if(_8004dd10.get() == a0 && loadingSstrmOvl_8004dd1e.get() == 0) {
      _8004dd1c.addu(0x1L);
      MEMORY.ref(4, callback).cast(ConsumerRef::new).run(callbackParam);
      return;
    }

    //LAB_80012b6c
    //LAB_80012b70
    _8005a2a8.offset(_8004dd14.get() * 12).setu(a0);
    _8005a2ac.offset(_8004dd14.get() * 12).setu(callback);
    _8005a2b0.offset(_8004dd14.get() * 12).setu(callbackParam);
    _8004dd14.addu(0x1L).and(0xfL);

    //LAB_80012ba4
  }

  @Method(0x80012bb4L)
  public static void FUN_80012bb4() {
    if(_8004dd1c.getSigned() > 0) {
      _8004dd1c.subu(1);
    }
  }

  @Method(0x80012bd4L)
  public static void FUN_80012bd4(final int callbackIndex) {
    if(_8004dd0c.get() != 0) {
      _8004dd0c.setu(0);
      return;
    }

    //LAB_80012bf0
    final long v0 = callback_8004dbc0.get(callbackIndex).ptr_08.get();
    if(v0 != 0) {
      long a0 = MEMORY.ref(4, v0).get();
      long v1 = MEMORY.ref(4, v0).offset(0x4L).get() / 0x4L;

      //LAB_80012c1c
      while(v1 > 0) {
        MEMORY.ref(4, a0).setu(0);
        a0 += 0x4L;
        v1--;

        //LAB_80012c24
      }
    }
    //LAB_80012c30
  }

  @Method(0x80012c48L)
  public static void smapLoadedCallback(final long address, final long fileSize, final long callbackIndex) {
    loadingSmapOvl_8004dd08.setu(0);
  }

  @Method(0x80012c54L)
  public static void sstrmLoadedCallback(final long address, final long fileSize, final long a2) {
    loadingSstrmOvl_8004dd1e.setu(0);
    FUN_80012c7c(a2);
  }

  @Method(0x80012c7cL)
  public static void FUN_80012c7c(final long a0) {
    long s1 = a0;

    //LAB_80012cd0
    while(s1 != _8004dd14.get()) {
      if(_8004dd10.get() == _8005a2a8.offset(s1 * 12).get()) {
        if(_8005a2ac.offset(s1 * 12).get() != 0) {
          _8004dd1c.addu(0x1L);
          _8005a2ac.offset(s1 * 12).deref(4).cast(ConsumerRef::new).run(_8005a2b0.offset(s1 * 12).get());
        }

        //LAB_80012d14
        _8005a2a8.offset(s1 * 12).setu(-0x1L);
        _8005a2ac.offset(s1 * 12).setu(0);
      }

      //LAB_80012d20
      s1 = s1 + 0x1L & 0xfL;
    }

    //LAB_80012d30
  }

  @Method(0x80012d58L)
  public static void FUN_80012d58() {
    doubleBufferFrame_800bb108.setu(PSDIDX_800c34d4);

    _8007a3ac.setu(_8009a7c0.offset(PSDIDX_800c34d4.get() * 0x20400L).getAddress());

    _1f8003d4.setu(_8007a3ac);
    tags_1f8003d0.set(_8005a398.get((int)PSDIDX_800c34d4.get()));
    linkedListAddress_1f8003d8.setu(_8007a3c0.offset(PSDIDX_800c34d4.get() * 0x20400L).getAddress());

    GsClearOt(0, 0, orderingTables_8005a370.get((int)PSDIDX_800c34d4.get()));
  }

  @Method(0x80012df8L)
  public static void endFrame() {
    if(renderFlags_8004dd36.get(0x2L) == 0) { // Height: 240
      long a1 = linkedListAddress_1f8003d8.get();
      MEMORY.ref(1, a1).offset(0x3L).setu(0x2L); // 2 words
      MEMORY.ref(4, a1).offset(0x4L).setu(0xe600_0000L); // Mask bit setting - set mask while drawing
      MEMORY.ref(4, a1).offset(0x8L).setu(0);

      insertElementIntoLinkedList(tags_1f8003d0.deref().get(3).getAddress(), a1);
      linkedListAddress_1f8003d8.addu(0xcL);

      a1 = linkedListAddress_1f8003d8.get();
      MEMORY.ref(1, a1).offset(0x3L).setu(0x2L); // 2 words
      MEMORY.ref(4, a1).offset(0x4L).setu(0xe600_0001L); // Mask bit setting - force bit 15
      MEMORY.ref(4, a1).offset(0x8L).setu(0);

      insertElementIntoLinkedList(tags_1f8003d0.deref().get((int)(_1f8003c8.get() - 1)).getAddress(), a1);
      linkedListAddress_1f8003d8.addu(0xcL);
    }

    //LAB_80012e8c
    syncFrame_8004dd3c.deref().run();
    swapDisplayBuffer_8004dd40.deref().run();
  }

  @Method(0x80012eccL)
  public static void syncFrame() {
    if(renderFlags_8004dd36.get(0x2L) == 0) { // Height: 240
      //LAB_80012efc
      DrawSync(0);
      VSync((int)vsyncMode_8007a3b8.getSigned());
    } else {
      VSync(0);
      FUN_8003b0d0();
    }

    //LAB_80012f14
  }

  /**
   * {@link Scus94491BpeSegment_8004#syncFrame_8004dd3c} is changed to this for one frame end then switched back when the graphics mode changes
   */
  @Method(0x80012f24L)
  public static void syncFrame_reinit() {
    final long flags;
    if(renderFlags_8004dd36.get(0x3L) == 0) { // Height: 240, not interlaced
      flags = 0b110100L;
    } else {
      flags = 0b110101L;
    }

    //LAB_80012f5c
    final long use24BitColour = renderFlags_8004dd36.get() >>> 0x2L & 0x1L;
    final long height480 = renderFlags_8004dd36.get() & 0x2L;
    final long length = _8004dd38.get();

    _800babc0.setu(0);
    _800bb104.setu(0);
    _8007a3a8.setu(0);

    final RECT rect1 = new RECT((short)0, (short)16, (short)width_8004dd34.get(), (short)240);
    final RECT rect2 = new RECT((short)0, (short)256, (short)width_8004dd34.get(), (short)240);

    orderingTables_8005a370.get(0).length_00.set(length);
    orderingTables_8005a370.get(1).length_00.set(length);

    _1f8003c0.setu(length);
    _1f8003c4.setu(0xeL - length);
    _1f8003c8.setu(0x1L << length);
    _1f8003cc.setu((0x1L << length) - 0x2L);

    VSync(0);
    SetDispMask(0);
    DrawSync(0);
    VSync(0);

    final long displayHeight;
    if(height480 == 0) {
      //LAB_80013040
      GsDefDispBuff((short)0, (short)16, (short)0, (short)256);
      displayHeight = 240L;
    } else {
      GsDefDispBuff((short)0, (short)16, (short)0, (short)16);
      displayHeight = 480L;
    }

    //LAB_80013060
    GsInitGraph((short)width_8004dd34.get(), (short)displayHeight, (short)flags, true, use24BitColour != 0);

    if(width_8004dd34.get() == 384L) {
      DISPENV_800c34b0.screen.x.set((short)9);
    }

    //LAB_80013080
    GsClearOt(0, 0, orderingTables_8005a370.get(0));
    GsClearOt(0, 0, orderingTables_8005a370.get(1));
    ClearImage(rect1, (byte)0, (byte)0, (byte)0);
    ClearImage(rect2, (byte)0, (byte)0, (byte)0);
    FUN_8003c5e0();
    setProjectionPlaneDistance(320);

    DrawSync(0);
    VSync(0);
    SetDispMask(1);

    syncFrame_8004dd3c.set(MEMORY.ref(4, getMethodAddress(Scus94491BpeSegment.class, "syncFrame")).cast(RunnableRef::new));

    if(use24BitColour == 0) {
      swapDisplayBuffer_8004dd40.set(MEMORY.ref(4, getMethodAddress(Scus94491BpeSegment.class, "swapDisplayBuffer_24bpp")).cast(RunnableRef::new));
    } else {
      swapDisplayBuffer_8004dd40.set(MEMORY.ref(4, getMethodAddress(Scus94491BpeSegment.class, "swapDisplayBuffer_15bpp")).cast(RunnableRef::new));
    }
  }

  @Method(0x80013148L)
  public static void swapDisplayBuffer_24bpp() {
    GsSwapDispBuff();

    if(renderFlags_8004dd36.get(0x2L) == 0) { // Height: 240
      GsSortClear(_8007a3a8.get(), _800bb104.get(), _800babc0.get(), orderingTables_8005a370.get((int)doubleBufferFrame_800bb108.get()));
    }

    //LAB_800131b0
    drawOTag(orderingTables_8005a370.get((int)doubleBufferFrame_800bb108.get()));
  }

  @Method(0x800131e0L)
  public static void swapDisplayBuffer_15bpp() {
    GsSwapDispBuff();
  }

  @Method(0x80013200L)
  public static void setWidthAndFlags(final long width, final long flags) {
    if(width != displayWidth_1f8003e0.get() || flags != renderFlags_8004dd36.get()) {
      // Change the syncFrame callback to the reinitializer for a frame to reinitialize everything with the new size/flags
      syncFrame_8004dd3c.set(MEMORY.ref(4, getMethodAddress(Scus94491BpeSegment.class, "syncFrame_reinit")).cast(RunnableRef::new));
      width_8004dd34.setu(width);
      renderFlags_8004dd36.setu(flags);
    }
  }

  @Method(0x8001324cL)
  public static void FUN_8001324c(final long a0) {
    if(_1f8003c0.get() != a0) {
      syncFrame_8004dd3c.set(MEMORY.ref(4, getMethodAddress(Scus94491BpeSegment.class, "syncFrame_reinit")).cast(RunnableRef::new));
      _8004dd38.setu(a0);
    }

    //LAB_80013274
  }

  @Method(0x800133acL)
  public static long FUN_800133ac() {
    final long v1;
    if(_8004dd44.get(0xfffL) == 0) {
      v1 = VSync(-1) * 0x9L;
    } else {
      v1 = _8004dd44.get();
    }

    //LAB_800133dc
    _8004dd44.setu(v1 * 0x9L + 0x3711L);
    return _8004dd44.get() / 0x2L & 0xffffL;
  }

  @Method(0x80013434L)
  public static void FUN_80013434(final long a0, final long a1, final long a2, final long a3) {
    long s4 = 0;

    //LAB_8001347c
    while(s4 << 0x1L < a1) {
      s4 += s4 + 0x1L;
    }

    //LAB_80013490
    final long s6 = a1 * a2 & 0xffff_ffffL;
    final long s7 = a2 >>> 0x2L;

    //LAB_800134a4
    while(s4 > 0) {
      final long s5 = s4 * a2 & 0xffff_ffffL;
      long s3 = s5;

      //LAB_800134b8
      while(s3 < s6) {
        long s2 = s3 - s5;

        //LAB_800134c4
        while(s2 >= 0) {
          final long s0 = a0 + s2;
          final long s1 = s0 + s5;

          if((long)MEMORY.ref(4, a3).cast(BiFunctionRef::new).run(s0, s1) <= 0) {
            break;
          }

          //LAB_80013500
          for(int i = 0; i < s7; i++) {
            final long v1 = MEMORY.ref(4, s0).offset(i * 4L).get();
            final long v0 = MEMORY.ref(4, s1).offset(i * 4L).get();
            MEMORY.ref(4, s0).offset(i * 4L).setu(v0);
            MEMORY.ref(4, s1).offset(i * 4L).setu(v1);
          }

          //LAB_80013524
          s2 -= s5;
        }

        //LAB_80013530
        s3 += a2;
      }

      //LAB_80013540
      s4 >>= 0x1L;
    }

    //LAB_8001354c
  }

  @Method(0x8001357cL)
  public static void insertElementIntoLinkedList(final long previousElement, final long newElement) {
    MEMORY.ref(3, newElement).setu(MEMORY.ref(3, previousElement));
    MEMORY.ref(3, previousElement).setu(newElement);
  }

  @Method(0x80013598L)
  public static long rsin(final long angleDiv360Maybe) {
    return sin_cos_80054d0c.offset(2, 0x0L).offset((angleDiv360Maybe & 0xfffL) * 4).getSigned();
  }

  @Method(0x800135b8L)
  public static long rcos(final long angleDiv360Maybe) {
    return sin_cos_80054d0c.offset(2, 0x2L).offset((angleDiv360Maybe & 0xfffL) * 4).getSigned();
  }

  /**
   * 800135d8
   *
   * Copies the first n bytes of src to dest.
   *
   * @param dest Pointer to copy destination memory block
   * @param src Pointer to copy source memory block
   * @param length Number of bytes copied
   *
   * @return Pointer to destination (dest)
   */
  @Method(0x800135d8L)
  public static long memcpy(final long dest, long src, int length) {
    if(length == 0) {
      return dest;
    }

    final long v0 = (length | dest | src) & 0xfffffffcL;
    long a3 = dest;
    if(v0 == 0) {
      length = length / 4;

      //LAB_80013600
      while(length > 0) {
        MEMORY.ref(4, a3).setu(MEMORY.ref(4, src));
        src += 0x4L;
        a3 += 0x4L;
        length--;
      }

      return dest;
    }

    //LAB_80013630
    while(length > 0) {
      MEMORY.ref(1, a3).setu(MEMORY.ref(1, src));
      src++;
      a3++;
      length--;
    }

    //LAB_8001364c
    return dest;
  }

  @Method(0x80013658L)
  public static void fillMemory(final long address, final long fill, final long size) {
    // Fill in 4-byte segments if possible
    if((address & 0x3L) == 0 && (size & 0x3L) == 0) {
      for(int i = 0; i < size; i += 4) {
        MEMORY.set(address + i, 4, fill);
      }

      return;
    }

    for(int i = 0; i < size; i++) {
      MEMORY.set(address + i, (byte)fill);
    }
  }

  @Method(0x800136dcL)
  public static void scriptStartEffect(final long effectType, final long frames) {
    //LAB_800136f4
    scriptEffect_800bb140.type_00.set(effectType);
    scriptEffect_800bb140.totalFrames_08.set((int)frames > 0 ? frames : 0xfL);
    scriptEffect_800bb140.startTime_04.set(VSync(-1));

    if(_8004dd48.offset(effectType * 2).get() == 0x2L) {
      scriptEffect_800bb140.blue1_0c.set(0);
      scriptEffect_800bb140.green1_10.set(0);
      scriptEffect_800bb140.blue0_14.set(0);
      scriptEffect_800bb140.red1_18.set(0);
      scriptEffect_800bb140.green0_1c.set(0);
      scriptEffect_800bb140.red0_20.set(0);
    }

    scriptEffect_800bb140._24.set(_8004dd48.offset(effectType * 2).get());

    //LAB_80013768
  }

  /**
   * This handles the lightning flashes/darkening, the scene fade-in, etc.
   */
  @Method(0x80013778L)
  public static void FUN_80013778() {
    final long v1 = Math.min(scriptEffect_800bb140.totalFrames_08.get(), (VSync(-1) - scriptEffect_800bb140.startTime_04.get()) / 2);

    //LAB_800137d0
    final long colour;
    if(scriptEffect_800bb140.totalFrames_08.get() == 0) {
      colour = 0;
    } else {
      final long a1 = scriptEffect_800bb140._24.get();
      if(a1 == 0x1L) {
        //LAB_80013818
        colour = v1 * 255 / scriptEffect_800bb140.totalFrames_08.get();
      } else if((int)a1 < 0x2L) {
        if(a1 != 0) {
          scriptEffect_800bb140.type_00.set(0);
          scriptEffect_800bb140._24.set(0);
        }

        colour = 0;

        //LAB_80013808
      } else if(a1 != 0x2L) {
        scriptEffect_800bb140.type_00.set(0);
        scriptEffect_800bb140._24.set(0);
        colour = 0;
      } else { // a1 == 2
        //LAB_8001383c
        colour = v1 * 255 / scriptEffect_800bb140.totalFrames_08.get() ^ 0xffL;

        if(colour == 0) {
          //LAB_80013874
          scriptEffect_800bb140._24.set(0);
        }
      }
    }

    //LAB_80013880
    //LAB_80013884
    _800bb168.setu(colour);

    if(colour != 0) {
      //LAB_800138f0
      //LAB_80013948
      switch((int)scriptEffect_800bb140.type_00.get()) {
        case 1, 2 -> drawFullScreenRect(colour, 0x2L);
        case 3, 4 -> drawFullScreenRect(colour, 0x1L);

        case 5 -> {
          for(int s1 = 0; s1 < 8; s1++) {
            //LAB_800138f8
            for(int s0 = 0; s0 < 6; s0++) {
              FUN_80013d78(colour - (0xcL - (s0 + s1)) * 11, s1, s0);
            }
          }
        }

        case 6 -> {
          for(int s1 = 0; s1 < 8; s1++) {
            //LAB_80013950
            for(int s0 = 0; s0 < 6; s0++) {
              FUN_80013d78(colour - (s1 + s0) * 11, s1, s0);
            }
          }
        }
      }
    }

    //caseD_0
    //LAB_80013994

    // This causes the bright flash of light from the lightning, etc.
    if(scriptEffect_800bb140.red0_20.get() != 0 || scriptEffect_800bb140.green0_1c.get() != 0 || scriptEffect_800bb140.blue0_14.get() != 0) {
      //LAB_800139c4
      final long s0 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x10L);

      MEMORY.ref(1, s0).offset(0x3L).setu(0x3L); // 3 words

      MEMORY.ref(1, s0).offset(0x4L).setu(scriptEffect_800bb140.red0_20.get() & 0xffL); // R
      MEMORY.ref(1, s0).offset(0x5L).setu(scriptEffect_800bb140.green0_1c.get() & 0xffL); // G
      MEMORY.ref(1, s0).offset(0x6L).setu(scriptEffect_800bb140.blue0_14.get() & 0xffL); // B
      MEMORY.ref(1, s0).offset(0x7L).setu(0x60L); // Monochrome rectangle, variable size, opaque

      MEMORY.ref(2, s0).offset(0x8L).setu(-centreScreenX_1f8003dc.get()); // X
      MEMORY.ref(2, s0).offset(0xaL).setu(-centreScreenY_1f8003de.get()); // Y
      MEMORY.ref(2, s0).offset(0xcL).setu(displayWidth_1f8003e0.get() + 0x1L); // W
      MEMORY.ref(2, s0).offset(0xeL).setu(displayHeight_1f8003e4.get() + 0x1L); // H

      gpuLinkedListSetCommandTransparency(s0, true);
      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x27).getAddress(), s0);

      final long a1 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x8L);

      MEMORY.ref(1, a1).offset(0x3L).setu(0x1L); // 1 word
      MEMORY.ref(4, a1).offset(0x4L).setu(0xe1000205L | _800bb114.get(0x9ffL)); // Draw mode dither enabled, texpage X (320), whatever is or'd

      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x27).getAddress(), a1);
    }

    //LAB_80013adc

    // This causes the screen darkening from the lightning, etc.
    if(scriptEffect_800bb140.red1_18.get() != 0 || scriptEffect_800bb140.green1_10.get() != 0 || scriptEffect_800bb140.blue1_0c.get() != 0) {
      //LAB_80013b10
      final long s0 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x10L);

      MEMORY.ref(1, s0).offset(0x3L).setu(0x3L); // 3 words

      MEMORY.ref(1, s0).offset(0x4L).setu(scriptEffect_800bb140.red1_18.get() & 0xffL); // R
      MEMORY.ref(1, s0).offset(0x5L).setu(scriptEffect_800bb140.green1_10.get() & 0xffL); // G
      MEMORY.ref(1, s0).offset(0x6L).setu(scriptEffect_800bb140.blue1_0c.get() & 0xffL); // B
      MEMORY.ref(1, s0).offset(0x7L).setu(0x60L); // Monochrome rectangle, variable size, opaque

      MEMORY.ref(2, s0).offset(0x8L).setu(-centreScreenX_1f8003dc.get()); // X
      MEMORY.ref(2, s0).offset(0xaL).setu(-centreScreenY_1f8003de.get()); // Y
      MEMORY.ref(2, s0).offset(0xcL).setu(displayWidth_1f8003e0.get() + 0x1L); // W
      MEMORY.ref(2, s0).offset(0xeL).setu(displayHeight_1f8003e4.get() + 0x1L); // H

      gpuLinkedListSetCommandTransparency(s0, true);
      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x27).getAddress(), s0);

      final long a1 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x8L);

      MEMORY.ref(1, a1).offset(0x3L).setu(0x1L); // 1 word
      MEMORY.ref(4, a1).offset(0x4L).setu(0xe1000205L | _800bb118.get(0x9ffL)); // Draw mode dither enabled, texpage X (320), whatever is or'd

      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x27).getAddress(), a1);
    }

    //LAB_80013c20
  }

  @Method(0x80013c3cL)
  public static void drawFullScreenRect(final long colour, final long drawModeIndex) {
    long s0 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x10L);

    MEMORY.ref(1, s0).offset(0x3L).setu(0x3L); // 3 words

    MEMORY.ref(1, s0).offset(0x4L).setu(colour); // R
    MEMORY.ref(1, s0).offset(0x5L).setu(colour); // G
    MEMORY.ref(1, s0).offset(0x6L).setu(colour); // B
    MEMORY.ref(1, s0).offset(0x7L).setu(0x60L); // Monochrome rectangle (variable size, opaque)

    MEMORY.ref(2, s0).offset(0x8L).set(-centreScreenX_1f8003dc.get()); // xx
    MEMORY.ref(2, s0).offset(0xaL).set(-centreScreenY_1f8003de.get()); // xx
    MEMORY.ref(2, s0).offset(0xcL).setu(displayWidth_1f8003e0.offset(2, 0x0L).get() + 1); // yy
    MEMORY.ref(2, s0).offset(0xeL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + 1); // yy

    gpuLinkedListSetCommandTransparency(s0, true);
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x1e).getAddress(), s0);

    s0 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x8L);

    MEMORY.ref(1, s0).offset(0x3L).setu(0x1L); // 1 word
    MEMORY.ref(4, s0).offset(0x4L).setu(0xe1000205L | _800bb110.offset((drawModeIndex & 0x3L) * 4).get(0x9ffL)); // Draw mode/texpage
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x1e).getAddress(), s0);
  }

  @Method(0x80013d78L)
  public static void FUN_80013d78(final long a0, final long a1, final long a2) {
    assert false;
  }

  @Method(0x800145c4L)
  public static void FUN_800145c4() {
    long s0;
    long s2 = _800bb228.getAddress();
    final long s5 = _1f8003fc.get();
    long s3 = 0;
    long s1 = 0;

    //LAB_8001461c
    do {
      if(s2 == s5) {
        break;
      }

      long v1 = MEMORY.ref(1, s2).get();
      s2++;
      long a0;
      if(v1 >= 0x80L) {
        //LAB_80014654
        if(v1 < 0xe0L && v1 >= 0xa1L) {
          v1 -= 0x40L;

          //LAB_80014678
          s0 = _1f8003d4.get() - 0x44L;

          a0 = (v1 & 0xe0L) << 0x6L | (v1 & 0x1fL) << 0x3L;
          v1 = s3 - (centreScreenY_1f8003de.get() - 0xcL) - 1 << 0x10L | s1 - (centreScreenX_1f8003dc.get() - 0x4L) - 1 & 0xffffL;
          MEMORY.ref(1, s0).offset(0x03L).setu(0xcL);
          MEMORY.ref(4, s0).offset(0x04L).setu(0x7480_8080L);
          MEMORY.ref(4, s0).offset(0x08L).setu(v1 + 1);
          MEMORY.ref(4, s0).offset(0x0cL).setu(0x69b5_a800L | a0);
          MEMORY.ref(4, s0).offset(0x10L).setu(0x7480_8080L);
          MEMORY.ref(4, s0).offset(0x14L).setu(0x1_0000L | v1);
          MEMORY.ref(4, s0).offset(0x18L).setu(0x69b5_a800L | a0);
          MEMORY.ref(4, s0).offset(0x1cL).setu(0x7480_8080L);
          MEMORY.ref(4, s0).offset(0x20L).setu(0x1_0002L + v1);
          MEMORY.ref(4, s0).offset(0x24L).setu(0x69b5_a800L | a0);
          MEMORY.ref(4, s0).offset(0x28L).setu(0x7480_8080L);
          MEMORY.ref(4, s0).offset(0x2cL).setu(0x2_0001L + v1);
          MEMORY.ref(4, s0).offset(0x30L).setu(0x69b5_a800L | a0);

          MEMORY.ref(1, s0).offset(0x37L).setu(0x3L);
          MEMORY.ref(4, s0).offset(0x38L).setu(0x7480_8080L);
          MEMORY.ref(4, s0).offset(0x3cL).setu(0x1_0001L + v1);
          MEMORY.ref(4, s0).offset(0x40L).setu(0x69b4_a800L | a0);

          insertElementIntoLinkedList(tags_1f8003d0.deref().get(4).getAddress(), _1f8003d4.get() - 0x10L);
          insertElementIntoLinkedList(tags_1f8003d0.deref().get(5).getAddress(), s0);
          _1f8003d4.subu(0x44L);
        }

        //LAB_80014790
        s1 += 0x9L;

        //LAB_80014794
        if(s1 < 0x130L) {
          continue;
        }
      } else if(v1 >= 0x21L) {
        //LAB_80014670
        v1 -= 0x20L;

        //LAB_80014678
        s0 = _1f8003d4.get() - 0x44L;

        a0 = (v1 & 0xe0L) << 0x6L | (v1 & 0x1fL) << 0x3L;
        v1 = s3 - (centreScreenY_1f8003de.get() - 0xcL) - 1 << 0x10L | s1 - (centreScreenX_1f8003dc.get() - 0x4L) - 1 & 0xffffL;
        MEMORY.ref(1, s0).offset(0x3L).setu(0xcL);
        MEMORY.ref(1, s0).offset(0x37L).setu(0x3L);
        MEMORY.ref(4, s0).offset(0x38L).setu(0x7480_8080L);
        MEMORY.ref(4, s0).offset(0x28L).setu(0x7480_8080L);
        MEMORY.ref(4, s0).offset(0x1cL).setu(0x7480_8080L);
        MEMORY.ref(4, s0).offset(0x10L).setu(0x7480_8080L);
        MEMORY.ref(4, s0).offset(0x04L).setu(0x7480_8080L);
        MEMORY.ref(4, s0).offset(0x08L).setu(v1 + 1);
        MEMORY.ref(4, s0).offset(0x14L).setu(0x1_0000L | v1);
        MEMORY.ref(4, s0).offset(0x20L).setu(0x1_0002L + v1);
        MEMORY.ref(4, s0).offset(0x2cL).setu(0x2_0001L + v1);
        MEMORY.ref(4, s0).offset(0x3cL).setu(0x1_0001L + v1);
        MEMORY.ref(4, s0).offset(0x30L).setu(0x69b5_a800L | a0);
        MEMORY.ref(4, s0).offset(0x24L).setu(0x69b5_a800L | a0);
        MEMORY.ref(4, s0).offset(0x18L).setu(0x69b5_a800L | a0);
        MEMORY.ref(4, s0).offset(0x0cL).setu(0x69b5_a800L | a0);
        MEMORY.ref(4, s0).offset(0x40L).setu(0x69b4_a800L | a0);
        insertElementIntoLinkedList(tags_1f8003d0.deref().get(4).getAddress(), _1f8003d4.get() - 0x10L);
        insertElementIntoLinkedList(tags_1f8003d0.deref().get(5).getAddress(), s0);
        _1f8003d4.subu(0x44L);

        s1 += 0x9L;

        //LAB_80014794
        if(s1 < 0x130L) {
          continue;
        }
      } else if(v1 != 0xaL) {
        s1 += 0x9L;

        //LAB_80014794
        if(s1 < 0x130L) {
          continue;
        }
      } else if(s3 >= 0x101L) {
        //LAB_800147a0
        break;
      }

      // Changed this, no longer loading

      s3 += 0x9L;
      s1 = 0;
    } while(s3 < 0xe0L);

    //LAB_800147b4
    //LAB_800147b8
    s0 = _1f8003d4.get() - 0x8L;
    _1f8003d4.subu(0x8L);

    MEMORY.ref(1, s0).offset(0x3L).setu(0x1L);
    MEMORY.ref(4, s0).offset(0x4L).setu(_800bb348.get(0x9ffL) | 0xe100_0200L);
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(4).getAddress(), s0);

    s0 = _1f8003d4.get() - 0x8L;
    _1f8003d4.subu(0x8L);

    MEMORY.ref(1, s0).offset(0x3L).setu(0x1L);
    MEMORY.ref(4, s0).offset(0x4L).setu(_800bb348.get(0x9ffL) | 0xe100_0200L);
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(5).getAddress(), s0);

    _1f8003fc.setu(_800bb228.getAddress());

    //LAB_80014840
  }

  @Method(0x8001486cL)
  public static void decompressCurrentFile() {
    final FileLoadingInfo file = currentlyLoadingFileInfo_800bb468;

    if(!file.used.get()) {
      return;
    }

    if((file.type.get() & 1) == 0) {
      file.used.set(false);
      return;
    }

    LOGGER.info("Decompressing file %s...", file.namePtr.deref().get());

    final long v1 = file.type.get() & 0b110L;

    long transferDest;
    //LAB_800148b8
    if(v1 == 0) { // Decompress to file transfer dest
      //LAB_800148ec
      transferDest = file.transferDest.get();
      fileSize_800bb464.setu(decompress(transferDest_800bb460.get(), transferDest));
      removeFromLinkedList(transferDest_800bb460.get());
    } else {
      //LAB_8001491c
      transferDest = transferDest_800bb460.deref(4).offset(-0x8L).get();

      if(transferDest < 0x8000_0000L) {
        throw new RuntimeException("Illegal transfer destination 0x" + Long.toHexString(transferDest));
      }

      fileSize_800bb464.setu(decompress(transferDest_800bb460.get(), transferDest));

      final long address = FUN_80012444(transferDest, fileSize_800bb464.get());

      //LAB_80014984
      if(address != 0) {
        transferDest = address;
      }
    }

    //LAB_800149a4
    //LAB_800149a8
    if(transferDest < 0x8000_0000L) {
      throw new RuntimeException("Illegal transfer destination for decompression 0x" + Long.toHexString(transferDest));
    }

    transferDest_800bb460.setu(transferDest);
    file.used.set(false);

    switch(file.namePtr.deref().get()) {
      case "\\OVL\\SMAP.OV_" -> MEMORY.addFunctions(SMap.class);
      case "\\OVL\\S_STRM.OV_" -> MEMORY.addFunctions(SStrm.class);
      case "\\OVL\\TTLE.OV_" -> MEMORY.addFunctions(Ttle.class);
      case "\\OVL\\S_ITEM.OV_" -> MEMORY.addFunctions(SItem.class);
      case "\\OVL\\WMAP.OV_" -> MEMORY.addFunctions(WMap.class);
      case "\\OVL\\BTTL.OV_" -> MEMORY.addFunctions(Bttl.class);
      case "\\OVL\\S_BTLD.OV_" -> MEMORY.addFunctions(SBtld.class);
      case "\\OVL\\S_EFFE.OV_" -> MEMORY.addFunctions(SEffe.class);
      case "\\SECT\\DRGN0.BIN" -> { }
      default -> throw new RuntimeException("Loaded unknown file " + file.namePtr.deref().get());
    }

    //LAB_800149b4
  }

  @Method(0x800149ccL)
  public static long FUN_800149cc() {
    final FileLoadingInfo file = fileLoadingInfoArray_800bbad8.get(0);

    if(!file.used.get()) {
      //LAB_80014a74
      if(_8004ddd8.get() != 0x1L) {
        //LAB_80014b1c
        if(_8004ddd4.get() != 0x1L) {
          if(_8004ddd4.get() != 0x2L) {
            return 0;
          }

          if(_8004ddd0.get() == 0x2L) {
            callbackIndex_8004ddc4.setu(0xbL);
            _8004ddd0.setu(0x1L);
            return 0x1L;
          }

          //LAB_80014b7c
          callbackIndex_8004ddc4.setu(0x7L);
        } else {
          //LAB_80014b3c
          if(_8004ddd0.get() != 0x2L) {
            //LAB_80014b88
            _8004ddd4.setu(0);
            return 0x1L;
          }

          //LAB_80014b80
          callbackIndex_8004ddc4.setu(0xbL);
        }

        _8004ddd0.setu(_8004ddd4);
        _8004ddd4.setu(0);
        return 0x1L;
      }

      if(_8004ddd0.get() == 0x2L) {
        callbackIndex_8004ddc4.setu(0xbL);
        _8004ddd0.setu(0x1L);
        return 0x1L;
      }

      //LAB_80014aac
      if(_800bf0cf.get() >= 0x2L) {
        _800bf0cf.setu(0x1L);

        //LAB_80014ad0
        setCdVolume(0, 0);
        _800bf0e0.setu(0);
        return 0x1L;
      }

      //LAB_80014ae8
      if(_800bf0d8.get() != 0) {
        //LAB_80014af8
        FUN_800edb8c();

        //LAB_80014b00
        return 0x1L;
      }

      //LAB_80014b08
      //LAB_80014b10
      callbackIndex_8004ddc4.setu(0x1aL);
      _8004ddd0.setu(0x1L);
      return 0x1L;
    }

    if(_8004ddd0.get() == 0x2L) {
      callbackIndex_8004ddc4.setu(0xbL);
      _8004ddd0.setu(0x1L);
      return 0x1L;
    }

    //LAB_80014a10
    if(_800bf0cf.get() >= 0x2L) {
      _800bf0cf.setu(0x1L);
      setCdVolume(0, 0);
      _800bf0e0.setu(0);
      return 0x1L;
    }

    //LAB_80014a3c
    if(_800bf0d8.get() != 0) {
      FUN_800edb8c();
      return 0x1L;
    }

    if(_8004ddcc.get() == 0) {
      callbackIndex_8004ddc4.setu(0x1L);
      _8004ddd0.setu(0x1L);

      //LAB_80014b90
      return 0x1L;
    }

    return 0;
  }

  @Method(0x80014ba0L)
  public static long allocateFileTransferDest() {
    if(FUN_80036f20() != 0x1L) {
      return -0x1L;
    }

    final FileLoadingInfo file = fileLoadingInfoArray_800bbad8.get(0);

    fileSize_800bb48c.setu(file.size.get());
    numberOfTransfers_800bb490.setu((file.size.get() + 0x7ffL) / 0x800L);

    switch(file.type.get() & 0b111) {
      case 0 -> {
        if(file.transferDest.get() < 0x8000_0000L) {
          throw new RuntimeException("Illegal transfer destination for decompression 0x" + Long.toHexString(file.transferDest.get()));
        }

        fileTransferDest_800bb488.setu(file.transferDest);
        return 0;
      }

      case 1, 4 -> {
        final long size = numberOfTransfers_800bb490.get() * 0x800L;
        final long transferDest = addToLinkedListHead(size);

        if(transferDest == 0) {
          return -0x1L;
        }

        if(transferDest < 0x8000_0000L) {
          throw new RuntimeException("Illegal transfer destination for decompression 0x" + Long.toHexString(transferDest));
        }

        fileTransferDest_800bb488.setu(transferDest);
        return 0;
      }

      case 5 -> {
        final long s1 = numberOfTransfers_800bb490.get() * 0x0800L;
        final long size = numberOfTransfers_800bb490.get() * 0x1000L + 0x100L;
        final long dest = addToLinkedListHead(size);

        if(dest == 0) {
          return -0x1L;
        }

        final long transferDest = dest + size - s1;
        MEMORY.ref(4, transferDest).offset(-0x8L).setu(dest);
        MEMORY.ref(4, transferDest).offset(-0x4L).setu(size);

        if(transferDest < 0x8000_0000L) {
          throw new RuntimeException("Illegal transfer destination for decompression 0x" + Long.toHexString(transferDest));
        }

        fileTransferDest_800bb488.setu(transferDest);
        return 0;
      }

      case 3 -> {
        final long s1 = numberOfTransfers_800bb490.get() * 0x0800L;
        final long size = numberOfTransfers_800bb490.get() * 0x1000L + 0x100L;
        final long dest = addToLinkedListTail(size);

        //LAB_80014c98
        if(dest == 0) {
          return -0x1L;
        }

        final long transferDest = dest + size - s1;
        MEMORY.ref(4, transferDest).offset(-0x8L).setu(dest);
        MEMORY.ref(4, transferDest).offset(-0x4L).setu(size);

        if(transferDest < 0x8000_0000L) {
          throw new RuntimeException("Illegal transfer destination for decompression 0x" + Long.toHexString(transferDest));
        }

        fileTransferDest_800bb488.setu(transferDest);
        return 0;
      }

      case 2 -> {
        final long size = numberOfTransfers_800bb490.get() * 0x800L;
        final long transferDest = addToLinkedListTail(size);

        //LAB_80014cd0
        if(transferDest == 0) {
          return -0x1L;
        }

        if(transferDest < 0x8000_0000L) {
          throw new RuntimeException("Illegal transfer destination for decompression 0x" + Long.toHexString(transferDest));
        }

        fileTransferDest_800bb488.setu(transferDest);
        return 0;
      }

      default -> {
        file.used.set(false);
        popFirstFileIfUnused();
        callbackIndex_8004ddc4.setu(0);
        return -0x1L;
      }
    }
  }

  @Method(0x80014d20L)
  public static void FUN_80014d20() {
    FUN_80014d50();
    decompressCurrentFile();
    executeCurrentlyLoadingFileCallback();
  }

  @Method(0x80014d50L)
  public static void FUN_80014d50() {
    if(!SInitBinLoaded_800bbad0.get()) {
      return;
    }

    callbackArray_8004dddc.get((int)callbackIndex_8004ddc4.get()).deref().run();
    FUN_8002c86c();

    //LAB_80014d94
  }

  @Method(0x80014da4L)
  public static void executeCurrentlyLoadingFileCallback() {
    final FileLoadingInfo file = currentlyLoadingFileInfo_800bb468;

    if(!file.callback.isNull()) {
      LOGGER.info("Executing file callback %08x (param %08x)", file.callback.getPointer(), file.callbackParam.get());
      file.callback.deref().run(transferDest_800bb460.get(), fileSize_800bb464.get(), (long)file.callbackParam.get());
      file.callback.clear();
    }
  }

  @Method(0x80014df0L)
  public static long loadQueuedFile() {
    if(allocateFileTransferDest() != 0) {
      return 0;
    }

    transferIndex_800bb494.setu(0);

    final FileLoadingInfo file = fileLoadingInfoArray_800bbad8.get(0);

    LOGGER.info("Loading file %s to %08x", file.namePtr.deref().get(), fileTransferDest_800bb488.get());

    CDROM.readFromDisk(file.pos, (int)numberOfTransfers_800bb490.get(), fileTransferDest_800bb488.get());
    FUN_80014f64(SyncCode.COMPLETE, null);

    transferIndex_800bb494.setu(-0x1L); // Mark transfer complete
    callbackIndex_8004ddc4.setu(0x2L);

    return 1;
  }

  @Method(0x80014e54L)
  public static long FUN_80014e54() {
    if(transferIndex_800bb494.getSigned() >= 0) {
      return 0;
    }

    if(FUN_80014ef4() == 0) {
      fileLoadingInfoArray_800bbad8.get(0).used.set(false);

      popFirstFileIfUnused();
      callbackIndex_8004ddc4.setu(0);
      return 0;
    }

    //LAB_80014e98
    callbackIndex_8004ddc4.setu(0x3L);

    //LAB_80014ea0
    //LAB_80014ea4
    return 0;
  }

  @Method(0x80014ef4L)
  public static long FUN_80014ef4() {
    if(fileTransferDest_800bb488.get() < 0x8000_0000L) {
      throw new RuntimeException("Illegal transfer destination for decompression 0x" + Long.toHexString(fileTransferDest_800bb488.get()));
    }

    transferDest_800bb460.setu(fileTransferDest_800bb488);
    fileSize_800bb464.setu(fileSize_800bb48c);
    currentlyLoadingFileInfo_800bb468.set(fileLoadingInfoArray_800bbad8.get(0));

    return 0;
  }

  @Method(0x80014f64L)
  public static void FUN_80014f64(final SyncCode syncCode, final byte[] responses) {
    if(syncCode == SyncCode.COMPLETE) {
      resetDmaTransfer(getMethodAddress(Scus94491BpeSegment.class, "FUN_80014fac", SyncCode.class, byte[].class), -0x1L);
    } else {
      callbackIndex_8004ddc4.setu(0x1L);
    }
  }

  @Method(0x80014facL)
  public static void FUN_80014fac(final SyncCode syncCode, final byte[] responses) {
    if(syncCode == SyncCode.DATA_READY) {
      beginCdromTransfer(fileTransferDest_800bb488.get() + transferIndex_800bb494.get() * 0x800L, 0x200L);

      transferIndex_800bb494.addu(1);

      if(transferIndex_800bb494.get() >= numberOfTransfers_800bb490.get()) {
        FUN_80036674();
        transferIndex_800bb494.setu(-0x1L);
      }
    } else {
      //LAB_80015024
      callbackIndex_8004ddc4.setu(0x1L);
    }

    //LAB_8001502c
  }

  @Method(0x800151a0L)
  public static void popFirstFileIfUnused() {
    if(fileLoadingInfoArray_800bbad8.get(0).used.get()) {
      return;
    }

    fileCount_8004ddc8.subu(0x1L);

    //LAB_800151d4
    for(int i = 0; i < fileCount_8004ddc8.get(); i++) {
      final FileLoadingInfo file1 = fileLoadingInfoArray_800bbad8.get(i + 1);
      final FileLoadingInfo file2 = fileLoadingInfoArray_800bbad8.get(i);
      file2.set(file1);
    }

    //LAB_80015230
    fileLoadingInfoArray_800bbad8.get((int)fileCount_8004ddc8.get()).used.set(false);

    //LAB_80015244
  }

  @Method(0x8001524cL)
  public static long loadFile(final long param_1, final long transferDest, final long callback, final long callbackParam, final long param_5) {
    final long s0 = FUN_800155b8(MEMORY.ref(2, param_1).getSigned(), transferDest, param_5);

    if(s0 < 0) {
      return -0x1L;
    }

    final FileLoadingInfo file = addFile();
    if(file == null) {
      assert false : "File stack overflow";
      return -0x1L;
    }

    file.callback.set(MEMORY.ref(4, callback).cast(TriConsumerRef::new));
    file.transferDest.setu(transferDest);
    file.namePtr.set(MEMORY.ref(4, param_1).offset(0x4L).deref(20).cast(CString::new));
    file.callbackParam.set((int)callbackParam);
    file.type.set((short)s0);
    file.used.set(true);
    setLoadingFilePosAndSizeFromFile(file, param_1);

    LOGGER.info(file);

    return 0;
  }

  @Method(0x80015310L)
  public static long loadDrgnBinFile(final long index, final long fileIndex, final long fileTransferDest, final long callback, final long callbackParam, final long param_6) {
    final long drgnIndex = Math.min(index, 2);

    //LAB_80015388
    //LAB_8001538c
    final long s2 = FUN_800155b8(_8004dda0.offset(drgnIndex * 8).getSigned(), fileTransferDest, param_6);
    if(s2 < 0) {
      return -0x1L;
    }

    LOGGER.info("Queueing DRGN%d file %d (if you're a programmer), %d (if you're a zamboni)", index, fileIndex, fileIndex + 1);

    final FileLoadingInfo file = addFile();
    if(file == null) {
      //LAB_800153d4
      assert false : "File stack overflow";
      return -0x1L;
    }

    //LAB_800153dc
    file.callback.set(MEMORY.ref(4, callback).cast(TriConsumerRef::new));
    file.transferDest.setu(fileTransferDest);
    file.namePtr.set(fileNamePtr_8004dda4.offset(drgnIndex * 8).deref(16).cast(CString::new));
    file.callbackParam.set((int)callbackParam);
    file.type.set((short)s2);
    file.used.set(true);
    getDrgnFilePos(file, drgnIndex, fileIndex);

    LOGGER.info(file);

    //LAB_80015424
    return 0;
  }

  @Method(0x8001557cL)
  @Nullable
  public static FileLoadingInfo addFile() {
    if(fileCount_8004ddc8.get() >= 44L) {
      LOGGER.error("File stack overflow");
      return null;
    }

    final FileLoadingInfo file = fileLoadingInfoArray_800bbad8.get((int)fileCount_8004ddc8.get());
    fileCount_8004ddc8.addu(0x1L);

    return file;
  }

  @Method(0x800155b8L)
  public static long FUN_800155b8(final long a0, final long fileTransferDest, long a2) {
    if(a0 < 0) {
      return -0x1L;
    }

    a2 = a2 & 0xffffffefL | 0x8L;

    //LAB_800155d0
    if(fileTransferDest == 0 && (a2 & 0x8000L) == 0) {
      //LAB_800155ec
      if((a2 & 0x6L) == 0) {
        a2 |= 0x2L;
      }

      return a2;
    }

    //LAB_800155e4
    //LAB_800155fc
    return a2 & 0xfffffff9L;
  }

  @Method(0x80015604L)
  public static void setLoadingFilePosAndSizeFromFile(final FileLoadingInfo loadingFile, final long pointerToFileIndexMaybe) {
    final CdlFILE file = CdlFILE_800bb4c8.get((int)MEMORY.ref(2, pointerToFileIndexMaybe).get());
    loadingFile.pos.set(file.pos);
    loadingFile.size.set(file.size.get());
  }

  @Method(0x80015644L)
  public static long getDrgnFilePos(final FileLoadingInfo file, final long drgnIndex, final long fileIndex) {
    final long sector = CdlFILE_800bb4c8.get((int)_8004dda0.offset(drgnIndex * 8).get()).pos.pack();

    final MrgEntry entry = drgnMrg_800bc060.get((int)drgnIndex).deref().entries.get((int)fileIndex);

    file.pos.unpack(sector + entry.offset.get());
    file.size.set((int)entry.size.get());

    return 0;
  }

  @Method(0x800156f4L)
  public static void FUN_800156f4(final long a0) {
    _8004ddcc.setu(a0 == 0 ? 0 : 1);
  }

  @Method(0x80015704L)
  public static long FUN_80015704(long a0, long a1) {
    a1 = a1 - 0x1L;
    long a2 = MEMORY.ref(4, a0).offset(0x4L).get() * 0x8L + 0x8L;
    a0 = a0 + a1 * 0x8L;

    //LAB_80015724
    while((int)a1 >= 0) {
      final long v0 = (MEMORY.ref(4, a0).offset(0xcL).get() + 0x3L) & 0xffff_fffcL;
      final long v1 = MEMORY.ref(4, a0).offset(0x8L).get() + v0;

      if((int)a2 < (int)v1) {
        a2 = v1;
      }

      //LAB_80015748
      a0 = a0 - 0x8L;
      a1 = a1 - 0x1L;
    }

    //LAB_80015754
    return a2;
  }

  @Method(0x8001575cL)
  public static void executeScripts1() {
    isStackPointerModified_1f8003bc.set(true);
//    oldStackPointer_1f8003b8.setu(sp);
//    sp = temporaryStack_1f8003b4.getAddress();
    executeScriptFrame();
    executeScriptCallbacks1();
    scriptStateUpperBound_8004de4c.setu(0x9L);
    isStackPointerModified_1f8003bc.set(false);
//    sp = oldStackPointer_1f8003b8.get();
  }

  @Method(0x800157b8L)
  public static void executeScripts2() {
    isStackPointerModified_1f8003bc.set(true);
//    oldStackPointer_1f8003b8.setu(sp);
//    sp = temporaryStack_1f8003b4.getAddress();
    executeScriptCallbacks2();
    isStackPointerModified_1f8003bc.set(false);
//    sp = oldStackPointer_1f8003b8.get();
  }

  @Method(0x80015800L)
  public static long findFreeScriptState() {
    scriptStateUpperBound_8004de4c.addu(0x1L);

    if(scriptStateUpperBound_8004de4c.get() >= 0x48L) {
      scriptStateUpperBound_8004de4c.setu(0x9L);
    }

    //LAB_80015824
    //LAB_8001584c
    for(int i = (int)scriptStateUpperBound_8004de4c.get(); i < 0x48; i++) {
      if(scriptStatePtrArr_800bc1c0.get(i).getPointer() == scriptState_800bc0c0.getAddress()) {
        //LAB_800158c0
        scriptStateUpperBound_8004de4c.setu(i);
        return i;
      }
    }

    //LAB_8001586c
    //LAB_80015898
    for(int i = 0x9; i < scriptStateUpperBound_8004de4c.get(); i++) {
      if(scriptStatePtrArr_800bc1c0.get(i).getPointer() == scriptState_800bc0c0.getAddress()) {
        //LAB_800158c0
        scriptStateUpperBound_8004de4c.setu(i);
        return i;
      }
    }

    //LAB_800158b8
    return -0x1L;
  }

  @Method(0x800158ccL)
  public static long allocateScriptState(final long innerStructSize) {
    return allocateScriptState(innerStructSize, BigStruct::new);
  }

  public static <T extends MemoryRef> long allocateScriptState(final long innerStructSize, final Function<Value, T> type) {
    final long index = findFreeScriptState();

    if(index < 0) {
      return -0x1L;
    }

    return allocateScriptState(index, innerStructSize, false, 0, 0, type);
  }

  /**
   * @return index, or -1 on failure to allocate memory
   */
  @Method(0x80015918L)
  public static <T extends MemoryRef> long allocateScriptState(final long index, long innerStructSize, final boolean allocateOnHead, final long a3, final long a4) {
    return allocateScriptState(index, innerStructSize, allocateOnHead, a3, a4, BigStruct::new);
  }

  /**
   * @return index, or -1 on failure to allocate memory
   */
  public static <T extends MemoryRef> long allocateScriptState(final long index, long innerStructSize, final boolean allocateOnHead, final long a3, final long a4, final Function<Value, T> type) {
    final long linkedListAddress;
    if(allocateOnHead) {
      linkedListAddress = addToLinkedListHead(innerStructSize + 0x100L);
    } else {
      //LAB_80015954
      linkedListAddress = addToLinkedListTail(innerStructSize + 0x100L);
    }

    //LAB_80015968
    if(linkedListAddress == 0) {
      return -0x1L;
    }

    final ScriptState<T> scriptState = MEMORY.ref(4, linkedListAddress, ScriptState.of(type));

    //LAB_80015978
    scriptStatePtrArr_800bc1c0.get((int)index).set(scriptState);

    if(innerStructSize != 0) {
      //LAB_800159ac
      for(int i = 0; i < innerStructSize; i += 4) {
        MEMORY.ref(4, linkedListAddress).offset(0x100L).offset(i).setu(0);
      }

      scriptState.innerStruct_00.setPointer(linkedListAddress + 0x100L);
    } else {
      scriptState.innerStruct_00.clear();
    }

    //LAB_800159c0
    for(int i = 0; i < scriptState.commandStack_1c.length(); i++) {
      scriptState.commandStack_1c.get(i).clear();
    }

    scriptState.storage_44.get(0).set(index);
    scriptState.storage_44.get(1).set(0xffff_ffffL);
    scriptState.storage_44.get(2).set(0xffff_ffffL);
    scriptState.storage_44.get(3).set(0xffff_ffffL);
    scriptState.storage_44.get(4).set(0xffff_ffffL);
    scriptState.storage_44.get(5).set(0xffff_ffffL);
    scriptState.storage_44.get(6).set(0xffff_ffffL);
    scriptState.ui_60.set(0x080f_0000L);
    scriptState.storage_44.get(8).set(0xffff_ffffL);
    scriptState.storage_44.get(9).set(0xffff_ffffL);
    scriptState.storage_44.get(10).set(0xffff_ffffL);
    scriptState.storage_44.get(11).set(0xffff_ffffL);
    scriptState.storage_44.get(12).set(0xffff_ffffL);
    scriptState.storage_44.get(13).set(0xffff_ffffL);
    scriptState.storage_44.get(14).set(0xffff_ffffL);
    scriptState.storage_44.get(15).set(0xffff_ffffL);
    scriptState.storage_44.get(16).set(0xffff_ffffL);
    scriptState.storage_44.get(17).set(0xffff_ffffL);
    scriptState.storage_44.get(18).set(0xffff_ffffL);
    scriptState.storage_44.get(19).set(0xffff_ffffL);
    scriptState.storage_44.get(20).set(0xffff_ffffL);
    scriptState.storage_44.get(21).set(0xffff_ffffL);
    scriptState.storage_44.get(22).set(0xffff_ffffL);
    scriptState.storage_44.get(23).set(0xffff_ffffL);
    scriptState.storage_44.get(24).set(0xffff_ffffL);

    //LAB_800159f8
    //LAB_80015a14
    scriptState.ui_f8.set(a3);
    scriptState.ui_fc.set(a4);

    //LAB_80015a34
    return index;
  }

  @Method(0x80015a68L)
  public static <T extends MemoryRef> void setCallback04(final long index, @Nullable final TriConsumerRef<Integer, ScriptState<T>, T> callback) {
    final ScriptState<T> struct = (ScriptState<T>)scriptStatePtrArr_800bc1c0.get((int)index).deref();

    if(callback == null) {
      //LAB_80015aa0
      struct.callback_04.clear();
      struct.ui_60.or(0x0004_0000L);
    } else {
      struct.callback_04.set(callback);
      struct.ui_60.and(0xfffb_ffffL);
    }
  }

  @Method(0x80015ab4L)
  public static <T extends MemoryRef> void setCallback08(final long index, @Nullable final TriConsumerRef<Integer, ScriptState<T>, T> callback) {
    final ScriptState<T> struct = (ScriptState<T>)scriptStatePtrArr_800bc1c0.get((int)index).deref();

    if(callback == null) {
      //LAB_80015aec
      struct.callback_08.clear();
      struct.ui_60.or(0x0008_0000L);
    } else {
      struct.callback_08.set(callback);
      struct.ui_60.and(0xfff7_ffffL);
    }
  }

  @Method(0x80015b00L)
  public static <T extends MemoryRef> void setCallback0c(final long index, @Nullable final TriConsumerRef<Integer, ScriptState<T>, T> callback) {
    final ScriptState<T> struct = (ScriptState<T>)scriptStatePtrArr_800bc1c0.get((int)index).deref();

    if(callback == null) {
      //LAB_80015b38
      struct.callback_0c.clear();
      struct.ui_60.or(0x0800_0000L);
    } else {
      struct.callback_0c.set(callback);
      struct.ui_60.and(0xf7ff_ffffL);
    }
  }

  @Method(0x80015b4cL)
  public static <T extends MemoryRef> void setCallback10(final long index, @Nullable final TriFunctionRef<Integer, ScriptState<T>, T, Long> callback) {
    final ScriptState<T> struct = (ScriptState<T>)scriptStatePtrArr_800bc1c0.get((int)index).deref();

    if(callback == null) {
      //LAB_80015b80
      struct.callback_10.clear();
      struct.ui_60.and(0xfbff_ffffL);
    } else {
      struct.callback_10.set(callback);
      struct.ui_60.or(0x0400_0000L);
    }
  }

  @Method(0x80015b98L)
  public static void loadScriptFile(final long index, @Nullable final ScriptFile script) {
    loadScriptFile(index, script, 0);
  }

  @Method(0x80015bb8L)
  public static void loadScriptFile(final long index, @Nullable final ScriptFile script, final long offsetIndex) {
    final ScriptState<?> struct = scriptStatePtrArr_800bc1c0.get((int)index).deref();

    if(script != null) {
      LOGGER.info("Loading script index %d from 0x%08x (entry point 0x%x)", index, script.getAddress(), offsetIndex);

      struct.scriptPtr_14.set(script);
      struct.commandPtr_18.set(script.offsetArr_00.get((int)offsetIndex).deref());
      struct.ui_60.and(0xfffd_ffffL);
    } else {
      LOGGER.info("Clearing script index %d", index);

      struct.scriptPtr_14.clear();
      struct.commandPtr_18.clear();
      struct.ui_60.or(0x0002_0000L);
    }
  }

  @Method(0x80015c20L)
  public static void FUN_80015c20(final long a0) {
    final ScriptState<BigStruct> scriptState = scriptStatePtrArr_800bc1c0.get((int)a0).derefAs(ScriptState.classFor(BigStruct.class));
    final BigStruct struct = scriptState.innerStruct_00.derefNullableAs(BigStruct.class);
    if((scriptState.ui_60.get() & 0x810_0000L) == 0) {
      scriptState.callback_0c.deref().run((int)a0, scriptState, struct);
    }

    //LAB_80015c70
    scriptStatePtrArr_800bc1c0.get((int)a0).set(scriptState_800bc0c0);
    removeFromLinkedList(scriptState.getAddress());
  }

  @Method(0x80015c9cL)
  public static void FUN_80015c9c(final long a0) {
    final ScriptState<?> scriptState = scriptStatePtrArr_800bc1c0.get((int)a0).deref();

    long a0_0 = scriptStatePtrArr_800bc1c0.get((int)a0).deref().storage_44.get(6).get();

    //LAB_80015cdc
    while((int)a0_0 >= 0) {
      final long s0 = scriptStatePtrArr_800bc1c0.get((int)a0_0).deref().storage_44.get(6).get();
      FUN_80015c20(a0_0);
      a0_0 = s0;
    }

    //LAB_80015d04
    scriptState.storage_44.get(6).set(0xffff_ffffL);
    scriptState.storage_44.get(7).and(0xffdf_ffffL);
  }

  @Method(0x80015d38L)
  public static void FUN_80015d38(final long a0) {
    FUN_80015c9c(a0);
    FUN_80015c20(a0);
  }

  @Method(0x80015f64L)
  public static long scriptNotImplemented(final RunningScript a0) {
    assert false;
    return 0x2L;
  }

  @Method(0x80015f6cL)
  public static void executeScriptFrame() {
    long v0;
    long v1;

    if(_800bc0b9.get() != 0 || _800bc0b8.get() != 0) {
      return;
    }

    RunningScript_800bc070.ui_1c.set(0);

    //LAB_80015fd8
    for(int index = 0; index < 0x48; index++) {
      final ScriptState<BigStruct> scriptState = scriptStatePtrArr_800bc1c0.get(index).derefAs(ScriptState.classFor(BigStruct.class));

      if(scriptState.getAddress() != scriptState_800bc0c0.getAddress() && (scriptState.ui_60.get() & 0x12_0000L) == 0) {
        System.err.println("Exec script index " + index);

        RunningScript_800bc070.scriptStateIndex_00.set(index);
        RunningScript_800bc070.scriptState_04.set(scriptState);
        RunningScript_800bc070.commandPtr_0c.set(scriptState.commandPtr_18.deref());
        RunningScript_800bc070.parentPtr_08.set(scriptState.commandPtr_18.deref());

        long ret;
        //LAB_80016018
        do {
          final long parentCommand = RunningScript_800bc070.commandPtr_0c.deref().get();
          RunningScript_800bc070.parentCallbackIndex_10.set(parentCommand & 0xffL);
          RunningScript_800bc070.childCount_14.set(parentCommand >>> 8 & 0xffL);
          RunningScript_800bc070.parentParam_18.set(parentCommand >>> 16);

          System.err.println(Long.toHexString(RunningScript_800bc070.commandPtr_0c.getPointer()) + ": " + Long.toHexString(RunningScript_800bc070.commandPtr_0c.getPointer() - RunningScript_800bc070.scriptState_04.deref().scriptPtr_14.getPointer()) + " -- Parent callback: " + Long.toHexString(parentCommand & 0xffL) + ", children: " + Long.toHexString(parentCommand >>> 8 & 0xffL) + ", params: " + Long.toHexString(parentCommand >>> 16) + " (" + Long.toHexString(parentCommand) + ')');

          RunningScript_800bc070.commandPtr_0c.incr();

          //LAB_80016050
          for(int childIndex = 0; childIndex < RunningScript_800bc070.childCount_14.get(); childIndex++) {
            final long childCommand = RunningScript_800bc070.commandPtr_0c.deref().get();
            final int operation = (int)(childCommand >>> 24);
            final int param0 = (int)(childCommand >>> 16 & 0xff);
            final int param1 = (int)(childCommand >>> 8 & 0xff);
            final int param2 = (int)(childCommand & 0xff);

            System.err.println("Op: " + Long.toHexString(operation) + ", params: " + Long.toHexString(childCommand & 0xff_ffffL) + " (" + Long.toHexString(childCommand) + ')');

            RunningScript_800bc070.commandPtr_0c.incr();
            final long commandPtr = RunningScript_800bc070.commandPtr_0c.getPointer();

            if(operation == 0) {
              //LAB_80016574
              RunningScript_800bc070.params_20.get(childIndex).set(RunningScript_800bc070.commandPtr_0c.deref()).decr();
            } else if(operation == 0x1L) {
              //LAB_800161f4
              RunningScript_800bc070.params_20.get(childIndex).set(RunningScript_800bc070.commandPtr_0c.deref());
              RunningScript_800bc070.commandPtr_0c.incr();
            } else if(operation == 0x2L) {
              //LAB_80016200
              RunningScript_800bc070.params_20.get(childIndex).set(RunningScript_800bc070.scriptState_04.deref().storage_44.get(param2));
            } else if(operation == 0x3L) {
              //LAB_800160cc
              //LAB_8001620c
              final long a0_0 = RunningScript_800bc070.scriptState_04.deref().storage_44.get(param2).get();
              final long a1_0 = scriptStatePtrArr_800bc1c0.get((int)a0_0).deref().storage_44.get(param1).get();
              RunningScript_800bc070.params_20.get(childIndex).set(scriptStatePtrArr_800bc1c0.get((int)a1_0).deref().storage_44.get(param0));
            } else if(operation == 0x4L) {
              //LAB_80016258
              final long a0_0 = RunningScript_800bc070.scriptState_04.deref().storage_44.get(param2).get();
              final long a1_0 = param1 + RunningScript_800bc070.scriptState_04.deref().storage_44.get(param0).get();
              RunningScript_800bc070.params_20.get(childIndex).set(scriptStatePtrArr_800bc1c0.get((int)a0_0).deref().storage_44.get((int)a1_0));
            } else if(operation == 0x5L) {
              //LAB_80016290
              RunningScript_800bc070.params_20.get(childIndex).set(scriptPtrs_8004de58.get(param2).deref());
            } else if(operation == 0x6L) {
              //LAB_800162a4
              RunningScript_800bc070.params_20.get(childIndex).set(scriptPtrs_8004de58.get((int)(RunningScript_800bc070.scriptState_04.deref().storage_44.get(param1).get() + param2)).deref());
            } else if(operation == 0x7) {
              //LAB_800162d0
              final long a0_0 = RunningScript_800bc070.scriptState_04.deref().storage_44.get(param1).get();
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, scriptPtrs_8004de58.get(param2).getPointer() + a0_0 * 0x4L, UnsignedIntRef::new));
            } else if(operation == 0x8L) {
              //LAB_800160e8
              //LAB_800162f4
              v0 = RunningScript_800bc070.scriptState_04.deref().storage_44.get(param1).get();
              final long a1_0 = RunningScript_800bc070.scriptState_04.deref().storage_44.get(param0).get();
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, scriptPtrs_8004de58.get((int)(param2 + v0)).getPointer() + a1_0 * 0x4L, UnsignedIntRef::new));
            } else if(operation == 0x9L) {
              //LAB_80016328
              v1 = RunningScript_800bc070.parentPtr_08.getPointer() + (short)childCommand * 0x4L;
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v1, UnsignedIntRef::new));
            } else if(operation == 0xaL) {
              //LAB_80016118
              //LAB_80016334
              v0 = RunningScript_800bc070.parentPtr_08.getPointer() + ((short)childCommand + RunningScript_800bc070.scriptState_04.deref().storage_44.get(param0).get()) * 0x4L;
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v0, UnsignedIntRef::new));
            } else if(operation == 0xbL) {
              //LAB_80016360
              v0 = RunningScript_800bc070.parentPtr_08.getPointer() + (RunningScript_800bc070.scriptState_04.deref().storage_44.get(param0).get() + (short)childCommand) * 0x4L;
              final long a0_0 = RunningScript_800bc070.parentPtr_08.getPointer() + (MEMORY.ref(4, v0).get() + (short)childCommand) * 0x4L;
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, a0_0, UnsignedIntRef::new));
            } else if(operation == 0xcL) {
              //LAB_800163a0
              RunningScript_800bc070.commandPtr_0c.incr();
              v0 = commandPtr + MEMORY.ref(4, commandPtr).offset(RunningScript_800bc070.scriptState_04.deref().storage_44.get(param2).get() * 0x4L).get() * 0x4L + RunningScript_800bc070.scriptState_04.deref().storage_44.get(param1).get() * 0x4L;
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v0, UnsignedIntRef::new));
            } else if(operation == 0xdL) {
              //LAB_800163e8
              RunningScript_800bc070.params_20.get(childIndex).set(scriptStatePtrArr_800bc1c0.get((int)RunningScript_800bc070.scriptState_04.deref().storage_44.get(param2).get()).deref().storage_44.get(param1 + param0));
            } else if(operation == 0xeL) {
              //LAB_80016418
              RunningScript_800bc070.params_20.get(childIndex).set(scriptPtrs_8004de58.get(param1 + param2).deref());
            } else if(operation == 0xfL) {
              //LAB_8001642c
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, scriptPtrs_8004de58.get(param2).getPointer() + param1 * 0x4L, UnsignedIntRef::new));
            } else if(operation == 0x10L) {
              //LAB_80016180
              //LAB_8001643c
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, scriptPtrs_8004de58.get((int)(param2 + RunningScript_800bc070.scriptState_04.deref().storage_44.get(param1).get())).getPointer() + param0 * 0x4L, UnsignedIntRef::new));
            } else if(operation == 0x11L) {
              //LAB_80016468
//              ScriptStruct_800bc070.params_20.get(childIndex).set(scriptPtrs_8004de58.get(parentCommand * 0x4L).deref(4).offset(ScriptStruct_800bc070.scriptState_04.deref().ui_44.get(param0).get() * 0x4L).cast(UnsignedIntRef::new));
              assert false;
            } else if(operation == 0x12L) {
              //LAB_80016138
              //LAB_8001648c
//              ScriptStruct_800bc070.params_20.get(childIndex).set(scriptPtrs_8004de58.offset((param2 + param1) * 0x4L).deref(4).offset(param0 * 0x4L).cast(UnsignedIntRef::new));
              assert false;
            } else if(operation == 0x13L) {
              //LAB_800164a4
              v1 = RunningScript_800bc070.parentPtr_08.getPointer() + ((short)childCommand + param0) * 4;
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v1, UnsignedIntRef::new));
            } else if(operation == 0x14L) {
              //LAB_800164b4
              v1 = RunningScript_800bc070.parentPtr_08.getPointer() + (short)parentCommand * 0x4L;

              //LAB_800164cc
              v0 = MEMORY.ref(4, v1).offset(param0 * 0x4L).get() / 4;

              //LAB_800164d4
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v1 + v0, UnsignedIntRef::new));
            } else if(operation == 0x15L) {
              //LAB_800161a0
              //LAB_800164e0
              RunningScript_800bc070.commandPtr_0c.incr();
              v0 = commandPtr + MEMORY.ref(4, commandPtr).offset(RunningScript_800bc070.scriptState_04.deref().storage_44.get(param2).get() * 0x4L).get() * 0x4L + param1 * 0x4L;

              //LAB_80016580
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v0, UnsignedIntRef::new));
            } else if(operation == 0x16L) {
              //LAB_80016518
              RunningScript_800bc070.commandPtr_0c.incr();
              v0 = commandPtr + MEMORY.ref(4, commandPtr).offset(param2 * 0x4L).get() * 0x4L + RunningScript_800bc070.scriptState_04.deref().storage_44.get(param1).get() * 0x4L;
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v0, UnsignedIntRef::new));
            } else if(operation == 0x17L) {
              //LAB_800161d4
              //LAB_8001654c
              RunningScript_800bc070.commandPtr_0c.incr();
              v0 = commandPtr + MEMORY.ref(4, commandPtr).offset(param2 * 0x4L).get() * 0x4L + param1 * 0x4L;
              RunningScript_800bc070.params_20.get(childIndex).set(MEMORY.ref(4, v0, UnsignedIntRef::new));
            } else {
              assert false : "Unknown op";
            }

            //LAB_80016584
          }

          //LAB_80016598
          ret = scriptFunctions_8004e098.get((int)RunningScript_800bc070.parentCallbackIndex_10.get()).deref().run(RunningScript_800bc070);

          // Returning 0 continues execution
          // Returning 1 pauses execution until the next frame
          // Returning anything else pauses execution and repeats the same instruction next frame
          if(ret == 0 || ret == 0x1L) {
            //LAB_800165e8
            RunningScript_800bc070.parentPtr_08.set(RunningScript_800bc070.commandPtr_0c.deref());
          }
        } while(ret == 0);

        //LAB_800165f4
        if(scriptState.getAddress() != scriptState_800bc0c0.getAddress()) {
          scriptState.commandPtr_18.set(RunningScript_800bc070.parentPtr_08.deref());
        }
      }

      //LAB_80016614
    }

    //LAB_80016624
  }

  @Method(0x8001664cL)
  public static long scriptCompare(final RunningScript a0, final long operandA, final long operandB, final long op) {
    return switch((int)op) {
      case 0x0 -> (int)operandA <= (int)operandB ? 1 : 0;
      case 0x1 -> (int)operandA < (int)operandB ? 1 : 0;
      case 0x2 -> operandA == operandB ? 1 : 0;
      case 0x3 -> operandA != operandB ? 1 : 0;
      case 0x4 -> (int)operandA > (int)operandB ? 1 : 0;
      case 0x5 -> (int)operandA >= (int)operandB ? 1 : 0;
      case 0x6 -> operandA & operandB;
      case 0x7 -> (operandA & operandB) == 0 ? 1 : 0;
      default -> 0;
    };
  }

  /** Stop execution for this frame, resume next frame */
  @Method(0x800166d0L)
  public static long scriptPause(final RunningScript a0) {
    return 0x1L;
  }

  /** Stop execution for this frame, resume next frame and repeat same command */
  @Method(0x800166d8L)
  public static long scriptRewindAndPause(final RunningScript a0) {
    return 0x2L;
  }

  /**
   * Subtracts 1 from work array value 0 if nonzero
   *
   * @return 0 if value is already 0; 2 if value was decremented
   */
  @Method(0x800166e0L)
  public static long scriptWait(final RunningScript a0) {
    if(a0.params_20.get(0).deref().get() != 0) {
      a0.params_20.get(0).deref().sub(0x1L);
      return 0x2L;
    }

    return 0;
  }

  /**
   * <p>Compares work array values 0 and 2 based on an operand stored in the parent param</p>
   *
   * <p>
   *   Operations:
   *   <ol start="0">
   *     <li>Less than or equal to</li>
   *     <li>Less than</li>
   *     <li>Equal</li>
   *     <li>Inequal</li>
   *     <li>Greater than</li>
   *     <li>Greater than or equal to</li>
   *     <li>And</li>
   *     <li>Nand</li>
   *   </ol>
   * </p>
   *
   * @return 0 if comparison succeeds, otherwise return 2
   */
  @Method(0x8001670cL)
  public static long scriptCompare(final RunningScript a0) {
    return scriptCompare(a0, a0.params_20.get(0).deref().get(), a0.params_20.get(1).deref().get(), a0.parentParam_18.get()) == 0 ? 0x2L : 0;
  }

  /**
   * Set work array value 1 to value 0
   *
   * @return 0
   */
  @Method(0x80016774L)
  public static long scriptMove(final RunningScript a0) {
    a0.params_20.get(1).deref().set(a0.params_20.get(0).deref());
    return 0;
  }

  /**
   * Copy block of memory at work array parameter 1 to block of memory at work array parameter 2. Word count is at work array parameter 0.
   *
   * @return 0
   */
  @Method(0x800167bcL)
  public static long scriptMemCopy(final RunningScript a0) {
    Pointer<UnsignedIntRef> dest = a0.params_20.get(2);
    Pointer<UnsignedIntRef> src = a0.params_20.get(1);
    long count = (int)(a0.params_20.get(0).deref().get() << 2) >> 2;

    if(dest.getPointer() < src.getPointer()) {
      count--;

      //LAB_800167e8
      while((int)count >= 0) {
        dest.deref().set(src.deref());
        src.incr();
        dest.incr();
        count--;
      }

      return 0;
    }

    //LAB_8001680c
    if(src.getPointer() < dest.getPointer()) {
      count--;

      dest.add(count * 0x4L);
      src.add(count * 0x4L);

      //LAB_8001682c
      while((int)count >= 0) {
        dest.deref().set(src.deref());
        src.decr();
        dest.decr();
        count--;
      }
    }

    //LAB_80016848
    return 0;
  }

  @Method(0x80016854L)
  public static long scriptSetZero(final RunningScript a0) {
    a0.params_20.get(0).deref().set(0);
    return 0;
  }

  @Method(0x80016868L)
  public static long scriptAnd(final RunningScript a0) {
    a0.params_20.get(1).deref().and(a0.params_20.get(0).deref());
    return 0;
  }

  @Method(0x8001688cL)
  public static long scriptOr(final RunningScript a0) {
    a0.params_20.get(1).deref().or(a0.params_20.get(0).deref());
    return 0;
  }

  @Method(0x800168b0L)
  public static long FUN_800168b0(final RunningScript a0) {
    a0.params_20.get(1).deref().xor(a0.params_20.get(0).deref().get());
    return 0;
  }

  /**
   * Shift work array value 1 left by value 0 bits
   *
   * @return 0
   */
  @Method(0x80016920L)
  public static long scriptShiftLeft(final RunningScript a0) {
    a0.params_20.get(1).deref().shl(a0.params_20.get(0).deref());
    return 0;
  }

  /**
   * Shift work array value 1 right (arithmetic) by value 0 bits
   *
   * @return 0
   */
  @Method(0x80016944L)
  public static long scriptShiftRightArithmetic(final RunningScript a0) {
    a0.params_20.get(1).deref().set(((int)a0.params_20.get(1).deref().get() >> a0.params_20.get(0).deref().get()) & 0xffff_ffffL);
    return 0;
  }

  /**
   * Increment work array value 1 by value 0 (overflow allowed)
   *
   * @return 0
   */
  @Method(0x80016968L)
  public static long scriptAdd(final RunningScript a0) {
    a0.params_20.get(1).deref().addOverflow(a0.params_20.get(0).deref());
    return 0;
  }

  /**
   * Decrement work array value 1 by value 0 (overflow allowed)
   *
   * @return 0
   */
  @Method(0x8001698cL)
  public static long scriptSubtract(final RunningScript a0) {
    a0.params_20.get(1).deref().subOverflow(a0.params_20.get(0).deref());
    return 0;
  }

  /**
   * Increment work array value 0 by 1
   *
   * @return 0
   */
  @Method(0x800169d4L)
  public static long scriptIncrementBy1(final RunningScript a0) {
    a0.params_20.get(0).deref().incrOverflow();
    return 0;
  }

  /**
   * Decrement work array value 0 by 1
   *
   * @return 0
   */
  @Method(0x800169f4L)
  public static long scriptDecrementBy1(final RunningScript a0) {
    a0.params_20.get(0).deref().decrOverflow();
    return 0;
  }

  @Method(0x80016a14L)
  public static long scriptNegate(final RunningScript a0) {
    a0.params_20.get(0).deref().set(-(int)a0.params_20.get(0).deref().get() & 0xffff_ffffL);
    return 0;
  }

  /**
   * Multiply work array value 1 by value 0 (overflow allowed)
   *
   * @return 0
   */
  @Method(0x80016a5cL)
  public static long scriptMultiply(final RunningScript a0) {
    a0.params_20.get(1).deref().mulOverflow(a0.params_20.get(0).deref());
    return 0;
  }

  /**
   * Divide work array value 1 by value 0
   *
   * @return 0
   */
  @Method(0x80016a84L)
  public static long scriptDivide(final RunningScript a0) {
    a0.params_20.get(1).deref().div(a0.params_20.get(0).deref());
    return 0;
  }

  @Method(0x80016b2cL)
  public static long FUN_80016b2c(final RunningScript a0) {
    a0.params_20.get(1).deref().set((((int)a0.params_20.get(1).deref().get() >> 4) * ((int)a0.params_20.get(0).deref().get() >> 4) >> 4) & 0xffff_ffffL);
    return 0;
  }

  /**
   * Calculate square root of work array value 0 and store in value 1
   *
   * @return 0
   */
  @Method(0x80016bbcL)
  public static long scriptSquareRoot(final RunningScript a0) {
    a0.params_20.get(1).deref().set(SquareRoot0(a0.params_20.get(0).deref().get()));
    return 0;
  }

  @Method(0x80016c00L)
  public static long FUN_80016c00(final RunningScript a0) {
    a0.params_20.get(1).deref().set(((int)a0.params_20.get(0).deref().get() * FUN_800133ac()) >>> 16);
    return 0;
  }

  @Method(0x80016c4cL)
  public static long scriptSin(final RunningScript a0) {
    a0.params_20.get(1).deref().set(sin_cos_80054d0c.offset(2, (a0.params_20.get(0).deref().get() & 0xfffL) * 0x4L).getSigned() & 0xffff_ffffL); // Needs to be stored as unsigned
    return 0;
  }

  @Method(0x80016c80L)
  public static long scriptCos(final RunningScript a0) {
    a0.params_20.get(1).deref().set(sin_cos_80054d0c.offset(2, (a0.params_20.get(0).deref().get() & 0xfffL) * 0x4L).offset(0x2L).getSigned() & 0xffff_ffffL); // Needs to be stored as unsigned
    return 0;
  }

  @Method(0x80016cb4L)
  public static long FUN_80016cb4(final RunningScript a0) {
    a0.params_20.get(2).deref().set(ratan2(a0.params_20.get(0).deref().get(), a0.params_20.get(1).deref().get()) & 0xffff_ffffL);
    return 0;
  }

  /**
   * Executes the sub-function at {@link legend.game.Scus94491BpeSegment_8004#scriptSubFunctions_8004e29c} denoted by the parent param
   *
   * @return The value that the sub-function returns
   */
  @Method(0x80016cfcL)
  public static long scriptExecuteSubFunc(final RunningScript a0) {
    return scriptSubFunctions_8004e29c.get((int)a0.parentParam_18.get()).deref().run(a0);
  }

  /**
   * Jump to the value at work array element 0
   *
   * @return 0
   */
  @Method(0x80016d38L)
  public static long scriptJump(final RunningScript a0) {
    a0.commandPtr_0c.set(a0.params_20.get(0).deref());
    return 0;
  }

  /**
   * <p>Compares value at work array element 0 to element 1 using operation denoted by parent param. If true, jumps to value at element 2.</p>
   *
   * <p>
   *   Operations:
   *   <ol start="0">
   *     <li>Less than or equal to</li>
   *     <li>Less than</li>
   *     <li>Equal</li>
   *     <li>Inequal</li>
   *     <li>Greater than</li>
   *     <li>Greater than or equal to</li>
   *     <li>And</li>
   *     <li>Nand</li>
   *   </ol>
   * </p>
   *
   * @return 0
   */
  @Method(0x80016d4cL)
  public static long scriptConditionalJump(final RunningScript a0) {
    if(scriptCompare(a0, a0.params_20.get(0).deref().get(), a0.params_20.get(1).deref().get(), a0.parentParam_18.get()) != 0) {
      a0.commandPtr_0c.set(a0.params_20.get(2).deref());
    }

    //LAB_80016d8c
    return 0;
  }


  /**
   * <p>Compares constant 0 to work array element 0 using operation denoted by parent param. If true, jumps to value at element 1.</p>
   *
   * <p>
   *   Operations:
   *   <ol start="0">
   *     <li>Less than or equal to</li>
   *     <li>Less than</li>
   *     <li>Equal</li>
   *     <li>Inequal</li>
   *     <li>Greater than</li>
   *     <li>Greater than or equal to</li>
   *     <li>And</li>
   *     <li>Nand</li>
   *   </ol>
   * </p>
   *
   * @return 0
   */
  @Method(0x80016da0L)
  public static long scriptConditionalJump0(final RunningScript a0) {
    if(scriptCompare(a0, 0, a0.params_20.get(0).deref().get(), a0.parentParam_18.get()) != 0) {
      a0.commandPtr_0c.set(a0.params_20.get(1).deref());
    }

    //LAB_80016dd8
    return 0;
  }

  @Method(0x80016decL)
  public static long FUN_80016dec(final RunningScript a0) {
    a0.params_20.get(0).deref().decr();

    if(a0.params_20.get(0).deref().get() != 0) {
      a0.commandPtr_0c.set(a0.params_20.get(1).deref());
    }

    //LAB_80016e14
    return 0;
  }

  @Method(0x80016e1cL)
  public static long FUN_80016e1c(final RunningScript a0) {
    a0.commandPtr_0c.set(MEMORY.ref(4, a0.params_20.get(1).getPointer()).offset(MEMORY.ref(4, a0.params_20.get(1).getPointer()).offset(a0.params_20.get(0).deref().get() * 0x4L).get() * 0x4L).cast(UnsignedIntRef::new));
    return 0;
  }

  /**
   * Pushes the current command to the command stack and jumps to the value at work array element 0.
   *
   * @return 0
   */
  @Method(0x80016e50L)
  public static long scriptJumpAndLink(final RunningScript a0) {
    final ScriptState<?> struct = a0.scriptState_04.deref();

    for(int i = struct.commandStack_1c.length() - 1; i > 0; i--) {
      struct.commandStack_1c.get(i).setNullable(struct.commandStack_1c.get(i - 1).derefNullable());
    }

    struct.commandStack_1c.get(0).set(a0.commandPtr_0c.deref());
    a0.commandPtr_0c.set(a0.params_20.get(0).deref());

    return 0;
  }

  /**
   * Return from a JumpAndLink
   *
   * @return 0
   */
  @Method(0x80016f28L)
  public static long scriptJumpReturn(final RunningScript a0) {
    final ScriptState<?> struct = a0.scriptState_04.deref();

    a0.commandPtr_0c.set(struct.commandStack_1c.get(0).deref());

    for(int i = 0; i < struct.commandStack_1c.length() - 1; i++) {
      struct.commandStack_1c.get(i).setNullable(struct.commandStack_1c.get(i + 1).derefNullable());
    }

    struct.commandStack_1c.get(struct.commandStack_1c.length() - 1).clear();

    return 0;
  }

  @Method(0x80016ffcL)
  public static long scriptJumpAndLinkTable(final RunningScript a0) {
    final ScriptState<?> struct = a0.scriptState_04.deref();

    for(int i = struct.commandStack_1c.length() - 1; i > 0; i--) {
      struct.commandStack_1c.get(i).setNullable(struct.commandStack_1c.get(i - 1).derefNullable());
    }

    struct.commandStack_1c.get(0).set(a0.commandPtr_0c.deref());

    // Equivalent to a + a[b] * 4
    final long v1 = a0.params_20.get(1).getPointer();
    a0.commandPtr_0c.set(MEMORY.ref(4, v1 + MEMORY.ref(4, v1).offset(a0.params_20.get(0).deref().get() * 0x4L).getSigned() * 0x4L, UnsignedIntRef::new));
    return 0;
  }

  @Method(0x800172f4L)
  public static long FUN_800172f4(final RunningScript a0) {
    return 0;
  }

  @Method(0x8001734cL)
  public static long scriptRewindAndPause2(final RunningScript a0) {
    return 0x2L;
  }

  @Method(0x80017354L)
  public static long FUN_80017354(final RunningScript a0) {
    gameState_800babc8._4e3.set(a0.params_20.get(0).deref().get() != 0 ? 1 : 0);
    return 0;
  }

  /**
   * <p>Sets or clears a bit in the flags 1 array at {@link Scus94491BpeSegment_800b#gameState_800babc8#scriptFlags1_13c}.</p>
   * <p>If work array element 1 is non-zero, the bit is set. If it's 0, the bit is cleared.</p>
   * <p>The lower 5 bits of work array element 0 is what bit to set (i.e. 1 << n), and the upper 3 bits is the index into the array.</p>
   *
   * @return 0
   */
  @Method(0x80017390L)
  public static long scriptSetGlobalFlag1(final RunningScript a0) {
    final long shift = a0.params_20.get(0).deref().get() & 0x1fL;
    final int index = (int)(a0.params_20.get(0).deref().get() >>> 5);

    if(a0.params_20.get(1).deref().get() != 0) {
      gameState_800babc8.scriptFlags1_13c.get(index).or(0x1L << shift);
    } else {
      //LAB_800173dc
      gameState_800babc8.scriptFlags1_13c.get(index).and(~(0x1L << shift));
    }

    //LAB_800173f4
    return 0;
  }

  /**
   * <p>Reads a bit in the flags 1 array at {@link Scus94491BpeSegment_800b#gameState_800babc8#scriptFlags1_13c}.</p>
   * <p>If the flag is set, a 1 is stored as the value of the work array element 1; otherwise, 0 is stored.</p>
   * <p>The lower 5 bits of work array element 0 is what bit to read (i.e. 1 << n), and the upper 3 bits is the index into the array.</p>
   *
   * @return 0
   */
  @Method(0x800173fcL)
  public static long scriptReadGlobalFlag1(final RunningScript a0) {
    final long shift = a0.params_20.get(0).deref().get() & 0x1fL;
    final int index = (int)(a0.params_20.get(0).deref().get() >>> 5);

    a0.params_20.get(1).deref().set((gameState_800babc8.scriptFlags1_13c.get(index).get() & 0x1L << shift) != 0 ? 1 : 0);

    return 0;
  }

  /**
   * <p>Reads a bit in the flags 2 array at {@link Scus94491BpeSegment_800b#gameState_800babc8#scriptFlags2_bc}.</p>
   * <p>If the flag is set, a 1 is stored as the value of the work array element 1; otherwise, 0 is stored.</p>
   * <p>The lower 5 bits of work array element 0 is what bit to read (i.e. 1 << n), and the upper 3 bits is the index into the array.</p>
   *
   * @return 0
   */
  @Method(0x800174d8L)
  public static long scriptReadGlobalFlag2(final RunningScript a0) {
    final long shift = a0.params_20.get(0).deref().get() & 0x1fL;
    final int index = (int)(a0.params_20.get(0).deref().get() >>> 5);

    a0.params_20.get(1).deref().set((gameState_800babc8.scriptFlags2_bc.get(index).get() & 0x1L << shift) != 0 ? 1 : 0);

    return 0;
  }

  @Method(0x8001751cL)
  public static long scriptStartEffect(final RunningScript a0) {
    scriptStartEffect(a0.params_20.get(0).deref().get(), Math.max(1, a0.params_20.get(1).deref().get()));
    return 0;
  }

  @Method(0x80017584L)
  public static long FUN_80017584(final RunningScript a0) {
    FUN_8002bb38((int)a0.params_20.get(0).deref().get(), a0.params_20.get(1).deref().get());
    return 0;
  }

  @Method(0x800175b4L)
  public static long FUN_800175b4(final RunningScript a0) {
    final long shift = a0.params_20.get(1).deref().get() & 0x1fL;
    final long index = a0.params_20.get(1).deref().get() >>> 5;

    if(a0.params_20.get(2).deref().get() != 0) {
      MEMORY.ref(4, a0.params_20.get(0).getPointer()).offset(index * 0x4L).oru(0x1L << shift);
    } else {
      //LAB_800175fc
      MEMORY.ref(4, a0.params_20.get(0).getPointer()).offset(index * 0x4L).and(~(0x1L << shift));
    }

    //LAB_80017614
    if((int)gameState_800babc8.dragoonSpirits_19c.get(0).get() < 0) {
      gameState_800babc8.charData_32c.get(0).dlevel_13.set(5);
      gameState_800babc8.charData_32c.get(0)._0e.set(0x7fff);
    }

    //LAB_80017640
    return 0;
  }

  @Method(0x80017648L)
  public static long FUN_80017648(final RunningScript a0) {
    final long shift = a0.params_20.get(1).deref().get() & 0x1fL;
    final long index = a0.params_20.get(1).deref().get() >>> 5;

    a0.params_20.get(2).deref().set((MEMORY.ref(4, a0.params_20.get(0).getPointer()).offset(index * 0x4L).get() & (0x1L << shift)) > 0 ? 1 : 0);

    return 0;
  }

  @Method(0x8001770cL)
  public static void executeScriptCallbacks1() {
    if(_800bc0b8.get() != 0 || _800bc0b9.get() != 0) {
      return;
    }

    //LAB_80017750
    for(int i = 0; i < 0x48; i++) {
      final ScriptState<MemoryRef> scriptState = (ScriptState<MemoryRef>)scriptStatePtrArr_800bc1c0.get(i).deref();
      if(scriptState.getAddress() != scriptState_800bc0c0.getAddress()) {
        if((scriptState.ui_60.get() & 0x14_0000L) == 0) {
          scriptState.callback_04.deref().run(i, scriptState, scriptState.innerStruct_00.derefNullable());
        }
      }

      //LAB_80017788
    }

    //LAB_800177ac
    for(int i = 0; i < 0x48; i++) {
      final ScriptState<MemoryRef> scriptState = (ScriptState<MemoryRef>)scriptStatePtrArr_800bc1c0.get(i).deref();
      if(scriptState.getAddress() != scriptState_800bc0c0.getAddress()) {
        if((scriptState.ui_60.get() & 0x410_0000L) == 0x400_0000L) {
          if(scriptState.callback_10.deref().run(i, scriptState, scriptState.innerStruct_00.derefNullable()) != 0) {
            setCallback10(i, null);
          }
        }
      }

      //LAB_800177f8
    }

    //LAB_80017808
  }

  @Method(0x80017820L)
  public static void executeScriptCallbacks2() {
    if(_800bc0b9.get() != 0) {
      return;
    }

    //LAB_80017854
    for(int i = 0; i < 0x48; i++) {
      final ScriptState<MemoryRef> scriptState = (ScriptState<MemoryRef>)scriptStatePtrArr_800bc1c0.get(i).deref();
      if(scriptState.getAddress() != scriptState_800bc0c0.getAddress()) {
        if((scriptState.ui_60.get() & 0x18_0000L) == 0) {
          scriptState.callback_08.deref().run(i, scriptState, scriptState.innerStruct_00.derefNullable());
        }
      }

      //LAB_80017888
    }

    //LAB_80017898
  }

  @Method(0x800178b0L)
  public static void loadSceaLogo() {
    if(SInitBinLoaded_800bbad0.get()) {
      loadDrgnBinFile(0, 0x1669L, 0, getMethodAddress(Scus94491BpeSegment.class, "loadSceaLogoTexture", long.class, long.class, long.class), 1, 5);
    } else {
      loadSceaLogoTexture(sceaTexture_800d05c4.getAddress(), 0, 0);
    }
  }

  @Method(0x80017924L)
  public static void loadSceaLogoTexture(final long address, final long param_2, final long param_3) {
    timHeader_800bc2e0.set(parseTimHeader(MEMORY.ref(4, address).offset(0x4L)));
    final TimHeader header = timHeader_800bc2e0;

    final RECT imageRect = new RECT();
    imageRect.set((short)640, (short)0, header.getImageRect().w.get(), header.getImageRect().h.get());
    LoadImage(imageRect, header.getImageAddress());

    if(header.hasClut()) {
      final RECT clutRect = new RECT();
      clutRect.set((short)640, (short)255, header.getClutRect().w.get(), header.getClutRect().h.get());
      LoadImage(clutRect, header.getClutAddress());
    }

    _800bc300.setu(_800bb120).oru(0xaL);
    _800bc304.setu(_800bb120).oru(0xcL);
    _800bc308.setu(0x3fe8L);

    if(param_3 != 0) {
      FUN_800127cc(address, 0, 1);
    }
  }

  /**
   * Draws the TIM image located at {@link Scus94491BpeSegment_800b#timHeader_800bc2e0}
   *
   * NOTE: elements are added in reverse order
   */
  @Method(0x80017a3cL)
  public static void drawTim(final long colour) {
    final TimHeader tim = timHeader_800bc2e0;

    final Value c0 = linkedListAddress_1f8003d8.deref(4);
    c0.offset(1, 0x03L).setu(0x4L); // OT element size
    c0.offset(1, 0x04L).setu(colour); // R
    c0.offset(1, 0x05L).setu(colour); // G
    c0.offset(1, 0x06L).setu(colour); // B
    c0.offset(1, 0x07L).setu(0x64L); // Textured rectangle, variable size, opaque, texture-blending
    c0.offset(2, 0x08L).setu(-tim.getImageRect().w.get()); // X
    c0.offset(2, 0x0aL).setu(-tim.getImageRect().h.get() / 2); // Y
    c0.offset(1, 0x0cL).setu(0); // TX
    c0.offset(1, 0x0dL).setu(0); // TY
    c0.offset(2, 0x0eL).setu(_800bc308); // CLUT
    c0.offset(2, 0x10L).setu(0x100L); // W
    c0.offset(2, 0x12L).setu(tim.getImageRect().h.get()); // H
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x29).getAddress(), c0.getAddress());

    final Value c1 = linkedListAddress_1f8003d8.deref(4).offset(0x28L);
    c1.offset(1, 0x03L).setu(0x1L); // OT element size
    // Draw mode (texpage), forces dithering and gets the following values from memory:
    // 0-3 texture page x base (n*64)
    // 4   texture page y base (n*256)
    // 5-6 semi-transparency
    // 7-8 texture page colors (0=4-bit, 1=8-bit, 2=15-bit, 3=reserved)
    // 11  texture disable (0=normal, 1=disable if GP1(09h).bit0==1)
    c1.offset(4, 0x04L).setu(0xe1000200L | _800bc300.get(0x9ffL));
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x29).getAddress(), c1.getAddress());

    final Value c2 = linkedListAddress_1f8003d8.deref(4).offset(0x14L);
    c2.offset(1, 0x03L).setu(0x4L); // OT element size
    c2.offset(1, 0x04L).setu(colour); // R
    c2.offset(1, 0x05L).setu(colour); // G
    c2.offset(1, 0x06L).setu(colour); // B
    c2.offset(1, 0x07L).setu(0x64L); // Textured rectangle, variable size, opaque, texture-blending
    c2.offset(2, 0x08L).setu(-tim.getImageRect().w.get() + 0x100L); // X
    c2.offset(2, 0x0aL).setu(-tim.getImageRect().h.get() / 2); // Y
    c2.offset(1, 0x0cL).setu(0); // TX
    c2.offset(1, 0x0dL).setu(0); // TY
    c2.offset(2, 0x0eL).setu(_800bc308); // CLUT
    c2.offset(2, 0x10L).setu(tim.getImageRect().w.get() * 2 - 0x100L); // W
    c2.offset(2, 0x12L).setu(tim.getImageRect().h.get()); // H
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x29).getAddress(), c2.getAddress());

    final Value c3 = linkedListAddress_1f8003d8.deref(4).offset(0x30L);
    c3.offset(1, 0x03L).setu(0x1L); // OT element size
    // Draw mode (texpage), forces dithering and gets the following values from memory:
    // 0-3 texture page x base (n*64)
    // 4   texture page y base (n*256)
    // 5-6 semi-transparency
    // 7-8 texture page colors (0=4-bit, 1=8-bit, 2=15-bit, 3=reserved)
    // 11  texture disable (0=normal, 1=disable if GP1(09h).bit0==1)
    c3.offset(4, 0x04L).setu(0xe1000200L | _800bc304.get(0x9ffL));
    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x29).getAddress(), c3.getAddress());

    linkedListAddress_1f8003d8.addu(0x38L);
  }

  @Method(0x80017c44L)
  public static long FUN_80017c44(final long unused, final long archiveAddress, final long destinationAddress) {
    assert false : "Use decompress";
    return 0;
  }

  @Method(0x80017ce0L)
  public static void FUN_80017ce0(final long archiveAddress, final long destinationAddress, final long sizePtr) {
    assert false : "Use decompress";
  }

  @Method(0x80017d28L)
  public static void FUN_80017d28(final long archiveAddress, final long destinationAddress, final long sizePtr) {
    assert false : "Use decompress";
  }

  @Method(0x80017d58L)
  public static void FUN_80017d58(final long archiveAddress, final long destinationAddress, final long sizePtr) {
    assert false : "Use decompress";
  }

  @Method(0x80017d8cL)
  public static void FUN_80017d8c(final long archiveAddress, final long destinationAddress, final long sizePtr) {
    assert false : "Use decompress";
  }

  @Method(0x80017f94L)
  public static void FUN_80017f94() {
    isStackPointerModified_1f8003bc.set(true);
//    oldStackPointer_1f8003b8.setu(sp);
//    sp = temporaryStack_1f8003b4.getAddress();
    FUN_80017fdc();

    isStackPointerModified_1f8003bc.set(false);
//    sp = oldStackPointer_1f8003b8.get();
  }

  @Method(0x80017fdcL)
  public static void FUN_80017fdc() {
    // empty
  }

  @Method(0x80017fe4L)
  public static long FUN_80017fe4(final long archiveAddress, final long destinationAddress, final long callback, final long callbackParam, final long a4) {
    final long s0;
    if(destinationAddress == 0) {
      if((a4 & 0x4L) != 0) {
        s0 = addToLinkedListHead(MEMORY.ref(4, archiveAddress).get());
      } else {
        //LAB_8001803c
        s0 = addToLinkedListTail(MEMORY.ref(4, archiveAddress).get());
      }

      //LAB_8001804c
      if(s0 == 0) {
        return -0x1L;
      }
    } else {
      s0 = destinationAddress;
    }

    //LAB_8001805c
    //LAB_80018060
    final long size = decompress(archiveAddress, s0);

    if((a4 & 0x1L) != 0) {
      removeFromLinkedList(archiveAddress);
    }

    //LAB_80018088
    MEMORY.ref(4, callback).call(s0, size, callbackParam);

    //LAB_8001809c
    return 0;
  }

  @Method(0x800180c0L)
  public static long FUN_800180c0(final long a0, final long x, final long y) {
    if((x & 0x3fL) != 0 || (y & 0xffL) != 0) {
      //LAB_800180e0
      return 0x1L;
    }

    //LAB_800180e8
    long v1 = MEMORY.ref(4, a0).offset(0x0L).get();
    if(v1 != 0x151_434dL && v1 != 0x251_434dL) {
      return 0x1L;
    }

    //LAB_80018104
    v1 = MEMORY.ref(2, a0).offset(0xaL).get();
    if(v1 != 0x100L) {
      return 0x1L;
    }

    LoadImage(new RECT((short)x, (short)y, (short)MEMORY.ref(2, a0).offset(0x8L).get(), (short)v1), a0 + MEMORY.ref(4, a0).offset(0x4L).get());

    //LAB_8001813c
    return 0;
  }

  @Method(0x8001814cL)
  public static void FUN_8001814c(long a0, long a1, long a2, long a3, long a4, long a5, long a6) {
    long v0;
    long v1;
    long t0;
    long t1;
    long t2;
    long t3;
    long t4;
    long t5;
    long t6;
    long t7;
    long s0;
    long s1;
    long s2;
    long s3;
    long s4;
    long s5;
    long s6;
    long t8;
    long t9;
    long s8;
    t0 = a0;
    t3 = a2;
    s0 = a3;
    a0 = 0x251_0000L;
    s8 = a4;
    a0 = a0 | 0x434dL;
    v0 = MEMORY.ref(2, t0).offset(0xcL).get();
    v1 = MEMORY.ref(2, t0).offset(0xeL).get();
    t9 = a6;
    s2 = MEMORY.ref(2, t0).offset(0x14L).get();
    t8 = MEMORY.ref(2, t0).offset(0x16L).get();
    t6 = a1 + v0;
    t2 = t3 + v1;
    v0 = MEMORY.ref(2, t0).offset(0x10L).get();
    v1 = MEMORY.ref(2, t0).offset(0x12L).get();
    a1 = a1 + v0;
    t3 = t3 + v1;
    t5 = a1 & 0x3c0L;
    a1 = a1 << 2;
    v0 = 0x1f80_0000L;
    s4 = MEMORY.ref(4, v0).offset(0x3d8L).get();
    v0 = MEMORY.ref(4, t0).offset(0x0L).get();

    if(v0 == a0) {
      v0 = MEMORY.ref(2, t0).offset(0x28L).getSigned();
      v1 = MEMORY.ref(2, t0).offset(0x2aL).getSigned();
      s0 = s0 + v0;
      s8 = s8 + v1;
    }
    a3 = t3 & 0x100L;

    //LAB_800181e4
    a0 = 0xe100_0000L;
    a0 = a0 | 0x200L;
    a2 = s4;
    t0 = a2 + 0x8L;
    v0 = 0x1L;
    MEMORY.ref(1, s4).offset(0x3L).setu(v0);
    v0 = 0x800c_0000L;
    t1 = v0 - 0x4ef0L;
    v0 = (int)a3 >> 8;
    v0 = v0 << 1;
    v0 = v0 + t1;
    v1 = t5 & 0x3c0L;
    v0 = MEMORY.ref(2, v0).offset(0x0L).get();
    v1 = (int)v1 >> 6;
    v0 = v0 | v1;
    v0 = v0 & 0x9ffL;
    v0 = v0 | a0;
    MEMORY.ref(4, s4).offset(0x4L).setu(v0);
    v0 = t0 << 8;
    MEMORY.ref(3, a2).setu(v0 >>> 8); // SWL v0,$2(a2)
    v0 = 0x80L;
    if(t9 != v0) {
      //LAB_80018350
      if(s2 != 0) {
        t4 = 0;
        s3 = 0x3L;
        s1 = 0x7c80_0000L;
        s1 = s1 | 0x8080L;
        s6 = t1;
        s5 = t3 & 0x100L;

        //LAB_8001836c
        do {
          if(t8 != 0) {
            t1 = 0;
            v0 = (int)s5 >> 8;
            v0 = v0 << 1;
            t7 = v0 + s6;

            //LAB_80018380
            do {
              a2 = t0;
              v0 = t4 + s0;
              MEMORY.ref(2, a2).offset(0x8L).setu(v0);
              v0 = t1 + s8;
              v1 = t2 << 6;
              MEMORY.ref(2, a2).offset(0xaL).setu(v0);
              v0 = t6 & 0x3f0L;
              v0 = (int)v0 >> 4;
              v1 = v1 | v0;
              MEMORY.ref(1, a2).offset(0x3L).setu(s3);
              MEMORY.ref(4, a2).offset(0x4L).setu(s1);
              MEMORY.ref(1, a2).offset(0x4L).setu(t9);
              MEMORY.ref(1, a2).offset(0x5L).setu(t9);
              MEMORY.ref(1, a2).offset(0x6L).setu(t9);
              MEMORY.ref(1, a2).offset(0xcL).setu(a1);
              MEMORY.ref(1, a2).offset(0xdL).setu(t3);
              MEMORY.ref(2, a2).offset(0xeL).setu(v1);
              t0 = a2 + 0x10L;
              v0 = t0 << 8;
              MEMORY.ref(3, a2).setu(v0 >>> 8); // SWL v0,$2(a2)
              v0 = t3 + 0x10L;
              t3 = v0 & 0xf0L;
              if(t3 == 0) {
                v0 = a1 + 0x10L;
                a1 = v0 & 0xf0L;
                if(a1 == 0) {
                  t5 = t5 + 0x40L;
                  a0 = 0xe100_0000L;
                  a0 = a0 | 0x200L;
                  a2 = t0;
                  v0 = 0x1L;
                  v1 = t5 & 0x3c0L;
                  MEMORY.ref(1, a2).offset(0x3L).setu(v0);
                  v0 = MEMORY.ref(2, t7).offset(0x0L).get();
                  v1 = (int)v1 >> 6;
                  v0 = v0 | v1;
                  v0 = v0 & 0x9ffL;
                  v0 = v0 | a0;
                  MEMORY.ref(4, a2).offset(0x4L).setu(v0);
                  t0 = a2 + 0x8L;
                  v0 = t0 << 8;
                  MEMORY.ref(3, a2).setu(v0 >>> 8); // SWL v0,$2(a2)
                }
              }
              v0 = t2 + 0x1L;

              //LAB_80018434
              t2 = v0 & 0xffL;
              if(t2 == 0) {
                t6 = t6 + 0x10L;
              }

              //LAB_80018444
              t1 = t1 + 0x10L;
              t2 = t2 | a3;
            } while((int)t1 < (int)t8);
          }

          //LAB_80018454
          t4 = t4 + 0x10L;
        } while((int)t4 < (int)s2);
      }
    } else {
      if(s2 != 0) {
        t4 = 0;
        s3 = 0x3L;
        s1 = 0x7c80_0000L;
        s1 = s1 | 0x8080L;
        s6 = t1;
        s5 = t3 & 0x100L;

        //LAB_8001825c
        do {
          if(t8 != 0) {
            t1 = 0;
            t9 = t4 + s0;
            v0 = (int)s5 >> 8;
            v0 = v0 << 1;
            t7 = v0 + s6;

            //LAB_80018274
            do {
              a2 = t0;
              v0 = t1 + s8;
              v1 = t2 << 6;
              MEMORY.ref(2, a2).offset(0xaL).setu(v0);
              v0 = t6 & 0x3f0L;
              v0 = (int)v0 >> 4;
              v1 = v1 | v0;
              MEMORY.ref(1, a2).offset(0x3L).setu(s3);
              MEMORY.ref(4, a2).offset(0x4L).setu(s1);
              MEMORY.ref(2, a2).offset(0x8L).setu(t9);
              MEMORY.ref(1, a2).offset(0xcL).setu(a1);
              MEMORY.ref(1, a2).offset(0xdL).setu(t3);
              MEMORY.ref(2, a2).offset(0xeL).setu(v1);
              t0 = a2 + 0x10L;
              v0 = t0 << 8;
              MEMORY.ref(3, a2).setu(v0 >>> 8); // SWL v0,$2(a2)
              v0 = t3 + 0x10L;
              t3 = v0 & 0xf0L;
              if(t3 == 0) {
                v0 = a1 + 0x10L;
                a1 = v0 & 0xf0L;
                if(a1 == 0) {
                  t5 = t5 + 0x40L;
                  a0 = 0xe100_0000L;
                  a0 = a0 | 0x200L;
                  a2 = t0;
                  v0 = 0x1L;
                  v1 = t5 & 0x3c0L;
                  MEMORY.ref(1, a2).offset(0x3L).setu(v0);
                  v0 = MEMORY.ref(2, t7).offset(0x0L).get();
                  v1 = (int)v1 >> 6;
                  v0 = v0 | v1;
                  v0 = v0 & 0x9ffL;
                  v0 = v0 | a0;
                  MEMORY.ref(4, a2).offset(0x4L).setu(v0);
                  t0 = a2 + 0x8L;
                  v0 = t0 << 8;
                  MEMORY.ref(3, a2).setu(v0 >>> 8); // SWL v0,$2(a2)
                }
              }
              v0 = t2 + 0x1L;

              //LAB_80018318
              t2 = v0 & 0xffL;
              if(t2 == 0) {
                t6 = t6 + 0x10L;
              }

              //LAB_80018328
              t1 = t1 + 0x10L;
              t2 = t2 | a3;
            } while((int)t1 < (int)t8);
          }

          //LAB_80018338
          t4 = t4 + 0x10L;
        } while((int)t4 < (int)s2);
      }
    }

    //LAB_80018464
    v0 = 0x1f80_0000L;
    MEMORY.ref(4, v0).offset(0x3d8L).setu(t0);

    //LAB_8001846c
    FUN_8003b450(tags_1f8003d0.getPointer() + a5 * 0x4L, s4, a2);
  }

  @Method(0x800184b0L)
  public static void processControllerInput() {
    int a0 = (int)vsyncMode_8007a3b8.get();

    if(a0 == 0) {
      a0 = 1;
    }

    FUN_8002ae0c(a0);

    joypadPress_8007a398.setu(_800bee94);
    joypadInput_8007a39c.setu(_800bee90);
    joypadRepeat_8007a3a0.setu(_800bee98);
  }

  @Method(0x80018658L)
  public static void FUN_80018658() {
    FUN_800186a0();
  }

  @Method(0x800186a0L)
  public static void FUN_800186a0() {
    if(_800bc94c.get() != 0) {
      FUN_80018744();
      _8004f5d4.get((int)pregameLoadingStage_800bb10c.get()).deref().run();

      if(_800bc94c.get() != 0) {
        FUN_8001890c();
      }
    } else {
      //LAB_8001870c
      _8004f5d4.get((int)pregameLoadingStage_800bb10c.get()).deref().run();
    }

    //LAB_80018734
  }

  @Method(0x80018744L)
  public static void FUN_80018744() {
    if((_800bc960.get() & 0x400L) != 0) {
      if((_800bc960.get() & 0x8L) == 0 && FUN_800187cc() != 0) {
        _800bc960.oru(0x8L);
      }

      //LAB_80018790
      if((_800bc960.get() & 0x4L) == 0 && FUN_8001886c() != 0) {
        _800bc960.oru(0x4L);
      }
    }

    //LAB_800187b0
    FUN_800c7304();
  }

  @Method(0x800187ccL)
  public static long FUN_800187cc() {
    long v0;
    long a0;
    long s0;
    long s1;
    long s2;
    v0 = 0x800c_0000L;
    v0 = MEMORY.ref(4, v0).offset(0x677cL).get();
    s0 = 0;
    if((int)v0 > 0) {
      v0 = 0x800c_0000L;
      s2 = v0 - 0x3e40L;
      v0 = 0x8007_0000L;
      s1 = v0 - 0x1c68L;

      //LAB_80018800
      do {
        v0 = MEMORY.ref(4, s1).offset(0xe40L).get();

        v0 = v0 << 2;
        v0 = v0 + s2;
        v0 = MEMORY.ref(4, v0).offset(0x0L).get();

        v0 = MEMORY.ref(4, v0).offset(0x0L).get();

        a0 = MEMORY.ref(2, v0).offset(0x26cL).getSigned();

        if(FUN_800c90b0((int)a0) == 0) { //TODO
          return 0;
        }

        //LAB_8001883c
        v0 = 0x800c_0000L;
        v0 = MEMORY.ref(4, v0).offset(0x677cL).get();
        s0 = s0 + 0x1L;
        s1 = s1 + 0x4L;
      } while((int)s0 < (int)v0);
    }

    //LAB_80018850
    //LAB_80018854
    return 1;
  }

  @Method(0x8001886cL)
  public static long FUN_8001886c() {
    long v0;
    long a0;
    long s0;
    long s1;
    long s2;
    v0 = 0x800c_0000L;
    v0 = MEMORY.ref(4, v0).offset(0x6768L).get();
    s0 = 0;
    if((int)v0 > 0) {
      v0 = 0x800c_0000L;
      s2 = v0 - 0x3e40L;
      v0 = 0x8007_0000L;
      s1 = v0 - 0x1c68L;

      //LAB_800188a0
      do {
        v0 = MEMORY.ref(4, s1).offset(0xe50L).get();

        v0 = v0 << 2;
        v0 = v0 + s2;
        v0 = MEMORY.ref(4, v0).offset(0x0L).get();

        v0 = MEMORY.ref(4, v0).offset(0x0L).get();

        a0 = MEMORY.ref(2, v0).offset(0x26cL).getSigned();

        if(FUN_800c90b0((int)a0) == 0) { //TODO
          return 0;
        }

        //LAB_800188dc
        v0 = 0x800c_0000L;
        v0 = MEMORY.ref(4, v0).offset(0x6768L).get();
        s0 = s0 + 0x1L;
        s1 = s1 + 0x4L;
      } while((int)s0 < (int)v0);
    }

    //LAB_800188f0
    //LAB_800188f4
    return 1;
  }

  @Method(0x8001890cL)
  public static void FUN_8001890c() {
    FUN_800d8f10();
    FUN_800c7304();
    FUN_800c8cf0();
    FUN_800c882c();
  }

  @Method(0x80018944L)
  public static void FUN_80018944() {
    if(fileCount_8004ddc8.get() == 0 && _8004dd14.get() == _8004dd18.get() && loadingSmapOvl_8004dd08.get() == 0) {
      pregameLoadingStage_800bb10c.addu(0x1L);
    }

    //LAB_80018990
  }

  @Method(0x80018998L)
  public static void FUN_80018998() {
    pregameLoadingStage_800bb10c.addu(0x1L);
  }

  @Method(0x800194dcL)
  public static void FUN_800194dc() {
    FUN_800fbec8(_8004f65c.getAddress());
  }

  @Method(0x80019500L)
  public static void FUN_80019500() {
    initSound();
    sssqFadeIn(0, 0);
    FUN_8004c3f0(0x8L);
    sssqSetReverbType(0x3L);
    SsSetRVol(0x30, 0x30);

    //LAB_80019548
    for(int i = 0; i < 13; i++) {
      FUN_8001aa44(i);
    }

    FUN_8001aa64();
    FUN_8001aa78();
    FUN_8001aa90();

    //LAB_80019580
    for(int i = 0; i < 32; i++) {
      spu28Arr_800bd110.get(i)._00.set(0);
      spu28Arr_800bd110.get(i)._1c.set(0);
    }

    //LAB_800195a8
    for(int i = 0; i < 13; i++) {
      soundFileArr_800bcf80.get(i).used_00.set(false);
    }

    //LAB_800195c8
    for(int i = 0; i < 7; i++) {
      _800bd610.offset(i * 0x10L).setu(0);
    }

    sssqTempoScale_800bd100.setu(0x100L);
    _800bd780.setu(0);
    _800bd781.setu(0);
  }

  @Method(0x80019710L)
  public static void FUN_80019710() {
    if(mainCallbackIndex_8004dd20.get() != 0x5L && _8004dd28.get() == 0x5L) {
      sssqResetStuff();
      removeFromLinkedList(soundMrgSshdPtr_800bd784.getPointer());
      removeFromLinkedList(soundMrgSssqPtr_800bd788.getPointer());
      _800bd780.setu(0);
    }

    //LAB_8001978c
    //LAB_80019790
    if(mainCallbackIndex_8004dd20.get() != 0x6L && _8004dd28.get() == 0x6L) {
      sssqResetStuff();

      //LAB_800197c0
      for(int i = 0; i < 3; i++) {
        removeFromLinkedList(_800bc980.offset(i * 0xcL).offset(0x4L).get());
      }

      if(_800bd780.get() == 0x1L) {
        removeFromLinkedList(soundMrgSshdPtr_800bd784.getPointer());
        removeFromLinkedList(soundMrgSssqPtr_800bd788.getPointer());
        _800bd780.setu(0);
      }
    }

    //LAB_80019824
    //LAB_80019828
    switch((int)mainCallbackIndex_8004dd20.get()) {
      case 2 -> {
        setMainVolume(0x7f, 0x7f);
        sssqResetStuff();
        FUN_8001aa90();

        //LAB_80019a00
        if(drgnBinIndex_800bc058.get() == 0x1L) {
          // Load main menu background music
          loadMusicPackage(1, 0);
        } else {
          loadMusicPackage(98, 0);
        }
      }

      case 5 -> {
        sssqResetStuff();

        if(_800bd780.get() != 0x1L) {
          //LAB_80019978
          soundMrgSshdPtr_800bd784.set(MEMORY.ref(4, addToLinkedListTail(0x650L), SshdFile::new));
          soundMrgSssqPtr_800bd788.set(MEMORY.ref(4, addToLinkedListTail(0x5c30L), SssqFile::new));
          _800bd780.setu(0x1L);
        }
      }

      case 6 -> {
        sssqResetStuff();

        final long s4 = FUN_8001a810() * 0x2L - 0x1L;

        //LAB_800198e8
        for(int i = 0; i < 3; i++) {
          if(i == 0) {
            _800bc980.offset(i * 0xcL).offset(1, 0x1L).setu(0);
          } else {
            //LAB_800198f8
            _800bc980.offset(i * 0xcL).offset(1, 0x1L).setu(_8004f664.offset(i).offset(s4));
          }

          //LAB_80019908
          _800bc980.offset(i * 0xcL).offset(4, 0x4L).setu(addToLinkedListTail(_8004f6a4.offset(_800bc980.offset(i * 0xcL).offset(1, 0x1L).get() * 0x4L).get()));
          _800bc980.offset(i * 0xcL).offset(4, 0x8L).setu(_8004f6a4.offset(_800bc980.offset(i * 0xcL).offset(1, 0x1L).get() * 0x4L));
        }

        if(_800bd780.get() != 0x1L && submapScene_800bb0f8.get() == 0x1bbL) {
          //LAB_80019978
          soundMrgSshdPtr_800bd784.set(MEMORY.ref(4, addToLinkedListTail(0x650L), SshdFile::new));
          soundMrgSssqPtr_800bd788.set(MEMORY.ref(4, addToLinkedListTail(0x5c30L), SssqFile::new));
          _800bd780.setu(0x1L);
        }
      }

      case 0xf -> {
        FUN_8004d91c(0x1L);
        FUN_8004d034((int)_800bd0f0.offset(0x8L).getSigned(), 0);
        FUN_8004c390((int)_800bd0f0.offset(0x8L).getSigned());
        sssqUnloadPlayableSound(soundFileArr_800bcf80.get(11).playableSoundIndex_10.get());
        sssqUnloadPlayableSound(soundFileArr_800bcf80.get(0).playableSoundIndex_10.get());
      }

      case 0xc -> {
        sssqResetStuff();

        //LAB_80019a00
        loadMusicPackage(60, 0);
      }

      case 0xa -> {
        unloadSoundFile(0);
        sssqResetStuff();
      }

      case 4, 7, 8, 9, 0xb -> {
        sssqResetStuff();
      }
    }

    //case 3, d, e
    //LAB_80019a20
    //LAB_80019a24
    if(mainCallbackIndex_8004dd20.get() != 0x5L) {
      _800bd808.setu(-0x1L);
    }

    //LAB_80019a3c
  }

  /**
   * @param soundIndex 1: up/down, 2: choose menu option, 3: ...
   */
  @Method(0x80019a60L)
  public static void playSound(final int index, final int soundIndex, final long a2, final long a3, final short a4, final short a5) {
    if(!soundFileArr_800bcf80.get(index).used_00.get() || soundFileArr_800bcf80.get(index).playableSoundIndex_10.get() == -1) {
      return;
    }

    switch(index) {
      case 0 -> {
        if((loadedDrgnFiles_800bcf78.get() & 0x1L) != 0) {
          return;
        }
      }

      case 8 -> {
        //LAB_80019bd4
        if((loadedDrgnFiles_800bcf78.get() & 0x2L) != 0 || mainCallbackIndex_8004dd20.get() != 0x5L) {
          return;
        }
      }

      case 9 -> {
        //LAB_80019bd4
        if((loadedDrgnFiles_800bcf78.get() & 0x4L) != 0 || mainCallbackIndex_8004dd20.get() != 0x6L) {
          return;
        }
      }

      case 0xa -> {
        if((loadedDrgnFiles_800bcf78.get() & 0x20L) != 0) {
          return;
        }
      }

      case 0xc -> {
        //LAB_80019bd4
        if((loadedDrgnFiles_800bcf78.get() & 0x8000L) != 0 || mainCallbackIndex_8004dd20.get() != 0x8L) {
          return;
        }
      }
    }

    //LAB_80019be0
    //LAB_80019c00
    final SoundFile spu1c = soundFileArr_800bcf80.get(index);

    for(int i = 0; i < 32; i++) {
      if(spu28Arr_800bd110.get(i)._00.get() == 0) {
        if(index == 0x8L) {
          //LAB_80019b54
          FUN_8001a714(3, index, soundIndex, i, spu1c.playableSoundIndex_10.get(), spu1c.ptr_08.get() + soundIndex * 0x2L, MEMORY.ref(1, spu1c.ptr_0c.get()).offset(soundIndex).get(), (short)-1, (short)-1, (short)-1, a5, a4, -1);
        } else {
          FUN_8001a714(3, index, soundIndex, i, spu1c.playableSoundIndex_10.get(), spu1c.ptr_08.get() + soundIndex * 0x2L, 0, (short)-1, (short)-1, (short)-1, a5, a4, -1);
        }

        break;
      }
    }

    //LAB_80019c70
  }

  @Method(0x80019c80L)
  public static void FUN_80019c80(final long a0, final long a1, final long a2) {
    long v0;
    long v1;
    long s0;
    long s1;
    long s2;
    long s5;
    s1 = 0;
    s5 = -0x8000L;
    s2 = a2 & 0x1L;
    v0 = 0x800c_0000L;
    s0 = v0 - 0x3658L;

    //LAB_80019cc4
    do {
      if(MEMORY.ref(1, s0).offset(0x2L).get() == a0 && MEMORY.ref(1, s0).offset(0x3L).get() == a1) {
        FUN_8004d78c((short)(s1 | s5));
        if(s2 == 0) {
          break;
        }
      }

      //LAB_80019cfc
      s1 = s1 + 0x1L;
      s0 = s0 + 0x8L;
    } while((int)s1 < 0x18L);

    //LAB_80019d0c
    s1 = 0;
    v0 = 0x800c_0000L;
    v1 = v0 - 0x2ef0L;

    //LAB_80019d1c
    do {
      if(MEMORY.ref(1, v1).offset(0x0L).get() == 0x4L && MEMORY.ref(4, v1).offset(0xcL).get() == a1 && MEMORY.ref(4, v1).offset(0x8L).get() == a0) {
        MEMORY.ref(1, v1).offset(0x0L).setu(0);
        MEMORY.ref(4, v1).offset(0x1cL).setu(0);
      }

      //LAB_80019d54
      s1 = s1 + 0x1L;
      v1 = v1 + 0x28L;
    } while((int)s1 < 0x20L);

    v0 = (int)a2 >> 1;
    v0 = v0 & 0x1L;
    if(v0 != 0) {
      v0 = 0x800c_0000L;
      v0 = v0 - 0x2ef0L;
      v1 = v0;
      final long a0_0 = v1 + 0x500L;

      //LAB_80019d84
      do {
        //LAB_80019db4
        if(MEMORY.ref(1, v1).offset(0x0L).get() == 0x3L && (MEMORY.ref(2, v1).offset(0x20L).getSigned() != 0 || MEMORY.ref(2, v1).offset(0x24L).getSigned() != 0) || MEMORY.ref(4, v1).offset(0x1cL).get() != 0) {
          //LAB_80019dc4
          if(MEMORY.ref(4, v1).offset(0xcL).get() == a1 && MEMORY.ref(4, v1).offset(0x8L).get() == a0) {
            MEMORY.ref(1, v1).offset(0x0L).setu(0);
            MEMORY.ref(4, v1).offset(0x1cL).setu(0);
          }
        }

        //LAB_80019dec
        v1 = v1 + 0x28L;
      } while((int)v1 < (int)a0_0);
    }

    //LAB_80019dfc
  }

  @Method(0x8001a4e8L)
  public static void FUN_8001a4e8() {
    //LAB_8001a50c
    for(int i = 0; i < 32; i++) {
      final SpuStruct28 spu28 = spu28Arr_800bd110.get(i);

      if(spu28._00.get() != 0 && spu28._00.get() != 4) {
        if(spu28._24.get() != 0) {
          spu28._24.decr();

          if(spu28._24.get() <= 0) {
            FUN_8001a5fc(i);

            spu28._24.set((short)0);
            if(spu28._20.get() == 0) {
              spu28._00.set(0);
            }
          }
          //LAB_8001a564
        } else if(spu28._20.get() != 0) {
          spu28._22.decr();

          if(spu28._22.get() <= 0) {
            FUN_8001a5fc(i);

            if(spu28._20.get() != 0) {
              spu28._22.set(spu28._20);
            }
          }
        } else {
          //LAB_8001a5b0
          FUN_8001a5fc(i);

          if(spu28._1c.get() != 0) {
            spu28._00.set(4);
          } else {
            //LAB_8001a5d0
            spu28._00.set(0);
          }
        }
      }

      //LAB_8001a5d4
    }
  }

  @Method(0x8001a5fcL)
  public static void FUN_8001a5fc(final int a0) {
    final short s0;

    if(spu28Arr_800bd110.get(a0).pitchShiftVolRight_16.get() == -1 && spu28Arr_800bd110.get(a0).pitchShiftVolLeft_18.get() == -1 && spu28Arr_800bd110.get(a0).pitch_1a.get() == -1) {
      s0 = (short)FUN_8004d648(
        spu28Arr_800bd110.get(a0).playableSoundIndex_10.get(),
        spu28Arr_800bd110.get(a0)._12.get(),
        spu28Arr_800bd110.get(a0)._14.get()
      );
    } else {
      s0 = (short)sssqPitchShift(
        spu28Arr_800bd110.get(a0).playableSoundIndex_10.get(),
        spu28Arr_800bd110.get(a0)._12.get(),
        spu28Arr_800bd110.get(a0)._14.get(),
        spu28Arr_800bd110.get(a0).pitchShiftVolLeft_18.get(),
        spu28Arr_800bd110.get(a0).pitchShiftVolRight_16.get(),
        spu28Arr_800bd110.get(a0).pitch_1a.get()
      );
    }

    if(s0 != -0x1L) {
      _800bc9a8.get(s0).soundFileIndex_02.set((byte)spu28Arr_800bd110.get(a0).soundFileIndex_08.get());
      _800bc9a8.get(s0).soundIndex_03.set((byte)spu28Arr_800bd110.get(a0).soundIndex_0c.get());
      _800bc9a8.get(s0)._04.set(spu28Arr_800bd110.get(a0)._04.get());
    }

    //LAB_8001a704
  }

  @Method(0x8001a714L)
  public static void FUN_8001a714(final int a0, final int soundFileIndex, final int soundIndex, final int a3, final short playableSoundIndex, final long a5, final long a6, final short pitchShiftVolRight, final short pitchShiftVolLeft, final short pitch, final short a10, final short a11, final int a12) {
    final SpuStruct28 spu28 = spu28Arr_800bd110.get(a3);
    spu28._00.set(a0);
    spu28._04.set(a12);
    spu28.soundFileIndex_08.set(soundFileIndex);
    spu28.soundIndex_0c.set(soundIndex);
    spu28.playableSoundIndex_10.set(playableSoundIndex);
    spu28._12.set((short)MEMORY.ref(1, a5).offset(0x0L).get());
    spu28._14.set((short)MEMORY.ref(1, a5).offset(0x1L).get());
    spu28.pitchShiftVolRight_16.set(pitchShiftVolRight);
    spu28.pitchShiftVolLeft_18.set(pitchShiftVolLeft);
    spu28.pitch_1a.set(pitch);
    spu28._1c.set(a6 & 0xffL);
    spu28._20.set(a10);
    spu28._22.set((short)1);
    spu28._24.set(a11);

    long a1_1 = _800bd680.offset(0x50L).getAddress();
    long a2_1 = _800bd680.offset(0x3cL).getAddress();

    //LAB_8001a7b4
    for(int i = 3; i >= 0; i--) {
      MEMORY.ref(4, a1_1).offset(0x00L).setu(MEMORY.ref(4, a2_1).offset(0x00L));
      MEMORY.ref(4, a1_1).offset(0x04L).setu(MEMORY.ref(4, a2_1).offset(0x04L));
      MEMORY.ref(4, a1_1).offset(0x08L).setu(MEMORY.ref(4, a2_1).offset(0x08L));
      MEMORY.ref(4, a1_1).offset(0x0cL).setu(MEMORY.ref(4, a2_1).offset(0x0cL));
      MEMORY.ref(4, a1_1).offset(0x10L).setu(MEMORY.ref(4, a2_1).offset(0x10L));
      a1_1 -= 0x14L;
      a2_1 -= 0x14L;
    }

    _800bd680.setu(soundFileIndex);
    _800bd680.offset(0x4L).setu(soundIndex);
    _800bd680.offset(0x10L).setu(a6 & 0xffL);
  }

  @Method(0x8001a810L)
  public static long FUN_8001a810() {
    //LAB_8001a828
    int a2 = 0;
    long emptyCharSlots = 0;
    for(int charSlot = 0; charSlot < 3; charSlot++) {
      final int charIndex = gameState_800babc8.charIndex_88.get(charSlot).get();

      if(charIndex == -1) {
        emptyCharSlots++;
      } else {
        //LAB_8001a840
        a2 = charIndex;
      }

      //LAB_8001a844
    }

    if(emptyCharSlots == 2) {
      return _8004f698.get(a2).get();
    }

    //LAB_8001a878
    //LAB_8001a880
    final byte[] sp = new byte[26];

    //LAB_8001a8b0
    for(int i = 0; i < 3; i++) {
      if(gameState_800babc8.charIndex_88.get(i).get() != -1) {
        //LAB_8001a934
        _800bd6e8.get(i).set(gameState_800babc8.charIndex_88.get(i).get());
      } else {
        //LAB_8001a8cc
        long v1 = 7;
        for(int charIndex = 0; charIndex < 9; charIndex++) {
          if(v1 != gameState_800babc8.charIndex_88.get(0).get()) {
            if(v1 != gameState_800babc8.charIndex_88.get(1).get()) {
              if(v1 != gameState_800babc8.charIndex_88.get(2).get()) {
                if(v1 != _800bd6e8.get(0).get()) {
                  if(v1 != _800bd6e8.get(1).get()) {
                    //LAB_8001a92c
                    _800bd6e8.get(i).set(v1);
                    break;
                  }
                }
              }
            }
          }

          //LAB_8001a914
          v1--;
        }
      }

      //LAB_8001a938
      //LAB_8001a93c
    }

    //LAB_8001a954
    //LAB_8001a97c
    for(int charSlot = 0; charSlot < 3; charSlot++) {
      if(_800bd6e8.get(charSlot).get() != 0) {
        //LAB_8001a998
        for(int i = 0; i < 26; i++) {
          final long a1 = _8004f664.offset(i * 0x2L).getAddress(); //TODO
          final long v1 = _800bd6e8.get(charSlot).get();

          if(v1 == MEMORY.ref(1, a1).offset(0x0L).get() || v1 == MEMORY.ref(1, a1).offset(0x1L).get()) {
            //LAB_8001a9bc
            sp[i]++;
          }

          //LAB_8001a9d0
        }
      }

      //LAB_8001a9e0
    }

    //LAB_8001a9f8
    for(int i = 0; i < 26; i++) {
      if(sp[i] == 2) {
        return i;
      }
    }

    //LAB_8001aa1c
    throw new RuntimeException("Shouldn't get here");
  }

  @Method(0x8001aa24L)
  public static void FUN_8001aa24() {
    FUN_8001a4e8();
  }

  @Method(0x8001aa44L)
  public static void FUN_8001aa44(final int index) {
    soundFileArr_800bcf80.get(index).used_00.set(false);
  }

  @Method(0x8001aa64L)
  public static void FUN_8001aa64() {
    _800bd0f0.setu(0);
    _800bd6f8.setu(0);
  }

  @Method(0x8001aa78L)
  public static void FUN_8001aa78() {
    _800bca68.setu(0);
    _800bca6c.setu(0x7f00L);
  }

  @Method(0x8001aa90L)
  public static void FUN_8001aa90() {
    //LAB_8001aaa4
    for(int i = 0; i < 24; i++) {
      _800bc9a8.get(i)._00.set(0xffff);
    }
  }

  @Method(0x8001ab34L) // Button press (actually I think this is sound?)
  public static long FUN_8001ab34(final RunningScript a0) {
    playSound((int)a0.params_20.get(0).deref().get(), (int)a0.params_20.get(1).deref().get(), a0.params_20.get(2).deref().get(), a0.params_20.get(3).deref().get(), (short)a0.params_20.get(4).deref().get(), (short)a0.params_20.get(5).deref().get());
    return 0;
  }

  @Method(0x8001ab98L)
  public static long FUN_8001ab98(final RunningScript a0) {
    FUN_80019c80(a0.params_20.get(0).deref().get(), a0.params_20.get(1).deref().get(), a0.params_20.get(2).deref().get());
    return 0;
  }

  @Method(0x8001ad18L)
  public static void FUN_8001ad18() {
    //LAB_8001ad2c
    for(int i = 0; i < 32; i++) {
      spu28Arr_800bd110.get(i)._00.set(0);
      spu28Arr_800bd110.get(i)._1c.set(0);
    }

    FUN_8004d91c(0x1L);
  }

  @Method(0x8001ad5cL)
  public static long FUN_8001ad5c(final RunningScript a0) {
    //LAB_8001ad70
    for(int i = 0; i < 32; i++) {
      final SpuStruct28 struct = spu28Arr_800bd110.get(i);
      struct._00.set(0);
      struct._1c.set(0);
    }

    FUN_8004d91c(0x1L);
    return 0;
  }

  @Method(0x8001ada0L)
  public static void FUN_8001ada0() {
    FUN_8004cf8c((int)sssqChannelIndex_800bd0f8.get());
  }

  @Method(0x8001ae90L)
  public static void FUN_8001ae90() {
    if(_800bd0f0.get() == 0x2L) {
      FUN_8004d034((int)sssqChannelIndex_800bd0f8.get(), 0);
    }
  }

  @Method(0x8001af00L)
  public static void FUN_8001af00(long a0) {
    assert false;
  }

  @Method(0x8001b1a8L)
  public static void FUN_8001b1a8(final long a0) {
    FUN_8004c8dc((int)sssqChannelIndex_800bd0f8.get(), (short)a0);
    _800bd108.setu(a0);
  }

  /**
   * Something to do with sequenced audio
   */
  @Method(0x8001b33cL)
  public static long FUN_8001b33c(final RunningScript a0) {
    //TODO GH#3
    if(true) {
      return 0;
    }

    FUN_8004d41c((int)_800bd0f0.offset(2, 0x8L).getSigned(), (short)a0.params_20.get(0).deref().get(), (short)a0.params_20.get(1).deref().get());
    _800bd0f0.offset(2, 0x18L).setu(a0.params_20.get(1).deref().get());
    return 0;
  }

  @Method(0x8001b3e4L)
  public static long FUN_8001b3e4() {
    if(soundFileArr_800bcf80.get(11).used_00.get()) {
      return soundFileArr_800bcf80.get(11)._02.get();
    }

    //LAB_8001b408
    return 0;
  }

  @Method(0x8001b410L)
  public static void FUN_8001b410() {
    if(_8004f6e4.getSigned() == -0x1L) {
      return;
    }

    FUN_8001b54c();

    if(loadingSmapOvl_8004dd08.get() == 0x1L) {
      return;
    }

    if(_800bd740.getSigned() >= 0) {
      _800bd740.sub(0x1L);
      return;
    }

    //LAB_8001b460
    if(_800bd700.get() != 0) {
      FUN_8001c5bc();
    }

    //LAB_8001b480
    if(_800bc960.get(0x2L) != 0) {
      if(_8004f6ec.get() == 0) {
        _8004f6ec.setu(0x1L);
        FUN_8001c594(0x1L, 0x6L);
        scriptStartEffect(0x1L, 0x1L);
      }
    }

    //LAB_8001b4c0
    if(_8004f6ec.get() != 0) {
      //LAB_8001b4d4
      if(_8004f6ec.get() >= 0x7L) {
        if(loadingSmapOvl_8004dd08.get() == 0) {
          _8004f6e4.setu(-0x1L);
          _800bc960.oru(0x1L);
        }
      }

      //LAB_8001b518
      _8004f6ec.addu(0x1L);
    }

    //LAB_8001b528
    _8004f6e8.addu(0x1L);

    //LAB_8001b53c
  }

  @Method(0x8001b54cL)
  public static void FUN_8001b54c() {
    FUN_8001b92c();

    long sp10x4 = -displayWidth_1f8003e0.get() / 2;
    long sp14x4 = -displayHeight_1f8003e4.get() / 2;

    long a0 = displayHeight_1f8003e4.getSigned();
    if(a0 < 0) {
      a0 += 0x7L;
    }

    //LAB_8001b5c0
    a0 /= 8;
    if(0x64L / a0 == _800bd714.get()) {
      _800bd714.setu(0);
      _800bd710.addu(0x1L);

      final long v1 = a0 - 0x1L;
      if(v1 < _800bd710.get()) {
        _800bd710.setu(v1);
      }
    }

    //LAB_8001b608
    long sp18x4 = 0;
    long sp30x4 = 0x200L;

    //LAB_8001b620
    do {
      long sp1cx4 = 0;
      long sp24x4 = sp30x4 * 0x100L;
      long sp28x4 = sp30x4 * 0x100L + 0x8L;
      long sp2cx4 = sp10x4;

      long s5 = displayHeight_1f8003e4.get() - (_800bd710.get() + 1) * 8 + (sp18x4 << 0x3L);

      //LAB_8001b664
      do {
        long v0 = displayWidth_1f8003e0.get();
        if(v0 < 0) {
          v0 += 0x1fL;
        }

        //LAB_8001b67c
        v0 >>= 0x5L;
        v0 <<= 0x2L;
        if(sp1cx4 >= v0) {
          break;
        }

        long s6 = sp1cx4 << 0x3L;
        long sp20x4 = sp2cx4;

        //LAB_8001b6a4
        for(int s7 = 0; s7 < 1; s7++) {
          v0 = rand();

          //LAB_8001b6bc
          long s3 = v0 - (v0 >> 0x2L << 0x2L);
          if((rand() & 0x1L) != 0) {
            s3 = -s3;
          }

          //LAB_8001b6dc
          v0 = rand();
          final long s2 = v0 - ((v0 * 0x2aaaaaabL & 0xffffffffL) - (v0 >> 0x1fL)) * 6; //TODO

          final long s4;
          if(s6 >= 0xf9L) {
            //LAB_8001b720
            if(s6 >= 0x1f9L) {
              s4 = 0x8L;
            } else {
              s4 = 0x4L;
            }
          } else {
            s4 = 0;
          }

          //LAB_8001b734
          final long s0 = linkedListAddress_1f8003d8.get();
          linkedListAddress_1f8003d8.addu(0x28L);

          MEMORY.ref(1, s0).offset(0x3L).setu(0x9L); // 9 words

          MEMORY.ref(1, s0).offset(0x4L).setu(_800bd708).shra(0x8L);
          MEMORY.ref(1, s0).offset(0x5L).setu(_800bd708).shra(0x8L);
          MEMORY.ref(1, s0).offset(0x6L).setu(_800bd708).shra(0x8L);
          MEMORY.ref(1, s0).offset(0x7L).setu(0x2cL);

          MEMORY.ref(2, s0).offset(0x08L).setu(sp20x4 + s3);
          MEMORY.ref(2, s0).offset(0x0aL).setu(sp14x4 + sp24x4 + s5 + s2);
          MEMORY.ref(1, s0).offset(0x0cL).setu(s6);
          // 0xd set below
          // 0xe-f not set

          MEMORY.ref(2, s0).offset(0x10L).setu(sp20x4 + s3 + 0x8L);
          MEMORY.ref(2, s0).offset(0x12L).setu(sp14x4 + sp24x4 + s5 + s2);
          MEMORY.ref(1, s0).offset(0x14L).setu(s6 + 0x7L);
          // 0x15 set below
          // 0x16-17 set below

          MEMORY.ref(2, s0).offset(0x18L).setu(sp20x4 + s3);
          MEMORY.ref(2, s0).offset(0x1aL).setu(sp14x4 + sp28x4 + s5 + s2);
          MEMORY.ref(1, s0).offset(0x1cL).setu(s6);
          // 0x1e-1f not set

          MEMORY.ref(2, s0).offset(0x20L).setu(sp20x4 + s3 + 0x8L);
          MEMORY.ref(2, s0).offset(0x22L).setu(sp14x4 + sp28x4 + s5 + s2);
          MEMORY.ref(1, s0).offset(0x24L).setu(s6 + 0x7L);
          // 0x25 set below
          // 0x26-27 not set

          if(doubleBufferFrame_800bb108.get() == 0) {
            MEMORY.ref(1, s0).offset(0x0dL).setu(s5 + 0x10L);

            MEMORY.ref(1, s0).offset(0x15L).setu(s5 + 0x10L);
            MEMORY.ref(2, s0).offset(0x16L).setu(s4 + 0x100L);

            MEMORY.ref(1, s0).offset(0x1dL).setu(s5 + 0x18L);

            MEMORY.ref(1, s0).offset(0x25L).setu(s5 + 0x18L);
          } else {
            //LAB_8001b818
            MEMORY.ref(1, s0).offset(0x0dL).setu(s5);

            MEMORY.ref(1, s0).offset(0x15L).setu(s5);
            MEMORY.ref(2, s0).offset(0x16L).setu(s4 + 0x110L);

            MEMORY.ref(1, s0).offset(0x1dL).setu(s5 + 0x8L);

            MEMORY.ref(1, s0).offset(0x25L).setu(s5 + 0x8L);
          }

          //LAB_8001b868
          gpuLinkedListSetCommandTransparency(s0, true);
          insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);
        }

        sp2cx4 += 0x8L;
        sp1cx4++;
      } while(true);

      //LAB_8001b8b8
      sp30x4 += 0x200L;
      sp18x4++;
    } while(_800bd710.get() >= sp18x4);

    _800bd714.addu(0x1L);
    FUN_8001bbcc(sp10x4, sp14x4);
  }

  @Method(0x8001b92cL)
  public static void FUN_8001b92c() {
    long v1;
    long a1;
    long a2;

    a1 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x18L);

    MEMORY.ref(1, a1).offset(0x3L).setu(0x5L); // 5 words

    MEMORY.ref(1, a1).offset(0x4L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x5L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x6L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x7L).setu(0x28L);

    v1 = -displayWidth_1f8003e0.get() / 2;
    a2 = -displayHeight_1f8003e4.get() / 2;
    MEMORY.ref(2, a1).offset(0x8L).setu(v1 - 0x20L);
    MEMORY.ref(2, a1).offset(0xaL).setu(a2 - 0x20L);
    MEMORY.ref(2, a1).offset(0xcL).setu(displayWidth_1f8003e0.get() + v1 + 0x20L);
    MEMORY.ref(2, a1).offset(0xeL).setu(a1);

    MEMORY.ref(2, a1).offset(0x10L).setu(v1 - 0x20L);
    MEMORY.ref(2, a1).offset(0x12L).setu(a2 + 0x4L);
    MEMORY.ref(2, a1).offset(0x14L).setu(displayWidth_1f8003e0.get() + v1 + 0x20L);
    MEMORY.ref(2, a1).offset(0x16L).setu(a1);

    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), a1);

    a1 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x18L);

    MEMORY.ref(1, a1).offset(0x3L).setu(0x5L); // 5 words

    MEMORY.ref(1, a1).offset(0x4L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x5L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x6L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x7L).setu(0x28L);

    v1 = -displayWidth_1f8003e0.get() / 2;
    a2 = displayHeight_1f8003e4.get() / 2;
    MEMORY.ref(2, a1).offset(0x8L).setu(v1 - 0x20L);
    MEMORY.ref(2, a1).offset(0xaL).setu(a2 + 0x20L);
    MEMORY.ref(2, a1).offset(0xcL).setu(displayWidth_1f8003e0.get() + v1);
    MEMORY.ref(2, a1).offset(0xeL).setu(a2 + 0x20L);

    MEMORY.ref(2, a1).offset(0x10L).setu(v1 - 0x20L);
    MEMORY.ref(2, a1).offset(0x12L).setu(a2 - 0x4L);
    MEMORY.ref(2, a1).offset(0x14L).setu(displayWidth_1f8003e0.get() + v1);
    MEMORY.ref(2, a1).offset(0x16L).setu(a2 - 0x4L);

    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), a1);

    a1 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x18L);

    MEMORY.ref(1, a1).offset(0x3L).setu(0x5L); // 5 words

    MEMORY.ref(1, a1).offset(0x4L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x5L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x6L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x7L).setu(0x28L);

    v1 = -displayWidth_1f8003e0.get() / 2;
    a2 = -displayHeight_1f8003e4.get() / 2;
    MEMORY.ref(2, a1).offset(0x8L).setu(v1 - 0x20L);
    MEMORY.ref(2, a1).offset(0xaL).setu(a2);
    MEMORY.ref(2, a1).offset(0xcL).setu(v1 + 0x4L);
    MEMORY.ref(2, a1).offset(0xeL).setu(a2);

    MEMORY.ref(2, a1).offset(0x10L).setu(v1 - 0x20L);
    MEMORY.ref(2, a1).offset(0x12L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a2);
    MEMORY.ref(2, a1).offset(0x14L).setu(v1 + 0x4L);
    MEMORY.ref(2, a1).offset(0x16L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a2);

    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), a1);

    a1 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x18L);

    MEMORY.ref(1, a1).offset(0x3L).setu(0x5L); // 5 words

    MEMORY.ref(1, a1).offset(0x4L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x5L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x6L).setu(0x1L);
    MEMORY.ref(1, a1).offset(0x7L).setu(0x28L);

    a2 = displayWidth_1f8003e0.get() / 2;
    v1 = -displayHeight_1f8003e4.get() / 2;
    MEMORY.ref(2, a1).offset(0x8L).setu(a2 + 0x20L);
    MEMORY.ref(2, a1).offset(0xaL).setu(v1);
    MEMORY.ref(2, a1).offset(0xcL).setu(a2);
    MEMORY.ref(2, a1).offset(0xeL).setu(v1);

    MEMORY.ref(2, a1).offset(0x10L).setu(a2 + 0x20L);
    MEMORY.ref(2, a1).offset(0x12L).setu(displayHeight_1f8003e4.offset(2, 0x0L));
    MEMORY.ref(2, a1).offset(0x14L).setu(a2 - 0x4L);
    MEMORY.ref(2, a1).offset(0x16L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + v1);

    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), a1);
  }

  @Method(0x8001bbccL)
  public static void FUN_8001bbcc(final long a0, final long a1) {
    FUN_8001b92c();

    long s0;
    if(doubleBufferFrame_800bb108.get() == 0) {
      s0 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x28L);

      MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

      // Command
      MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L); // R
      MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L); // G
      MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L); // B
      MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL); // Textured four-point polygon, opaque, texture-blending

      // Vertex 1
      MEMORY.ref(2, s0).offset(0x08L).setu(a0 + 0x80L); // X
      MEMORY.ref(2, s0).offset(0x0aL).setu(a1); // Y
      MEMORY.ref(1, s0).offset(0x0cL).setu(0); // U
      MEMORY.ref(1, s0).offset(0x0dL).setu(0x10L); // V
      MEMORY.ref(2, s0).offset(0x0eL).setu(0x102L); // CLUT palette (note: this wasn't being set... pretty sure it needs to be)

      // Vertex 2
      MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0x17fL); // X
      MEMORY.ref(2, s0).offset(0x12L).setu(a1); // Y
      MEMORY.ref(1, s0).offset(0x14L).setu(0xffL); // U
      MEMORY.ref(1, s0).offset(0x15L).setu(0x10L); // V
      MEMORY.ref(2, s0).offset(0x16L).setu(0x102L); // CLUT palette

      // Vertex 3
      MEMORY.ref(2, s0).offset(0x18L).setu(a0 + 0x80L); // X
      MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L); // Y
      MEMORY.ref(1, s0).offset(0x1cL).setu(0); // U
      MEMORY.ref(1, s0).offset(0x1dL).setu(0xffL); // V
      // 0x1e-1f not set (CLUT palette)

      // Vertex 4
      MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0x17fL); // X
      MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L); // Y
      MEMORY.ref(1, s0).offset(0x24L).setu(0xffL); // U
      MEMORY.ref(1, s0).offset(0x25L).setu(0xffL); // V
      // 0x26-27 not set (CLUT palette)

      gpuLinkedListSetCommandTransparency(s0, false);
      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);

      s0 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x28L);

      MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

      // Command
      MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L); // R
      MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L); // G
      MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L); // B
      MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL); // Textured four-point polygon, opaque, texture-blending

      // Vertex 1
      MEMORY.ref(2, s0).offset(0x08L).setu(a0); // X
      MEMORY.ref(2, s0).offset(0x0aL).setu(a1); // Y
      MEMORY.ref(1, s0).offset(0x0cL).setu(0); // U
      MEMORY.ref(1, s0).offset(0x0dL).setu(0x10L); // V
      MEMORY.ref(2, s0).offset(0x0eL).setu(0x100L); // CLUT palette (note: this wasn't being set... pretty sure it needs to be)

      // Vertex 2
      MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0xffL); // X
      MEMORY.ref(2, s0).offset(0x12L).setu(a1); // Y
      MEMORY.ref(1, s0).offset(0x14L).setu(0xffL); // U
      MEMORY.ref(1, s0).offset(0x15L).setu(0x10L); // V
      MEMORY.ref(2, s0).offset(0x16L).setu(0x100L); // CLUT palette

      // Vertex 3
      MEMORY.ref(2, s0).offset(0x18L).setu(a0); // X
      MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L); // Y
      MEMORY.ref(1, s0).offset(0x1cL).setu(0); // U
      MEMORY.ref(1, s0).offset(0x1dL).setu(0xffL); // V
      // 0x1e-1f not set (CLUT palette)

      // Vertex 4
      MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0xffL); // X
      MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L); // Y
      MEMORY.ref(1, s0).offset(0x24L).setu(0xffL); // U
      MEMORY.ref(1, s0).offset(0x25L).setu(0xffL); // V
      // 0x26-27 not set (CLUT palette)

      gpuLinkedListSetCommandTransparency(s0, false);
      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);

      if(displayWidth_1f8003e0.get() == 0x280L) {
        s0 = linkedListAddress_1f8003d8.get();
        linkedListAddress_1f8003d8.addu(0x28L);

        MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

        MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL);

        MEMORY.ref(2, s0).offset(0x08L).setu(a0 + 0x100L);
        MEMORY.ref(2, s0).offset(0x0aL).setu(a1);
        MEMORY.ref(1, s0).offset(0x0cL).setu(0);
        MEMORY.ref(1, s0).offset(0x0dL).setu(0x10L);
        MEMORY.ref(2, s0).offset(0x0eL).setu(0x104L);

        MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0x1ffL);
        MEMORY.ref(2, s0).offset(0x12L).setu(a1);
        MEMORY.ref(1, s0).offset(0x14L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x15L).setu(0x10L);
        MEMORY.ref(2, s0).offset(0x16L).setu(0x104L);

        MEMORY.ref(2, s0).offset(0x18L).setu(a0 + 0x100L);
        MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x1cL).setu(0);
        MEMORY.ref(1, s0).offset(0x1dL).setu(0xffL);
        // 0x1e-1f not set

        MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0x1ffL);
        MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x24L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x25L).setu(0xffL);
        // 0x26-27 not set

        gpuLinkedListSetCommandTransparency(s0, false);
        insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);

        s0 = linkedListAddress_1f8003d8.get();
        linkedListAddress_1f8003d8.addu(0x28L);

        MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

        MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL);

        MEMORY.ref(2, s0).offset(0x08L).setu(a0 + 0x180L);
        MEMORY.ref(2, s0).offset(0x0aL).setu(a1);
        MEMORY.ref(1, s0).offset(0x0cL).setu(0);
        MEMORY.ref(1, s0).offset(0x0dL).setu(0x10L);
        MEMORY.ref(2, s0).offset(0x0eL).setu(0x106L);

        MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0x27fL);
        MEMORY.ref(2, s0).offset(0x12L).setu(a1);
        MEMORY.ref(1, s0).offset(0x14L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x15L).setu(0x10L);
        MEMORY.ref(2, s0).offset(0x16L).setu(0x106L);

        MEMORY.ref(2, s0).offset(0x18L).setu(a0 + 0x180L);
        MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x1cL).setu(0);
        MEMORY.ref(1, s0).offset(0x1dL).setu(0xffL);
        // 0x1e-1f not set

        MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0x27fL);
        MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x24L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x25L).setu(0xffL);
        // 0x26-27 not set

        gpuLinkedListSetCommandTransparency(s0, false);
        insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);
      }
    } else {
      s0 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x28L);

      MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

      MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L);
      MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L);
      MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L);
      MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL);

      MEMORY.ref(2, s0).offset(0x08L).setu(a0 + 0x80L);
      MEMORY.ref(2, s0).offset(0x0aL).setu(a1);
      MEMORY.ref(1, s0).offset(0x0cL).setu(0);
      MEMORY.ref(1, s0).offset(0x0dL).setu(0);
      MEMORY.ref(2, s0).offset(0x0eL).setu(0x112L);

      MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0x17fL);
      MEMORY.ref(2, s0).offset(0x12L).setu(a1);
      MEMORY.ref(1, s0).offset(0x14L).setu(0xffL);
      MEMORY.ref(1, s0).offset(0x15L).setu(0);
      MEMORY.ref(2, s0).offset(0x16L).setu(0x112L);

      MEMORY.ref(2, s0).offset(0x18L).setu(a0 + 0x80L);
      MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
      MEMORY.ref(1, s0).offset(0x1cL).setu(0);
      MEMORY.ref(1, s0).offset(0x1dL).setu(0xefL);
      // 0x1e-1f not set

      MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0x17fL);
      MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
      MEMORY.ref(1, s0).offset(0x24L).setu(0xffL);
      MEMORY.ref(1, s0).offset(0x25L).setu(0xefL);
      // 0x26-27 not set

      gpuLinkedListSetCommandTransparency(s0, false);
      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);

      s0 = linkedListAddress_1f8003d8.get();
      linkedListAddress_1f8003d8.addu(0x28L);

      MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

      MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L);
      MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L);
      MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L);
      MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL);

      MEMORY.ref(2, s0).offset(0x08L).setu(a0);
      MEMORY.ref(2, s0).offset(0x0aL).setu(a1);
      MEMORY.ref(1, s0).offset(0x0cL).setu(0);
      MEMORY.ref(1, s0).offset(0x0dL).setu(0);
      MEMORY.ref(2, s0).offset(0x0eL).setu(0x110L);

      MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0xffL);
      MEMORY.ref(2, s0).offset(0x12L).setu(a1);
      MEMORY.ref(1, s0).offset(0x14L).setu(0xffL);
      MEMORY.ref(1, s0).offset(0x15L).setu(0);
      MEMORY.ref(2, s0).offset(0x16L).setu(0x110L);

      MEMORY.ref(2, s0).offset(0x18L).setu(a0);
      MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
      MEMORY.ref(1, s0).offset(0x1cL).setu(0);
      MEMORY.ref(1, s0).offset(0x1dL).setu(0xefL);
      // 0x1e-1f not set

      MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0xffL);
      MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
      MEMORY.ref(1, s0).offset(0x24L).setu(0xffL);
      MEMORY.ref(1, s0).offset(0x25L).setu(0xefL);
      // 0x26-27 not set

      gpuLinkedListSetCommandTransparency(s0, false);
      insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);

      if(displayWidth_1f8003e0.get() == 0x280L) {
        s0 = linkedListAddress_1f8003d8.get();
        linkedListAddress_1f8003d8.addu(0x28L);

        MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

        MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL);

        MEMORY.ref(2, s0).offset(0x08L).setu(a0 + 0x100L);
        MEMORY.ref(2, s0).offset(0x0aL).setu(a1);
        MEMORY.ref(1, s0).offset(0x0cL).setu(0);
        MEMORY.ref(1, s0).offset(0x0dL).setu(0);
        MEMORY.ref(2, s0).offset(0x0eL).setu(0x114L);

        MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0x1ffL);
        MEMORY.ref(2, s0).offset(0x12L).setu(a1);
        MEMORY.ref(1, s0).offset(0x14L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x15L).setu(0);
        MEMORY.ref(2, s0).offset(0x16L).setu(0x114L);

        MEMORY.ref(2, s0).offset(0x18L).setu(a0 + 0x100L);
        MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x1cL).setu(0);
        MEMORY.ref(1, s0).offset(0x1dL).setu(0xefL);
        // 0x1e-1f not set

        MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0x1ffL);
        MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x24L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x25L).setu(0xefL);
        // 0x26-27 not set

        gpuLinkedListSetCommandTransparency(s0, false);
        insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);

        s0 = linkedListAddress_1f8003d8.get();
        linkedListAddress_1f8003d8.addu(0x28L);

        MEMORY.ref(1, s0).offset(0x03L).setu(0x9L); // 9 words

        MEMORY.ref(1, s0).offset(0x04L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x05L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x06L).setu(_800bd708.get() >> 0x8L);
        MEMORY.ref(1, s0).offset(0x07L).setu(0x2cL);

        MEMORY.ref(2, s0).offset(0x08L).setu(a0 + 0x180L);
        MEMORY.ref(2, s0).offset(0x0aL).setu(a1);
        MEMORY.ref(1, s0).offset(0x0cL).setu(0);
        MEMORY.ref(1, s0).offset(0x0dL).setu(0);
        MEMORY.ref(2, s0).offset(0x0eL).setu(0x116L);

        MEMORY.ref(2, s0).offset(0x10L).setu(a0 + 0x27fL);
        MEMORY.ref(2, s0).offset(0x12L).setu(a1);
        MEMORY.ref(1, s0).offset(0x14L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x15L).setu(0);
        MEMORY.ref(2, s0).offset(0x16L).setu(0x116L);

        MEMORY.ref(2, s0).offset(0x18L).setu(a0 + 0x180L);
        MEMORY.ref(2, s0).offset(0x1aL).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x1cL).setu(0);
        MEMORY.ref(1, s0).offset(0x1dL).setu(0xefL);
        // 0x1e-1f not set

        MEMORY.ref(2, s0).offset(0x20L).setu(a0 + 0x27fL);
        MEMORY.ref(2, s0).offset(0x22L).setu(displayHeight_1f8003e4.offset(2, 0x0L).get() + a1 - 0x1L);
        MEMORY.ref(1, s0).offset(0x24L).setu(0xffL);
        MEMORY.ref(1, s0).offset(0x25L).setu(0xefL);
        // 0x26-27 not set

        gpuLinkedListSetCommandTransparency(s0, false);
        insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);
      }
    }

    //LAB_8001c26c
    s0 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x8L);
    MEMORY.ref(1, s0).offset(0x3L).setu(0x1L); // 1 word
    MEMORY.ref(4, s0).offset(0x4L).setu(0xe1000100L);

    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);

    s0 = linkedListAddress_1f8003d8.get();
    linkedListAddress_1f8003d8.addu(0x8L);
    MEMORY.ref(1, s0).offset(0x3L).setu(0x1L); // 1 word
    MEMORY.ref(4, s0).offset(0x4L).setu(0xe1000110L);

    insertElementIntoLinkedList(tags_1f8003d0.deref().get(0x6).getAddress(), s0);
  }

  @Method(0x8001c4ecL)
  public static void FUN_8001c4ec() {
    mrg10Addr_800c6710.clear();
    _8004f6ec.setu(0);
    playSound(0, 16, 0, 0, (short)0, (short)0);
    vsyncMode_8007a3b8.setu(0x2L);
    _800bd740.setu(0x2L);
    _800bd700.setu(0);
    _800bd704.setu(0);
    _800bd708.setu(0x8000L);
    _800bd714.setu(0);
    _800bd710.setu(0);
    _8007a3a8.setu(0);
    _800bb104.setu(0);
    _800babc0.setu(0);
    _8004f6e4.setu(0x1L);
    _8004f6e8.setu(0);
  }

  @Method(0x8001c594L)
  public static void FUN_8001c594(final long a0, final long a1) {
    _800bd700.setu(a0);
    _800bd704.setu(a1);
    _800bd708.setu(0x8000L);
    _800bd70c.setu(0x8000L / a1);
  }

  @Method(0x8001c5bcL)
  public static void FUN_8001c5bc() {
    if(_800bd700.get() == 0x1L) {
      _800bd704.subu(0x1L);
      _800bd708.subu(_800bd70c);

      if(_800bd704.get() == 0) {
        _800bd700.setu(0);
      }
    }
  }

  @Method(0x8001c60cL)
  public static long FUN_8001c60c() {
    final long s0 = FUN_8001b3e4();
    long a1;

    //LAB_8001c63c
    jmp_8001c7a0:
    {
      long a3 = 0;
      do {
        if(_800bd808.get() == _8004fb00.offset(1, a3 * 0x8L).get()) {
          //LAB_8001c680
          long v1 = 0;
          do {
            if(_800bd808.get() != 0x39L || _8004fb00.offset(4, a3 * 0x8L).offset(0x4L).deref(2).offset(v1).getSigned() == submapCut_80052c30.get()) {
              //LAB_8001c6ac
              if((gameState_800babc8._1a4.get(0).get() & 0x1L) == 0 || (_8004fb00.offset(4, a3 * 0x8L).offset(0x4L).deref(2).offset(v1).getSigned() == submapCut_80052c30.get() && (gameState_800babc8._1a4.get((int)(a3 / 0x20)).get() & 0x1L << a3) != 0)) {
                //LAB_8001c7c0
                a1 = _8004fb00.offset(2, a3 * 0x8L).offset(0x2L).getSigned();
                break jmp_8001c7a0;
              }
            }

            //LAB_8001c6e4
            v1 += 0x2L;
          } while(_8004fb00.offset(4, a3 * 0x8L).offset(0x4L).deref(2).offset(v1).getSigned() != -0x1L);
        }

        //LAB_8001c700
        a3++;
      } while(_8004fb00.offset(1, a3 * 0x8L).get() != 0x63L || _8004fb00.offset(2, a3 * 0x8L).offset(0x2L).getSigned() != 0x63L);

      long a0 = 0;

      //LAB_8001c728
      do {
        if(_800bd808.get() == _8004fa98.offset(1, a0).get()) {
          long v1 = _8004fa98.offset(4, a0).offset(0x4L).get();

          //LAB_8001c748
          do {
            if(MEMORY.ref(2, v1).getSigned() == submapCut_80052c30.get()) {
              //LAB_8001c7d8
              return FUN_8001c84c(s0, _8004fa98.offset(2, a0).offset(0x2L).getSigned());
            }

            v1 += 0x2L;
          } while(MEMORY.ref(2, v1).getSigned() != -0x1L);
        }

        //LAB_8001c76c
        a0 += 0x8L;
      } while(_8004fa98.offset(1, a0).get() != 0x63L || _8004fa98.offset(2, a0).offset(0x2L).getSigned() != 0x63L);

      a1 = FUN_8001c874();
    }

    //LAB_8001c7a0
    final long v1 = FUN_8001c84c(s0, a1);
    if(v1 != -0x2L) {
      return v1;
    }

    //LAB_8001c7ec
    if((FUN_8004d52c((int)sssqChannelIndex_800bd0f8.getSigned()) & 0x1L) == 0) {
      return -0x2L;
    }

    //LAB_8001c808
    return -0x3L;
  }

  @Method(0x8001c84cL)
  public static long FUN_8001c84c(final long a0, final long a1) {
    if(a0 != a1) {
      return a1;
    }

    if(a0 == -0x1L) {
      return -0x1L;
    }

    return -0x2L;
  }

  @Method(0x8001c874L)
  public static long FUN_8001c874() {
    if(_800bd808.get() == 0x38L) {
      for(int i = 0; ; i += 0x8) {
        if(_8004ff10.offset(i).get() == submapCut_80052c30.get()) {
          return _8004ff14.offset(i).get();
        }
      }
    }

    //LAB_8001c8bc
    return _80050068.offset(_800bd808.get() * 0x2L).getSigned();
  }

  @Method(0x8001cae0L)
  public static void FUN_8001cae0(final long address, final long fileSize, final long param) {
    long v0;
    long v1;
    long a0 = address;
    long a1;
    long a2;
    long s0;
    long s1;
    long s2;
    long s3;
    long s4;
    long s5;
    long s6;
    long s7;
    s7 = a0;
    v0 = 0x800c_0000L;
    s3 = 0;
    MEMORY.ref(4, v0).offset(-0x2898L).setu(s7);
    v0 = 0x800c_0000L;
    s4 = v0 - 0x3080L;
    v0 = 0x8005_0000L;
    s1 = v0 + 0xf8L;
    s6 = s3;
    v0 = 0x800c_0000L;
    s5 = v0 - 0x3680L;

    //LAB_8001cb34
    do {
      a1 = 0x3L;
      v0 = s3 << a1;
      v0 = s7 + v0;
      v0 = MEMORY.ref(4, v0).offset(0x8L).get();
      v1 = MEMORY.ref(4, s1).offset(0x0L).get();
      s2 = s7 + v0;
      v0 = v1 << a1;
      v0 = v0 - v1;
      v1 = MEMORY.ref(4, s2).offset(0x8L).get();
      v0 = v0 << 2;
      v1 = s2 + v1;
      v1 = MEMORY.ref(2, v1).offset(0x0L).get();
      v0 = v0 + s4;
      MEMORY.ref(2, v0).offset(0x2L).setu(v1);
      v1 = MEMORY.ref(4, s1).offset(0x0L).get();
      a0 = s2;
      v0 = v1 << a1;
      v0 = v0 - v1;
      v0 = v0 << 2;
      v1 = MEMORY.ref(4, s5).offset(0x4L).get();
      v0 = v0 + s4;
      MEMORY.ref(4, v0).offset(0x4L).setu(v1);
      v0 = FUN_80015704(a0, a1);
      a0 = MEMORY.ref(4, s1).offset(0x0L).get();
      a1 = s2;
      v1 = a0 << 3;
      v1 = v1 - a0;
      v1 = v1 << 2;
      v1 = v1 + s4;
      a0 = MEMORY.ref(4, v1).offset(0x4L).get();
      a2 = v0;
      memcpy(a0, a1, (int)a2);
      v0 = MEMORY.ref(4, s1).offset(0x0L).get();

      v1 = v0 << 3;
      v1 = v1 - v0;
      v1 = v1 << 2;
      v1 = v1 + s4;
      s0 = MEMORY.ref(4, v1).offset(0x4L).get();

      v0 = MEMORY.ref(4, s0).offset(0x10L).get();

      v0 = s0 + v0;
      MEMORY.ref(4, v1).offset(0x8L).setu(v0);
      v1 = MEMORY.ref(4, s1).offset(0x0L).get();

      v0 = v1 << 3;
      v0 = v0 - v1;
      v1 = MEMORY.ref(4, s0).offset(0x8L).get();
      v0 = v0 << 2;
      v1 = s0 + v1;
      v1 = MEMORY.ref(2, v1).offset(0x0L).get();
      v0 = v0 + s4;
      MEMORY.ref(2, v0).offset(0x2L).setu(v1);

      if((int)s3 >= 0) {
        if((int)s3 < 0x2L) {
          a0 = 0x8002_0000L;

          //LAB_8001cc30
          a0 = a0 - 0x1734L;
          setSpuDmaCompleteCallback(a0);
        } else {
          a0 = 0x8002_0000L;
          v0 = 0x2L;
          if(s3 == v0) {
            //LAB_8001cc38
            a0 = 0x8002_0000L;
            a0 = a0 - 0x172cL;

            //LAB_8001cc40
            setSpuDmaCompleteCallback(a0);
          }
        }
      }

      //LAB_8001cc48
      v0 = 0x8005_0000L;
      v0 = v0 + 0x190L;

      //LAB_8001cc50
      v0 = s6 + v0;
      s6 = s6 + 0x4L;
      s5 = s5 + 0xcL;
      a2 = MEMORY.ref(4, v0).offset(0x0L).get();
      a0 = MEMORY.ref(4, s2).offset(0x20L).get();
      a1 = MEMORY.ref(4, s0).offset(0x18L).get();
      a0 = s2 + a0;
      a1 = s0 + a1;
      v0 = loadSshdAndSoundbank(a0, MEMORY.ref(4, a1, SshdFile::new), a2);
      a0 = MEMORY.ref(4, s1).offset(0x0L).get();
      s3 = s3 + 0x1L;
      v1 = a0 << 3;
      v1 = v1 - a0;
      v1 = v1 << 2;
      v1 = v1 + s4;
      MEMORY.ref(2, v1).offset(0x10L).setu(v0);
      v1 = MEMORY.ref(4, s1).offset(0x0L).get();

      v0 = v1 << 3;
      v0 = v0 - v1;
      v0 = v0 << 2;
      v0 = v0 + s4;
      v1 = 0x1L;
      MEMORY.ref(2, v0).offset(0x0L).setu(v1);
      s1 = s1 + 0x4L;
    } while((int)s3 < 0x3L);
  }

  @Method(0x8001d1c4L)
  public static void FUN_8001d1c4() {
    loadedDrgnFiles_800bcf78.oru(0x10L);

    final long v1 = submapScene_800bb0f8.get();
    final long a1;
    if(v1 == 0x186L) {
      //LAB_8001d21c
      a1 = 0x50aL;
    } else if(v1 == 0x1afL) {
      //LAB_8001d244
      a1 = 0x510L;
      //LAB_8001d208
    } else if(v1 == 0x1bbL) {
      //LAB_8001d270
      a1 = 0x50cL;
    } else {
      //LAB_8001d298
      a1 = submapScene_800bb0f8.get() + 0x30aL;
    }

    //LAB_8001d2c0
    loadDrgnBinFile(0, a1, 0, getMethodAddress(Scus94491BpeSegment.class, "FUN_8001d51c", long.class, long.class, long.class), 0, 0x4L);
  }

  @Method(0x8001d51cL)
  public static void FUN_8001d51c(final long address, final long fileSize, final long param) {
    long v0;
    long v1;
    long a0 = address;
    long a1;
    long a2;
    long s0;
    long s1;
    long s2;
    long s3;
    long s4;
    long s5;
    long s6;

    final byte[] sp0x10 = new byte[4];
    final byte[] sp0x18 = new byte[4];

    s5 = a0;
    s6 = 0x5_a1e0L;
    a1 = soundFileArr_800bcf80.getAddress();

    //LAB_8001d588
    for(int i = 0; i < 4; i++) {
      MEMORY.ref(2, a1).offset(_800500e8.offset(i * 0x4L).get() * 0x1cL).offset(0x2L).setu(-0x1L);
      MEMORY.ref(2, a1).offset(_800500e8.offset(i * 0x4L).get() * 0x1cL).setu(0);
    }

    a0 = 0x800c_0000L;
    v1 = MEMORY.ref(4, s5).offset(0x4L).get();
    v0 = 0x800c_0000L;
    MEMORY.ref(4, v0).offset(-0x28a8L).setu(s5);
    MEMORY.ref(4, a0).offset(-0x2884L).setu(v1);

    //LAB_8001d5f8
    for(int i = 0; i < MEMORY.ref(4, s5).offset(0x4L).get(); i++) {
      s2 = s5 + MEMORY.ref(4, s5).offset(i * 0x8L).offset(0x8L).get();

      if(MEMORY.ref(4, s2).offset(0x24L).get() >= 0xbL) {
        sp0x10[i] = 1;
      }

      //LAB_8001d624
    }

    //LAB_8001d63c

    //LAB_8001d658
    s1 = MEMORY.ref(4, s5).offset(0x4L).get();
    while((int)s1 > 0) {
      v1 = s1 - 0x1L;

      if(sp0x10[(int)v1] == 1) {
        sp0x18[(int)v1] = 1;
        break;
      }

      //LAB_8001d678
      if(s1 == 0x1L) {
        FUN_8001eaa0();
        return;
      }

      //LAB_8001d690
      s1 = v1;
    }

    //LAB_8001d698
    s4 = soundFileArr_800bcf80.getAddress();
    s3 = _800500e8.getAddress();

    //LAB_8001d6c0
    for(int i = 0; i < MEMORY.ref(4, s5).offset(0x4L).get(); i++) {
      s2 = s5 + MEMORY.ref(4, s5).offset(i * 0x8L).offset(0x8L).get();
      a1 = MEMORY.ref(4, s2).offset(0x24L).get();

      if(a1 <= 0x30L) {
        if(sp0x18[i] == 1) {
          FUN_8001eaa0();
        }
      } else {
        //LAB_8001d704
        a0 = 0x3L;
        v1 = 0xc750L;
        a2 = MEMORY.ref(4, s2).offset(0x24L).get();
        _800bd774.setu(a1);

        //LAB_8001d718
        do {
          if(a2 >= v1) {
            break;
          }
          a0 = a0 - 0x1L;
          v1 = v1 - 0x4270L;
        } while((int)a0 >= 0);

        //LAB_8001d72c
        v0 = FUN_80015704(s2, 0x3L);
        v0 = addToLinkedListTail(v0);
        MEMORY.ref(4, s4).offset(MEMORY.ref(4, s3).get() * 0x1cL).offset(0x4L).setu(v0);
        v0 = FUN_80015704(s2, 0x3L);
        a0 = MEMORY.ref(4, s4).offset(MEMORY.ref(4, s3).get() * 0x1cL).offset(0x4L).get();
        memcpy(a0, s2, (int)v0);

        v1 = s4 + MEMORY.ref(4, s3).get() * 0x1cL;
        s0 = MEMORY.ref(4, v1).offset(0x4L).get();
        MEMORY.ref(4, v1).offset(0x8L).setu(s0 + MEMORY.ref(4, s0).offset(0x10L).get());

        v1 = s0 + MEMORY.ref(4, s0).offset(0x8L).get();
        v1 = MEMORY.ref(2, v1).offset(0x0L).get();
        MEMORY.ref(2, s4).offset(MEMORY.ref(4, s3).get() * 0x1cL).offset(0x2L).setu(v1);

        if(sp0x18[i] != 1) {
          a0 = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001ea98");
        } else {
          //LAB_8001d804
          a0 = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001eaa0");
        }

        //LAB_8001d80c
        setSpuDmaCompleteCallback(a0);
        a0 = s2 + MEMORY.ref(4, s2).offset(0x20L).get();
        a1 = s0 + MEMORY.ref(4, s0).offset(0x18L).get();
        v0 = loadSshdAndSoundbank(a0, MEMORY.ref(4, a1, SshdFile::new), s6);
        MEMORY.ref(2, s4).offset(MEMORY.ref(4, s3).get() * 0x1cL).offset(0x10L).setu(v0);
        v1 = MEMORY.ref(4, s2).offset(0x24L).get();
        v1 += (v1 & 0xfL);
        s6 = s6 + v1;
        FUN_8004cb0c(MEMORY.ref(2, s4).offset(MEMORY.ref(4, s3).get() * 0x1cL).offset(0x10L).getSigned(), 0x7fL);
        MEMORY.ref(2, s4).offset(MEMORY.ref(4, s3).get() * 0x1cL).setu(0x1L);
      }

      //LAB_8001d894
      //LAB_8001d898
      s3 = s3 + 0x4L;
    }

    //LAB_8001d8ac
  }

  @Method(0x8001d9d0L)
  public static void FUN_8001d9d0() {
    final long struct = _80109a98.offset(submapScene_800bb0f8.get() * 0x10L).getAddress();

    if(MEMORY.ref(1, struct).offset(0x1L).get() != 0xffL) {
      loadedDrgnFiles_800bcf78.oru(0x80L);

      final long fileIndex;
      final long callback;
      final long callbackParam;
      if((MEMORY.ref(1, struct).offset(0x1L).get() & 0x1fL) == 0x13L) {
        unloadSoundFile(8);
        fileIndex = 0x2dcL;
        callback = getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class);
        callbackParam = 0x2_dc00L;
      } else {
        //LAB_8001da58
        fileIndex = _800501bc.get((int)(MEMORY.ref(1, struct).offset(0x1L).get() & 0x1fL)).get();
        callback = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001fb44", long.class, long.class, long.class);
        callbackParam = 0;
      }

      //LAB_8001daa4
      loadDrgnBinFile(0, fileIndex, 0, callback, callbackParam, 0x4L);
    }

    //LAB_8001daac
  }

  @Method(0x8001dabcL)
  public static void musicPackageLoadedCallback(final long addressPtr, final long fileSize, final long a2) {
    LOGGER.info("Music package %d loaded", a2 >> 8);

    final MrgFile soundMrg = MEMORY.ref(4, addressPtr, MrgFile::new);
    soundMrgPtr_800bd76c.set(soundMrg);

    if(mainCallbackIndex_8004dd20.get() == 0x5L || mainCallbackIndex_8004dd20.get() == 0x6L && submapScene_800bb0f8.get() == 0x1bbL) {
      //LAB_8001db1c
      memcpy(soundMrgSshdPtr_800bd784.getPointer(), soundMrg.getFile(3), (int)soundMrg.entries.get(3).size.get());
      memcpy(soundMrgSssqPtr_800bd788.getPointer(), soundMrg.getFile(2), (int)soundMrg.entries.get(2).size.get());

      _800bd0fc.setu(a2);
      soundFileArr_800bcf80.get(11).spuRamOffset_14.set(0);
      soundFileArr_800bcf80.get(11)._02.set((int)MEMORY.ref(2, soundMrg.getFile(0)).get());
      soundFileArr_800bcf80.get(11)._18.set((int)(MEMORY.ref(1, soundMrg.getFile(1)).get() - 0x1L));
      setSpuDmaCompleteCallback(getMethodAddress(Scus94491BpeSegment.class, "FUN_8001f810"));
      soundFileArr_800bcf80.get(11).playableSoundIndex_10.set(loadSshdAndSoundbank(soundMrg.getFile(4), soundMrgSshdPtr_800bd784.deref(), 0x2_1f70L + soundFileArr_800bcf80.get(11).spuRamOffset_14.get()));
      soundFileArr_800bcf80.get(11).used_00.set(true);
      soundFileArr_800bcf80.get(11).spuRamOffset_14.add(soundMrg.entries.get(4).size.get());
      sssqChannelIndex_800bd0f8.setu(FUN_8004c1f8(soundFileArr_800bcf80.get(11).playableSoundIndex_10.get(), soundMrgSssqPtr_800bd788.deref()));
      _800bd781.setu(0x1L);
    } else {
      //LAB_8001dbf0
      if(soundMrg.count.get() == 0x5L) {
        soundFileArr_800bcf80.get(11).soundMrgPtr_04.set(MEMORY.ref(4, addToLinkedListTail(soundMrg.entries.get(4).offset.get()), MrgFile::new));
        memcpy(soundFileArr_800bcf80.get(11).soundMrgPtr_04.getPointer(), soundMrg.getAddress(), (int)soundMrg.entries.get(4).offset.get());
        _800bd0fc.setu(a2);
        soundFileArr_800bcf80.get(11).spuRamOffset_14.set(0);
        soundFileArr_800bcf80.get(11)._02.set((int)MEMORY.ref(2, soundFileArr_800bcf80.get(11).soundMrgPtr_04.deref().getFile(0)).get());
        soundFileArr_800bcf80.get(11)._18.set((int)(MEMORY.ref(1, soundMrg.getFile(1)).get() - 0x1L));
        setSpuDmaCompleteCallback(getMethodAddress(Scus94491BpeSegment.class, "FUN_8001f810"));
        soundFileArr_800bcf80.get(11).playableSoundIndex_10.set(loadSshdAndSoundbank(soundMrg.getFile(4), soundFileArr_800bcf80.get(11).soundMrgPtr_04.deref().getFile(3, SshdFile::new), 0x2_1f70L + soundFileArr_800bcf80.get(11).spuRamOffset_14.get()));
        soundFileArr_800bcf80.get(11).used_00.set(true);
        soundFileArr_800bcf80.get(11).spuRamOffset_14.add(soundMrg.entries.get(4).size.get());
        sssqChannelIndex_800bd0f8.setu(FUN_8004c1f8(soundFileArr_800bcf80.get(11).playableSoundIndex_10.get(), soundFileArr_800bcf80.get(11).soundMrgPtr_04.deref().getFile(2, SssqFile::new)));
      } else {
        //LAB_8001dcdc
        soundFileArr_800bcf80.get(11).soundMrgPtr_04.set(MEMORY.ref(4, addToLinkedListTail(soundMrg.entries.get(3).offset.get()), MrgFile::new));
        memcpy(soundFileArr_800bcf80.get(11).soundMrgPtr_04.getPointer(), soundMrg.getAddress(), (int)soundMrg.entries.get(3).offset.get());
        _800bd0fc.setu(a2);
        soundFileArr_800bcf80.get(11)._02.set((int)MEMORY.ref(2, soundFileArr_800bcf80.get(11).soundMrgPtr_04.deref().getFile(0)).get());

        final long callback;
        if((a2 & 0x1L) == 0) {
          callback = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001fa18");
        } else {
          //LAB_8001dd3c
          callback = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001fab4");
        }

        //LAB_8001dd44
        setSpuDmaCompleteCallback(callback);

        soundFileArr_800bcf80.get(11).playableSoundIndex_10.set(loadSshdAndSoundbank(soundMrg.getFile(3), soundFileArr_800bcf80.get(11).soundMrgPtr_04.deref().getFile(2, SshdFile::new), 0x2_1f70L));
        soundFileArr_800bcf80.get(11).used_00.set(true);
        sssqChannelIndex_800bd0f8.setu(FUN_8004c1f8(soundFileArr_800bcf80.get(11).playableSoundIndex_10.get(), soundFileArr_800bcf80.get(11).soundMrgPtr_04.deref().getFile(1, SssqFile::new)));
      }

      //LAB_8001dd98
      _800bd781.setu(0);
    }

    //LAB_8001dda0
    FUN_8001b1a8(0x28L);
    _800bd0f0.setu(0x2L);
  }

  @Method(0x8001ddd8L)
  public static void FUN_8001ddd8() {
    removeFromLinkedList(soundMrgPtr_800bd76c.getPointer());
    FUN_8004cf8c((int)sssqChannelIndex_800bd0f8.getSigned());
    _800bd610.offset(2, 0x60L).setu(0x2L);
    final long a2 = _800bd610.offset(4, 0x64L).get();
    final long v0 = a2 + MEMORY.ref(4, a2).offset(0x10L).get();
    final long a1 = a2 + MEMORY.ref(4, a2).offset(0x18L).get();
    _800bd610.offset(2, 0x6cL).setu(FUN_8004c1f8(soundFileArr_800bcf80.get((int)MEMORY.ref(2, v0).getSigned()).playableSoundIndex_10.get(), MEMORY.ref(4, a1, SssqFile::new)));
    _800bd782.addu(0x1L);
    loadedDrgnFiles_800bcf78.and(0xffff_ff7fL);
  }

  @Method(0x8001de84L)
  public static void FUN_8001de84(final long a0) {
    unloadSoundFile(1);
    unloadSoundFile(3);
    unloadSoundFile(4);
    unloadSoundFile(5);
    unloadSoundFile(6);

    if(_80109a98.offset(submapScene_800bb0f8.get() * 0x10L).offset(1, 0x1L).get() != 0xffL) {
      FUN_800201c8(0x6L);

      final long v1 = _8005019c.offset(_80109a98.offset(submapScene_800bb0f8.get() * 0x10L).offset(1, 0x1L).get() & 0x1fL).offset(1, 0x0L).get();
      if(v1 == 0xcL) {
        FUN_8001fcf4(696);
      } else if(v1 == 0xdL) {
        //LAB_8001df68
        FUN_8001fcf4(697);
      } else if(v1 == 0xeL) {
        //LAB_8001df70
        FUN_8001fcf4(698);
      } else if(v1 == 0xfL) {
        //LAB_8001df78
        FUN_8001fcf4(699);
        //LAB_8001df44
      } else if(v1 == 0x56L) {
        //LAB_8001df84
        FUN_8001fcf4(700);
      } else if(v1 == 0x58L) {
        //LAB_8001df80
        FUN_8001fcf4(701);
      }

      //LAB_8001df8c
      unloadSoundFile(8);
      FUN_8001d9d0();
    }

    //LAB_8001df9c
    loadDrgnBinFile(0, _80050104.offset(FUN_8001a810() * 0x4L).get(), 0, getMethodAddress(Scus94491BpeSegment.class, "FUN_8001cae0", long.class, long.class, long.class), 0, 0x4L);
    loadedDrgnFiles_800bcf78.oru(0x8L);
    FUN_8001d1c4();
    FUN_80012bb4();
  }

  //TODO verify this method
  @Method(0x8001e010L)
  public static void FUN_8001e010(final long a0) {
    long v1;
    long a1;
    long s0;
    if(a0 == 0) {
      //LAB_8001e054
      FUN_80020360(spu28Arr_800bd110, spu28Arr_800bca78);
      FUN_8001ad18();
      unloadSoundFile(8);
      unloadSoundFile(8);

      loadedDrgnFiles_800bcf78.oru(0x80L);
      loadDrgnBinFile(0, 5815, 0, getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class), 5815 * 0x100L, 0x4L);

      //LAB_8001e044
    } else if(a0 == 0x1L) {
      //LAB_8001e094
      FUN_8001ad18();
      unloadSoundFile(8);
      unloadSoundFile(8);

      //LAB_8001e0bc
      loadedDrgnFiles_800bcf78.oru(0x80L);
      loadDrgnBinFile(0, 5900, 0, getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class), 5900 * 0x100L, 0x4L);
    } else if((int)a0 == -0x1L) {
      //LAB_8001e0f8
      if(_800bdc34.get() != 0) {
        if(mainCallbackIndex_8004dd20.get() == 0x8L && gameState_800babc8._4e4.get() != 0) {
          sssqResetStuff();
          unloadSoundFile(8);

          //LAB_8001e23c
          loadedDrgnFiles_800bcf78.oru(0x80L);
          loadDrgnBinFile(0, 5850, 0, getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class), 5850 * 0x100L, 0x4L);
        }
      } else {
        //LAB_8001e160
        FUN_800201c8(0x6L);
        unloadSoundFile(8);
        v1 = mainCallbackIndex_8004dd20.get();
        if(v1 == 0x5L) {
          //LAB_8001e1ac
          s0 = FUN_8001c60c();
          if((int)s0 == -0x2L || (int)s0 == -0x3L) {
            if((int)s0 == -0x2L) {
              //LAB_8001e1f4
              FUN_8001ada0();
            }
            //LAB_8001e1dc
          } else if((int)s0 < -0x1L || s0 != a0) {
            //LAB_8001e20c
            unloadSoundFile(8);
            a1 = 5815 + s0 * 5;

            //LAB_8001e23c
            loadedDrgnFiles_800bcf78.oru(0x80L);
            loadDrgnBinFile(0, a1, 0, getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class), a1 * 0x100L, 0x4L);
          } else {
            FUN_8001ae90();

            //LAB_8001e200
            _800bd782.setu(0x1L);
          }
        } else if(v1 == 0x6L) {
          //LAB_8001e264
          sssqResetStuff();
        } else if(v1 == 0x8L) {
          unloadSoundFile(8);
        }
      }

      //LAB_8001e26c
      FUN_8001ad18();
      FUN_80020360(spu28Arr_800bca78, spu28Arr_800bd110);

      //LAB_8001e288
    }
  }

  @Method(0x8001e29cL)
  public static void unloadSoundFile(final int fileIndex) {
    switch(fileIndex) {
      case 0 -> {
        if(soundFileArr_800bcf80.get(0).used_00.get()) {
          sssqUnloadPlayableSound(soundFileArr_800bcf80.get(0).playableSoundIndex_10.get());
          removeFromLinkedList(soundFileArr_800bcf80.get(0).soundMrgPtr_04.getPointer());
          soundFileArr_800bcf80.get(0).used_00.set(false);
        }
      }

      case 1 -> {
        //LAB_8001e324
        for(int i = 0; i < 3; i++) {
          if(soundFileArr_800bcf80.get((int)_800500f8.offset(i * 0x4L).get()).used_00.get()) {
            sssqUnloadPlayableSound(soundFileArr_800bcf80.get((int)_800500f8.offset(i * 0x4L).get()).playableSoundIndex_10.get());
            soundFileArr_800bcf80.get((int)_800500f8.offset(i * 0x4L).get()).used_00.set(false);
          }

          //LAB_8001e374
        }
      }

      case 2 -> {
        if(soundFileArr_800bcf80.get(4).used_00.get()) {
          sssqUnloadPlayableSound(soundFileArr_800bcf80.get(4).playableSoundIndex_10.get());
          removeFromLinkedList(soundFileArr_800bcf80.get(4).soundMrgPtr_04.getPointer());
          soundFileArr_800bcf80.get(4).used_00.set(false);
        }
      }

      case 3 -> {
        //LAB_8001e3dc
        for(int i = 0; i < 4; i++) {
          if(soundFileArr_800bcf80.get((int)_800500e8.offset(i * 0x4L).get()).used_00.get()) {
            removeFromLinkedList(soundFileArr_800bcf80.get((int)_800500e8.offset(i * 0x4L).get()).soundMrgPtr_04.getPointer());
            sssqUnloadPlayableSound(soundFileArr_800bcf80.get((int)_800500e8.offset(i * 0x4L).get()).playableSoundIndex_10.get());
            soundFileArr_800bcf80.get((int)_800500e8.offset(i * 0x4L).get()).used_00.set(false);
          }

          //LAB_8001e450
        }
      }

      case 4 -> {
        if(soundFileArr_800bcf80.get(8).used_00.get()) {
          sssqUnloadPlayableSound(soundFileArr_800bcf80.get(8).playableSoundIndex_10.get());
          removeFromLinkedList(soundFileArr_800bcf80.get(8).soundMrgPtr_04.getPointer());
          soundFileArr_800bcf80.get(8).used_00.set(false);
        }
      }

      case 5 -> {
        if(soundFileArr_800bcf80.get(9).used_00.get()) {
          sssqUnloadPlayableSound(soundFileArr_800bcf80.get(9).playableSoundIndex_10.get());
          removeFromLinkedList(soundFileArr_800bcf80.get(9).soundMrgPtr_04.getPointer());
          soundFileArr_800bcf80.get(9).used_00.set(false);
        }
      }

      case 6, 7 -> {
        if(soundFileArr_800bcf80.get(10).used_00.get()) {
          sssqUnloadPlayableSound(soundFileArr_800bcf80.get(10).playableSoundIndex_10.get());
          removeFromLinkedList(soundFileArr_800bcf80.get(10).soundMrgPtr_04.getPointer());
          soundFileArr_800bcf80.get(10).used_00.set(false);
        }
      }

      case 8 -> {
        if(_800bd0f0.getSigned() != 0) {
          FUN_8004d034((int)sssqChannelIndex_800bd0f8.getSigned(), 0x1L);
          FUN_8004c390((int)sssqChannelIndex_800bd0f8.getSigned());

          if(_800bd781.get() == 0) {
            removeFromLinkedList(soundFileArr_800bcf80.get(11).soundMrgPtr_04.getPointer());
          }

          //LAB_8001e56c
          _800bd0f0.setu(0);
        }

        //LAB_8001e570
        if(soundFileArr_800bcf80.get(11).used_00.get()) {
          sssqUnloadPlayableSound(soundFileArr_800bcf80.get(11).playableSoundIndex_10.get());
          soundFileArr_800bcf80.get(11).used_00.set(false);
        }
      }

      case 9 -> {
        if(soundFileArr_800bcf80.get(12).used_00.get()) {
          sssqUnloadPlayableSound(soundFileArr_800bcf80.get(12).playableSoundIndex_10.get());
          removeFromLinkedList(soundFileArr_800bcf80.get(12).soundMrgPtr_04.getPointer());
          soundFileArr_800bcf80.get(12).used_00.set(false);
        }
      }
    }

    //LAB_8001e5d4
  }

  @Method(0x8001e5ecL)
  public static void loadDRGN0_mrg_62802_sounds() {
    loadedDrgnFiles_800bcf78.oru(0x1L);
    loadDrgnBinFile(0, 5739, 0, getMethodAddress(Scus94491BpeSegment.class, "DRGN0_mrg_62802_loaded", long.class, long.class, long.class), 0, 0x4L);
  }

  /**
   * 0: unknown, 2-byte file (00 00)
   * 1: unknown, 0x64 byte file, counts up from 0000 to 0033
   * 2: SShd file
   * 3: Soundbank (has some map and battle sounds)
   */
  @Method(0x8001e694L)
  public static void DRGN0_mrg_62802_loaded(final long address, final long fileSize, final long a2) {
    FUN_800156f4(0x1L);

    final MrgFile mrg = MEMORY.ref(4, address, MrgFile::new);

    soundbank_800bd778.setu(addToLinkedListHead(mrg.entries.get(3).size.get()));
    memcpy(soundbank_800bd778.get(), mrg.getFile(3), (int)mrg.entries.get(3).size.get());

    soundFileArr_800bcf80.get(0).soundMrgPtr_04.set(MEMORY.ref(4, addToLinkedListTail(mrg.entries.get(3).offset.get()), MrgFile::new));
    memcpy(soundFileArr_800bcf80.get(0).soundMrgPtr_04.getPointer(), address, (int)mrg.entries.get(3).offset.get());

    removeFromLinkedList(address);

    soundFileArr_800bcf80.get(0).ptr_08.set(soundFileArr_800bcf80.get(0).soundMrgPtr_04.deref().getFile(1));
    setSpuDmaCompleteCallback(getMethodAddress(Scus94491BpeSegment.class, "unloadSoundbank_800bd778"));

    soundFileArr_800bcf80.get(0).playableSoundIndex_10.set(loadSshdAndSoundbank(soundbank_800bd778.get(), soundFileArr_800bcf80.get(0).soundMrgPtr_04.deref().getFile(2, SshdFile::new), 0x1010L));
    soundFileArr_800bcf80.get(0).used_00.set(true);
  }

  @Method(0x8001e780L)
  public static void unloadSoundbank_800bd778() {
    removeFromLinkedList(soundbank_800bd778.get());
    FUN_800156f4(0);
    loadedDrgnFiles_800bcf78.and(0xffff_fffeL);
  }

  @Method(0x8001e8ccL)
  public static void FUN_8001e8cc() {
    // empty
  }

  @Method(0x8001e8d4L)
  public static void FUN_8001e8d4() {
    removeFromLinkedList(_800bd768.get());
    loadedDrgnFiles_800bcf78.and(0xffff_fff7L);
  }

  @Method(0x8001eaa0L)
  public static void FUN_8001eaa0() {
    removeFromLinkedList(_800bd758.get());
    loadedDrgnFiles_800bcf78.and(0xffff_ffefL);
  }

  @Method(0x8001eadcL)
  public static void FUN_8001eadc(final long a0) {
    loadedDrgnFiles_800bcf78.oru(0x2L);
    loadDrgnBinFile(0, a0 + 5750L, 0, getMethodAddress(Scus94491BpeSegment.class, "FUN_8001eb38", long.class, long.class, long.class), 0, 0x4L);
  }

  @Method(0x8001eb38L)
  public static void FUN_8001eb38(final long address, final long fileSize, final long a2) {
    final MrgFile mrg = MEMORY.ref(4, address, MrgFile::new);
    soundMrgPtr_800bd748.set(mrg);

    final MrgFile mrg2 = MEMORY.ref(4, addToLinkedListTail(mrg.entries.get(4).offset.get()), MrgFile::new);
    soundFileArr_800bcf80.get(8).soundMrgPtr_04.set(mrg2);

    memcpy(mrg2.getAddress(), mrg.getAddress(), (int)mrg.entries.get(4).offset.get());

    soundFileArr_800bcf80.get(8).ptr_08.set(mrg2.getFile(2)); //TODO this might be an SSsq
    soundFileArr_800bcf80.get(8).ptr_0c.set(mrg2.getFile(1));
    soundFileArr_800bcf80.get(8)._02.set((int)MEMORY.ref(2, mrg2.getFile(0)).get());
    setSpuDmaCompleteCallback(getMethodAddress(Scus94491BpeSegment.class, "FUN_8001ec18"));
    soundFileArr_800bcf80.get(8).playableSoundIndex_10.set(loadSshdAndSoundbank(mrg.getFile(4), mrg2.getFile(3, SshdFile::new), 0x4_de90L));
    FUN_8004cb0c(soundFileArr_800bcf80.get(8).playableSoundIndex_10.get(), 0x7fL);
    soundFileArr_800bcf80.get(8).used_00.set(true);
  }

  @Method(0x8001ec18L)
  public static void FUN_8001ec18() {
    removeFromLinkedList(soundMrgPtr_800bd748.getPointer());
    loadedDrgnFiles_800bcf78.and(0xffff_fffdL);
    _800bd782.addu(0x1L);
  }

  @Method(0x8001eea8L)
  public static void FUN_8001eea8(final long a0) {
    loadedDrgnFiles_800bcf78.oru(0x8000L);
    loadDrgnBinFile(0, 5740L + a0, 0, getMethodAddress(Scus94491BpeSegment.class, "FUN_8001eefc", long.class, long.class, long.class), 0, 0x4L);
  }

  @Method(0x8001eefcL)
  public static void FUN_8001eefc(final long address, final long size, final long param) {
    long v0;
    long s0;
    long s1;

    soundMrgPtr_800bd748.set(MEMORY.ref(4, address, MrgFile::new));
    v0 = addToLinkedListTail(MEMORY.ref(4, address).offset(0x28L).get());
    s1 = soundFileArr_800bcf80.getAddress();
    MEMORY.ref(4, s1).offset(0x154L).setu(v0);
    memcpy(v0, address, (int)MEMORY.ref(4, address).offset(0x28L).get());
    s0 = MEMORY.ref(4, s1).offset(0x154L).get();

    v0 = s0 + MEMORY.ref(4, s0).offset(0x18L).get();
    MEMORY.ref(4, s1).offset(0x158L).setu(v0);
    v0 = s0 + MEMORY.ref(4, s0).offset(0x8L).get();
    v0 = MEMORY.ref(2, v0).offset(0x0L).get();
    MEMORY.ref(2, s1).offset(0x152L).setu(v0);
    setSpuDmaCompleteCallback(getMethodAddress(Scus94491BpeSegment.class, "FUN_8001efcc"));
    v0 = loadSshdAndSoundbank(address + MEMORY.ref(4, address).offset(0x28L).get(), MEMORY.ref(4, s0 + MEMORY.ref(4, s0).offset(0x20L).get(), SshdFile::new), 0x4_de90L);
    MEMORY.ref(2, s1).offset(0x160L).setu(v0);
    FUN_8004cb0c((short)v0, 0x7fL);
    MEMORY.ref(2, s1).offset(0x150L).setu(0x1L);
  }

  @Method(0x8001efccL)
  public static void FUN_8001efcc() {
    removeFromLinkedList(soundMrgPtr_800bd748.getPointer());
    loadedDrgnFiles_800bcf78.and(0xffff_7fffL);
  }

  /**
   * Loads an audio MRG from DRGN0. File index is 5815 + index * 5.
   *
   * @param index <ol start="0">
   *   <li>Inventory music</li>
   *   <li>Main menu music</li>
   *   <li>Seems to be the previous one and the next one combined?</li>
   *   <li>Battle music, more?</li>
   *   <li>Same as previous</li>
   *   <li>...</li>
   * </ol>
   */
  @Method(0x8001f3d0L)
  public static void loadMusicPackage(final long index, final long a1) {
    unloadSoundFile(8);
    loadedDrgnFiles_800bcf78.oru(0x80L);
    final long fileIndex = 5815 + index * 5;
    loadDrgnBinFile(0, fileIndex, 0, getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class), fileIndex * 0x100 | a1, 4);
  }

  @Method(0x8001f450L)
  public static long scriptLoadMusicPackage(final RunningScript a0) {
    unloadSoundFile(8);
    loadedDrgnFiles_800bcf78.oru(0x80L);
    final long fileIndex = 5815 + a0.params_20.get(0).deref().get() * 5;
    loadDrgnBinFile(0, fileIndex, 0, getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class), fileIndex << 8 | a0.params_20.get(1).deref().get(), 0x4L);
    return 0;
  }

  @Method(0x8001f708L)
  public static void FUN_8001f708(final long chapterIndex, final long a1) {
    unloadSoundFile(8);
    loadedDrgnFiles_800bcf78.oru(0x80L);
    final long fileIndex = 5850 + chapterIndex * 5;
    loadDrgnBinFile(0, fileIndex, 0, getMethodAddress(Scus94491BpeSegment.class, "musicPackageLoadedCallback", long.class, long.class, long.class), fileIndex << 8 | a1, 0x4L);
  }

  @Method(0x8001f810L)
  public static void FUN_8001f810() {
    removeFromLinkedList(soundMrgPtr_800bd76c.getPointer());

    long s0 = soundFileArr_800bcf80.get(11)._18.get() - 0x1L;
    long s1 = 0x1L;

    //LAB_8001f860
    while((int)s0 >= 0) {
      final long a3;
      if(s0 != 0) {
        a3 = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001f8e0", long.class, long.class, long.class);
      } else {
        //LAB_8001f87c
        a3 = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001f968", long.class, long.class, long.class);
      }

      //LAB_8001f88c
      loadDrgnBinFile(0, (_800bd0fc.get() >> 8) + s1, 0, a3, 0, 0x4L);
      s0--;
      s1++;
    }

    //LAB_8001f8b4
    _800bd782.addu(0x1L);
  }

  @Method(0x8001f8e0L)
  public static void FUN_8001f8e0(final long audioAddress, final long spuTransferSize, final long unused) {
    setSpuDmaCompleteCallback(0);

    SPU.directWrite(0x2_1f70 + (int)soundFileArr_800bcf80.get(11).spuRamOffset_14.get(), audioAddress, (int)spuTransferSize);

    soundFileArr_800bcf80.get(11).spuRamOffset_14.add(spuTransferSize);
    removeFromLinkedList(audioAddress);
  }

  @Method(0x8001f968L)
  public static void FUN_8001f968(final long audioAddress, final long spuTransferSize, final long unused) {
    setSpuDmaCompleteCallback(0);

    //TODO make sure directWrite works
    SPU.directWrite(0x2_1f70 + (int)soundFileArr_800bcf80.get(11).spuRamOffset_14.get(), audioAddress, (int)spuTransferSize);

    if(_800bd0fc.get(0x1L) == 0) {
      FUN_8004cf8c((int)sssqChannelIndex_800bd0f8.get());
    }

    removeFromLinkedList(audioAddress);
    loadedDrgnFiles_800bcf78.and(0xffff_ff7fL);
  }

  @Method(0x8001fa18L)
  public static void FUN_8001fa18() {
    FUN_8004cf8c((int)sssqChannelIndex_800bd0f8.get());
    sssqTempo_800bd104.setu(sssqGetTempo((int)sssqChannelIndex_800bd0f8.get()) * sssqTempoScale_800bd100.get());
    sssqSetTempo((int)sssqChannelIndex_800bd0f8.get(), (int)(sssqTempo_800bd104.get() << 8) >> 16);
    removeFromLinkedList(soundMrgPtr_800bd76c.getPointer());

    _800bd782.addu(0x1L);
    loadedDrgnFiles_800bcf78.and(0xffff_ff7fL);
  }

  @Method(0x8001fb44L)
  public static void FUN_8001fb44(final long address, final long fileSize, final long param) {
    long v0;
    long v1;
    long a0;
    long s0;
    long s2;
    soundMrgPtr_800bd76c.setPointer(address);
    v0 = addToLinkedListTail(MEMORY.ref(4, address).offset(0x20L).get());
    s0 = soundFileArr_800bcf80.getAddress();
    MEMORY.ref(4, s0).offset(0x138L).setu(v0);
    memcpy(v0, address, (int)MEMORY.ref(4, address).offset(0x20L).get());

    s2 = MEMORY.ref(4, s0).offset(0x138L).get();
    v0 = s2 + MEMORY.ref(4, s2).offset(0x8L).get();
    v0 = MEMORY.ref(2, v0).offset(0x0L).get();
    MEMORY.ref(2, s0).offset(0x136L).setu(v0);
    if(param == 0) {
      a0 = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001ddd8");
    } else {
      //LAB_8001fbc4
      a0 = getMethodAddress(Scus94491BpeSegment.class, "FUN_8001fc54");
    }

    //LAB_8001fbcc
    setSpuDmaCompleteCallback(a0);
    v0 = loadSshdAndSoundbank(address + MEMORY.ref(4, address).offset(0x20L).get(), MEMORY.ref(4, s2 + MEMORY.ref(4, s2).offset(0x18L).get(), SshdFile::new), 0x2_1f70L);
    v1 = soundFileArr_800bcf80.getAddress();
    MEMORY.ref(2, v1).offset(0x144L).setu(v0);
    MEMORY.ref(2, v1).offset(0x134L).setu(0x1L);
    v0 = FUN_8004c1f8((short)v0, MEMORY.ref(4, s2 + MEMORY.ref(4, s2).offset(0x10L).get(), SssqFile::new));
    v1 = _800bd0f0.getAddress();
    MEMORY.ref(2, v1).offset(0x8L).setu(v0);
    FUN_8001b1a8(0x28L);
    _800bd781.setu(0);
    _800bd0f0.setu(0x2L);
  }

  @Method(0x8001fcf4L)
  public static void FUN_8001fcf4(final long fileIndex) {
    loadedDrgnFiles_800bcf78.oru(0x4000L);
    loadDrgnBinFile(0, fileIndex, 0, getMethodAddress(Scus94491BpeSegment.class, "FUN_8001fd4c", long.class, long.class, long.class), 0x6L, 0x2L);
  }

  @Method(0x8001fd4cL)
  public static void FUN_8001fd4c(final long address, final long fileSize, final long param) {
    final long s0 = _800bd610.offset(param * 0x10L).getAddress();
    MEMORY.ref(4, s0).offset(0x4L).setu(address);
    MEMORY.ref(2, s0).offset(0x0L).setu(0x2L);
    MEMORY.ref(4, s0).offset(0x8L).setu(address + MEMORY.ref(4, address).offset(0x18L).get());
    FUN_8004c8dc((int)MEMORY.ref(2, s0).offset(0xcL).getSigned(), 0x28L);
    MEMORY.ref(2, s0).offset(0x0L).setu(0x2L);
    loadedDrgnFiles_800bcf78.and(0xffff_bfffL);
  }

  @Method(0x8001ff74L)
  public static void FUN_8001ff74() {
    FUN_80012b1c(1, getMethodAddress(Scus94491BpeSegment.class, "FUN_8001de84", long.class), 0);
  }

  @Method(0x8001ffb0L)
  public static long getLoadedDrgnFiles() {
    return loadedDrgnFiles_800bcf78.get();
  }
}
