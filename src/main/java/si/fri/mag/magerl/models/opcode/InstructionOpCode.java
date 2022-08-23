package si.fri.mag.magerl.models.opcode;

import si.fri.mag.magerl.models.Instruction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum InstructionOpCode implements OpCode {

    TRAP,
    MUL,
    MULU,
    DIV,
    DIVU,
    ADD,
    ADDU,
    SUB,
    SUBU,
    CMP,
    CMPU,
    NEG,
    NEGU,
    SL,
    SLU,
    SR,
    SRU,
    BN,
    BZ,
    BP,
    BOD,
    BNN,
    BNZ,
    BNP,
    BEV,
    PBN,
    PBZ,
    PBP,
    PBOD,
    PBNN,
    PBNZ,
    PBNP,
    PBEV,
    CSN,
    CSZ,
    CSP,
    CSOD,
    CSNN,
    CSNZ,
    CSNP,
    CSEV,
    ZSN,
    ZSZ,
    ZSP,
    ZSOD,
    ZSNN,
    ZSNZ,
    ZSNP,
    ZSEV,
    LDB,
    LDBU,
    LDW,
    LDWU,
    LDT,
    LDTU,
    LDO,
    LDOU,
    LDSF,
    LDHT,
    CSWAP,
    LDUNC,
    LDVTS,
    PRELD,
    PREGO,
    GO,
    STB,
    STBU,
    STW,
    STWU,
    STT,
    STTU,
    STO,
    STOU,
    STSF,
    STHT,
    STCO,
    STUNC,
    SYNCD,
    PREST,
    SYNCID,
    PUSHGO,
    OR,
    ORN,
    NOR,
    XOR,
    AND,
    ANDN,
    NAND,
    NXOR,
    BDIF,
    WDIF,
    TDIF,
    ODIF,
    MUX,
    SADD,
    MOR,
    MXOR,
    SETH,
    SETMH,
    SETML,
    SETL,
    INCH,
    INCMH,
    INCML,
    INCL,
    ORH,
    ORMH,
    ORML,
    ORL,
    ANDNH,
    ANDNMH,
    ANDNML,
    ANDNL,
    JMP,
    PUSHJ,
    GETA,
    PUT,
    POP,
    RESUME,
    SAVE,
    SYNC,
    SWYM,
    GET,
    TRIP,

    // Non-official opcodes
    SET,
    LDA,
    ;
    private static final List<InstructionOpCode> BRANCH_INSTRUCTIONS = List.of(
            BN, BZ, BP, BOD, BNN, BNZ, BNP, BEV, PBN, PBZ, PBP, PBOD, PBNN, PBNZ, PBNP, PBEV
    );

    private static final List<InstructionOpCode> STORE_INSTRUCTIONS = List.of(
            STB, STW, STT, STO, STBU, STWU, STTU, STOU
    );

    private static final List<InstructionOpCode> LOAD_INSTRUCTIONS = List.of(
            LDB, LDW, LDT, LDO, LDBU, LDWU, LDTU, LDOU
    );

    private static final List<InstructionOpCode> ARITHMETIC_INSTRUCTIONS = List.of(
            ADD, SUB, MUL, DIV, ADDU, SUBU, MULU, DIVU
    );

    public static Optional<InstructionOpCode> from(String opCode) {
        return Arrays.stream(InstructionOpCode.values()).filter(o -> o.name().equals(opCode)).findFirst();
    }

    public static boolean isSignedLoadInstructionOpCode(InstructionOpCode instructionOpCode) {
        return instructionOpCode == LDB || instructionOpCode == LDW || instructionOpCode == LDT || instructionOpCode == LDO;
    }

    public static boolean isLoadInstructionOpCode(InstructionOpCode instructionOpCode) {
        return LOAD_INSTRUCTIONS.contains(instructionOpCode);
    }

    public static boolean isBranchInstructionOpCode(InstructionOpCode instructionOpCode) {
        return BRANCH_INSTRUCTIONS.contains(instructionOpCode);
    }

    public static boolean isStoreInstructionOpCode(InstructionOpCode instructionOpCode) {
        return STORE_INSTRUCTIONS.contains(instructionOpCode);
    }

    public static boolean isBranchOrJumpInstructionOpCode(InstructionOpCode instructionOpCode) {
        return BRANCH_INSTRUCTIONS.contains(instructionOpCode) || instructionOpCode == JMP;
    }

    public static boolean isSubroutineInstructionOpCode(InstructionOpCode instructionOpCode) {
        return instructionOpCode == PUSHJ || instructionOpCode == PUSHGO;
    }

    public static boolean isArithmeticInstructionOpCode(InstructionOpCode instructionOpCode) {
        return ARITHMETIC_INSTRUCTIONS.contains(instructionOpCode);
    }
}
